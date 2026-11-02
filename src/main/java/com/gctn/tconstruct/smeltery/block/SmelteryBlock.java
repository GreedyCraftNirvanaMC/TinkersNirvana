/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

import javax.annotation.Nullable;

public class SmelteryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public final Kind kind;

    public SmelteryBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new SmelteryBlock(kind, properties));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (kind == Kind.CHANNEL) {
            facing = context.getClickedFace().getOpposite();
        } else if (kind == Kind.FAUCET) {
            facing = context.getClickedFace().getOpposite();
            if (facing == Direction.DOWN) {
                facing = Direction.UP;
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.CONTROLLER ? new SmelteryBlockEntity(pos, state) : new SmelteryComponentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (world, pos, blockState, entity) -> {
            if (world.isClientSide) {
                if (entity instanceof SmelteryComponentBlockEntity component) {
                    component.clientTick();
                }
                return;
            }
            if (entity instanceof SmelteryBlockEntity controller) {
                controller.tick();
            } else if (entity instanceof SmelteryComponentBlockEntity component) {
                component.tick();
            }
        };
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return kind == Kind.FAUCET || kind == Kind.CHANNEL;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (kind == Kind.TANK && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component && !component.tank.isEmpty()) {
            return component.tank.getFluid().getFluidType().getLightLevel(component.tank.getFluid());
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if ((kind == Kind.TANK || kind == Kind.DRAIN) && FluidUtil.getFluidHandler(stack).isPresent()) {
            if (!level.isClientSide) {
                FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection());
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return interact(level, pos, player, hand, hit) ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, player, InteractionHand.MAIN_HAND, hit) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    private boolean interact(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() && kind != Kind.CHANNEL) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof SmelteryBlockEntity controller) {
            if (!level.isClientSide && controller.active()) {
                player.openMenu(controller, buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeVarInt(controller.getContainerSize());
                });
            }
            return true;
        }
        if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            if (kind == Kind.FAUCET || kind == Kind.TABLE || kind == Kind.BASIN || kind == Kind.CHANNEL) {
                if (!level.isClientSide) {
                    component.interact(player, hand, hit);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            component.neighborChanged(from);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.FAUCET && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            component.activate();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            if (kind == Kind.TANK) {
                // copy_custom_data loot preserves fuel without retaining a controller's ownership link.
                var data = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (data.contains("Tank")) {
                    component.tank.readFromNBT(level.registryAccess(), data.getCompound("Tank"));
                    component.changed();
                }
            }
            component.placed(placer);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SmelteryBlockEntity controller) {
                controller.releaseComponents();
                Containers.dropContents(level, pos, controller);
            } else if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                component.onBroken();
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return kind == Kind.TANK || kind == Kind.DRAIN || kind == Kind.TABLE || kind == Kind.BASIN;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component ? component.comparator() : 0;
    }

    @Override
    public boolean isLadder(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, LivingEntity entity) {
        return kind == Kind.LADDER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (kind == Kind.TABLE) {
            return Shapes.or(box(0, 10, 0, 16, 16, 16), box(0, 0, 0, 4, 10, 4), box(12, 0, 0, 16, 10, 4),
                    box(0, 0, 12, 4, 10, 16), box(12, 0, 12, 16, 10, 16));
        }
        if (kind == Kind.BASIN) {
            return Shapes.or(box(0, 4, 0, 16, 16, 16), box(0, 0, 0, 5, 4, 5), box(11, 0, 0, 16, 4, 5),
                    box(0, 0, 11, 5, 4, 16), box(11, 0, 11, 16, 4, 16));
        }
        if (kind == Kind.FAUCET) {
            return switch (state.getValue(FACING)) {
                case UP -> box(4, 10, 4, 12, 16, 12);
                case NORTH -> box(4, 4, 0, 12, 10, 6);
                case SOUTH -> box(4, 4, 10, 12, 10, 16);
                case WEST -> box(0, 4, 4, 6, 10, 12);
                default -> box(10, 4, 4, 16, 10, 12);
            };
        }
        if (kind == Kind.CHANNEL) {
            return box(0, 0, 0, 16, 6, 16);
        }
        return Shapes.block();
    }

    public enum Kind {
        CONTROLLER(false), BRICK(true), DRAIN(true), TANK(true), GLASS(true), LADDER(true),
        FAUCET(false), CHANNEL(false), TABLE(false), BASIN(false);

        public final boolean structure;

        Kind(boolean structure) {
            this.structure = structure;
        }
    }
}
