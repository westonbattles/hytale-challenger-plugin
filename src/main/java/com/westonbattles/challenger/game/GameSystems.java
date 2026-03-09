package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;

import javax.annotation.Nonnull;

public class GameSystems {

	public static class CountdownSystem extends TickingSystem<EntityStore> {

		private float elapsed = 0f;
		private int countdown = 5;
		private final int countdownDuration = countdown;

		@Override
		public void tick(float delta, int index, @Nonnull Store<EntityStore> store) {
			GameManager gameManager = ChallengerPlugin.get().getGameManager();
			if (gameManager.state != GameState.Countdown) {
				if (countdown != countdownDuration) countdown = countdownDuration; // reset the countdown
				return;
			}

			World world = store.getExternalData().getWorld();
			if (!world.equals(gameManager.getWorld())) return;

			elapsed += delta;
			if (elapsed >= 1f) {
				elapsed = 0f;
				if (countdown <= 0) {
					countdown = countdownDuration;
					gameManager.startGame();
					return;
				}
				if (countdown == countdownDuration
						|| countdown == countdownDuration/2
						|| countdown == 3
						|| countdown == 2
						|| countdown == 1) {
					world.sendMessage(Message.raw("Game starting in " + countdown + "!"));
				}
				countdown--;
			}

		}
	}

}
