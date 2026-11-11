package com.gctn.tconstruct.compat.kubejs;

import com.gctn.tconstruct.api.MaterialDefinition;
import com.gctn.tconstruct.library.TinkerMaterials;
import dev.latvian.mods.kubejs.event.KubeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MaterialModificationEvent implements KubeEvent {
    private record Edit(MaterialDefinition definition, dev.latvian.mods.kubejs.script.SourceLine line) {}
    private final List<Edit> edits = new ArrayList<>();

    public void modify(dev.latvian.mods.rhino.Context cx, String id, Consumer<MaterialDefinition> action) {
        var material = TinkerMaterials.getMaterial(id);
        if (material == null) { throw new IllegalArgumentException("Unknown material: " + id); }
        MaterialDefinition edit = new MaterialDefinition(material);
        action.accept(edit);
        edits.add(new Edit(edit, dev.latvian.mods.kubejs.script.SourceLine.of(cx)));
    }

    void apply() {
        for (Edit edit : edits) {
            try {
                edit.definition().install();
            } catch (RuntimeException exception) {
                throw new dev.latvian.mods.kubejs.error.KubeRuntimeException(
                        "Invalid material edit " + edit.definition().material().identifier + " at " + edit.line(), exception).source(edit.line());
            }
        }
    }
}
