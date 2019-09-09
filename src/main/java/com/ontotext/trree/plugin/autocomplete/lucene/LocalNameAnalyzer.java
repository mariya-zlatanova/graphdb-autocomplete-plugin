package com.ontotext.trree.plugin.autocomplete.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

/**
 * Created by desislava on 17/11/15.
 */
public class LocalNameAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new LocalNameTokenizer();
        TokenStream tok = new LowerCaseFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, tok);
    }

}
