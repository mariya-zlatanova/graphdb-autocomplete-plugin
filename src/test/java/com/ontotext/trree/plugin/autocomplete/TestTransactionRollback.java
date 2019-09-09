package com.ontotext.trree.plugin.autocomplete;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * Verifies that rollbacked transactions don't affect the index
 */
public class TestTransactionRollback extends AutocompletePluginTestBase {
    static {
        System.setProperty("register-plugins", "com.ontotext.trree.plugin.faults.FaultsPlugin");
    }

    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestTransactionRollback(boolean useAskControl) {
        super(useAskControl);
    }

    @Before
    public void enableBeforePlugin() throws Exception {
        enablePlugin();
    }

    @Test
    public void testRollback() {
        // Add some entities and check they are there
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("a:abcde"));
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:2"), vf.createIRI("b:abcab"));
        executeQueryAndVerifyResults("ab", 2);

        try {
            connection.begin();
            connection.add(vf.createIRI("s:1"), vf.createIRI("p:1"), vf.createIRI("b:abckj"));
            connection.add(vf.createIRI("urn:foo"), vf.createIRI("http://www.ontotext.com/graphdb/faults#throw"), vf.createLiteral(""));
            connection.commit();
            fail("Must fail by faults plugin.");
        } catch (Exception e) {
            connection.rollback();
        }

        executeQueryAndVerifyResults("ab", 2);

        // Add more
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:2"), vf.createIRI("b:abxnl"));
        executeQueryAndVerifyResults("ab", 3);

        // Add more but rollback the transaction, the newly added entities shouldn't be in the autocomplete index
        connection.begin();
        connection.add(vf.createIRI("s:1"), vf.createIRI("p:3"), vf.createIRI("b:abqxy"));
        connection.rollback();
        executeQueryAndVerifyResults("ab", 3);
    }
}
