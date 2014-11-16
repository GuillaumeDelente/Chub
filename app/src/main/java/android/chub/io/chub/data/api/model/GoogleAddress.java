package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleAddress {

    public final String description;
    public final Terms terms;
    public final List<MatchedString> matched_substrings;

    public GoogleAddress(String description, Terms terms, List<MatchedString> matchedString) {
        this.description = description;
        this.terms = terms;
        this.matched_substrings = matchedString;
    }

    public class MatchedString {

        public final int length;
        public final int offset;

        MatchedString(int length, int offset) {
            this.length = length;
            this.offset = offset;
        }
    }

}
