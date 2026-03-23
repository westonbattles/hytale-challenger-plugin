package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.protocol.EntityPart;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

	public static class BossParticleSystem extends TickingSystem<EntityStore> {

		private static final ModelParticle[] BOSS_PARTICLES = {
		new ModelParticle("Fire_ChargeWhite", 0.2f, null, EntityPart.Self, "Head", new Vector3f(0.1f, 0.2f, 0.15f), null, false),
				new ModelParticle("Fire_ChargeWhite", 0.2f, null, EntityPart.Self, "Head", new Vector3f(-0.1f, 0.2f, 0.15f), null, false),
				new ModelParticle("Fire_Red", 1.0f, null, EntityPart.Self, "Head", new Vector3f(0f, 0.3f, 0f), null, false),
				new ModelParticle("Fire_Red", 1.0f, null, EntityPart.Self, "Head", new Vector3f(-0.1f, 0f, 0f), null, false),
				new ModelParticle("Fire_Red", 1.0f, null, EntityPart.Self, "Head", new Vector3f(0.1f, 0f, 0f), null, false),
		};

		// Tracks which players have already received the particle packet.
		// Particles from SpawnModelParticles are persistent (no remove API exists),
		// so we must only send once per player to avoid stacking.
		private final Set<UUID> sentTo = new HashSet<>();

		// Delay in seconds after reset() before sending particles,
		// giving the client time to load the new Boss model.
		private static final float SEND_DELAY = 1.0f;
		private float delaySinceReset = -1f; // -1 = not waiting

		/**
		 * Call when the boss changes to re-send particles to all players.
		 */
		public void reset() {
			sentTo.clear();
			delaySinceReset = 0f;
		}

		@Override
		public void tick(float delta, int index, @Nonnull Store<EntityStore> store) {
			GameManager gameManager = ChallengerPlugin.get().getGameManager();

			if (gameManager.state == GameState.Waiting || gameManager.state == GameState.Countdown) return;
			if (gameManager.getPlayers().isEmpty()) return;

			World world = store.getExternalData().getWorld();
			if (!world.equals(gameManager.getWorld())) return;

			PlayerRef bossRef = gameManager.getBoss();

			// Only send particles if the boss actually has the Boss role
			PlayerComponent bossPC = gameManager.getPlayerComponent(bossRef);
			if (bossPC == null || bossPC.getRole() != PlayerRole.Boss) return;

			// Wait for client to load the new model before sending particles
			if (delaySinceReset >= 0f && delaySinceReset < SEND_DELAY) {
				delaySinceReset += delta;
				return;
			}
			if (delaySinceReset >= 0f) {
				delaySinceReset = -1f; // done waiting
			}

			Ref<EntityStore> bossEntityRef = bossRef.getReference();
			if (bossEntityRef == null || !bossEntityRef.isValid()) return;

			NetworkId networkId = store.getComponent(bossEntityRef, NetworkId.getComponentType());
			if (networkId == null) return;

			SpawnModelParticles packet = null;

			for (PlayerRef playerRef : gameManager.getPlayers()) {
				// hide the particles from he boss
				if (playerRef.equals(bossRef)) continue;

				if (sentTo.contains(playerRef.getUuid())) continue;

				if (packet == null) {
					packet = new SpawnModelParticles(networkId.getId(), BOSS_PARTICLES);
				}
				playerRef.getPacketHandler().write(packet);
				sentTo.add(playerRef.getUuid());
				ChallengerPlugin.LOGGER.atInfo().log("Sent boss particles to " + playerRef.getUsername() + " (entity " + networkId.getId() + ")");
			}
		}
	}

}
