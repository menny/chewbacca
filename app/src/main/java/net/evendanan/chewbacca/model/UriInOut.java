package net.evendanan.chewbacca.model;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UriInOut implements JsonSerializer<Uri>, JsonDeserializer<Uri> {
    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Uri deserialize(final JsonElement src, final Type srcType,
                           final JsonDeserializationContext context) throws JsonParseException {
        return Uri.parse(src.getAsString());
    }
}