package feedback;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class JsonUtil {
    private static final JsonWriterFactory writerFactory;

    static {
        final Map<String, Object> options = new HashMap<>();
        options.put(JsonGenerator.PRETTY_PRINTING,true);
        writerFactory = Json.createWriterFactory(options);
    }

    public static JsonObject loadJson(final InputStream inputStream) throws IOException {
        try (final JsonReader reader = Json.createReader(inputStream)) {
            return reader.readObject();
        }
    }

    static JsonObject loadJson(final Path path) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return loadJson(inputStream);
        }
    }

    static JsonObject loadJson(final File file) throws IOException {
        return loadJson(file.toPath());
    }

    static JsonObject loadJson(final String path) throws IOException {
        return loadJson(Paths.get(path));
    }

    static JsonObjectBuilder json2builder(final JsonObject json) {
        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        json.forEach(jsonObjectBuilder::add);
        return jsonObjectBuilder;
    }

    static void dumpJson(final JsonObject json, final File file) throws IOException {
        try (final FileWriter fileWriter = new FileWriter(file);
             final JsonWriter jsonWriter = writerFactory.createWriter(fileWriter)) {
            jsonWriter.writeObject(json);
        }
    }

    static void dumpJson(final JsonObject json, final String path) throws IOException {
        dumpJson(json, new File(path));
    }

    static JsonArrayBuilder createArrayBuilder() {
        return Json.createArrayBuilder();
    }

    static JsonObjectBuilder createObjectBuilder() {
        return Json.createObjectBuilder();
    }

    static Stream<Integer> toIntStream(final JsonArray array) {
        return array.stream().map(v -> ((JsonNumber)v).intValue());
    }

    static Stream<String> toStringStream(final JsonArray array) {
        return array.stream().map(v -> ((JsonString)v).getString());
    }
}
