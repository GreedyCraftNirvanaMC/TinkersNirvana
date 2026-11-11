package com.gctn.tconstruct.library.materials;

import com.gctn.tconstruct.library.stats.AbstractMaterialStats;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Material {
    public final String identifier;
    public int color;
    public final boolean hidden;

    public Map<String, AbstractMaterialStats> stats = new HashMap<>();
    private final Map<String, List<AbstractTraits.TraitEntry>> traits = new HashMap<>();

    // 是否可以直接使用部件加工台(默认false)
    protected boolean craftable = false;

    // 是否可以使用浇筑方式合成(默认false)
    protected boolean castable = false;

    private BooleanSupplier availability = () -> true;
    private Supplier<ItemStack> representativeItem = () -> ItemStack.EMPTY;

    private String translationKey;
    private net.minecraft.network.chat.Component displayName;
    private final List<Supplier<ItemStack>> fallbackItems = new ArrayList<>();

    public Material displayName(net.minecraft.network.chat.Component name) { displayName = name; return this; }
    public Material translationKey(String key) { translationKey = key; return this; }
    public Material fallbackItem(Supplier<ItemStack> item) { fallbackItems.add(item); return this; }

    public net.minecraft.network.chat.Component getDisplayName() {
        ItemStack representative = getRepresentativeItem();
        ItemStack input = fallbackItems.stream().map(Supplier::get).filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        net.minecraft.network.chat.Component fallback = displayName != null ? displayName.copy()
                : !representative.isEmpty() ? representative.getHoverName()
                : !input.isEmpty() ? input.getHoverName()
                : net.minecraft.network.chat.Component.literal(identifier);
        var legacy = com.gctn.tconstruct.api.ContentNames.translated("material." + identifier + ".name", fallback);
        String key = translationKey == null ? com.gctn.tconstruct.api.ContentNames.key("material", identifier, "name") : translationKey;
        return com.gctn.tconstruct.api.ContentNames.translated(key, legacy);
    }

    public static net.minecraft.network.chat.Component nameOf(String id) {
        Material material = com.gctn.tconstruct.library.TinkerMaterials.getMaterial(id);
        return material == null ? net.minecraft.network.chat.Component.literal(id) : material.getDisplayName();
    }

    public void removeTrait(String id) {
        String canonical = com.gctn.tconstruct.api.ContentNames.traitId(id);
        traits.values().forEach(list -> list.removeIf(entry -> com.gctn.tconstruct.api.ContentNames.traitId(entry.trait().identifier).equals(canonical)));
    }

    public void clearTraits() { traits.clear(); }

    private static List<AbstractTraits.TraitEntry> resolveTraits(List<AbstractTraits.TraitEntry> source) {
        List<AbstractTraits.TraitEntry> resolved = new ArrayList<>();
        for (var entry : source) {
            var current = com.gctn.tconstruct.tools.traits.TinkerTraits.getTrait(entry.trait().identifier);
            if (current != null) { resolved.add(new AbstractTraits.TraitEntry(current, Math.min(entry.level(), current.maxLevel))); }
        }
        return List.copyOf(resolved);
    }

    // 参数不全的默认初始化
    public Material(String identifier, int color) { this(identifier, color, false); }
    public Material(String identifier) { this(identifier, 0xFFFFFF); }

    public Material(String identifier, int color, boolean hidden) {
        this.identifier = identifier;
        this.color = color;
        this.hidden = hidden;
    }

    // 设置合成方式
    public Material setCraftable(boolean craftable) {
        this.craftable = craftable;
        return this;
    }
    public Material setCastable(boolean castable) {
        this.castable = castable;
        return this;
    }

    public boolean isHidden() {
        return hidden || !availability.getAsBoolean();
    }

    /** Shared by the book and creative catalogs; evaluate against the current item tags after reload. */
    public Material setAvailability(BooleanSupplier availability) {
        this.availability = Objects.requireNonNull(availability);
        return this;
    }

    /** Optional display stack, resolved after item registration; callers receive an independent single-item copy. */
    public Material setRepresentativeItem(Supplier<ItemStack> item) {
        this.representativeItem = Objects.requireNonNull(item);
        return this;
    }

    public ItemStack getRepresentativeItem() { return representativeItem.get().copyWithCount(1); }

    /** All required item tags must contain an item, matching Antique's material integration requirements. */
    public Material requireItems(String... tags) {
        List<TagKey<Item>> requirements = Arrays.stream(tags).map(id -> TagKey.create(Registries.ITEM, ResourceLocation.parse(id))).toList();
        return setAvailability(() -> {
            for (var tag : requirements) {
                // Registry named sets may retain removed tags; holder membership reflects the latest binding.
                if (BuiltInRegistries.ITEM.getTag(tag).filter(items -> items.stream().anyMatch(item -> item.is(tag))).isEmpty()) { return false; }
            }
            return true;
        });
    }

    public boolean isCraftable() {
        return craftable || (castable && com.gctn.tconstruct.Config.CRAFT_CASTABLE_MATERIALS.get());
    }

    public boolean isCastable() {
        return castable;
    }

    // 添加材料属性
    public void addMaterialStats(AbstractMaterialStats... stats) {
        for (AbstractMaterialStats stat : stats) {
            this.stats.put(stat.identifier, stat);
        }
    }

    // 获取材料属性
    public Map<String, AbstractMaterialStats> getStats() {
        return stats;
    }

    public boolean hasStats(String identifier) {
        return stats.containsKey(identifier);
    }

    public <T extends AbstractMaterialStats> T getStats(String identifier, Class<T> statClass) {
        AbstractMaterialStats stat = stats.get(identifier);
        return statClass.isInstance(stat) ? statClass.cast(stat) : null;
    }

    // 添加词条属性
    public void addMaterialTraits(AbstractTraits.TraitPosPair... traitPairs) {
        for (AbstractTraits.TraitPosPair traitPair : traitPairs) {
            List<AbstractTraits.TraitEntry> list = this.traits.computeIfAbsent(traitPair.pos(), k -> new ArrayList<>());
            for (AbstractTraits.TraitEntry entry : traitPair.traits()) {
                if (list.stream().anyMatch(existing -> existing.trait().identifier.equals(entry.trait().identifier))) {
                    throw new IllegalArgumentException("Duplicate material trait: " + entry.trait().identifier);
                }
                list.add(entry);
            }
        }
    }

    // 获取词条属性
    public List<AbstractTraits.TraitEntry> getTraits(String pos) {
        // null 位置表示默认配置；指定位置的配置整体替换默认配置，而不是追加。
        return resolveTraits(traits.getOrDefault(pos, traits.getOrDefault(null, List.of())));
    }

    /** 多属性部件只合并显式部位词条；全部缺失时才采用默认列表。 */
    public List<AbstractTraits.TraitEntry> getTraitsForStats(List<String> positions) {
        List<AbstractTraits.TraitEntry> result = new ArrayList<>();
        for (String position : positions) { result.addAll(traits.getOrDefault(position, List.of())); }
        return resolveTraits(result.isEmpty() ? traits.getOrDefault(null, List.of()) : result);
    }

    public int getColor() { return color; }

}
