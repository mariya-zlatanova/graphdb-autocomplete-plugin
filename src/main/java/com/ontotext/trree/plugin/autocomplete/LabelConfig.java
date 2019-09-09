package com.ontotext.trree.plugin.autocomplete;

import org.eclipse.rdf4j.model.IRI;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Label indexing config.
 */
public class LabelConfig {
    IRI labelIRI;
    long labelId;
    Set<String> languages;

    LabelConfig(IRI labelIRI, long labelId, String languagesString) {
        this.labelIRI = labelIRI;
        this.labelId = labelId;
        this.languages = Collections.emptySet();
        if (!languagesString.isEmpty()) {
            this.languages = new LinkedHashSet<>(Arrays.asList(languagesString.split(",\\s*")));
        }
    }

    boolean languageMatches(String language) {
        if (languages.isEmpty()) {
            return true;
        }

        // This is rather naive, use LangMatch.
        return languages.contains(language == null ? "empty" : language);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LabelConfig)) {
            return false;
        }

        LabelConfig other = (LabelConfig) obj;

        return other.labelIRI.equals(this.labelIRI) && other.languages.equals(this.languages);
    }
}
