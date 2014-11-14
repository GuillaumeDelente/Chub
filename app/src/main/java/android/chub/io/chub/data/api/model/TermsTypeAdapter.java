package android.chub.io.chub.data.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by guillaume on 11/12/14.
 */
public class TermsTypeAdapter implements JsonDeserializer<TermsTypeAdapter.Terms> {

    public Terms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        Terms terms = new Terms();
        for (int i = 0; i < array.size(); i++) {
            JsonElement jsonElement = array.get(i);
            JsonObject termArray = jsonElement.getAsJsonObject();
            int value = termArray.get("offset").getAsInt();
            switch (value) {
                case 0:
                    terms.address = (termArray.get("value").getAsString());
                    break;
                case 16:
                    terms.city = (termArray.get("value").getAsString());
                    break;
                case 31:
                    terms.state = (termArray.get("value").getAsString());
                    break;
            }
        }
        return terms;
    }

    public class Terms {
        public String address;
        public String city;
        public String state;
    }
}