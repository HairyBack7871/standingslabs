package hairyback.standingslabs;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VerticalSlabBakedModel implements BakedModel {

    private final BakedModel parentModel;

    public VerticalSlabBakedModel(BakedModel parentModel) {
        this.parentModel = parentModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        // FIX: If state is null (Item), we default to NORTH so it looks like a slice in the UI
        Direction dir = (state == null) ? Direction.NORTH : state.get(VerticalSlabBlock.FACING);

        float x1 = 0, x2 = 1, y1 = 0, y2 = 1, z1 = 0, z2 = 1;

        // Apply the "Slice" coordinates
        switch (dir) {
            case NORTH -> z2 = 0.5f;
            case SOUTH -> z1 = 0.5f;
            case EAST  -> x1 = 0.5f;
            case WEST  -> x2 = 0.5f;
        }

        List<BakedQuad> result = new ArrayList<>();

        // We iterate through all faces of the parent model to find quads to clip
        // Note: We pass 'null' as the face to the parent to get ALL quads
        // because our custom clipping might change which face a quad belongs to.
        for (Direction d : Direction.values()) {
            List<BakedQuad> parentQuads = parentModel.getQuads(state, d, random);

            for (BakedQuad quad : parentQuads) {
                BakedQuad clipped = QuadClipper.clipQuad(quad, x1, y1, z1, x2, y2, z2);
                if (clipped != null) result.add(clipped);
            }
        }

        return result;
    }

    @Override public boolean useAmbientOcclusion() { return parentModel.useAmbientOcclusion(); }
    @Override public boolean hasDepth() { return parentModel.hasDepth(); }
    @Override public boolean isSideLit() { return parentModel.isSideLit(); }
    @Override public boolean isBuiltin() { return false; }
    @Override public Sprite getParticleSprite() { return parentModel.getParticleSprite(); }

    // IMPORTANT: Ensure the item has the correct perspective transforms (held in hand, on ground, etc.)
    @Override public ModelTransformation getTransformation() { return parentModel.getTransformation(); }
    @Override public ModelOverrideList getOverrides() { return ModelOverrideList.EMPTY; }
}