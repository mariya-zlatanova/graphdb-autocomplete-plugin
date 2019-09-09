package com.ontotext.trree.plugin.autocomplete;

import com.google.common.collect.Maps;
import com.ontotext.trree.OwlimSchemaRepository;
import com.ontotext.trree.entitypool.EntityPoolConnection;
import com.ontotext.trree.sdk.Plugin;
import com.ontotext.trree.sdk.PluginLocator;
import com.ontotext.trree.sdk.RDFRankProvider;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 28-Mar-2016
 */
public class TestBulgWithMockedRdfRanks extends AutocompletePluginTestBase {
    private static Map<String, Double> uris2Ranks;
    private Map<Long, Double> ids2Ranks;

    @Parameterized.Parameters(name = "useAskControl = {0}")
    public static List<Object[]> getParams() {
        return AutocompletePluginTestBase.getParams();
    }

    public TestBulgWithMockedRdfRanks(boolean useAskControl) {
        super(useAskControl);
    }

    @BeforeClass
    public static void getUris2HarcodedRanks() throws Exception {
        uris2Ranks = Maps.newLinkedHashMap();
        List<String> records = FileUtils.readLines(new File("src/test/resources/bulg_fuzzy_match_suggestions_unique.csv"));
        for (String line : records) {
            String[] columns = line.split("\\*");
            uris2Ranks.put(columns[0], Double.parseDouble(columns[1]));
        }
    }

    @Before
    public void setup() throws Exception {
        importData("src/test/resources/bulg_fuzzy_match_suggestions_unique.n3", RDFFormat.N3);
        setupMockery();
    }

    private void setupMockery() {
        OwlimSchemaRepository osr = (((OwlimSchemaRepository)((SailRepository)getRepository()).getSail()));

        // Create a map from entity id to the fake rank
        ids2Ranks = new HashMap<>(uris2Ranks.size());
        EntityPoolConnection epc = osr.getEntities().getConnection();
        for (Map.Entry<String, Double> e : uris2Ranks.entrySet()) {
            long id = epc.getId(vf.createIRI(e.getKey()));
            assertNotEquals(0, id);
            ids2Ranks.put(id, e.getValue());
        }
        epc.close();

        // Mock an RDF Rank plugin that returns our fake ranks
        RDFRankProvider mockRdfRank = Mockito.mock(RDFRankProvider.class);
        Mockito.when(mockRdfRank.getNormalizedRank(Mockito.anyLong()))
                .then(invocationOnMock -> ids2Ranks.get((Long) invocationOnMock.getArguments()[0]));

        // Tell our plugin to use the mocked RDF Rank plugin
        AutocompletePlugin autocompletePlugin = (AutocompletePlugin) osr.getPluginManager().getPlugin("autocomplete");
        autocompletePlugin.setLocator(new PluginLocator() {
            @Override
            public Plugin locate(String s) {
                return null;
            }

            @Override
            public RDFRankProvider locateRDFRankProvider() {
                return mockRdfRank;
            }
        });
    }

    @Test
    public void testSameRanksOnDefault() throws Exception {
        enablePlugin();
        List<String> results = executeQueryAndGetResults("http://dbpedia.org/resource/;Bulg");
        assertTrue(results.size() > 10);
        assertEquals("Bulgaria must come first", "http://dbpedia.org/resource/Bulgaria; http://dbpedia.org/resource/<b>Bulg</b>aria", results.get(0));

        // Print the first 10 results just 'cause we're curious
        for (int i = 0; i < 10; i++) {
            System.out.println("RESULT: " + results.get(i));
        }
    }

}
