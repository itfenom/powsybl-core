package com.powsybl.cgmes.conversion.test.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.cgmes.conversion.tools.RunAnalysisTool;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;

public class RunAnalysisToolTest extends AbstractToolTest {

    private final RunAnalysisTool tool = new RunAnalysisTool();

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "cases-analysis", 5, 2);
        assertEquals("Computation", command.getTheme());
        assertEquals("Compare cases results with Loadflow results", command.getDescription());
        assertNull(command.getUsageFooter());
        assertOption(command.getOptions(), "cases", true, true);
        assertOption(command.getOptions(), "output-file", true, true);
        assertOption(command.getOptions(), "loadflow-engine", false, true);
        assertOption(command.getOptions(), "parameters-file", false, true);
        assertOption(command.getOptions(), "skip-postproc", false, false);
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new RunAnalysisTool());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        fileSystem = FileSystems.getDefault();
    }

    @Test
    public void testCaseAnalysis() throws IOException {
        assertCommand(new String[] {"cases-analysis", "--cases", "C:\\cgmes-csi\\IOP\\CGMES_IOP_20191204\\", "--output-file", "c:\\tmp\\output.csv", "--loadflow-engine", "LoadFlowResultCompletionMock"}, 0, "", "");
    }
}
