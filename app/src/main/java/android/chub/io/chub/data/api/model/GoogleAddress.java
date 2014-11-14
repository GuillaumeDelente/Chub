package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleAddress {

    public final String description;
    public final TermsTypeAdapter.Terms terms;

    public GoogleAddress(String description, TermsTypeAdapter.Terms terms) {
        this.description = description;
        this.terms = terms;
    }

}
