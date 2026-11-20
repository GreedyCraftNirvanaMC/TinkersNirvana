package com.gctn.tconstruct.compat.jade;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity;
import com.gctn.tconstruct.smeltery.SmelteryTank;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.FluidSmelteryMaster;
import com.gctn.tconstruct.smeltery.block.entity.HeatingContainer;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.TinkerTankBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

enum MachineStatus implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "machine_status");
    private static final String DATA = ID.toString();
    private static final String TEXT = "tooltip.tconsnirvana.jade.";
    private static final int MAX_DISPLAYED_FLUIDS = 4;

    @Override public ResourceLocation getUid() {
        return ID;
    }

    @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        var registries = accessor.getLevel().registryAccess();
        CompoundTag status = new CompoundTag();

        if (entity instanceof HeatingContainer heater) {
            status.putBoolean("active", heater.active());
            if (heater.active()) {
                status.putInt("temperature", heater.temperature());
                int heated = 0;
                long heat = 0;
                long required = 0;
                for (int slot = 0; slot < heater.getContainerSize(); slot++) {
                    int target = heater.required(slot);
                    if (target > 0) {
                        heated++;
                        heat += Math.clamp(heater.heat(slot), 0, target);
                        required += target;
                    }
                }
                if (heated > 0 && required > 0) {
                    putProgress(status, "heating", (float) heat / required);
                }
            }
        }
        if (entity instanceof TinkerTankBlockEntity tank) {
            status.putBoolean("active", tank.active());
        }
        if (status.getBoolean("active") && entity instanceof FluidSmelteryMaster master) {
            writeTank(status, master.liquids(), registries);
        }
        if (entity instanceof SmelteryComponentBlockEntity casting
                && (casting.kind() == SmelteryBlock.Kind.TABLE || casting.kind() == SmelteryBlock.Kind.BASIN)) {
            ItemStack output = casting.castingResult();
            if (!output.isEmpty()) {
                putProgress(status, "casting", casting.progress());
                status.put("output", output.save(registries));
            }
        }
        if (entity instanceof RackBlockEntity rack && rack.drying() && rack.progress() > 0) {
            putProgress(status, "drying", rack.progress());
        }

        if (!status.isEmpty()) { data.put(DATA, status); }
    }

    private static void writeTank(CompoundTag status, SmelteryTank tank, HolderLookup.Provider registries) {
        status.putInt("amount", tank.amount());
        status.putInt("capacity", tank.capacity());
        ListTag fluids = new ListTag();
        // 总量包含全部流体，详细列表只发送底部几层，避免较大储罐撑满提示框。
        tank.fluids().stream().filter(fluid -> !fluid.isEmpty()).limit(MAX_DISPLAYED_FLUIDS)
                .forEach(fluid -> fluids.add(fluid.save(registries)));
        status.put("fluids", fluids);
        status.putInt("remaining", Math.max(0, tank.fluids().size() - fluids.size()));
    }

    private static void putProgress(CompoundTag status, String key, float progress) {
        status.putString("stage", key);
        status.putInt("progress", Math.round(100 * Math.clamp(progress, 0, 1)));
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData().getCompound(DATA);
        if (data.contains("active") && !data.getBoolean("active")) {
            tooltip.add(Component.translatable(TEXT + "unformed"));
            return;
        }

        if (data.contains("capacity")) {
            tooltip.add(Component.translatable(TEXT + "volume", data.getInt("amount"), data.getInt("capacity")));
            if (accessor.showDetails()) {
                var registries = accessor.getLevel().registryAccess();
                for (Tag tag : data.getList("fluids", Tag.TAG_COMPOUND)) {
                    FluidStack fluid = FluidStack.parseOptional(registries, (CompoundTag) tag);
                    if (!fluid.isEmpty()) {
                        tooltip.add(Component.translatable(TEXT + "fluid", fluid.getHoverName(), fluid.getAmount()));
                    }
                }
                int remaining = data.getInt("remaining");
                if (remaining > 0) { tooltip.add(Component.translatable(TEXT + "more_fluids", remaining)); }
            }
        }

        int temperature = data.getInt("temperature");
        if (temperature > 0) {
            tooltip.add(Component.translatable(TEXT + "temperature", temperature));
        }
        if (data.contains("stage")) {
            tooltip.add(Component.translatable(TEXT + data.getString("stage"), data.getInt("progress")));
            if (data.contains("output")) {
                ItemStack output = ItemStack.parseOptional(accessor.getLevel().registryAccess(), data.getCompound("output"));
                if (!output.isEmpty()) { tooltip.append(IElementHelper.get().smallItem(output)); }
            }
        }
    }
}
