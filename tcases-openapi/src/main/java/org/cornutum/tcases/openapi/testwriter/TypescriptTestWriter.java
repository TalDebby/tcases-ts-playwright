//////////////////////////////////////////////////////////////////////////////
//
//                    Copyright 2020, Cornutum Project
//                             www.cornutum.org
//
//////////////////////////////////////////////////////////////////////////////

package org.cornutum.tcases.openapi.testwriter;

import org.cornutum.tcases.io.IndentedWriter;
import org.cornutum.tcases.openapi.resolver.RequestCase;
import org.cornutum.tcases.openapi.testwriter.TestCaseWriter;
import org.cornutum.tcases.openapi.testwriter.TestSource;
import org.cornutum.tcases.openapi.testwriter.TestTarget;
import org.cornutum.tcases.openapi.testwriter.TestWriter;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.cornutum.tcases.DefUtils.toNumberIdentifiers;
import static org.cornutum.tcases.util.CollectionUtils.fromCsv;

/**
 * D Writes Typescript source code for a test that executes API requests.
 */
public abstract class TypescriptTestWriter extends TestWriter<TestSource, TestTarget>
{
    /**
     * Creates a new TypescriptTestWriter instance.
     */
    protected TypescriptTestWriter(TestCaseWriter testCaseWriter)
    {
        super( testCaseWriter);
    }

    /**
     * Returns the test name derived from the given base name.
     */
    @Override
    protected String getTestName( String baseName)
    {
        String[] words = baseName.split( "\\W+");

        return
                IntStream.range( 0, words.length)
                        .mapToObj(i -> i==0 ? words[i].toLowerCase(): capitalize(words[i]))
                        .collect( joining( ""));
    }

    /**
     * Writes the target test docstring to the given stream.
     */
    @Override
    protected void writeDocstring( TestTarget target, String testName, IndentedWriter targetWriter)
    {
        super.writeDocstring(target, testName, targetWriter);
    };

    /**
     * Writes the target test opening to the given stream.
     */
    @Override
    protected void writeOpening( TestTarget target, String testName, IndentedWriter targetWriter)
    {

    }

    /**
     * Writes the target test dependencies to the given stream.
     */
    @Override
    protected void writeDependencies( TestTarget target, String testName, IndentedWriter targetWriter)
    {
    }

    /**
     * Writes the target test declarations to the given stream.
     */
    @Override
    protected void writeDeclarations( TestTarget target, String testName, IndentedWriter targetWriter)
    {
    }

    /**
     * Returns the resource directory derived from the given target file and resource directory options.
     */
    @Override
    protected File getTestResourceDir( File targetFile, File resourceDir)
    {
        return
                super.getTestResourceDir(
                        targetFile,
                        resourceDir);
    }

    /**
     * Returns the target file defined by the given target.
     */
    @Override
    protected File getTargetFile( TestTarget target, String testName)
    {
        File targetFile = super.getTargetFile( target, testName);

        return
                targetFile != null && isBlank( getExtension( targetFile.getName()))
                        ? new File( targetFile.getParentFile(), String.format( "%s.ts", getBaseName( targetFile.getName())))
                        : targetFile;
    }

    /**
     * Returns a test method name for the given request case.
     */
    protected String getMethodName( RequestCase requestCase)
    {
        StringBuilder methodName = new StringBuilder();

        // The request operation...
        methodName.append( requestCase.getOperation().toLowerCase());

        // ... followed by...
        Arrays.stream( requestCase.getPath().split( "/"))
                // ... for each segment of the request path...
                .forEach( segment -> {
                    Matcher segmentMatcher = uriSegmentPattern_.matcher( segment);
                    while( segmentMatcher.find())
                    {
                        // ... the sequence of identifiers it contains...
                        methodName.append( toIdentifier( segmentMatcher.group()));
                    }
                });

        // ... followed by the request case description
        getDescriptor( requestCase)
                .ifPresent( descriptor -> methodName.append( " ").append( descriptor));

        return methodName.toString();
    }

    /**
     * Returns an identifier containing a description of the given request case.
     * Returns <CODE>Optional.empty()</CODE> if no description is needed.
     */
    protected Optional<String> getDescriptor( RequestCase requestCase)
    {
        return
                "None.Defined='No'".equals( requestCase.getName())
                        ? Optional.empty()
                        : Optional.of( createDescriptor( requestCase));
    }

    /**
     * Returns an identifier containing a description of the given request case.
     */
    protected String createDescriptor( RequestCase requestCase)
    {
        return
                Optional.ofNullable( requestCase.getName())
                        .map( name -> getBindingsDescriptor( name).orElse( toIdentifier( name)))
                        .orElse( String.valueOf( requestCase.getId()));
    }

    /**
     * If the given text describes a set of variable bindings, returns a description of the bindings.
     * Otherwise, returns <CODE>Optional.empty()</CODE>.
     */
    protected Optional<String> getBindingsDescriptor( String text)
    {
        Stream.Builder<String> bindings = Stream.builder();
        Matcher varBindingMatcher = varBindingPattern_.matcher( text);
        while( varBindingMatcher.find())
        {
            String varId = toIdentifier( removeEnd( varBindingMatcher.group(1), ".Is"));

            String[] value = fromCsv( varBindingMatcher.group(2)).toArray( String[]::new);
            String valueId = value.length == 0 ?
                    "Empty" :
                    value[0] == null ?
                            "Null" :
                            isBlank( value[0])?
                                    "Blank" :
                                    toIdentifier(toNumberIdentifiers( value[0])
                                            .replaceAll( " *<= *", "leq")
                                            .replaceAll( " *< *", "lt")
                                            .replaceAll( " *>= *", "geq ")
                                            .replaceAll( " *> *", "gt "));

            bindings.add( String.format( "%s Is %s", varId, valueId));
        }

        String descriptor = bindings.build().collect( joining( "_"));

        return
                descriptor.isEmpty()
                        ? Optional.empty()
                        : Optional.of( descriptor);
    }

    /**
     * Reduces the given text to a single identifier.
     */
    protected String toIdentifier( String text)
    {
        return
                Arrays.stream( text.trim().split( "\\W+"))
                        .map( id -> capitalize( id))
                        .collect( joining( ""));
    }

    private static final Pattern uriSegmentPattern_ = Pattern.compile( "([^{}]+)|\\{([^}]+)\\}");
    private static final Pattern varBindingPattern_ = Pattern.compile( "([\\w\\-.]+)=([^\\&]+)");
}

