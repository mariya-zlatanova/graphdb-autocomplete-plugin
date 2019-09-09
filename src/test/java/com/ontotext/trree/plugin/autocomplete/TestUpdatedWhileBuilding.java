package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;

/**
 * Created by desislava on 11/11/15.
 */
public class TestUpdatedWhileBuilding extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestUpdatedWhileBuilding(boolean useAskControl) {
        super(useAskControl);
    }

    @Test
    public void testCreateAutocompleteIndex() throws Exception {
        importData("src/test/resources/import/bulgaria_uris.ttl", RDFFormat.TURTLE);
        enablePlugin();
        connection.add(vf.createIRI("prefix:ala"), vf.createIRI("prefix:bala"), vf.createIRI("prefix:ontotext"));
        connection.add(vf.createIRI("prefix:ala1"), vf.createIRI("prefix:bala1"), vf.createIRI("prefix:ontotext1"));
        Thread.sleep(200L);
        connection.add(vf.createIRI("prefix:ala2"), vf.createIRI("prefix:bala2"), vf.createIRI("prefix:ontotext2"));
        while (!getPluginStatus().startsWith(IndexStatus.READY.toString())) {
            Thread.sleep(1000L);
        }
        executeQueryAndVerifyResults("prefix:;ala", 3);
    }

    @Test
    public void testCreateAutocompleteIndexHeavy() throws Exception {
        importData("src/test/resources/import/bulgaria_uris.ttl", RDFFormat.TURTLE);
        enablePlugin();
        for (int i = 0; i < 10; i++) {
            connection.add(vf.createIRI("prefix:ala" + i), vf.createIRI("prefix:bala" + i), vf.createIRI("prefix:ontotext" + i));
        }
        Thread.sleep(200L);
        for (int i = 10; i < 15; i++) {
            connection.add(vf.createIRI("prefix:ala" + i), vf.createIRI("prefix:bala" + i), vf.createIRI("prefix:ontotext" + i));
        }
        while (!getPluginStatus().startsWith(IndexStatus.READY.toString())) {
            Thread.sleep(1000L);
        }
        for (int i = 15; i < 20; i++) {
            connection.begin();
            connection.add(vf.createIRI("prefix:ala" + i), vf.createIRI("prefix:bala" + i), vf.createIRI("prefix:ontotext" + i));
            connection.commit();
        }
        executeQueryAndVerifyResults("prefix:;ala", 20);
    }

}
