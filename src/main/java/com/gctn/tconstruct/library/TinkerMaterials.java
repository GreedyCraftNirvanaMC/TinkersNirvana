package com.gctn.tconstruct.library;

import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.stats.HandleMaterialStats;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import com.google.common.collect.Lists;

import java.util.List;

public class TinkerMaterials {
    public static final List<Material> materials = Lists.newArrayList();
    public static final List<Material> craftableMaterials = Lists.newArrayList();
    public static final List<Material> castableMaterials = Lists.newArrayList();

    // natural resources/blocks
    public static final Material wood       = mat("wood", 0xff8e661b);
    public static final Material stone      = mat("stone", 0xff999999);
    public static final Material flint      = mat("flint", 0xff696969);
    public static final Material cactus     = mat("cactus", 0xff00a10f);
    public static final Material bone       = mat("bone", 0xffede6bf);
    public static final Material obsidian   = mat("obsidian", 0xff601cc4);
    public static final Material prismarine = mat("prismarine", 0xff7edebc);
    public static final Material endstone   = mat("endstone", 0xffe0d890);
    public static final Material paper      = mat("paper", 0xffffffff);
    public static final Material sponge     = mat("sponge", 0xffcacc4e);
    public static final Material firewood   = mat("firewood", 0xffcc5300);

    // Slime
    public static final Material knightslime= mat("knightslime", 0xfff18ff0);
    public static final Material slime      = mat("slime", 0xff82c873);
    public static final Material blueslime  = mat("blueslime", 0xff74c8c7);
    public static final Material magmaslime = mat("magmaslime", 0xffff960d);

    // Metals
    public static final Material iron       = mat("iron", 0xffcacaca);
    public static final Material pigiron    = mat("pigiron", 0xffef9e9b);

    // Nether Materials
    public static final Material netherrack = mat("netherrack", 0xffb84f4f);
    public static final Material ardite     = mat("ardite", 0xffd14210);
    public static final Material cobalt     = mat("cobalt", 0xff2882d4);
    public static final Material manyullyn  = mat("manyullyn", 0xffa15cf8);

    // Special Bone Materials
    public static final Material bloodbone  = mat("bloodbone", 0xffc70000);

    // Common Metals + Alloys
    public static final Material copper     = mat("copper", 0xffed9f07);
    public static final Material bronze     = mat("bronze", 0xffe3bd68);
    public static final Material lead       = mat("lead", 0xff4d4968);
    public static final Material silver     = mat("silver", 0xffd1ecf6);
    public static final Material electrum   = mat("electrum", 0xffe8db49);
    public static final Material steel      = mat("steel", 0xffa7a7a7);
    public static final Material alubrass   = mat("alubrass", 0xfff0d467);
    public static final Material alumite    = mat("alumite", 0xffffa7e9);

    // specul
    public static final Material xu;

    // unknown
    public static final Material unknown;

    // bowstring materials 弓弦
    public static final Material string    = mat("string", 0xffeeeeee);
    public static final Material vine      = mat("vine", 0xff40a10f);
    public static final Material slimevine_blue   = mat("slimevine_blue", 0xff74c8c7);
    //public static final Material slimevine_orange = mat("slimevine_orange", 0xffff960d);
    public static final Material slimevine_purple = mat("slimevine_purple", 0xffc873c8);

    // additional arrow shaft 额外箭杆
    public static final Material blaze     = mat("blaze", 0xffffc100);
    public static final Material reed      = mat("reed", 0xffaadb74);
    public static final Material ice       = mat("ice", 0xff97d7e0);
    public static final Material endrod    = mat("endrod", 0xffe8ffd6);

    // fletching 箭羽
    public static final Material feather   = mat("feather", 0xffeeeeee);
    public static final Material leaf      = mat("leaf", 0xff1d730c);
    public static final Material slimeleaf_blue   = mat("slimeleaf_blue", 0xff74c8c7);
    public static final Material slimeleaf_orange = mat("slimeleaf_orange", 0xffff960d);
    public static final Material slimeleaf_purple = mat("slimeleaf_purple", 0xffc873c8);

    public static Material mat(String name, int color) {
        // make materials show by default, integration will make them invisible
        Material mat = new Material(name, color, false);
        materials.add(mat);
        return mat;
    }

    public static void setCraftableMaterials(Material... matList) {
        for (Material mat : matList) {
            mat.setCraftable(true);
        }
    }

    public static void setCastableMaterials(Material... matList) {
        for (Material mat : matList) {
            mat.setCastable(true);
        }
    }

