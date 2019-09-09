package com.ontotext.trree.plugin.autocomplete;

import java.util.List;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;
import org.junit.runners.Parameterized;

public class TestEnableDisableFromPluginControl extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

	public TestEnableDisableFromPluginControl(boolean useAskControl) {
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
        stopPlugin();
        executeQueryAndVerifyResults("prefix:;ala", 0);
        connection.add(vf.createIRI("prefix:ala3"), vf.createIRI("prefix:bala3"), vf.createIRI("prefix:ontotext3"));
        startPlugin();
        reindex();
        executeQueryAndVerifyResults("prefix:;ala", 4);
    }
    
    private void startPlugin() {
        startOrStopPlugin("start");
    }

    private void stopPlugin() {
        startOrStopPlugin("stop");
    }

    private void startOrStopPlugin(String startOrStop) {
        connection.begin();
        String update = String.format("insert data { [] <http://www.ontotext.com/owlim/system#%splugin> \"autocomplete\" }", startOrStop);
        connection.prepareUpdate(update).execute();
        connection.commit();
    }
    
	
}
