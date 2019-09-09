package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests basic indexing/removal of labels.
 */
public class TestCapitalsHandling extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestCapitalsHandling(boolean useAskControl) {
        super(useAskControl);
    }

    @Test
    public void loadThenIndex() throws Exception {
        insertSomeData();
        enablePlugin();
        testValidSuggestions();
    }

    @Test
    public void indexThenLoad() throws Exception {
        enablePlugin();
        insertSomeData();
        testValidSuggestions();
    }

    public void testValidSuggestions() throws Exception {
        List<String> results = executeQueryAndGetResults(";US");
        System.out.println(String.join("\n", results));
        assertTrue(results.contains("urn:USRegion; urn:<b>US</b>Region"));
        assertTrue(results.contains("urn:USRR; urn:<b>US</b>RR"));

        results = executeQueryAndGetResults(";USR");
        assertTrue(results.contains("urn:USRegion; urn:<b>US</b><b>R</b>egion"));
        assertTrue(results.contains("urn:USRR; urn:<b>USR</b>R"));

        results = executeQueryAndGetResults(";NATO");
        assertTrue(results.contains("urn:NATO; urn:<b>NATO</b>"));
    }

    private void insertSomeData() {
        connection.begin();
        connection.add(vf.createIRI("urn:USRegion"), RDFS.LABEL, vf.createLiteral("САЩ"));
        connection.add(vf.createIRI("urn:USRR"), RDFS.LABEL, vf.createLiteral("union of socialist russian republics"));
        connection.add(vf.createIRI("urn:NATO"), RDFS.LABEL, vf.createLiteral("OTAN"));
        connection.commit();
    }
}
