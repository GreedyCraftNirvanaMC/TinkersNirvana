/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BlockSlimeChannel. */
package com.gctn.tconstruct.gadgets.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

import java.util.Locale;

public class SlimeChannelBlock extends Block {
    public enum Connection implements StringRepresentable {
        NONE, INNER, OUTER;
        @Override public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
    }
    public static final DirectionProperty SIDE = DirectionProperty.create("side");
    public static final EnumProperty<ChannelDirection> DIRECTION = EnumProperty.create("direction", ChannelDirection.class);
    public static final EnumProperty<Connection> CONNECTED = EnumProperty.create("connected", Connection.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final Geometry[][][] GEOMETRY = new Geometry[6][8][3];
    public final int color;
    static {
        for (Direction side : Direction.values()) { for (ChannelDirection direction : ChannelDirection.values()) { for (Connection connection : Connection.values()) {
            Direction facing = direction.getFacing();
            AABB main = half(side), secondary = null;
            if (facing != null && connection == Connection.INNER) {
                main = side == Direction.DOWN ? lower(facing) : side == Direction.UP ? upper(facing) : switch (facing) {
                    case NORTH -> upper(side); case SOUTH -> lower(side); case WEST -> corner(side); case EAST -> corner(side.getClockWise()); default -> main;
                };
            } else if (facing != null && connection == Connection.OUTER) {
                secondary = side == Direction.DOWN ? upper(facing.getOpposite()) : side == Direction.UP ? lower(facing.getOpposite()) : switch (facing) {
                    case NORTH -> lower(side.getOpposite()); case SOUTH -> upper(side.getOpposite()); case WEST -> corner(side.getOpposite()); case EAST -> corner(side.getCounterClockWise()); default -> null;
                };
            }
            VoxelShape shape = Shapes.create(main);
            if (secondary != null) { shape = Shapes.or(shape, Shapes.create(secondary)); }
            GEOMETRY[side.ordinal()][direction.ordinal()][connection.ordinal()] = new Geometry(main, secondary, shape);
        } } }
    }
    public record Geometry(AABB main, AABB secondary, VoxelShape shape) {
        public boolean intersects(AABB box) { return main.intersects(box) || secondary != null && secondary.intersects(box); }
        public boolean contains(Vec3 point) { return main.contains(point) || secondary != null && secondary.contains(point); }
        public double height(AABB box) {
            double height = main.intersects(box) ? main.maxY - box.minY : 0;
            return secondary != null && secondary.intersects(box) ? Math.max(height, secondary.maxY - box.minY) : height;
        }
    }
    public SlimeChannelBlock(int color, Properties properties) {
        super(properties); this.color = color;
        registerDefaultState(stateDefinition.any().setValue(SIDE, Direction.DOWN).setValue(DIRECTION, ChannelDirection.SOUTH).setValue(CONNECTED, Connection.NONE).setValue(POWERED, false));
    }
    @Override protected MapCodec<? extends Block> codec() { return simpleCodec(properties -> new SlimeChannelBlock(color, properties)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(SIDE, DIRECTION, CONNECTED, POWERED); }
    public static Geometry geometry(BlockState state) { return GEOMETRY[state.getValue(SIDE).ordinal()][state.getValue(DIRECTION).ordinal()][state.getValue(CONNECTED).ordinal()]; }
    private static AABB half(Direction side) { return switch (side) {
        case DOWN -> new AABB(0, 0, 0, 1, .5, 1); case UP -> new AABB(0, .5, 0, 1, 1, 1);
        case NORTH -> new AABB(0, 0, 0, 1, 1, .5); case SOUTH -> new AABB(0, 0, .5, 1, 1, 1);
        case WEST -> new AABB(0, 0, 0, .5, 1, 1); case EAST -> new AABB(.5, 0, 0, 1, 1, 1);
    }; }
    private static AABB lower(Direction side) { AABB bounds = half(side); return bounds.setMaxY(.5); }
    private static AABB upper(Direction side) { AABB bounds = half(side); return bounds.setMinY(.5); }
    private static AABB corner(Direction side) { return switch (side) {
        case NORTH -> new AABB(0, 0, 0, .5, 1, .5); case SOUTH -> new AABB(.5, 0, .5, 1, 1, 1);
        case WEST -> new AABB(0, 0, .5, .5, 1, 1); case EAST -> new AABB(.5, 0, 0, 1, 1, .5);
        default -> throw new IllegalArgumentException("Horizontal corner required");
    }; }
    private BlockState connected(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction side = state.getValue(SIDE), flow = state.getValue(DIRECTION).getFlow(side);
        BlockState outside = level.getBlockState(pos.relative(side.getOpposite()));
        BlockState inside = level.getBlockState(pos.relative(side));
        Connection connection = outside.getBlock() instanceof SlimeChannelBlock && outside.getValue(SIDE).getOpposite() == flow ? Connection.OUTER
                : inside.getBlock() instanceof SlimeChannelBlock && inside.getValue(SIDE) == flow ? Connection.INNER : Connection.NONE;
        return state.setValue(CONNECTED, connection);
    }
    private void refresh(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            BlockState next = connected(state, level, pos).setValue(POWERED, level.hasNeighborSignal(pos));
            if (state != next) { level.setBlock(pos, next, UPDATE_ALL); }
        }
    }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moving) { refresh(state, level, pos); }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) { refresh(state, level, pos); }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) { return connected(state, level, pos); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace().getOpposite();
        Vec3 hit = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        int u, v;
        if (side.getAxis() == Direction.Axis.Y) { u = (int) (hit.x * 16); v = (int) (hit.z * 16); }
        else {
            v = 15 - (int) (hit.y * 16);
            u = switch (side) { case NORTH -> (int) (hit.x * 16); case SOUTH -> 15 - (int) (hit.x * 16); case WEST -> 15 - (int) (hit.z * 16); case EAST -> (int) (hit.z * 16); default -> 0; };
        }
        ChannelDirection direction = v < 5 ? (u < 5 ? ChannelDirection.NORTHWEST : u > 10 ? ChannelDirection.NORTHEAST : ChannelDirection.NORTH)
                : v > 10 ? (u < 5 ? ChannelDirection.SOUTHWEST : u > 10 ? ChannelDirection.SOUTHEAST : ChannelDirection.SOUTH)
                : u < 5 ? ChannelDirection.WEST : u > 10 ? ChannelDirection.EAST : null;
        if (direction == null) {
            direction = ChannelDirection.values()[Mth.floor(context.getRotation() * 8 / 360 + .5) & 7];
            direction = switch (side) { case SOUTH -> direction.opposite(); case WEST -> direction.rotate90(); case EAST -> direction.rotate90().opposite(); default -> direction; };
        }
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) { direction = direction.opposite(); }
        return connected(defaultBlockState().setValue(SIDE, side).setValue(DIRECTION, direction).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())), context.getLevel(), context.getClickedPos());
    }
    private BlockState transform(BlockState state, java.util.function.UnaryOperator<Direction> operation) {
        Direction oldSide = state.getValue(SIDE), side = operation.apply(oldSide);
        var wanted = state.getValue(DIRECTION).getFlowDiagonals(oldSide).stream().map(operation).toList();
        for (ChannelDirection direction : ChannelDirection.values()) {
            var flows = direction.getFlowDiagonals(side);
            if (flows.size() == wanted.size() && flows.containsAll(wanted)) { return state.setValue(SIDE, side).setValue(DIRECTION, direction); }
        }
        return state;
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return transform(state, rotation::rotate); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return transform(state, mirror::mirror); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return geometry(state).shape; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return geometry(state).shape; }
    @Override protected boolean skipRendering(BlockState state, BlockState neighbor, Direction side) {
        return state.is(neighbor.getBlock()) && !Shapes.joinIsNotEmpty(geometry(state).shape.getFaceShape(side), geometry(neighbor).shape.getFaceShape(side.getOpposite()), BooleanOp.ONLY_FIRST);
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(POWERED)) { return; }
        Geometry geometry = geometry(state); AABB box = entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ());
        boolean main = geometry.main.intersects(box), extra = geometry.secondary != null && geometry.secondary.intersects(box);
        if (!main && !extra) { return; }
        var flows = state.getValue(DIRECTION).getFlowDiagonals(state.getValue(SIDE));
        Direction side = state.getValue(SIDE);
        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.y < 0 && (main ? !flows.contains(Direction.DOWN) : side != Direction.DOWN)) { velocity = velocity.multiply(1, .5, 1); }
        double speed = entity instanceof ItemEntity ? .015 : .01;
        if (main) { for (Direction flow : flows) { velocity = boost(velocity, flow, speed); } }
        if (extra) { velocity = boost(velocity, side, speed); }
        if (entity instanceof ItemEntity && (main && flows.contains(Direction.UP) || extra && side == Direction.UP)) { entity.setOnGround(false); }
        entity.setDeltaMovement(velocity); entity.hasImpulse = true; entity.setAirSupply(300); entity.clearFire(); entity.fallDistance = 0;
    }
    private static Vec3 boost(Vec3 velocity, Direction flow, double speed) { return velocity.add(flow.getStepX() * speed, flow.getStepY() * speed * (flow == Direction.UP ? 3 : 1), flow.getStepZ() * speed); }
}
