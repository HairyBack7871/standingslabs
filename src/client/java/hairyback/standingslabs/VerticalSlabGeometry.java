package hairyback.standingslabs;

import com.mojang.serialization.Codec;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class VerticalSlabGeometry {

    // Codec.unit is perfect for a parameterless constructor
    public static final Codec<VerticalSlabGeometry> CODEC =
            Codec.unit(new VerticalSlabGeometry());

    public VerticalSlabGeometry() {}

    public UnbakedModel createUnbaked() {
        // In Yarn 1.21.1, we use Identifier.of()
        // "block/stone" is the standard path
        return new VerticalSlabUnbaked(Identifier.of("minecraft", "block/stone"));
    }
}