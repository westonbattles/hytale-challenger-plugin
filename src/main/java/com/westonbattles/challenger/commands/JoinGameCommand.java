package com.westonbattles.challenger.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;
import com.westonbattles.challenger.game.PlayerRole;
import com.westonbattles.challenger.ui.BossSelectUI;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class JoinGameCommand extends AbstractPlayerCommand {

    public JoinGameCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        //Player player = ctx.senderAs(Player.class);

        GameManager gameManager = ChallengerPlugin.get().getGameManager();
        gameManager.addPlayer(playerRef);

        for (PlayerRef player : gameManager.getPlayers()) {
            playerRef.sendMessage(Message.raw("Player: " + player.getUsername()));
        }


        /*PlayerRole role = gameManager.getRole(playerRef);

        if (role == null) playerRef.sendMessage(Message.raw("Player role is null"));
        else playerRef.sendMessage(Message.raw("Player role: " + role.name()));*/
    }
}