    // 注册材料属性
    public static void registerToolMaterialStats() {
        wood.addMaterialStats(new HeadMaterialStats(35, HarvestLevels.STONE, 2.00f, 2.00f),
                new HandleMaterialStats(1.00f, 25), new ExtraMaterialStats(15));
        stone.addMaterialStats(new HeadMaterialStats(120, HarvestLevels.IRON, 3.00f, 4.00f),
                new HandleMaterialStats(0.50f, -50), new ExtraMaterialStats(20));
        flint.addMaterialStats(new HeadMaterialStats(150, HarvestLevels.IRON, 2.90f, 5.00f),
                new HandleMaterialStats(0.60f, -60), new ExtraMaterialStats(40));
        cactus.addMaterialStats(new HeadMaterialStats(210, HarvestLevels.IRON, 3.40f, 4.00f),
                new HandleMaterialStats(0.85f, 20), new ExtraMaterialStats(50));
        bone.addMaterialStats(new HeadMaterialStats(200, HarvestLevels.IRON, 2.50f, 5.09f),
                new HandleMaterialStats(1.10f, 50), new ExtraMaterialStats(65));
        obsidian.addMaterialStats(new HeadMaterialStats(139, HarvestLevels.COBALT, 4.20f, 7.07f),
                new HandleMaterialStats(0.90f, -100), new ExtraMaterialStats(90));
        prismarine.addMaterialStats(new HeadMaterialStats(430, HarvestLevels.IRON, 6.20f, 5.50f),
                new HandleMaterialStats(0.60f, -150), new ExtraMaterialStats(100));
        endstone.addMaterialStats(new HeadMaterialStats(420, HarvestLevels.OBSIDIAN, 3.23f, 3.23f),
                new HandleMaterialStats(0.85f, 0), new ExtraMaterialStats(42));
        paper.addMaterialStats(new HeadMaterialStats(12, HarvestLevels.STONE, 0.05f, 0.51f),
                new HandleMaterialStats(0.10f, 5), new ExtraMaterialStats(15));
        sponge.addMaterialStats(new HeadMaterialStats(1050, HarvestLevels.STONE, 0.00f, 3.02f),
                new HandleMaterialStats(1.20f, 250), new ExtraMaterialStats(250));
        slime.addMaterialStats(new HeadMaterialStats(1000, HarvestLevels.STONE, 1.80f, 4.24f),
                new HandleMaterialStats(0.70f, 0), new ExtraMaterialStats(350));
        blueslime.addMaterialStats(new HeadMaterialStats(780, HarvestLevels.STONE, 1.80f, 4.03f),
                new HandleMaterialStats(1.30f, -50), new ExtraMaterialStats(200));
        knightslime.addMaterialStats(new HeadMaterialStats(850, HarvestLevels.OBSIDIAN, 5.10f, 5.8f),
                new HandleMaterialStats(0.50f, 500), new ExtraMaterialStats(125));
        magmaslime.addMaterialStats(new HeadMaterialStats(600, HarvestLevels.STONE, 7.00f, 2.1f),
                new HandleMaterialStats(0.85f, -200), new ExtraMaterialStats(150));
        netherrack.addMaterialStats(new HeadMaterialStats(270, HarvestLevels.IRON, 3.00f, 4.50f),
                new HandleMaterialStats(0.85f, -150), new ExtraMaterialStats(75));
        cobalt.addMaterialStats(new HeadMaterialStats(780, HarvestLevels.COBALT, 4.10f, 12.00f),
                new HandleMaterialStats(0.90f, 100), new ExtraMaterialStats(300));
        ardite.addMaterialStats(new HeadMaterialStats(990, HarvestLevels.COBALT, 3.60f, 3.50f),
                new HandleMaterialStats(1.40f, -200), new ExtraMaterialStats(450));
        manyullyn.addMaterialStats(new HeadMaterialStats(820, HarvestLevels.COBALT, 8.72f, 7.02f),
                new HandleMaterialStats(0.50f, 250), new ExtraMaterialStats(50));
        firewood.addMaterialStats(new HeadMaterialStats(550, HarvestLevels.STONE, 5.50f, 6.00f),
                new HandleMaterialStats(1.0f, -200), new ExtraMaterialStats(150));
        bloodbone.addMaterialStats(new HeadMaterialStats(200, HarvestLevels.IRON, 2.50f, 5.09f),
                new HandleMaterialStats(1.10f, 50), new ExtraMaterialStats(65));
        iron.addMaterialStats(new HeadMaterialStats(204, HarvestLevels.DIAMOND, 4.00f, 6.00f),
                new HandleMaterialStats(0.85f, 60), new ExtraMaterialStats(50));
        pigiron.addMaterialStats(new HeadMaterialStats(380, HarvestLevels.DIAMOND, 4.50f, 6.20f),
                new HandleMaterialStats(1.20f, 0), new ExtraMaterialStats(170));
        copper.addMaterialStats(new HeadMaterialStats(210, HarvestLevels.IRON, 3.00f, 5.30f),
                new HandleMaterialStats(1.05f, 30), new ExtraMaterialStats(100));
        bronze.addMaterialStats(new HeadMaterialStats(430, HarvestLevels.DIAMOND, 3.50f, 6.80f),
                new HandleMaterialStats(1.10f, 70), new ExtraMaterialStats(80));
        lead.addMaterialStats(new HeadMaterialStats(434, HarvestLevels.IRON, 3.50f, 5.25f),
                new HandleMaterialStats(0.70f, -50), new ExtraMaterialStats(100));
        silver.addMaterialStats(new HeadMaterialStats(250, HarvestLevels.IRON, 5.00f, 5.00f),
                new HandleMaterialStats(0.95f, 50), new ExtraMaterialStats(150));
        electrum.addMaterialStats(new HeadMaterialStats(50, HarvestLevels.IRON, 3.00f, 12.00f),
                new HandleMaterialStats(1.10f, -25), new ExtraMaterialStats(250));
        steel.addMaterialStats(new HeadMaterialStats(540, HarvestLevels.OBSIDIAN, 6.00f, 7.00f),
                new HandleMaterialStats(0.9f, 150), new ExtraMaterialStats(25));
        alubrass.addMaterialStats(new HeadMaterialStats(450, HarvestLevels.DIAMOND, 3.50f, 7.00f),
                new HandleMaterialStats(1.10f, 80), new ExtraMaterialStats(90));
        alumite.addMaterialStats(new HeadMaterialStats(700, HarvestLevels.COBALT, 5.50f, 8.00f),
                new HandleMaterialStats(1.25f, 150), new ExtraMaterialStats(100));
        wood.addMaterialStats(new BowMaterialStats(1f, 1f, 0));
        cactus.addMaterialStats(new BowMaterialStats(1.05f, 0.9f, 0));
        bone.addMaterialStats(new BowMaterialStats(0.95f, 1.15f, 0));
        paper.addMaterialStats(new BowMaterialStats(1.5f, 0.4f, -2f));
        sponge.addMaterialStats(new BowMaterialStats(1.15f, 0.75f, 0));
        slime.addMaterialStats(new BowMaterialStats(0.85f, 1.3f, 0));
        blueslime.addMaterialStats(new BowMaterialStats(1.05f, 1f, 0));
        knightslime.addMaterialStats(new BowMaterialStats(0.4f, 2f, 2f));
        magmaslime.addMaterialStats(new BowMaterialStats(1.1f, 1.05f, 1f));
        cobalt.addMaterialStats(new BowMaterialStats(0.75f, 1.3f, 3f));
        ardite.addMaterialStats(new BowMaterialStats(0.45f, 0.8f, 1f));
        manyullyn.addMaterialStats(new BowMaterialStats(0.65f, 1.2f, 4f));
        firewood.addMaterialStats(new BowMaterialStats(1f, 1f, 0f));
        iron.addMaterialStats(new BowMaterialStats(0.5f, 1.5f, 7f));
        pigiron.addMaterialStats(new BowMaterialStats(0.6f, 1.4f, 7f));
        bloodbone.addMaterialStats(new BowMaterialStats(0.95f, 1.15f, 0));
        copper.addMaterialStats(new BowMaterialStats(0.6f, 1.45f, 5f));
        bronze.addMaterialStats(new BowMaterialStats(0.55f, 1.5f, 6f));
        lead.addMaterialStats(new BowMaterialStats(0.4f, 1.3f, 3f));
        silver.addMaterialStats(new BowMaterialStats(1.2f, 0.8f, 2f));
        electrum.addMaterialStats(new BowMaterialStats(1.5f, 1f, 4f));
        steel.addMaterialStats(new BowMaterialStats(0.4f, 2f, 9f));
        alubrass.addMaterialStats(new BowMaterialStats(0.45f, 1.5f, 6f));
        alumite.addMaterialStats(new BowMaterialStats(0.4f, 1.5f, 8f));
        stone.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        flint.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        obsidian.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        prismarine.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        endstone.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        netherrack.addMaterialStats(new BowMaterialStats(0.2F, 0.4F, -1));
        string.setCraftable(true);
        string.addMaterialStats(new BowStringMaterialStats(1));
        vine.setCraftable(true);
        vine.addMaterialStats(new BowStringMaterialStats(1));
        slimevine_blue.setCraftable(true);
        slimevine_blue.addMaterialStats(new BowStringMaterialStats(1));
        slimevine_purple.setCraftable(true);
        slimevine_purple.addMaterialStats(new BowStringMaterialStats(1));
        wood.setCraftable(true);
        wood.addMaterialStats(new ArrowShaftMaterialStats(1f, 0));
        bone.setCraftable(true);
        bone.addMaterialStats(new ArrowShaftMaterialStats(0.9f, 5));
        blaze.setCraftable(true);
        blaze.addMaterialStats(new ArrowShaftMaterialStats(0.8f, 3));
        reed.setCraftable(true);
        reed.addMaterialStats(new ArrowShaftMaterialStats(1.5f, 20));
        ice.setCraftable(true);
        ice.addMaterialStats(new ArrowShaftMaterialStats(0.95f, 0));
        endrod.setCraftable(true);
        endrod.addMaterialStats(new ArrowShaftMaterialStats(0.7f, 1));
        feather.setCraftable(true);
        feather.addMaterialStats(new FletchingMaterialStats(1.0f, 1f));
        leaf.setCraftable(true);
        leaf.addMaterialStats(new FletchingMaterialStats(0.5f, 1.5f));
        slimeleaf_blue.setCraftable(true);
        slimeleaf_blue.addMaterialStats(new FletchingMaterialStats(0.8F, 1.25F));
        slimeleaf_orange.setCraftable(true);
        slimeleaf_orange.addMaterialStats(new FletchingMaterialStats(0.8F, 1.25F));
        slimeleaf_purple.setCraftable(true);
        slimeleaf_purple.addMaterialStats(new FletchingMaterialStats(0.8F, 1.25F));
    }

