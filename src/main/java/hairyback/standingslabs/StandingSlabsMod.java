package hairyback.standingslabs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StandingSlabsMod implements ModInitializer {

    public static RecipeSerializer<VerticalSlabRecipe> VERTICAL_SLAB_SERIALIZER;
    public static final String MOD_ID = "standingslabs";

    @Override
    public void onInitialize() {
        System.out.println("=== StandingSlabs: Initialization Start ===");

        List<Block> slabsToProcess = new ArrayList<>();
        for (Block block : Registries.BLOCK) {
            if (block instanceof SlabBlock) {
                slabsToProcess.add(block);
            }
        }

        // 1. Register all blocks and items
        for (Block originalSlab : slabsToProcess) {
            registerVerticalSlab(originalSlab);
        }

        // 2. Creative Menu Placement (Slab followed by Vertical Slab)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            for (Block originalSlab : slabsToProcess) {
                Identifier verticalId = Identifier.of(MOD_ID, "vertical_" + Registries.BLOCK.getId(originalSlab).getPath());
                Block verticalBlock = Registries.BLOCK.get(verticalId);

                if (verticalBlock != null && verticalBlock != Registries.BLOCK.get(Registries.BLOCK.getDefaultId())) {
                    content.addAfter(originalSlab, verticalBlock);
                }
            }
        });

        // 3. Register Serializer
        VERTICAL_SLAB_SERIALIZER = Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of(MOD_ID, "vertical_slab_crafting"),
                new SpecialRecipeSerializer<>(VerticalSlabRecipe::new)
        );

        System.out.println("[StandingSlabs] Successfully processed " + slabsToProcess.size() + " slabs.");
        System.out.println("=== StandingSlabs: Initialization Complete ===");
    }

    private void registerVerticalSlab(Block originalSlab) {
        Identifier originalId = Registries.BLOCK.getId(originalSlab);
        if (originalId.equals(Registries.BLOCK.getDefaultId())) return;

        Identifier verticalId = Identifier.of(MOD_ID, "vertical_" + originalId.getPath());
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, verticalId);

        // Copy settings including the "Burnable" flag (internal for Lava)
        VerticalSlabBlock verticalBlock = new VerticalSlabBlock(
                AbstractBlock.Settings.copy(originalSlab)
                        .nonOpaque()
                        .dynamicBounds()
                        .dropsNothing(),
                originalSlab
        );

        Registry.register(Registries.BLOCK, blockKey, verticalBlock);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, verticalId);
        BlockItem verticalItem = new BlockItem(verticalBlock, new Item.Settings()) {
            @Override
            public Text getName(ItemStack stack) {
                // Wild-Ready dynamic naming
                return Text.translatable("text.standingslabs.vertical_wrapper",
                        Text.translatable(originalSlab.getTranslationKey()));
            }
        };

        Registry.register(Registries.ITEM, itemKey, verticalItem);

        // --- DYNAMIC FLAMMABILITY & FUEL ---

        // Look up parent flammability
        FlammableBlockRegistry flammableRegistry = FlammableBlockRegistry.getDefaultInstance();
        int burnChance = flammableRegistry.get(originalSlab).getBurnChance();
        int spreadChance = flammableRegistry.get(originalSlab).getSpreadChance();

        if (burnChance > 0 || spreadChance > 0) {
            // Apply burning to the world block
            flammableRegistry.add(verticalBlock, burnChance, spreadChance);

            // If the block is flammable in the world, it is ALMOST ALWAYS fuel in a furnace.
            // This is safer than checking the FuelRegistry directly, which is often empty during Init.
            FuelRegistry.INSTANCE.add(verticalItem, 150);
        } else {
            // Backup check: some mods register fuel but not flammability (e.g. Coal blocks)
            Integer fuelValue = FuelRegistry.INSTANCE.get(originalSlab.asItem());
            if (fuelValue != null && fuelValue > 0) {
                FuelRegistry.INSTANCE.add(verticalItem, 150);
            }
        }
    }
}