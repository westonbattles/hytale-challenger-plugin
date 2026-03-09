package com.westonbattles.challenger.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ReadyCommand extends AbstractPlayerCommand {

    public ReadyCommand(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = commandContext.senderAs(Player.class);

        CompletableFuture.runAsync(() -> {

            PlayerComponent playerComponent = ChallengerPlugin.get().getGameManager().getPlayerComponent(playerRef);
            if (playerComponent == null) commandContext.sendMessage(Message.raw("You are not a player!"));
            else {
                playerComponent.toggleReady();
                if (playerComponent.isReady()) commandContext.sendMessage(Message.raw("Ready!"));
                else commandContext.sendMessage(Message.raw("Uneady!"));
            }

        }, world);
    }
}