package hairyback.standingslabs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class StandingSlabsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> {

            // 1. BLOCK RESOLVER (For placed blocks)
            for (Block block : Registries.BLOCK) {
                if (block instanceof VerticalSlabBlock verticalBlock) {
                    VerticalSlabUnbaked model = createModel(verticalBlock);

                    pluginContext.registerBlockStateResolver(block, (resContext) -> {
                        block.getStateManager().getStates().forEach(state -> {
                            resContext.setModel(state, model);
                        });
                    });
                }
            }

            // 2. ITEM RESOLVER (For inventory/hand)
            // Fixed: 1.21 uses 'resolveModel' and the context parameter is 'context' (no .id())
            pluginContext.resolveModel().register(context -> {
                Identifier id = context.id(); // If this still fails, check if your Fabric API is up to date!

                // Check if this is an item model for our mod
                // Items are typically identified as "standingslabs:item/vertical_oak_slab"
                if (id.getNamespace().equals(StandingSlabsMod.MOD_ID) && id.getPath().startsWith("item/")) {
                    // Extract the name (e.g., "vertical_oak_slab")
                    String blockPath = id.getPath().replace("item/", "");
                    Block block = Registries.BLOCK.get(Identifier.of(id.getNamespace(), blockPath));

                    if (block instanceof VerticalSlabBlock verticalBlock) {
                        // Return the same slim model used for the blocks
                        return createModel(verticalBlock);
                    }
                }
                return null;
            });
        });
    }

    private VerticalSlabUnbaked createModel(VerticalSlabBlock verticalBlock) {
        Identifier textureId = ParentModelResolver.findParentModelId(verticalBlock.parent);
        return new VerticalSlabUnbaked(textureId);
    }
}