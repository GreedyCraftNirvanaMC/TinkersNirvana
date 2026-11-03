/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's Exploder and ExplosionEFLN. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

/** Spherical, full-drop excavation, spread across ticks without loading chunks. */
@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class EflnBlast {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TinkersNirvana.MODID);
    private static final Supplier<AttachmentType<ArrayDeque<EflnBlast>>> QUEUE = ATTACHMENTS.register("efln_blasts", () -> AttachmentType.builder(() -> new ArrayDeque<EflnBlast>()).build());
    private static final List<BlockPos> OFFSETS;
    static {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -6; x <= 6; x++) { for (int y = -6; y <= 6; y++) { for (int z = -6; z <= 6; z++) { if (x * x + y * y + z * z <= 36) { offsets.add(new BlockPos(x, y, z)); } } } }
        offsets.sort(Comparator.comparingInt((BlockPos pos) -> Math.max(Math.abs(pos.getX()), Math.max(Math.abs(pos.getY()), Math.abs(pos.getZ())))).thenComparing(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed()).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getX));
        OFFSETS = List.copyOf(offsets);
    }
    private final ServerLevel level;
    private final Entity source;
    private final BlockPos origin;
    private final Explosion explosion;
    private final List<ItemStack> drops = new ArrayList<>();
    private int cursor;
    private EflnBlast(ServerLevel level, Entity source) {
        this.level = level; this.source = source; origin = source.blockPosition();
        explosion = new Explosion(level, source, source.getX(), source.getY(), source.getZ(), 6, false, Explosion.BlockInteraction.DESTROY);
    }
    public static void register(IEventBus bus) { ATTACHMENTS.register(bus); }
    public static void start(ServerLevel level, Entity source) {
        EflnBlast blast = new EflnBlast(level, source);
        if (EventHooks.onExplosionStart(level, blast.explosion)) { return; }
        blast.entities();
        level.playSound(null, blast.origin, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4, (1 + (level.random.nextFloat() - level.random.nextFloat()) * .2F) * .7F);
        level.gameEvent(source, GameEvent.EXPLODE, blast.origin);
        level.getData(QUEUE).addLast(blast);
    }
    private void entities() {
        Vec3 center = Vec3.atLowerCornerOf(origin);
        List<Entity> entities = level.getEntities(source, new AABB(origin).inflate(7), entity -> entity.isAlive() && !entity.isSpectator() && !entity.ignoreExplosion(explosion) && entity.distanceToSqr(center) <= 36);
        EventHooks.onExplosionDetonate(level, explosion, entities, 12);
        for (Entity entity : entities) {
            Vec3 direction = entity.position().subtract(source.position().add(0, -3, 0));
            double strength = Math.max(.3, (6 - direction.length()) / 6);
            Vec3 motion = direction.normalize().scale(6 * strength * .3).add(0, .5, 0);
            entity.push(motion.x, motion.y, motion.z);
            entity.hurt(Explosion.getDefaultDamageSource(level, source), (float) (strength * 6));
            entity.onExplosionHit(source);
            if (entity instanceof ServerPlayer player) { PacketDistributor.sendToPlayer(player, new GearNetwork.Velocity(player.getDeltaMovement(), false)); }
        }
    }
    @SubscribeEvent private static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.hasData(QUEUE)) { return; }
        var queue = level.getData(QUEUE); int remaining = queue.size(), budget = 256;
        while (remaining-- > 0 && budget > 0) {
            EflnBlast blast = queue.removeFirst(); int used = blast.step(Math.min(50, budget)); budget -= Math.max(1, used);
            if (blast.cursor < OFFSETS.size()) { queue.addLast(blast); } else { blast.finish(); }
        }
    }
    @SubscribeEvent private static void stopping(ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) { if (level.hasData(QUEUE)) { var queue = level.getData(QUEUE); queue.forEach(EflnBlast::finish); queue.clear(); } }
    }
    private int step(int limit) {
        explosion.clearToBlow();
        while (cursor < OFFSETS.size() && explosion.getToBlow().size() < limit) {
            BlockPos offset = OFFSETS.get(cursor++), pos = origin.offset(offset);
            if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) { continue; }
            var state = level.getBlockState(pos); if (state.isAir()) { continue; }
            var fluid = state.getFluidState();
            float resistance = source.getBlockExplosionResistance(explosion, level, pos, state, fluid,
                    Math.max(state.getExplosionResistance(level, pos, explosion), fluid.getExplosionResistance(level, pos, explosion)));
            double power = 6 * (1 - (offset.getX() * offset.getX() + offset.getY() * offset.getY() + offset.getZ() * offset.getZ()) / 36D) - (resistance + .3) * .3;
            if (power > 0 && source.shouldBlockExplode(explosion, level, pos, state, (float) power)) { explosion.getToBlow().add(pos); }
        }
        int count = explosion.getToBlow().size();
        EventHooks.onExplosionDetonate(level, explosion, new ArrayList<>(), 12);
        for (BlockPos pos : List.copyOf(explosion.getToBlow())) {
            if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) { continue; }
            level.getBlockState(pos).onExplosionHit(level, pos, explosion, (stack, location) -> aggregate(stack));
            level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 2, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 1, 0, 0, 0, 0);
        }
        return count;
    }
    private void aggregate(ItemStack stack) {
        if (stack.isEmpty()) { return; }
        for (ItemStack existing : drops) { if (ItemStack.isSameItemSameComponents(stack, existing)) { existing.grow(stack.getCount()); return; } }
        drops.add(stack.copy());
    }
    private void finish() {
        for (ItemStack stack : drops) {
            while (!stack.isEmpty()) { BlockPos pos = origin.offset(-3 + level.random.nextInt(6), -3 + level.random.nextInt(6), -3 + level.random.nextInt(6)); Block.popResource(level, pos, stack.split(stack.getMaxStackSize())); }
        }
        drops.clear();
    }
}
