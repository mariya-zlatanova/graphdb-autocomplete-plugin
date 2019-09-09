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
public class TestAutocompleteBulgaria extends AutocompletePluginTestBase {
    private static final List<String> EXPECTED_SUGGESTIONS = Arrays.asList(
            "http://dbpedia.org/resource/Bulgaria; http://dbpedia.org/resource/<b>Bulgaria</b>",
            "http://dbpedia.org/resource/Bulgarian; http://dbpedia.org/resource/<b>Bulgaria</b>n",
            "http://dbpedia.org/resource/Bulgarianish; http://dbpedia.org/resource/<b>Bulgaria</b>nish",
            "http://dbpedia.org/resource/Bulgarian_jewry; http://dbpedia.org/resource/<b>Bulgaria</b>n_jewry",
            "http://dbpedia.org/resource/Bulgarian_Empire; http://dbpedia.org/resource/<b>Bulgaria</b>n_Empire",
            "http://dbpedia.org/resource/Bulgarian_Tournament_Cup; http://dbpedia.org/resource/<b>Bulgaria</b>n_Tournament_Cup",
            "http://dbpedia.org/resource/Bulgarian_Agrarian_Union; http://dbpedia.org/resource/<b>Bulgaria</b>n_Agrarian_Union",
            "http://dbpedia.org/resource/Bulgaria_Boxing_Association; http://dbpedia.org/resource/<b>Bulgaria</b>_Boxing_Association",
            "http://dbpedia.org/resource/Bulgarian_Theological_College; http://dbpedia.org/resource/<b>Bulgaria</b>n_Theological_College",
            "http://dbpedia.org/resource/Bulgarian_Softball_Federation; http://dbpedia.org/resource/<b>Bulgaria</b>n_Softball_Federation"
    );

    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestAutocompleteBulgaria(boolean useAskControl) {
        super(useAskControl);
    }

    @Test
    public void loadThenIndex() throws Exception {
        importData("src/test/resources/import/bulgaria_uris.ttl", RDFFormat.TURTLE);
        enablePlugin();
        testValidSuggestions();
    }

    @Test
    public void indexThenLoad() throws Exception {
        enablePlugin();

        connection.begin();
        connection.add(new File("src/test/resources/import/bulgaria_uris.ttl"), "urn:base", RDFFormat.TURTLE);
        connection.commit();

        testValidSuggestions();
    }

    public void testValidSuggestions() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<String> results = executeQueryAndGetResults("http://dbpedia.org/resource/;Bulgaria");
        assertEquals(100, results.size());
        List<String> subResults = results.subList(0, 10);
        assertEquals(EXPECTED_SUGGESTIONS, subResults);
    }

}
