

package org.cornutum.tcases.openapi.playwright;

import org.cornutum.tcases.io.IndentedWriter;
import org.cornutum.tcases.openapi.resolver.*;
import org.cornutum.tcases.openapi.resolver.ParamDef.Location;
import org.cornutum.tcases.openapi.test.MediaRange;
import org.cornutum.tcases.openapi.testwriter.BaseTestCaseWriter;
import org.cornutum.tcases.openapi.testwriter.TestWriterException;
import org.cornutum.tcases.openapi.testwriter.TestWriterUtils;
import org.cornutum.tcases.openapi.testwriter.encoder.FormUrlEncoder;
import org.cornutum.tcases.resolve.DataValue;
import org.cornutum.tcases.resolve.ObjectValue;

import static org.cornutum.tcases.openapi.testwriter.TestWriterUtils.*;
import static org.cornutum.tcases.openapi.testwriter.java.TestCaseWriterUtils.*;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Writes the source code for REST Assured test cases that execute API requests.
 */
public class PlaywrightTestCaseWriter extends BaseTestCaseWriter
{
    public void writeExpectDef(String testName, IndentedWriter targetWriter, Depends dependencies)
    {
        targetWriter.println("const expect = baseExpect.extend({");
        targetWriter.indent();

        writeExpectSuccessStatusDef(testName, targetWriter);

        if( dependencies.dependsFailure())
        {
            writeExpectBadStatusDef(testName, targetWriter);
        }

        if( dependencies.dependsAuthFailure())
        {
            writeExpectUnauthorizedStatusDef(testName, targetWriter);
        }

        if(dependencies.validateResponses())
        {
            writeExpectValidHeadersDef(testName, targetWriter);
            writeExpectValidBodyDef(testName, targetWriter);
        }
        targetWriter.unindent();
        targetWriter.println("})");
        targetWriter.println();
    }

    public void writeExpectSuccessStatusDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("toBeSuccess(response: APIResponse) {");
        targetWriter.indent();
        targetWriter.println("const assertionName = 'toBeSuccess';");
        targetWriter.println("const pass = response.status() >= 200 && response.status() < 300;");
        targetWriter.println();
        targetWriter.println("const message = () =>");
        targetWriter.indent();
        targetWriter.println("this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +");
        targetWriter.println("'\\n\\n' +");
        targetWriter.println("`Status Code: ${response.status()}\\n` +");
        targetWriter.println("`Expected: ${this.isNot ? 'not ' : ''}${\"between 200-300\"}\\n`;");
        targetWriter.unindent();
        targetWriter.println("return {");
        targetWriter.indent();
        targetWriter.println("message,");
        targetWriter.println("pass,");
        targetWriter.println("name: assertionName");
        targetWriter.unindent();
        targetWriter.println("};");
        targetWriter.unindent();
        targetWriter.println("},");
    }

    public void writeExpectBadStatusDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("toBeBadRequest(response: APIResponse) {");
        targetWriter.indent();
        targetWriter.println("const assertionName = 'toBeBadRequest';");
        targetWriter.println("const pass = response.status() >= 400 && response.status() < 500;");
        targetWriter.println();
        targetWriter.println("const message = () =>");
        targetWriter.indent();
        targetWriter.println("this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +");
        targetWriter.println("'\\n\\n' +");
        targetWriter.println("`Status Code: ${response.status()}\\n` +");
        targetWriter.println("`Expected: ${this.isNot ? 'not ' : ''}${\"between 400-500\"}\\n`;");
        targetWriter.unindent();
        targetWriter.println("return {");
        targetWriter.indent();
        targetWriter.println("message,");
        targetWriter.println("pass,");
        targetWriter.println("name: assertionName");
        targetWriter.unindent();
        targetWriter.println("};");
        targetWriter.unindent();
        targetWriter.println("},");
    }
    public void writeExpectUnauthorizedStatusDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("toBeUnauthorized(response: APIResponse) {");
        targetWriter.indent();
        targetWriter.println("const assertionName = 'toBeUnauthorized';");
        targetWriter.println("const pass = response.status() == 401;");
        targetWriter.println();
        targetWriter.println("const message = () =>");
        targetWriter.indent();
        targetWriter.println("this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +");
        targetWriter.println("'\\n\\n' +");
        targetWriter.println("`Status Code: ${response.status()}\\n` +");
        targetWriter.println("`Expected: ${this.isNot ? 'not ' : ''}${\"401\"}\\n`;");
        targetWriter.unindent();
        targetWriter.println("return {");
        targetWriter.indent();
        targetWriter.println("message,");
        targetWriter.println("pass,");
        targetWriter.println("name: assertionName");
        targetWriter.unindent();
        targetWriter.println("};");
        targetWriter.unindent();
        targetWriter.println("},");
    }

    public void writeExpectValidHeadersDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("async toBeValidHeaders(response: APIResponse, requestType: string, path: string)");
        targetWriter.println("{");
        targetWriter.indent();
        targetWriter.println("let pass = true;");
        targetWriter.println("let message = () => '';");
        targetWriter.println("const assertionName = 'toBeValidHeaders';");
        targetWriter.println("try");
        targetWriter.println("{");
        targetWriter.indent();
        targetWriter.println("const result = await execShellCommand(");
        targetWriter.indent();
        targetWriter.println("`tcases-response-validator headers -r ${requestType} -p ${path} -s ${response.status()} -h - ${responsesPath}`,");
        targetWriter.println("JSON.stringify(response.headersArray().map(header => ({[header.name]: header.value})))");
        targetWriter.unindent();
        targetWriter.println(");");
        targetWriter.println("pass = !result;");
        targetWriter.println("message = () => this.utils.matcherHint(assertionName, undefined, undefined, " +
                "{ isNot: this.isNot }) +");
        targetWriter.indent();
        targetWriter.println("'\\n\\n' +");
        targetWriter.println("result?.message;");
        targetWriter.unindent();
        targetWriter.unindent();
        targetWriter.println("} catch (e) {");
        targetWriter.indent();
        targetWriter.println("pass = false;");
        targetWriter.println("message = () => (e instanceof Error ? e.message : '');");
        targetWriter.unindent();
        targetWriter.println("}");
        targetWriter.println();
        targetWriter.println("return {");
        targetWriter.indent();
        targetWriter.println("message,");
        targetWriter.println("pass,");
        targetWriter.println("name: assertionName");
        targetWriter.unindent();
        targetWriter.println("};");
        targetWriter.unindent();
        targetWriter.println("},");
    }

    public void writeExpectValidBodyDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("async toBeValidBody(response: APIResponse, requestType: string, path: string)");
        targetWriter.println("{");
        targetWriter.indent();
        targetWriter.println("let pass = true;");
        targetWriter.println("let message = () => '';");
        targetWriter.println("const assertionName = 'toBeValidHeaders';");
        targetWriter.println("try {");
        targetWriter.indent();
        targetWriter.println("const result = await execShellCommand(");
        targetWriter.indent();
        targetWriter.println("`tcases-response-validator body -r ${requestType} -p ${path} -s ${response.status()} " +
                "-f \"${response.headers()['content-type']}\" -c - ${responsesPath}`,");
        targetWriter.println("(await response.body()).toString(\"utf-8\")");
        targetWriter.unindent();
        targetWriter.println(");");
        targetWriter.println("pass = !result;");
        targetWriter.println("message = () => this.utils.matcherHint(assertionName, undefined, undefined, " +
                "{ isNot: this.isNot }) +");
        targetWriter.indent();
        targetWriter.println("'\\n\\n' +");
        targetWriter.println("result?.message;");
        targetWriter.unindent();
        targetWriter.unindent();
        targetWriter.println("} catch (e) {");
        targetWriter.indent();
        targetWriter.println("pass = false;");
        targetWriter.println("if (e instanceof Error)");
        targetWriter.indent();
        targetWriter.println("message = () => e.message;");
        targetWriter.unindent();
        targetWriter.unindent();
        targetWriter.println("}");
        targetWriter.println("return {");
        targetWriter.indent();
        targetWriter.println("message,");
        targetWriter.println("pass,");
        targetWriter.println("name: assertionName");
        targetWriter.unindent();
        targetWriter.println("};");
        targetWriter.unindent();
        targetWriter.println("},");
    }

    public void writeExecShellCommandDef(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("const execShellCommand = (command: string, stdinData: string) => {");
        targetWriter.indent();
        targetWriter.println("return new Promise<ExecException | undefined>((resolve, reject) => {");
        targetWriter.indent();
        targetWriter.println("const child = exec(command, (error, stdout, stderr) => {");
        targetWriter.indent();
        targetWriter.println("if (error) {");
        targetWriter.indent();
        targetWriter.println("console.warn(error);");
        targetWriter.unindent();
        targetWriter.println("}");
        targetWriter.println();
        targetWriter.println("resolve(error ? error : undefined);");
        targetWriter.unindent();
        targetWriter.println("});");
        targetWriter.println();
        targetWriter.println("child.stdin?.write(stdinData);");
        targetWriter.println("child.stdin?.end();");
        targetWriter.unindent();
        targetWriter.println("});");
        targetWriter.unindent();
        targetWriter.println("};");
    }

    /**
     * Creates a new PlaywrightTestCaseWriter instance.
     */
    public  PlaywrightTestCaseWriter()
    {

    }

    @Override
    public void writeDependencies(String testName, IndentedWriter targetWriter)
    {
        targetWriter.println("import { expect as baseExpect, type APIRequestContext, type APIResponse } from '@playwright/test'");
        targetWriter.println("import { type ExecException, exec } from \"child_process\";");
        targetWriter.println();
    }

    @Override
    public void writeDeclarations(String testName, IndentedWriter targetWriter)
    {
        writeRequestOptionsType(targetWriter);
        if (getDepends().validateResponses())
        {
            writeExecShellCommandDef(testName, targetWriter);
        }
        writeExpectDef(testName, targetWriter, getDepends());
        writeAuthCredentialsDef(testName, targetWriter, getDepends());
        writeTestServerDef( testName, targetWriter, getDepends());
    }

    @Override
    public void writeTestCase(String testName, URI testServer, RequestCase requestCase, IndentedWriter targetWriter)
    {
        try
        {
            targetWriter.print("async ({playwright}) => {");
            targetWriter.println();
            targetWriter.indent();

            writeServer(testName, testServer, requestCase, targetWriter);
            targetWriter.println();

            Map<Location, List<ParamData>> paramsByLocation = getParamsByLocation(requestCase);
            writePathParams(testName, paramsByLocation.get(Location.PATH), targetWriter);
            targetWriter.println(String.format("const url = new URL(%s, uri);",
                    stringLiteral( getPathLiteralTemplate(requestCase.getPath()), '`')));

            writeQueryParams(testName, paramsByLocation.get(Location.QUERY), targetWriter);
            targetWriter.println();
            targetWriter.println(String.format("const requestOptions: RequestOptions<%s> = {};",
                    stringLiteral(requestCase.getOperation().toLowerCase())));

            writeAuthDefs(testName, requestCase, targetWriter);

            writeHeaderParams(testName, paramsByLocation.get(Location.HEADER), targetWriter);
            writeCookieParams(testName, paramsByLocation.get(Location.COOKIE), targetWriter);
            writeBody(testName, requestCase, targetWriter);

            writeRequest(requestCase, targetWriter);
            targetWriter.println();
            writeExpectResponse(testName, requestCase, targetWriter, getDepends());
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
        targetWriter.println("const response = ");
        targetWriter.indent();
        targetWriter.println(String.format(
                "await request.%s(url.toString(), requestOptions);",
                requestCase.getOperation().toLowerCase()));
        targetWriter.unindent();
    }

    /**
     * Writes the request body for a target test case to the given stream.
     */
    protected void writeBody( String testName, RequestCase requestCase, IndentedWriter targetWriter)
    {
        Optional.ofNullable( requestCase.getBody())
                .ifPresent( body -> {
                    Optional.ofNullable( body.getValue())
                            .ifPresent( value -> {

                                MediaRange mediaType = MediaRange.of( body.getMediaType());
                                targetWriter.println(String.format(
                                        "requestOptions.headers = { ...requestOptions.headers, 'content-type': %s};",
                                        stringLiteral( mediaType)));

                                // Write binary value?
                                if( "application/octet-stream".equals( mediaType.base()))
                                {
                                    // Yes
                                    writeBodyBinary( testName, value, targetWriter);
                                }

                                // Write form value?
                                else if( "application/x-www-form-urlencoded".equals( mediaType.base()))
                                {
                                    writeBodyForm( testName, body, targetWriter);
                                }

                                // Write multipart form value?
                                else if( "multipart/form-data".equals( mediaType.base()))
                                {
                                    writeBodyMultipart( testName, body, targetWriter);
                                }

                                else
                                {
                                    // No, serialize body value according to media type
                                    targetWriter.println(
                                            String.format(
                                                    "requestOptions.data = %s",
                                                    stringLiteral(
                                                            getConverter( mediaType)
                                                                    .orElseThrow( () -> new TestWriterException( String.format( "No serializer defined for mediaType=%s", mediaType)))
                                                                    .convert( value))));
                                }
                            });
                });
    }

    /**
     * Writes the request body as a byte array for a target test case to the given stream.
     */
    protected void writeBodyBinary(String testName, DataValue<?> value, IndentedWriter targetWriter)
    {
        List<String> segments = byteInitializerFor( value);

        // If small value...
        if( segments.size() == 1)
        {
            // ... write a single line
            targetWriter.println( String.format( "requestOptions.data =  Buffer.from([%s]);", segments.get(0)));
        }
        else
        {
            // Otherwise, write as multiple lines.
            targetWriter.println( "requestOptions.data =  Buffer.from([");
            targetWriter.indent();

            for( String segment : segments)
            {
                targetWriter.println( segment);
            }

            targetWriter.unindent();
            targetWriter.println( "]);");
        }
    }

    /**
     * Writes the request body as an <CODE>application/x-www-form-urlencoded</CODE> form for a target test case to the given stream.
     */
    protected void writeBodyForm(String testName, MessageData body, IndentedWriter targetWriter)
    {
        Map<String,List<Map.Entry<String, String>>> encodedBody = FormUrlEncoder.encode( body.getValue(),
                body.getEncodings(),
                false).stream().collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.toList()));

        Function<Map.Entry<String, String>, String> toFormParamFormat = entry -> entry.getValue() == null
                ? "%s: '',"
                : "%s: %s,";

        if (!encodedBody.isEmpty())
        {
            targetWriter.println("requestOptions.form = {");
            targetWriter.indent();

            encodedBody.forEach( (key, entries) -> {
                if (entries.size() > 1)
                {
                    targetWriter.println(String.format("%s: JSON.stringify([", key));
                    targetWriter.indent();
                    entries.forEach( entry -> targetWriter.println(String.format("%s,",
                            stringLiteral(entry.getValue()))));
                    targetWriter.unindent();
                    targetWriter.println("]),");
                }
                else
                {
                    Map.Entry<String, String> entry = entries.get(0);

                    targetWriter.println(
                            String.format(
                                    toFormParamFormat.apply(entry),
                                    entry.getKey(),
                                    stringLiteral( entry.getValue())));
                }});
            targetWriter.unindent();
            targetWriter.println("};");
        }
    }

    /**
     * Writes the request body as <CODE>multipart/form-data</CODE> form for a target test case to the given stream.
     */
    protected void writeBodyMultipart( String testName, MessageData body, IndentedWriter targetWriter)
    {
        // Multipart forms apply only to object values. Non-object values, which may be supplied by failure test cases, are all
        // handled as "empty body".
        if( body.getValue().getType() == DataValue.Type.OBJECT)
        {
            targetWriter.println( "requestOptions.multipart = { ...requestOptions.multipart,");
            targetWriter.indent();
            ObjectValue objectValue = (ObjectValue) body.getValue();
            objectValue.getValue().forEach( (property, value) -> writeMultipartPart( property, value, body.getEncodings().get( property), targetWriter));
            targetWriter.unindent();
            targetWriter.println( "};");
        }
    }

    protected void writeMultipartPart(String property, DataValue<?> value, EncodingData encoding, IndentedWriter targetWriter)
    {
        MediaRange contentType = MediaRange.of( encoding.getContentType());

        if( "application/octet-stream".equals( contentType.base()))
        {
            targetWriter.println( String.format( "%s: {", stringLiteral( property)));
            targetWriter.indent();
            targetWriter.println( "name: ''");
            targetWriter.println( String.format( "mimeType: %s", stringLiteral( contentType)));

            List<String> segments = byteInitializerFor( value);

            // If small value...
            if( segments.size() == 1)
            {
                // ... write a single line
                targetWriter.println( String.format( "buffer: Buffer.from([%s])", segments.get(0)));
            }
            else
            {
                // Otherwise, write as multiple lines.
                targetWriter.println( "buffer: Buffer.from([");
                targetWriter.indent();

                for( String segment : segments)
                {
                    targetWriter.println( segment);
                }

                targetWriter.unindent();
                targetWriter.println( "]))");
            }
            targetWriter.unindent();
            targetWriter.println("},");
        }

        else
        {
            String partData;
            if( "application/x-www-form-urlencoded".equals( contentType.base()))
            {
                partData = FormUrlEncoder.toForm( value);
            }
            else
            {
                partData =
                        getConverter( contentType)
                                .orElseThrow( () -> new TestWriterException( String.format( "No serializer defined for contentType=%s", contentType)))
                                .convert( value);
            }

            targetWriter.println( String.format( "%s: %s,", stringLiteral(property), stringLiteral( partData)));
        }

// **Headers are not supported out of the box**
//
//        encoding.getHeaders().stream()
//                .forEach( headerData -> {
//                    targetWriter.println(
//                            String.format(
//                                    ".header( %s, %s)",
//                                    stringLiteral( headerData.getName()),
//                                    stringLiteral( SimpleValueEncoder.encode( headerData.getValue(), headerData.isExploded()))));
//                });
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

        if( !serverUri.isPresent())
        {
            getDepends().setDependsServer();
        }

        targetWriter.println("const request = await playwright.request.newContext({");
        targetWriter.indent();

        if( getDepends().trustServer())
        {
            targetWriter.println("ignoreHTTPSErrors: true,");
        }

        targetWriter.unindent();
        targetWriter.println("});");
        targetWriter.println(String.format("const uri = new URL(%s);", forTestServer(getDepends().dependsServer() ?
                Optional.empty() :
                serverUri)));
    }

    /**
     * Writes request parameter definitions for a target test case to the given stream.
     */
    protected Map<ParamDef.Location, List<ParamData>> getParamsByLocation(RequestCase requestCase)
    {
        return StreamSupport.stream(requestCase.getParams().spliterator(), false)
                .collect(Collectors.groupingBy(ParamData::getLocation));
    }

    /**
     * Writes a query parameters definition for a target test case to the given stream.
     */
    protected void writeQueryParams( String testName, List<ParamData> params, IndentedWriter targetWriter)
    {
        if (params != null)
        {
            targetWriter.println("const queryParams = url.searchParams;");
            params.stream().map(TestWriterUtils::getQueryParameters)
                    .flatMap(List::stream)
                    .forEach((entry) -> {
                        targetWriter.println(String.format("%squeryParams.append(%s, %s);",
                                entry.getValue() != null ? "" : "// ",
                                stringLiteral(entry.getKey()),
                                stringLiteral(entry.getValue())));
                    });
        }
    }

    /**
     * Writes a path parameters definition for a target test case to the given stream.
     */
    protected void writePathParams( String testName, List<ParamData> params, IndentedWriter targetWriter)
    {
        if(params != null)
        {
            targetWriter.println("const pathParams = {");
            targetWriter.indent();
            params.forEach(param -> targetWriter.println( String.format( "%s: %s,",
                    param.getName(),
                    stringLiteral( getPathParameterValue( param))))
            );
            targetWriter.unindent();
            targetWriter.println("};");
        }
    }

    /**
     * Writes a headers parameter definition for a target test case to the given stream.
     */
    protected void writeHeaderParams( String testName, List<ParamData> params, IndentedWriter targetWriter)
    {
        if(params != null)
        {
            targetWriter.println("const headers = {");
            targetWriter.indent();
            params.forEach(param -> getHeaderParameterValue( param)
                    .ifPresent( value -> targetWriter.println( String.format( "%s: %s,",
                            param.getName(),
                            stringLiteral( value)))));
            targetWriter.unindent();
            targetWriter.println("};");
            targetWriter.println("requestOptions.headers = { ...requestOptions.headers, ...headers};");
        }
    }

    /**
     * Writes a cookie parameters definition for a target test case to the given stream.
     */
    protected void writeCookieParams( String testName, List<ParamData> params, IndentedWriter targetWriter)
    {
        if(params != null)
        {
            List<Map.Entry<String, String>> cookieParameters = params.stream()
                    .map(TestWriterUtils::getCookieParameters)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!cookieParameters.isEmpty())
            {
                if(cookieParameters.size() > 1)
                {
                    targetWriter.println("requestOptions.headers.Cookie = [requestOptions.headers.Cookie,");
                    targetWriter.indent();
                    cookieParameters.forEach(cookie ->
                            targetWriter.println(stringLiteral(String.format("%s=%s",
                                    cookie.getKey(),
                                    cookie.getValue()))));
                    targetWriter.unindent();
                    targetWriter.println("].filter((cookie) => cookie != undefined).join('; ')");
                }
                else
                {
                    targetWriter.println(
                            String.format("requestOptions.headers = { ...requestOptions.headers, Cookie: %s }",
                                    stringLiteral(String.format("%s=%s",
                                            cookieParameters.get(0).getKey(),
                                            cookieParameters.get(0).getValue()))));
                }
            }
        }
    }

    /**
     * Writes request authentication definitions for a target test case to the given stream.
     **/
    protected void writeAuthDefs( String testName, RequestCase requestCase, IndentedWriter targetWriter)
    {
        for( AuthDef authDef : requestCase.getAuthDefs())
        {
            writeAuthDef( testName, authDef, targetWriter);
        }
    }

    /**
     * Writes a request authentication definition for a target test case to the given stream.
     */
    protected void writeAuthDef( String testName, AuthDef authDef, IndentedWriter targetWriter)
    {
        switch( authDef.getLocation())
        {
            case QUERY:
            {
                targetWriter.println( String.format( "url.append( %s, %s)", stringLiteral( authDef.getName()), "tcasesApiKey()"));
                break;
            }

            case HEADER:
            {
                targetWriter.println( String.format( "requestOptions.headers = { ...requestOptions.headers, %s: %s};", stringLiteral( authDef.getName()), headerValueOf( authDef)));
                break;
            }

            case COOKIE:
            {
                targetWriter.println( String.format( "requestOptions.headers.Cookie = [requestOptions.headers.Cookie, '%s=%s'].filter((cookie) => cookie != undefined).join('; ')", stringLiteral( authDef.getName()), "tcasesApiKey()"));
                break;
            }

            default:
            {
                throw new IllegalStateException( String.format( "Invalid location for authentication value=%s", authDef));
            }
        }
    }



    protected String getPathLiteralTemplate(String path)
    {
        Pattern p = Pattern.compile("\\{(.*?)\\}");
        Matcher m = p.matcher(path);

        StringBuffer sb = new StringBuffer();

        while (m.find())
        {
            m.appendReplacement(sb, String.format("\\${pathParams.%s}",m.group(1)));
        }

        m.appendTail(sb);

        return sb.toString();
    }


    protected void writeRequestOptionsType(IndentedWriter targetWriter)
    {
        targetWriter.println("type RequestOptions<T extends keyof APIRequestContext> = (APIRequestContext[T] extends");
        targetWriter.indent();
        targetWriter.println("(url: never, options?: infer K) => unknown ? K : never);");
        targetWriter.unindent();
    }

    /**
     * Writes response expectations for a target test case to the given stream.
     */
    protected void writeExpectResponse( String testName, RequestCase requestCase,
                                        IndentedWriter targetWriter, Depends dependencies)
    {
        if( requestCase.isFailure())
        {
            targetWriter.println( String.format( "// %s", requestCase.getInvalidInput()));
            targetWriter.println( String.format( "expect(response).%s",
                    requestCase.isAuthFailure()? "toBeUnauthorized();" : "toBeBadRequest();"));
        }
        else
        {
            targetWriter.println( "expect(response).toBeSuccess();");
        }

        if(dependencies.validateResponses())
        {
            targetWriter.println(String.format("await expect(response).toBeValidHeaders(%s, %s)",
                    stringLiteral(requestCase.getOperation(), '\''),
                    stringLiteral(requestCase.getPath(), '\'')));
            targetWriter.println(String.format("await expect(response).toBeValidBody(%s, %s)",
                    stringLiteral(requestCase.getOperation(), '\''),
                    stringLiteral(requestCase.getPath(), '\'')));
        }
    }

    /**
     * Writes the definition of standard methods for runtime specification of authentication credentials to the given stream.
     */
    protected void writeAuthCredentialsDef( String testName, IndentedWriter targetWriter, Depends dependencies)
    {
        if( dependencies.dependsApiKey())
        {
            targetWriter.println( "const tcasesApiKey = () => process.env.tcasesApiKey ?? '';");
            targetWriter.println();
        }

        if( dependencies.dependsHttpBearer())
        {
            targetWriter.println();
            targetWriter.println( "const tcasesApiBearer = () => process.env.tcasesApiBearer ?? '';");
            targetWriter.println();
            targetWriter.println( "const tcasesApiBearerCredentials = () => `Bearer ${tcasesApiBearer()}`;");
        }

        if( dependencies.dependsHttpBasic())
        {
            targetWriter.println( "const tcasesApiUser = () => process.env.tcasesApiUser ?? '';");
            targetWriter.println();
            targetWriter.println( "const tcasesApiPassword = () => process.env.tcasesApiPassword ?? '';");
            targetWriter.println();
            targetWriter.println( "private String asToken64( String value) {");

            targetWriter.println( "private String tcasesApiBasicCredentials() {");
            targetWriter.println();
            targetWriter.println("const asToken64 = (value: string) => {");
            targetWriter.indent();
            targetWriter.println("try {");
            targetWriter.indent();
            targetWriter.println("Buffer.from(value, 'binary').toString('base64');");
            targetWriter.unindent();
            targetWriter.println("} catch (e) {");
            targetWriter.indent();
            targetWriter.println("throw new Error(`Can't get Base64 token for value=${value}, error=${e}`)");
            targetWriter.unindent();
            targetWriter.println("}");
            targetWriter.unindent();
            targetWriter.println("};");
            targetWriter.println();
        }
    }

    protected void writeTestServerDef( String testName, IndentedWriter targetWriter, Depends dependencies)
    {
        targetWriter.println( "const tcasesApiServer = () => process.env.tcasesApiServer ?? '';");

        if( dependencies.dependsServer())
        {
            targetWriter.println();
            targetWriter.println( "const forTestServer = tcasesApiServer;");
        }
        else
        {
            targetWriter.println();
            targetWriter.println( "const forTestServer = (defaultUri?: string) => {");
            targetWriter.indent();
            targetWriter.println( "const testServer = tcasesApiServer();");
            targetWriter.println( "return defaultUri == undefined || testServer != ''");
            targetWriter.indent();
            targetWriter.println( "? testServer");
            targetWriter.println( ": defaultUri;");
            targetWriter.unindent();
            targetWriter.unindent();
            targetWriter.println( "}");
            targetWriter.println();
        }
    }
}

