package com.westonbattles.challenger.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class TemplateComponent implements Component<EntityStore> {

    private String exampleVar;

    public TemplateComponent() {
        this.exampleVar = "example";
    }

    public TemplateComponent(String exampleVar) {
        this.exampleVar = exampleVar;
    }

    public TemplateComponent(TemplateComponent other) {
        this.exampleVar = other.exampleVar;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new TemplateComponent(this);
    }

    public String getExampleVar() {
        return exampleVar;
    }

}
