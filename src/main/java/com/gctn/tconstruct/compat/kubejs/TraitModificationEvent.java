package com.gctn.tconstruct.compat.kubejs;

import com.gctn.tconstruct.api.TraitDefinition;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import dev.latvian.mods.kubejs.event.KubeEvent;

import java.util.function.Consumer;

public final class TraitModificationEvent implements KubeEvent {
    public void modify(String id, Consumer<TraitDefinition> action) {
        var current = TinkerTraits.getTrait(id);
        if (current == null) { throw new IllegalArgumentException("Unknown trait: " + id); }
        TraitDefinition definition = new TraitDefinition(current.identifier, current);
        action.accept(definition);
        TinkerTraits.replaceTrait(definition);
    }

    public void remove(String id) { TinkerTraits.removeTrait(id); }
}
