package org.cornutum.tcases.openapi;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cornutum.tcases.HelpException;
import org.cornutum.tcases.openapi.test.ResponseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.cornutum.tcases.CommandUtils.*;
import static org.cornutum.tcases.CommandUtils.throwUsageException;

public class ApiResponseValidatorCommand {
    public static class Options
    {
        public enum ValidationType { BODY, HEADERS }
        /**
         * Creates a new Options object.
         */
        public Options()
        {
            setWorkingDir( null);
            setStatusCode(200);
            setContent("application/json");
        }

        /**
         * Creates a new Options object.
         */
        public Options( String[] args)
        {
            this();

            int i;

            // Handel command
            i = handelCommand(args, 0);

            // Handle options
            for( ; i < args.length && args[i].charAt(0) == '-'; i = handleOption( args, i));

            // Handle additional arguments.
            handleArgs( args, i);
        }

        protected int handelCommand(String[] args, int i)
        {
            if(args.length > 0)
            {
                try {
                    setValidationType(args[i]);
                }
                catch (Exception e) {
                    throwUsageException( "Invalid command", e);
                }

                i++;
            }

            return  i;
        }

        /**
         * Handles the i'th option and return the index of the next argument.
         */
        protected int handleOption( String[] args, int i)
        {
            String arg = args[i];

            if( arg.equals( "-help"))
            {
                throwHelpException();
            }
            else if( arg.equals( "-v"))
            {
                setShowVersion( true);
            }
            else if(arg.equals("-r"))
            {
                i++;
                if( i >= args.length)
                {
                    throwMissingValue( arg);
                }
                setOperation(args[i]);
            }
            else if(arg.equals("-p"))
            {
                i++;
                if( i >= args.length)
                {
                    throwMissingValue( arg);
                }
                setPath(args[i]);
            }
            else if(arg.equals("-s"))
            {
                i++;
                if( i >= args.length)
                {
                    throwMissingValue( arg);
                }
                try {
                    setStatusCode(args[i]);
                }
                catch (Exception e)
                {
                    throwMissingValue( arg);
                }
            }
            else if(getValidationType().equals(ValidationType.BODY))
            {
                if(arg.equals("-f"))
                {
                    i++;
                    if( i >= args.length)
                    {
                        throwMissingValue( arg);
                    }
                    setContentType(args[i]);
                }
                else if(arg.equals("-c"))
                {
                    i++;
                    if( i >= args.length)
                    {
                        throwMissingValue( arg);
                    }
                    setContent(args[i]);
                }
            }
            else if(getValidationType().equals(ValidationType.HEADERS))
            {
                if(arg.equals("-h"))
                {
                    i++;
                    if( i >= args.length)
                    {
                        throwMissingValue( arg);
                    }
                    try {
                        setHeaders(args[i]);
                    }
                    catch( Exception e)
                    {
                        throwUsageException( "Invalid test type", e);
                    }
                }
            }
            else
            {
                throwUsageException( String.format( "Unknown option: %s", arg));
            }

            return i + 1;
        }

        /**
         * Handles the non-option arguments i, i+1, ...
         */
        protected void handleArgs( String[] args, int i)
        {
            int nargs = args.length - i;

            if( nargs > 1)
            {
                throwUsageException( String.format( "Unexpected argument: %s", args[i+1]));
            }

            if( nargs > 0)
            {
                setApiResponses( new File( args[i]));
            }
        }

        /**
         * Throws a HelpException after printing usage information to standard error.
         */
        protected void throwHelpException()
        {
            printUsage();
            throw new HelpException();
        }

        protected void printUsage()
        {
            for(String line: new String[] {
                    "a",
                    "b"
            }) {
                System.err.println( line);
            };
        }

        /**
         * Changes the API responses file definition
         */
        public void setApiResponses( File apiResponses)
        {
            this.apiResponses_ = apiResponses;
        }

        /**
         * Returns the API responses file definition
         */
        public File getApiResponses()
        {
            return apiResponses_;
        }

        /**
         * Changes the current working directory used to complete relative path names.
         */
        public void setWorkingDir( File workingDir)
        {
            workingDir_ =
                    workingDir == null
                            ? new File( ".")
                            : workingDir;
        }

        /**
         * Returns the current working directory used to complete relative path names.
         */
        public File getWorkingDir()
        {
            return workingDir_;
        }

        /**
         * Changes if the current version should be shown.
         */
        public void setShowVersion( boolean showVersion)
        {
            showVersion_ = showVersion;
        }

        /**
         * Returns if the current version should be shown.
         */
        public boolean showVersion()
        {
            return showVersion_;
        }

        /**
         * Changes the validator type to check responses
         */
        public void setValidationType(ValidationType validationType)
        {
            this.ValidationType_ = validationType;
        }

