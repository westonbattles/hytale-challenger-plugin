package com.westonbattles.challenger.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;
import com.westonbattles.challenger.game.GameState;
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

    public boolean isReady() { return isReady; }
    public void toggleReady() {
        GameManager gameManager = ChallengerPlugin.get().getGameManager();
        isReady = !isReady;

        if (isReady) {
            if (gameManager.shouldStart()) gameManager.startCountdown();
        } else if (gameManager.state == GameState.Countdown) {
            gameManager.getWorld().sendMessage(Message.raw("Not all players are ready!"));
            gameManager.setState(GameState.Waiting);
        }
    }

    public PlayerRole getRole() { return this.role; }
    public void setRole(PlayerRole role) { this.role = role; }

}
