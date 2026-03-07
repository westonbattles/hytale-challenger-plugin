package com.westonbattles.challenger.listeners;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;

public class PlayerReadyListener {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameManager gameManager = ChallengerPlugin.get().getGameManager();

        if (world == null) return;

        // Get the playerRef
        PlayerRef playerRef = gameManager.getPlayerRef(player, world);
        if (playerRef == null) return;

        // Make sure player is in the correct world for the minigame
        boolean playerJoinedMinigameWorld = world.equals(gameManager.getWorld());
        if (playerJoinedMinigameWorld) {
            // Add the player
            gameManager.addPlayer(playerRef);
            // Check if component add worked
            playerRef.sendMessage(Message.raw("Added player with role: " + gameManager.getRole(playerRef)));
        }
        // Otherwise, they are joining a different world, so we need to remove them as a player if they're leaving the minigame world
        else if (gameManager.getPlayers().contains(playerRef)){
            // Remove the player
            gameManager.removePlayer(playerRef, world);
            playerRef.sendMessage(Message.raw("Removed player"));
        }
    }
}