        /**
         * Changes the validator type to check responses
         */
        public void setValidationType(String testType)
        {
            setValidationType(ValidationType.valueOf( String.valueOf( testType).toUpperCase()));
        }

        /**
         * Returns the validator type
         */
        public ValidationType getValidationType()
        {
            return this.ValidationType_;
        }

        /**
         * Changes the HTTP operation eg. POST, GUEST, ...
         */
        public void setOperation(String operation)
        {
            this.Operation_ = operation;
        }

        /**
         * Returns the HTTP operation e.g., POST, GUEST, ...
         */
        public String getOperation()
        {
            return Operation_;
        }

        /**
         * Changes the response path
         */
        public void setPath(String path)
        {
            this.Path_ = path;
        }

        /**
         * Returns the response path
         */
        public String getPath()
        {
            return Path_;
        }

        /**
         * Changes the response status code
         */
        public void setStatusCode(String statusCode)
        {
            this.StatusCode_ = Integer.parseInt(statusCode);
        }

        /**
         * Changes the response status code
         */
        public void setStatusCode(int statusCode)
        {
            this.StatusCode_ = statusCode;
        }

        /**
         * Returns the response status code
         */
        public int getStatusCode()
        {
            return StatusCode_;
        }

        /**
         * Changes the response content Type
         */
        public void setContentType(String contentType)
        {
            this.ContentType_ = contentType;
        }

        /**
         * Returns the response content Type
         */
        public String getContentType()
        {
            return ContentType_;
        }

        /**
         * Changes the response content
         */
        public void setContent(String content)
        {
            this.Content_ = content;
        }

        /**
         * Returns the response content
         */
        public String getContent()
        {
            return Content_;
        }

        /**
         * Changes the response headers
         */
        public void setHeaders(String headers)
        {
            try
            {
                setHeaders(new ObjectMapper().readValue(headers, new TypeReference<Map<String,List<String>>>(){}));}
            catch (Exception e)
            {
                throwUsageException( "Invalid content type", e);
            }
        }

        /**
         * Changes the response headers
         */
        public void setHeaders(Map<String, List<String>> headers)
        {
            this.Headers_ = headers;
        }

        /**
         * Returns the response headers
         */
        public Map<String, List<String>> getHeaders()
        {
            return Headers_;
        }

        private File apiResponses_;
        private File workingDir_;
        private boolean showVersion_;
        private ValidationType ValidationType_;
        private String Operation_;
        private String Path_;
        private int StatusCode_;
        private String ContentType_;
        private String Content_;
        private Map<String, List<String>> Headers_;
    }


    /**
     * Asserts response against a response definition .
     */
    public static void main( String[] args)
    {
        int exitCode = 0;
        try
        {
            run( new ApiResponseValidatorCommand.Options( args));
        }
        catch( HelpException h)
        {
            exitCode = 1;
        }
        catch( Throwable e)
        {
            exitCode = 1;
            e.printStackTrace( System.err);
        }
        finally
        {
            System.exit( exitCode);
        }
    }

    /**
     * Generates input models and test models for API clients and servers, based on an OpenAPI v3 compliant API definition,
     * using the given {@link ApiTestCommand.Options command line options}.
     */
    public static void run( ApiResponseValidatorCommand.Options options) throws Exception
    {
        if( options.showVersion())
        {
            System.out.println( getVersion());
            return;
        }
        logger_.info( "{}", getVersion());

        // Identify the API definition file
        File apiResponsesFile = options.getApiResponses();
        if( apiResponsesFile != null && !apiResponsesFile.isAbsolute())
        {
            apiResponsesFile = new File( options.getWorkingDir(), apiResponsesFile.getPath());
        }

        // Generate requested input definition
        logger_.info( "Reading API responses from {}", Objects.toString( apiResponsesFile,  "standard input"));
        ResponseValidator validator = new ResponseValidator(new FileInputStream(apiResponsesFile));

        if(options.getOperation() == null)
        {
            throwUsageException("Missing option, operation");
        }
        if(options.getPath() == null)
        {
            throwUsageException("Missing option, path");
        }
        switch (options.getValidationType())
        {
            case BODY:
            {
                if(options.getContent() == null)
                {
                    throwUsageException("Missing option, content");
                }

                logger_.info("Asserting body");

                validator.assertBodyValid(options.getOperation(), options.getPath(), options.getStatusCode(),
                        options.getContentType(), options.getContent());
                break;
            }
            case HEADERS:
            {
                logger_.info("Asserting headers");

                validator.assertHeadersValid(options.getOperation(), options.getPath(), options.getStatusCode(),
                        options.getHeaders());
                break;
            }
        }
    }

    private static final Logger logger_ = LoggerFactory.getLogger( ApiTestCommand.class);
}
