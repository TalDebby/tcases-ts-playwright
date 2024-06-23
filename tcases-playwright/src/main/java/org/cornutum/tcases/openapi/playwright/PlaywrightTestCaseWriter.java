

package org.cornutum.tcases.openapi.playwright;

import org.cornutum.tcases.io.IndentedWriter;
import org.cornutum.tcases.openapi.resolver.ParamData;
import org.cornutum.tcases.openapi.resolver.RequestCase;
import org.cornutum.tcases.openapi.testwriter.BaseTestCaseWriter;
import org.cornutum.tcases.openapi.testwriter.TestWriterException;
import org.cornutum.tcases.openapi.playwright.tryimport;

import static org.cornutum.tcases.openapi.testwriter.TestWriterUtils.*;
import static org.cornutum.tcases.openapi.testwriter.java.TestCaseWriterUtils.forTestServer;
import static org.cornutum.tcases.openapi.testwriter.java.TestCaseWriterUtils.serverUri;

import java.net.URI;
import java.util.Optional;

/**
 * Writes the source code for REST Assured test cases that execute API requests.
 */
public class PlaywrightTestCaseWriter extends BaseTestCaseWriter
{
    /**
     * Creates a new PlaywrightTestCaseWriter instance.
     */
    PlaywrightTestCaseWriter()
    {
    }

    @Override
    public void writeDependencies(String testName, IndentedWriter targetWriter)
    {
      targetWriter.println("import test from '@playwright/test'");

      if( getDepends().validateResponses()) {
          targetWriter.println("const request = ");
      }

      if( getDepends().dependsMultipart())
      {

      }

    }

    @Override
    public void writeDeclarations(String testName, IndentedWriter targetWriter)
    {

    }

    @Override
    public void writeTestCase(String testName, URI testServer, RequestCase requestCase, IndentedWriter targetWriter)
    {
        try
        {
            targetWriter.println("async ({playwright}) => {");
            targetWriter.println("const request = await playwright.request.newContext({");
            targetWriter.indent();
            targetWriter.println(String.format("baseURL: "));
            targetWriter.println("extraHTTPHeaders");
            targetWriter.unindent();
            targetWriter.println("})");
            targetWriter.indent();
            writeServer(testName, testServer, requestCase, targetWriter);
            if(getDepends().validateResponses())
            {
                targetWriter.println("const response = ");
                targetWriter.indent();
            }

            writeRequest(requestCase, targetWriter);


            targetWriter.unindent();
            targetWriter.print("}");
        }
        catch( Exception e)
        {
            throw new TestWriterException( String.format( "Can't write test case=%s", requestCase), e);
        }
    }

    @Override
    public void writeClosing(String testName, IndentedWriter targetWriter)
    {

    }

    protected void writeRequest(RequestCase requestCase, IndentedWriter targetWriter)
    {
        targetWriter.println(
                String.format(
                        "await .%s(%s)",
                        stringLiteral( requestCase.getOperation().toLowerCase()),
                        stringLiteral( requestCase.getPath())));
    }

    protected void writeContext( String testName, URI testServer, RequestCase requestCase, IndentedWriter targetWriter) {
        //writeServer();
    }

    /**
     * Writes the server URI for a target test case to the given stream.
     */
    protected void writeServer( String testName, URI testServer, RequestCase requestCase, IndentedWriter targetWriter)
    {
        Optional<String> serverUri = serverUri( testServer, requestCase);
        targetWriter.println( String.format("baseURL: %s", serverUri));

        if( !serverUri.isPresent())
        {
            getDepends().setDependsServer();
        }

        if( getDepends().trustServer())
        {
            targetWriter.println("ignoreHTTPSErrors: true");
        }
    }

    /**
     * Writes a cookie parameter definition for a target test case to the given stream.
     */
    protected void writeCookieParam(String testName, ParamData param, IndentedWriter targetWriter)
    {
        getCookieParameters( param).stream()
                .forEach( entry -> targetWriter.println( String.format( ".cookie( %s, %s)", stringLiteral( entry.getKey()), stringLiteral( entry.getValue()))));
    }
}
