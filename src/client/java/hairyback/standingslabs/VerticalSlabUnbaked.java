package hairyback.standingslabs;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class VerticalSlabUnbaked implements UnbakedModel {

    private final Identifier parentModelId;

    public VerticalSlabUnbaked(Identifier parentModelId) {
        this.parentModelId = parentModelId;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(parentModelId);
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelGetter) {
        // Required by interface, nothing needed here
    }

    @Nullable
    @Override
    public BakedModel bake(
            Baker baker,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer
    ) {
        // Bake the parent block's model
        BakedModel parentModel = baker.bake(parentModelId, rotationContainer);

        // Wrap it in the vertical slab baked model
        return new VerticalSlabBakedModel(parentModel);
    }
}
