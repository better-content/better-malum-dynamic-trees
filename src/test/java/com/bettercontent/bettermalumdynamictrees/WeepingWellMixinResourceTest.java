package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WeepingWellMixinResourceTest {
    @Test
    void shippedMixinConfigRegistersWeepingWellGate() {
        var stream = getClass().getClassLoader().getResourceAsStream("better_malum_dynamic_trees.mixins.json");
        assertNotNull(stream, "the mixin configuration must be packaged");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var mixins = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("mixins");
            assertTrue(mixins.asList().stream()
                .anyMatch(entry -> "WeepingWellStructureMixin".equals(entry.getAsString())));
        } catch (Exception error) {
            throw new AssertionError("could not parse the packaged mixin configuration", error);
        }
    }
}
