package com.ontotext.trree.plugin.autocomplete;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;

/**
 * Created by desislava on 14.11.16.
 */
public class TestRebuildAutocompleteIndex extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestRebuildAutocompleteIndex(boolean useAskControl) {
        super(useAskControl);
    }

    @Before
    public void enableBeforePlugin() throws Exception {
        enablePlugin();
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("a:abcde"));
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("a:xyz"));
    }

    @Test
    public void testReindex() throws Exception {
        reindex();
        executeQueryAndVerifyResults("a:;abc", 1);
    }

}
