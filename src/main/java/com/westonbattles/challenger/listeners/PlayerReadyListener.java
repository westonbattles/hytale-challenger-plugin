package com.westonbattles.challenger.listeners;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;

// PlayerReady is a hytale listener that triggers when the player loads into to a world
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
        }
        // Otherwise, they are joining a different world, so we need to remove them as a player if they're leaving the minigame world
        else if (gameManager.getPlayers().contains(playerRef)){
            // Remove the player
            gameManager.removePlayer(playerRef, world);
        }
    }
}

