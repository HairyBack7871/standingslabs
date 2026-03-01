package hairyback.standingslabs;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class VerticalSlabBlock extends Block implements Waterloggable {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public final Block parent;

    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 8);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0, 0, 8, 16, 16, 16);
    protected static final VoxelShape EAST_SHAPE  = Block.createCuboidShape(8, 0, 0, 16, 16, 16);
    protected static final VoxelShape WEST_SHAPE  = Block.createCuboidShape(0, 0, 0, 8, 16, 16);

    public VerticalSlabBlock(Settings settings, Block parent) {
        super(settings);
        this.parent = parent;

        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    // --- UNIVERSAL LOGIC OVERRIDES ---

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        // Inherit breaking speed and tool requirements from the original slab
        return this.parent.getDefaultState().calcBlockBreakingDelta(player, world, pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient) {
                // 1. PISTON CHECK: If the block is being moved, do not drop an item (prevents duplication)
                if (moved) {
                    super.onStateReplaced(state, world, pos, newState, moved);
                    return;
                }

                // 2. DIRECT FIRE/LAVA CHECK: Check if the replacing block is fire or lava
                boolean isFire = newState.getBlock() instanceof AbstractFireBlock;
                boolean isLava = newState.getFluidState().isOf(Fluids.LAVA) ||
                        newState.getFluidState().isOf(Fluids.FLOWING_LAVA);

                // 3. NEIGHBOR FIRE CHECK: Solves the "3 out of 8" drop issue
                // If a flammable block is replaced by Air, check neighbors for fire to see if it's burning
                boolean fireNearby = false;
                boolean isFlammable = FlammableBlockRegistry.getDefaultInstance().get(this).getBurnChance() > 0;

                if (isFlammable && newState.isAir()) {
                    for (Direction dir : Direction.values()) {
                        if (world.getBlockState(pos.offset(dir)).getBlock() instanceof AbstractFireBlock) {
                            fireNearby = true;
                            break;
                        }
                    }
                }

                // 4. FINAL DROP LOGIC: Only drop if destruction was not caused by fire, lava, or movement
                if (!isFire && !isLava && !fireNearby) {
                    Block.dropStack(world, pos, new ItemStack(this.asItem()));
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    // --- GEOMETRY & PLACEMENT ---

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST  -> EAST_SHAPE;
            case WEST  -> WEST_SHAPE;
            default    -> NORTH_SHAPE;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    // --- WATERLOGGING ---

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public MutableText getName() {
        // Universal naming logic that pulls the translated parent name
        return Text.translatable("text.standingslabs.vertical_wrapper",
                Text.translatable(this.parent.getTranslationKey()));
    }
}