package com.ontotext.trree.plugin.autocomplete.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by desislava on 17/11/15.
 */
final class LocalNameTokenizer extends Tokenizer {

    private static final Logger log = LoggerFactory.getLogger(LocalNameTokenizer.class);

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    private String stringToTokenize;
    private int tokenStart;

    LocalNameTokenizer() {
        super();
    }

    private void readStringToTokenize(Reader input) {
        int numChars;
        char[] buffer = new char[1024];
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((numChars =
                    input.read(buffer, 0, buffer.length)) != -1) {
                stringBuilder.append(buffer, 0, numChars);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.tokenStart = 0;
        this.stringToTokenize = stringBuilder.toString();
    }

    private boolean isOtherNameCharacter(char character) {
        return character == '\'' || character == '`';
    }

    @Override
    public void end() throws IOException {
        super.end();
        if (stringToTokenize != null) {
            offsetAtt.setOffset(stringToTokenize.length(), stringToTokenize.length());
        }
        // Reset string buffer
        stringToTokenize = null;
    }

    @Override
    public boolean incrementToken() throws IOException {
        try {
            this.clearAttributes();
            if (stringToTokenize == null) {
                // Read string buffer
                readStringToTokenize(this.input);
            }
            int endOfString = stringToTokenize.length();
            // Reached end of string
            if (!(tokenStart < endOfString)) {
                return false;
            }

            char startTokenChar = stringToTokenize.charAt(tokenStart);

            // Skip tokens that consist of spaces, tabs or new lines
            while (startTokenChar == ' ' || startTokenChar == '\t' || startTokenChar == '\n' || startTokenChar == '\r') {
                tokenStart++;
                if (tokenStart == endOfString) {
                    return false;
                }
                startTokenChar = stringToTokenize.charAt(tokenStart);
            }

            // This is the last character
            if (!(tokenStart + 1 < endOfString)) {
                termAtt.setEmpty().append(stringToTokenize.substring(tokenStart));
                positionIncrementAttribute.setPositionIncrement(1);
                offsetAtt.setOffset(tokenStart, stringToTokenize.length());
                tokenStart++;
                return true;
            }
            // There is at least one more token after the current
            int tokenEnd = tokenStart + 1;
            char nextTokenChar = stringToTokenize.charAt(tokenEnd);

            if (Character.isLowerCase(startTokenChar)) {
                while (tokenEnd < endOfString && lowerCaseOrOtherName(stringToTokenize.charAt(tokenEnd))) {
                    tokenEnd++;
                }
            } else if (Character.isUpperCase(startTokenChar)) {
                if (Character.isUpperCase(nextTokenChar)) {
                    // ABC* => ABC *..
                    while (tokenEnd < endOfString && upperCaseOrOtherName(stringToTokenize.charAt(tokenEnd))) {
                        tokenEnd++;
                    }
                    // AAa => A Aa
                    if (tokenEnd < endOfString && Character.isLowerCase(stringToTokenize.charAt(tokenEnd))) {
                        tokenEnd--;
                    }
                }
                // AaaaAbbb => Aaaa Abbb
                else if (Character.isLowerCase(nextTokenChar)) {
                    while (tokenEnd < endOfString && lowerCaseOrOtherName(stringToTokenize.charAt(tokenEnd))) {
                        tokenEnd++;
                    }
                }
            } else if (Character.isDigit(startTokenChar)) {
                // ad123A => 123 A..
                while (tokenEnd < endOfString && Character.isDigit(stringToTokenize.charAt(tokenEnd))) {
                    tokenEnd++;
                }
            } else {
                while (tokenEnd < endOfString && !(letterDigitOrOtherName(stringToTokenize.charAt(tokenEnd)))) {
                    tokenEnd++;
                }
            }

            if (tokenStart == tokenEnd) {
                return false;
            }

            termAtt.setEmpty().append(stringToTokenize.substring(tokenStart, tokenEnd));
            positionIncrementAttribute.setPositionIncrement(1);
            // Unfortunately setting offset breaks the one letter completions, but is needed for the highlighting
            offsetAtt.setOffset(tokenStart, tokenEnd);
            tokenStart = tokenEnd;
            return true;
        } catch (Exception e) {
            log.error("Error while processing token", e);
            stringToTokenize = null;
            throw new RuntimeException(e);
        }
    }

    private boolean lowerCaseOrOtherName(char c) {
        return Character.isLowerCase(c) || isOtherNameCharacter(c);
    }

    private boolean upperCaseOrOtherName(char c) {
        return Character.isUpperCase(c) || isOtherNameCharacter(c);
    }

    private boolean letterDigitOrOtherName(char c) {
        return Character.isLetterOrDigit(c) || isOtherNameCharacter(c);
    }
}
