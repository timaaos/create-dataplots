package ru.timaaos.blocks;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import ru.timaaos.CreateDataAndPlots;

public class PortablePlayerInterfaceBlock extends Block implements BlockEntityProvider, IWrenchable {
    public static final DirectionProperty FACING = Properties.FACING;

    protected static final VoxelShape SHAPE_UP = Block.createCuboidShape(0, 0, 0, 16, 14, 16);
    protected static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(0, 2, 0, 16, 16, 16);
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 0, 2, 16, 16, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0, 0, 0, 16, 16, 14);
    protected static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 0, 0, 14, 16, 16);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(2, 0, 0, 16, 16, 16);

    public PortablePlayerInterfaceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_UP;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getSide();
        if (ctx.getPlayer() != null && ctx.getPlayer().isSneaking()) {
            facing = facing.getOpposite();
        }
        return this.getDefaultState().with(FACING, facing);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return CreateDataAndPlots.PORTABLE_PLAYER_INTERFACE.getSibling(Registries.BLOCK_ENTITY_TYPE).get().instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}