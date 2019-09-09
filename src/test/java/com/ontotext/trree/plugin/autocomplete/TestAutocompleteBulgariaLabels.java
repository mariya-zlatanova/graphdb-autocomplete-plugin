package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests if sorting works as expected.
 */
public class TestAutocompleteBulgariaLabels extends AutocompletePluginTestBase {

    private static final List<String> EXPECTED_SUGGESTIONS = Arrays.asList(
            "http://dbpedia.org/resource/Bulgaria; <b>Бълга</b>рия &lt;http://dbpedia.org/resource/Bulgaria&gt;",
            "http://dbpedia.org/resource/Bulgarian_language; <b>бълга</b>рски &lt;http://dbpedia.org/resource/Bulgarian_language&gt;"
            //"http://dbpedia.org/resource/Bulgarian_language; <b>Бълга</b>рски език <http://dbpedia.org/resource/Bulgarian_language>"
    );

    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestAutocompleteBulgariaLabels(boolean useAskControl) {
        super(useAskControl);
    }

    @Test
    public void loadThenIndex() throws Exception {
        importData("src/test/resources/import/bulgaria_labels.ttl", RDFFormat.TURTLE);
        enablePlugin();
        testValidSuggestions();
    }

    @Test
    public void indexThenLoad() throws Exception {
        enablePlugin();

        connection.begin();
        connection.add(new File("src/test/resources/import/bulgaria_labels.ttl"), "urn:base", RDFFormat.TURTLE);
        connection.commit();

        testValidSuggestions();
    }

    public void testValidSuggestions() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<String> results = executeQueryAndGetResults(";бълга");
        assertEquals(EXPECTED_SUGGESTIONS, results);
    }
}
