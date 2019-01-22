package io.awesdroid.awesauth.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import android.net.Uri;

import java.io.IOException;

/**
 * @auther Awesdroid
 */
public final class UriAdapter extends TypeAdapter<Uri> {
    @Override
    public void write(JsonWriter out, Uri uri) throws IOException {
        out.value(uri.toString());
    }

    @Override
    public Uri read(JsonReader in) throws IOException {
        return Uri.parse(in.nextString());
    }
}
