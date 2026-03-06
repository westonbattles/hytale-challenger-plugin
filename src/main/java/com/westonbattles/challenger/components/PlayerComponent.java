package com.westonbattles.challenger.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.game.PlayerRole;

import javax.annotation.Nullable;

public class PlayerComponent implements Component<EntityStore> {

    private boolean isReady;
    private PlayerRole role;

    public PlayerComponent() {
        this.isReady = false;
        this.role = PlayerRole.Unassigned;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new PlayerComponent();
    }

    public boolean isReady() {return isReady; }
    public void toggleReady() {isReady = !isReady;}

    public PlayerRole getRole() { return this.role; }
    public void setRole(PlayerRole role) { this.role = role; }

}
