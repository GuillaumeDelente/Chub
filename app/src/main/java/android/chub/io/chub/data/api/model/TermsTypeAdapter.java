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
public class TermsTypeAdapter implements JsonDeserializer<Terms> {

    public Terms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        Terms terms = new Terms();
        for (int i = 0; i < array.size(); i++) {
            JsonElement jsonElement = array.get(i);
            JsonObject termArray = jsonElement.getAsJsonObject();
            switch (i) {
                case 0:
                    terms.number = (termArray.get("value").getAsString());
                    break;
                case 1:
                    terms.address = (termArray.get("value").getAsString());
                    break;
                case 2:
                    terms.city = (termArray.get("value").getAsString());
                    break;
                case 3:
                    terms.state = (termArray.get("value").getAsString());
                    break;
            }
        }
        return terms;
    }
}