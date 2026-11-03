/* SPDX-License-Identifier: LGPL-3.0-only; Antique's eight relative channel directions. */
package com.gctn.tconstruct.gadgets.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public enum ChannelDirection implements StringRepresentable {
    SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST;
    private final EnumMap<Direction, List<Direction>> flows = new EnumMap<>(Direction.class);
    static {
        for (ChannelDirection direction : values()) { for (Direction side : Direction.values()) {
            direction.flows.put(side, switch (direction) {
                case NORTHWEST -> List.of(NORTH.getFlow(side), WEST.getFlow(side));
                case NORTHEAST -> List.of(NORTH.getFlow(side), EAST.getFlow(side));
                case SOUTHWEST -> List.of(SOUTH.getFlow(side), WEST.getFlow(side));
                case SOUTHEAST -> List.of(SOUTH.getFlow(side), EAST.getFlow(side));
                default -> List.of(direction.getFlow(side));
            });
        } }
    }
    @Override public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
    public ChannelDirection opposite() { return values()[(ordinal() + 4) & 7]; }
    public ChannelDirection rotate90() { return values()[(ordinal() + 2) & 7]; }
    public Direction getFacing() { return (ordinal() & 1) == 0 ? Direction.from2DDataValue(ordinal() / 2) : null; }
    public Direction getFlow(Direction side) {
        if ((ordinal() & 1) != 0) { return null; }
        if (side.getAxis() == Direction.Axis.Y) { return getFacing(); }
        if (this == NORTH) { return Direction.UP; }
        if (this == SOUTH) { return Direction.DOWN; }
        return switch (side) {
            case NORTH -> this == WEST ? Direction.WEST : Direction.EAST;
            case SOUTH -> this == WEST ? Direction.EAST : Direction.WEST;
            case WEST -> this == WEST ? Direction.SOUTH : Direction.NORTH;
            case EAST -> this == WEST ? Direction.NORTH : Direction.SOUTH;
            default -> throw new IllegalStateException("Non-horizontal channel side");
        };
    }
    public List<Direction> getFlowDiagonals(Direction side) { return flows.get(side); }
}
