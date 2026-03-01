package hairyback.standingslabs;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

public class QuadClipper {

    public static BakedQuad clipQuad(BakedQuad quad,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2) {

        int[] data = quad.getVertexData().clone();

        for (int i = 0; i < 4; i++) {
            int off = i * 8;

            float x = Float.intBitsToFloat(data[off]);
            float y = Float.intBitsToFloat(data[off + 1]);
            float z = Float.intBitsToFloat(data[off + 2]);

            x = clamp(x, x1, x2);
            y = clamp(y, y1, y2);
            z = clamp(z, z1, z2);

            data[off]     = Float.floatToRawIntBits(x);
            data[off + 1] = Float.floatToRawIntBits(y);
            data[off + 2] = Float.floatToRawIntBits(z);
        }

        return new BakedQuad(data, quad.getColorIndex(), quad.getFace(), quad.getSprite(), quad.hasShade());
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
