package com.powsybl.cgmes.conversion.tools;

import static com.powsybl.iidm.tools.ConversionToolUtils.readProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionToolUtils;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlow.Runner;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;

public class RunAnalysisTool implements Tool {

    private static final String CASES_INPUT = "cases";
    private static final String LOADFLOW_ENGINE = "loadflow-engine";
    private static final String PARAMETERS_FILE = "parameters-file";
    private static final String SKIP_POSTPROC = "skip-postproc";
    private static final String OUTPUT_FILE = "output-file";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "cases-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Compare cases results with Loadflow results";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASES_INPUT)
                    .desc("the cases path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                    .desc("Analysis result output file")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(LOADFLOW_ENGINE)
                    .desc("loadflow engine name")
                    .hasArg()
                    .argName("ENGINE")
                    .build());
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE)
                    .desc("loadflow parameters as JSON file")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(SKIP_POSTPROC)
                    .desc("skip network importer post processors (when configured)")
                    .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        File file = new File("c:\\tmp\\err.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);

        Path casesInput = context.getFileSystem().getPath(line.getOptionValue(CASES_INPUT));
        boolean skipPostProc = line.hasOption(SKIP_POSTPROC);
        Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));

        ImportConfig importConfig = (!skipPostProc) ? ImportConfig.load() : new ImportConfig();
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);

        LoadFlowParameters params = LoadFlowParameters.load();
        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonLoadFlowParameters.update(params, parametersFile);
        }

        ComputationManager computationManager = context.getShortTimeExecutionComputationManager();
        String name = "HELM";
        if (line.hasOption(LOADFLOW_ENGINE)) {
            name = line.getOptionValue(LOADFLOW_ENGINE);
        }
        Runner runner = LoadFlow.find(name);

        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            runAnalysis(runner, computationManager, casesInput, importConfig, inputParams, params, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void runAnalysis(Runner runner, ComputationManager computationManager, Path casesInput, ImportConfig importConfig,
        Properties inputParams, LoadFlowParameters params, Writer writer) {
        CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
        try (TableFormatter formatter = csvTableFormatterFactory.create(writer, "analysis results", TableFormatterConfig.load(),
            new Column("Case"),
            new Column("Minimum"),
            new Column("Maximum"),
            new Column("Average"),
            new Column("Deviation"))) {
            runAnalysis(runner, computationManager, casesInput, importConfig, inputParams, params, formatter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void runAnalysis(Runner runner, ComputationManager computationManager, Path casesInput, ImportConfig importConfig,
        Properties inputParams, LoadFlowParameters params, TableFormatter formatter) throws IOException {
        if (Files.isDirectory(casesInput)) {
            Files.walk(casesInput).forEach(caseFile -> {
                if (Files.isDirectory(caseFile)) {
                    return;
                }
                String caseName = caseFile.toString().replace(casesInput.toString(), "");
                try {
                    runCaseAnalysis(runner, computationManager, caseName, caseFile, importConfig, inputParams, params, formatter);
                } catch (CgmesModelException e) {
                    System.err.println(caseName + " -> " + e.getMessage());
                }
            });
        }
    }

    private void runCaseAnalysis(Runner runner, ComputationManager computationManager, String caseName, Path caseFile, ImportConfig importConfig,
        Properties inputParams, LoadFlowParameters params, TableFormatter formatter) throws CgmesModelException {
        Network n = Importers.loadNetwork(caseFile, computationManager, importConfig, inputParams);
        if (n == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        String variant0 = n.getVariantManager().getWorkingVariantId();
        String variant1 = LOADFLOW_VARIANT_ID;

        n.getVariantManager().cloneVariant(n.getVariantManager().getWorkingVariantId(), variant1);

        runner.run(n, computationManager, params);

        // Get voltages for variant0
        n.getVariantManager().setWorkingVariant(variant0);
        Map<String, Double> vv0 = new HashMap<>();
        n.getBusView().getBuses().forEach(b -> vv0.put(b.getId(), b.getV()));

        // Switch to variant1 and report differences
        n.getVariantManager().setWorkingVariant(variant1);
        DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics();
        Map<String, Double> diff = new HashMap<>();
        for (Bus b : n.getBusView().getBuses()) {
            double v0 = vv0.get(b.getId());
            double v1 = b.getV();
            doubleSummaryStatistics.accept(Math.abs(v1 - v0));
            diff.put(b.getId(), Math.abs(v1 - v0));
        }

        long busesCount = doubleSummaryStatistics.getCount();
        double average = doubleSummaryStatistics.getAverage();
        double deviation = Math.sqrt(diff.values().stream().mapToDouble(x -> Math.pow(x.doubleValue() - average, 2.0)).sum() / busesCount);
        try {
            formatter.writeCell(caseName);
            formatter.writeCell(doubleSummaryStatistics.getMin());
            formatter.writeCell(doubleSummaryStatistics.getMax());
            formatter.writeCell(average);
            formatter.writeCell(deviation);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final String LOADFLOW_VARIANT_ID = "Loadflow-values";
}
