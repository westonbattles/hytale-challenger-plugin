package com.westonbattles.challenger.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;
import com.westonbattles.challenger.game.GameState;

public class PlayerDisconnectListener {

	public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
		PlayerRef playerRef = event.getPlayerRef();
		GameManager gameManager = ChallengerPlugin.get().getGameManager();

		// if player was in player list (and got removed), we need to check if we should start the game now
		if (gameManager.removePlayerFromListOnly(playerRef)){
			if (gameManager.shouldStart()) gameManager.startCountdown();
		}
	}
}
