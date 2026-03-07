package com.westonbattles.challenger.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;

public class PlayerDisconnectListener {

	public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
		PlayerRef playerRef = event.getPlayerRef();
		ChallengerPlugin.get().getGameManager().removePlayerFromListOnly(playerRef);
	}
}
