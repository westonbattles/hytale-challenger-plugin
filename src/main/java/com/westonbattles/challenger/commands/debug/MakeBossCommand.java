package com.westonbattles.challenger.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;
import com.westonbattles.challenger.game.GameManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MakeBossCommand extends AbstractPlayerCommand {

    public MakeBossCommand(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        CompletableFuture.runAsync(() -> {

            GameManager gameManager = ChallengerPlugin.get().getGameManager();
            PlayerComponent playerComponent = gameManager.getPlayerComponent(playerRef);

            if (playerComponent == null || !world.equals(gameManager.getWorld())) return;

            gameManager.makeBoss(playerRef);
        }, world);
    }
}