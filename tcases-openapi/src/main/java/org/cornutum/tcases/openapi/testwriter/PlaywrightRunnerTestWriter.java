package org.cornutum.tcases.openapi.testwriter;

import org.cornutum.tcases.io.IndentedWriter;
import org.cornutum.tcases.openapi.resolver.RequestCase;

import java.io.File;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.cornutum.tcases.openapi.testwriter.TestWriterUtils.*;

public class PlaywrightRunnerTestWriter extends TypescriptTestWriter {
    /**
     * Creates a new PlaywrightRunnerTestWriter instance.
     */
    public PlaywrightRunnerTestWriter(TestCaseWriter testCaseWriter) {
        super(testCaseWriter);
    }

    @Override
    protected void writeDependencies(  TestTarget target, String testName, IndentedWriter targetWriter)
    {
        super.writeDependencies( target, testName, targetWriter);

        targetWriter.println("import test from '@playwright/test'");

        targetWriter.println(String.format("const responsesPath = %s",
                stringLiteral(getResponsesFile(target, testName)), '\''));
    }

    /**
     * Writes a target test case to the given stream.
     */
    @Override
    protected void writeTestCase(TestTarget target, String testName, RequestCase requestCase, IndentedWriter targetWriter)
    {
        targetWriter.println();
        // TODO: fix test name
        targetWriter.print( String.format( "test(%s,", stringLiteral(getMethodName(requestCase))));

        super.writeTestCase( target, testName, requestCase, targetWriter);

        targetWriter.print( ");");
        targetWriter.println();
    }

    @Override
    protected void writeClosing(TestTarget target, String testName, IndentedWriter targetWriter)
    {
    }

    protected File getResponsesFile(TestTarget target, String testName)
    {
        File targetFile = getTargetFile(target, testName);
        File resourceDir = getTestResourceDir(targetFile, target.getResourceDir());
        File responsesFile = new File(resourceDir, String.format( "%s-Responses.json", getBaseName( targetFile.getName())));
        return responsesFile;
    }
}
