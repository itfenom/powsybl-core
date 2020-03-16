package com.powsybl.cgmes.conversion.tools;

import static com.powsybl.iidm.tools.ConversionToolUtils.createImportParameterOption;
import static com.powsybl.iidm.tools.ConversionToolUtils.createImportParametersFileOption;
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
import com.powsybl.iidm.network.ValidationException;
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
    private static final String OUTPUT_DETAILED_FILE = "output-detailed-file";

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
                options.addOption(Option.builder().longOpt(OUTPUT_DETAILED_FILE)
                    .desc("Biggest differences output file")
                    .hasArg()
                    .argName("FILE")
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
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
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
        String temp = System.getProperty("java.io.tmpdir");
        if (temp == null) {
            temp = "tmp";
        }

        try (FileOutputStream errFos = new FileOutputStream(new File(temp + "err.txt"))) {
            System.setErr(new PrintStream(errFos));

            Path casesInput = context.getFileSystem().getPath(line.getOptionValue(CASES_INPUT));
            boolean skipPostProc = line.hasOption(SKIP_POSTPROC);
            Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));
            Path outputDetailedFile = null;
            if (line.hasOption(OUTPUT_DETAILED_FILE)) {
                outputDetailedFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_DETAILED_FILE));
            }

            String name = "HELM";
            if (line.hasOption(LOADFLOW_ENGINE)) {
                name = line.getOptionValue(LOADFLOW_ENGINE);
            }

            ComputationManager computationManager = context.getShortTimeExecutionComputationManager();

            ImportConfig importConfig = (!skipPostProc) ? ImportConfig.load() : new ImportConfig();
            Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);

            LoadFlowParameters params = LoadFlowParameters.load();
            if (line.hasOption(PARAMETERS_FILE)) {
                Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
                JsonLoadFlowParameters.update(params, parametersFile);
            }

            CaseAnalyzer analyzer = new CaseAnalyzer(computationManager, name, outputFile, outputDetailedFile);
            runAnalysis(analyzer, casesInput, importConfig, inputParams, params);
        }
    }

    private void runAnalysis(CaseAnalyzer analyzer, Path casesInput, ImportConfig importConfig,
        Properties inputParams, LoadFlowParameters params) throws IOException {
        if (Files.isDirectory(casesInput)) {
            Files.walk(casesInput).forEach(caseFile -> {
                if (Files.isDirectory(caseFile)) {
                    return;
                }
                String caseName = caseFile.toString().replace(casesInput.toString(), "");
                try {
                    analyzer.analyse(caseName, caseFile, importConfig, inputParams, params);
                } catch (AssertionError | PowsyblException e) {
                    System.err.println(caseName + " -> " + e.getMessage());
                }
            });
        }
    }

    class CaseAnalyzer {

        CaseAnalyzer(ComputationManager computationManager, String name, Path outputFile, Path outputDetailedFile) throws IOException {
            this.computationManager = computationManager;
            runner = LoadFlow.find(name);

            Writer outputWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
            CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
            outputFormatter = csvTableFormatterFactory.create(outputWriter, "analysis results", TableFormatterConfig.load(),
                new Column("Case"),
                new Column("Minimum"),
                new Column("Maximum"),
                new Column("Average"),
                new Column("Deviation"));

            if (outputDetailedFile == null) {
                outputDetailedFormatter = null;
                return;
            }

            Writer outputDetailedWriter = Files.newBufferedWriter(outputDetailedFile, StandardCharsets.UTF_8);
            outputDetailedFormatter = csvTableFormatterFactory.create(outputDetailedWriter, "detailed results", TableFormatterConfig.load(),
                new Column("Case"),
                new Column("Bus"),
                new Column("Loadflow"),
                new Column("Case"));
        }

        public void analyse(String caseName, Path caseFile, ImportConfig importConfig, Properties inputParams, LoadFlowParameters params) {
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
                if (Math.abs(v1 - v0) > 10.0 && outputDetailedFormatter != null) {
                    try {
                        outputDetailedFormatter.writeCell(caseName);
                        outputDetailedFormatter.writeCell(b.getId());
                        outputDetailedFormatter.writeCell(v1);
                        outputDetailedFormatter.writeCell(v0);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            long busesCount = doubleSummaryStatistics.getCount();
            double average = doubleSummaryStatistics.getAverage();
            double deviation = Math.sqrt(diff.values().stream().mapToDouble(x -> Math.pow(x.doubleValue() - average, 2.0)).sum() / busesCount);
            try {
                outputFormatter.writeCell(caseName);
                outputFormatter.writeCell(doubleSummaryStatistics.getMin());
                outputFormatter.writeCell(doubleSummaryStatistics.getMax());
                outputFormatter.writeCell(average);
                outputFormatter.writeCell(deviation);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private final ComputationManager computationManager;
        private final Runner runner;
        private final TableFormatter outputFormatter;
        private final TableFormatter outputDetailedFormatter;
    }

    private static final String LOADFLOW_VARIANT_ID = "Loadflow-values";
}
