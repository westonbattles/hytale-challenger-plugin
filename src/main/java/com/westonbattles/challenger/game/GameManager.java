package com.westonbattles.challenger.game;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

import java.util.HashMap;

public class GameManager {

	public GameState state = GameState.Waiting;
	private HashMap<UUID, PlayerRef> players;

	public void addPlayer(PlayerRef player) {
		players.put(player.getUuid(), player);
	}

	public void removePlayer(PlayerRef player) {
		players.remove(player.getUuid());
	}

	public boolean isReady(){
		// TODO: loop through players and check if they are ready based on their game component ready variable
		return false;
	}

	// Transition methods
	public void startGame(){
		state = GameState.Countdown;
	}

}
