package hairyback.standingslabs;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput; // The crucial 1.21 import
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryWrapper;

public class VerticalSlabRecipe extends SpecialCraftingRecipe {
    public VerticalSlabRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    // 1.21 uses CraftingRecipeInput instead of RecipeInputInventory
    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int count = 0;
        // input.getStacks() is the 1.21 way to iterate
        for (ItemStack stack : input.getStacks()) {
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof BlockItem blockItem && isTransformable(blockItem.getBlock())) {
                    count++;
                } else {
                    return false;
                }
            }
        }
        return count == 1;
    }

    // Note the change in parameters for 1.21: WrapperLookup is often used instead of RegistryManager
    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        for (ItemStack stack : input.getStacks()) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                Block inputBlock = blockItem.getBlock();

                // 1. Vertical -> Horizontal
                if (inputBlock instanceof VerticalSlabBlock vsb) {
                    return new ItemStack(vsb.parent);
                }

                // 2. Horizontal -> Vertical
                Block vertical = findVerticalBlock(inputBlock);
                if (vertical != null) {
                    return new ItemStack(vertical);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isTransformable(Block block) {
        return block instanceof VerticalSlabBlock || findVerticalBlock(block) != null;
    }

    private Block findVerticalBlock(Block horizontal) {
        for (Block block : Registries.BLOCK) {
            if (block instanceof VerticalSlabBlock vsb && vsb.parent == horizontal) {
                return vsb;
            }
        }
        return null;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StandingSlabsMod.VERTICAL_SLAB_SERIALIZER;
    }
}