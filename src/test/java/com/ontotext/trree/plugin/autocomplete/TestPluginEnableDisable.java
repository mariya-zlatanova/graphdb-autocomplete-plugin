package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


public class TestPluginEnableDisable extends AutocompletePluginTestBase {
    private static final String ASK_ENABLED_AUTOCOMPLETE = "ask where { _:s <http://www.ontotext.com/plugins/autocomplete#enabled> ?o . } ";

    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestPluginEnableDisable(boolean useAskControl) {
        super(useAskControl);
    }

    @Before
    public void insertData() throws RepositoryException, RDFParseException, IOException {
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("a:abcde"));
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("a:xyz"));
    }

    @Test
    public void testPluginDefaultDisabledState() throws Exception {
        executeQueryAndVerifyResults("a:;abc", 0);
    }

    @Test
    public void testPluginEnabledDisabledPredicates() throws Exception {
        enablePlugin();
        executeAskQuery(true);
        executeQueryAndVerifyResults("a:;abc", 1);
        disablePlugin();
        executeAskQuery(false);
        executeQueryAndVerifyResults("a:;abc", 0);
    }

    private void executeAskQuery(boolean expected) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        BooleanQuery bq = connection.prepareBooleanQuery(QueryLanguage.SPARQL, ASK_ENABLED_AUTOCOMPLETE);
        assertEquals(expected, bq.evaluate());
    }

}
