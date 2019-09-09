package com.ontotext.trree.plugin.autocomplete.lucene;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Pavel Mihaylov on 09/10/2017.
 */
public class TestLocalNameTokenizer {
    @Test
    public void test() throws IOException {
        assertArrayEquals(new String[] {"us:0,2", "R:2,3"}, getTokens("usR"));
        assertArrayEquals(new String[] {"us:0,2", "r:3,4"}, getTokens("us r"));
        assertArrayEquals(new String[] {"this:0,4", "is:5,7", "it:8,10"}, getTokens("this is it"));
        assertArrayEquals(new String[] {"US:0,2", "Region:2,8"}, getTokens("USRegion"));
        assertArrayEquals(new String[] {"Ivan:0,4", "Petrov:4,10"}, getTokens("IvanPetrov"));
        assertArrayEquals(new String[] {"USSR:0,4"}, getTokens("USSR"));
    }

    private String[] getTokens(String input) throws IOException {
        List<String> result = new ArrayList<>();

        LocalNameTokenizer tokenizer = new LocalNameTokenizer();
        OffsetAttribute offsetAttribute = tokenizer.getAttribute(OffsetAttribute.class);
        CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);
        tokenizer.setReader(new StringReader(input));
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            String term = new String(termAttribute.buffer(), 0, termAttribute.length());

            String termWithOffset = term + ":" + startOffset + "," + endOffset;
            result.add(termWithOffset);
        }

        tokenizer.close();

        return result.toArray(new String[result.size()]);
    }
}
