package hairyback.standingslabs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class ParentModelResolver {

    // These MUST point to valid .json model files to avoid FileNotFound warnings
    private static final Map<String, String> FALLBACKS = Map.of(
            "petrified_oak_slab", "minecraft:block/oak_planks",
            "purpur_slab", "minecraft:block/purpur_block",
            "quartz_slab", "minecraft:block/quartz_block" // Fixed: Points to model, not texture
    );

    public static Identifier findParentModelId(Block slab) {
        Identifier slabId = Registries.BLOCK.getId(slab);
        String path = slabId.getPath();

        if (FALLBACKS.containsKey(path)) {
            return Identifier.of(FALLBACKS.get(path));
        }

        Identifier blockstateId = Identifier.of(
                slabId.getNamespace(),
                "blockstates/" + path + ".json"
        );

        try (InputStream stream = ParentModelResolver.class.getClassLoader()
                .getResourceAsStream("assets/" + blockstateId.getNamespace() + "/" + blockstateId.getPath())) {

            if (stream == null) return fallback(slabId);

            JsonObject json = com.google.gson.JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            JsonObject variants = json.getAsJsonObject("variants");

            for (String key : variants.keySet()) {
                if (key.contains("type=double")) {
                    JsonElement modelElement = variants.get(key);
                    String modelPath = modelElement.isJsonObject()
                            ? modelElement.getAsJsonObject().get("model").getAsString()
                            : modelElement.getAsString();

                    return sanitize(Identifier.of(modelPath));
                }
            }

        } catch (Exception e) {
            return fallback(slabId);
        }

        return fallback(slabId);
    }

    private static Identifier sanitize(Identifier id) {
        String path = id.getPath();

        // 1. Handle Copper (Waxed blocks don't have their own models)
        if (path.startsWith("block/waxed_")) {
            path = "block/" + path.substring("block/waxed_".length());
        }

        // 2. Handle Purpur naming inconsistency
        if (path.equals("block/purpur")) {
            path = "block/purpur_block";
        }

        return Identifier.of(id.getNamespace(), path);
    }

    /**
     * Converts a Model ID (like quartz_block) into the specific Texture ID
     * we want for our vertical slab sides.
     */
    public static Identifier getTextureIdFromModel(Identifier modelId) {
        String path = modelId.getPath();

        // If it's a Quartz block model, we specifically want the 'side' texture
        if (path.equals("block/quartz_block")) {
            return Identifier.of(modelId.getNamespace(), "block/quartz_block_side");
        }

        // For 99% of other blocks, the model name matches the texture name
        return modelId;
    }

    private static Identifier fallback(Identifier slabId) {
        String path = slabId.getPath().replace("_slab", "");
        return Identifier.of(slabId.getNamespace(), "block/" + path);
    }
}