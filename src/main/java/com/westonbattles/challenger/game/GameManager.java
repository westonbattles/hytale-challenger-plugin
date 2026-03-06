package com.westonbattles.challenger.game;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;

public class GameManager {

	public GameState state = GameState.Waiting;
	private List<PlayerRef> players = new ArrayList<>();

	public void addPlayer(PlayerRef player) {
		players.add(player);
	}

	public void removePlayer(PlayerRef player) {
		players.remove(player);
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