    // 注册材料词条
    public static void registerToolMaterialTraits() {
        wood.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, TinkerTraits.ECOLOGICAL_1));
        stone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", TinkerTraits.CHEAPSKATE_1),
                AbstractTraits.TraitPosPair.of(null, TinkerTraits.CHEAP_1));
        flint.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("crude", 2)));
        flint.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("crude", 1)));
        cactus.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("prickly", 1)));
        cactus.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("spiky", 1)));
        obsidian.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("duritos", 1)));
        prismarine.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("jagged", 1), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("aquadynamic", 1)));
        prismarine.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("aquadynamic", 1)));
        netherrack.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("aridiculous", 1), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("hellish", 1)));
        netherrack.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("hellish", 1)));
        endstone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("alien", 1)), AbstractTraits.TraitPosPair.of("Projectile", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("enderference", 1)));
        endstone.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("enderference", 1)));
        bone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("splintering", 1)));
        bone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Shaft", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("splitting", 1)));
        bone.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("fractured", 1)));
        paper.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("writable", 2)));
        paper.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("writable", 1)));
        sponge.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("squeaky", 1)));
        firewood.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("autosmelt", 1)));
        slime.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("slimey_green", 1)));
        blueslime.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("slimey_blue", 1)));
        knightslime.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("crumbling", 1)));
        knightslime.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("unnatural", 1)));
        magmaslime.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("superheat", 1)));
        magmaslime.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("flammable", 1)));
        iron.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("magnetic", 2)));
        iron.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("magnetic", 1)));
        pigiron.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("baconlicious", 1), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("tasty", 1)));
        pigiron.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("tasty", 1)));
        cobalt.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("momentum", 1)));
        cobalt.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("lightweight", 1)));
        ardite.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("stonebound", 1)));
        ardite.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("petramor", 1)));
        manyullyn.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("insatiable", 1)));
        manyullyn.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("coldblooded", 1)));
        bloodbone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("raging", 2), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("splintering", 1)));
        bloodbone.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("raging", 1), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("fractured", 1)));
        copper.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("established", 1)));
        bronze.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("dense", 1)));
        lead.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("poisonous", 1), com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("heavy", 1)));
        silver.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("holy", 1)));
        electrum.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("shocking", 1)));
        steel.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("sharp", 1)));
        steel.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("stiff", 1)));
        alubrass.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("depthdigger", 1)));
        alumite.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("duritos", 1)));
        blaze.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("hovering", 1)));
        reed.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("breakable", 1)));
        ice.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("freezing", 1)));
        endrod.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, com.gctn.tconstruct.tools.traits.AntiqueTraits.entry("endspeed", 1)));
    }

    static {
        xu = new Material("unstable", 0xFFFFFFFF, true);
        unknown = new Material("unknown", 0xFFFFFFFF, true);
    }

    // 设定合成方式
    static {
        setCraftableMaterials(wood, stone, flint, cactus, bone, obsidian, prismarine, endstone, paper, sponge, firewood,
                slime, blueslime, magmaslime, knightslime, netherrack, bloodbone);
        setCastableMaterials(iron, pigiron, knightslime, obsidian, cobalt, ardite, manyullyn, copper, bronze, lead, silver, electrum, steel, alubrass, alumite);

        registerToolMaterialStats();
        registerToolMaterialTraits();
    }

    public static Material getMaterial(String materialId) {
        for (Material material : TinkerMaterials.materials) {
            if (material.identifier.equals(materialId)) {
                return material;
            }
        }
        return null;
    }

}
