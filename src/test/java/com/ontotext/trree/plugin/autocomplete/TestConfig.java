package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

public class TestConfig extends AutocompletePluginTestBase {
    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestConfig(boolean useAskControl) {
        super(useAskControl);
    }

    @Test
    public void testAddRemoveUpdate() throws Exception {
        // Initial state has RDFS.LABEL with any language (default)
        Map<IRI, String> configs = listLanguageConfigs();
        Assert.assertEquals(1, configs.size());
        Assert.assertEquals("", configs.get(RDFS.LABEL));

        // Enabling plugin will set the status to READY
        enablePlugin();
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Add config and check it's there, status changes to READY_CONFIG
        addLanguageConfig(FOAF.NAME, "bg,en, de,  empty");
        configs = listLanguageConfigs();
        Assert.assertEquals(2, configs.size());
        Assert.assertEquals("", configs.get(RDFS.LABEL));
        Assert.assertEquals("bg, en, de, empty", configs.get(FOAF.NAME));
        Assert.assertEquals(IndexStatus.READY_CONFIG.name(), getPluginStatus());

        // Restart and recheck, everything should be the same
        restartRepository();
        configs = listLanguageConfigs();
        Assert.assertEquals(2, configs.size());
        Assert.assertEquals("", configs.get(RDFS.LABEL));
        Assert.assertEquals("bg, en, de, empty", configs.get(FOAF.NAME));
        Assert.assertEquals(IndexStatus.READY_CONFIG.name(), getPluginStatus());

        // Remove config and check it disappeared, status back to READY because the config matches
        // the one before we added foaf:name
        removeLanguageConfig(FOAF.NAME);
        configs = listLanguageConfigs();
        Assert.assertEquals(1, configs.size());
        Assert.assertEquals("", configs.get(RDFS.LABEL));
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Update config and check it changed, status stays READY_CONFIG
        addLanguageConfig(RDFS.LABEL, "fr");
        configs = listLanguageConfigs();
        Assert.assertEquals(1, configs.size());
        Assert.assertEquals("fr", configs.get(RDFS.LABEL));
        Assert.assertEquals(IndexStatus.READY_CONFIG.name(), getPluginStatus());

        // Reindex to take changes into effect, status changes to READY
        reindex();
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Restart and recheck, everything should be the same
        restartRepository();
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());
    }

    @Test
    public void testIRIConfig() throws Exception {
        // Check the defaults
        Assert.assertFalse(isPluginEnabled());
        Assert.assertTrue(shouldIndexIRIs());
        // Enabling plugin will set the status to READY
        enablePlugin();
        Assert.assertTrue(isPluginEnabled());
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Restart and recheck, everything should be the same
        restartRepository();
        Assert.assertTrue(isPluginEnabled());
        Assert.assertTrue(shouldIndexIRIs());
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Disable index IRIs should change the status to READY_CONFIG
        setShouldIndexIris(false);
        Assert.assertFalse(shouldIndexIRIs());
        Assert.assertEquals(IndexStatus.READY_CONFIG.name(), getPluginStatus());

        // Re-enabling index IRIs should restore the status back to READY
        setShouldIndexIris(true);
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Disable index IRIs and reindex, status should be READY
        setShouldIndexIris(false);
        reindex();
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());

        // Restart and recheck, everything should be the same
        restartRepository();
        Assert.assertEquals(IndexStatus.READY.name(), getPluginStatus());
    }

    @Test
    public void shouldNotThrowErrorsOnEmptyIndex() throws Exception {
        // Disable IRI indexing and remove all configured labels
        setShouldIndexIris(false);
        for (IRI labelPredicate : listLanguageConfigs().keySet()) {
            removeLanguageConfig(labelPredicate);
        }

        // Enable plugin
        enablePlugin();

        // Execute plugin query and expect empty result
        Assert.assertTrue(executeQueryAndGetResults(";test").isEmpty());
    }
}
