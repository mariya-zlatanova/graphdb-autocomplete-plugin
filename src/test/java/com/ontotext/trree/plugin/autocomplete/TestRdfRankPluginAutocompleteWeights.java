package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 08-Mar-2016
 */
public class TestRdfRankPluginAutocompleteWeights extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestRdfRankPluginAutocompleteWeights(boolean useAskControl) {
        super(useAskControl);
    }

    @Before
    public void setup() throws Exception {
        importData("src/test/resources/import/wine.rdf", RDFFormat.RDFXML);
    }

    private void init(boolean useRank) throws Exception {
        if (useRank) {
            // compute RDF ranks using RDF rank plugin
            connection.begin();
            connection.prepareUpdate("INSERT DATA { _:b1 <http://www.ontotext.com/owlim/RDFRank#compute> _:b2. }").execute();
            connection.commit();
            waitForRankStatus("COMPUTED");
        }

        // enable autocomplete plugin which should discover RDF Rank plugin dependency
        enablePlugin();
    }

    @Test
    public void shouldUseRdfRankForTheReturnedCompletions() throws Exception {
        init(true);

        List<String> results = executeQueryAndGetResults("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#;Re");

        assertTrue(results.size() > 10);
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Region; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>gion", results.get(0));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Red; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>d", results.get(1));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#RedWine; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>dWine", results.get(2));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#FrenchRegion; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#French<b>Re</b>gion", results.get(3));
    }

    @Test
    public void shouldNotUseRdfRankForTheReturnedCompletions() throws Exception {
        init(false);

        List<String> results = executeQueryAndGetResults("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#;Re");

        assertTrue(results.size() > 10);

        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Red; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>d", results.get(0));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Region; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>gion", results.get(1));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#RedWine; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>dWine", results.get(2));
        assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#RedBurgundy; http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#<b>Re</b>dBurgundy", results.get(3));
    }
}
