package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class GameManager {

	public GameState state = GameState.Waiting;
	private final List<PlayerRef> players = new ArrayList<>();
	Store<EntityStore> store = GameManager.getWorld().getEntityStore().getStore();

	public void addPlayer(@Nonnull PlayerRef playerRef) {

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Failed adding " + playerRef.getUsername() +" to player list: playerRef.getReference() was null.");
			players.remove(playerRef);
			return;
		}

		// Add playerComponent to player
		PlayerComponent playerComponent = new PlayerComponent();
		// putComponent appears to automatically replace the component if the entity already has it
		store.putComponent(ref, ChallengerPlugin.get().getPlayerComponentType(), playerComponent);

		// Add player to player list
		players.add(playerRef);
	}

	public void removePlayer(@Nonnull PlayerRef playerRef) {

		// Make sure the player we are trying to remove is actually in the list of players
		if (!players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Failed removing " + playerRef.getUsername() +" from player list as they are not in the list.");
			return;
		}

		// Remove the player component of the player

		players.remove(playerRef);
	}


	/**
	 * Loop through all players and check if they are ready
	 *
	 * Readiness is determined based on their playerComponent isReady variable. Returns true if 0 players are playing
	 */
	public boolean playersReady(){

		for (PlayerRef playerRef : players) {
			// Get reference to the player reference (lmao)
			Ref<EntityStore> ref = playerRef.getReference();
			// If the reference is null we just remove the player from the list of players idk what would cause this to happen but oh well
			if (ref == null) {
				ChallengerPlugin.LOGGER.atSevere().log("Failed to get ref for " + playerRef.getUsername() +", removing them from player list...");
				players.remove(playerRef);
				continue;
			}

			// Get the playerComponent of the player (and do similar null checking)
			PlayerComponent playerComponent = store.getComponent(ref, ChallengerPlugin.get().getPlayerComponentType());
			if (playerComponent == null) {
				ChallengerPlugin.LOGGER.atSevere().log("Could not get PlayerComponent for " + playerRef.getUsername() +", removing them from player list...");
				players.remove(playerRef);
				continue;
			}

			// We return false unless every player is ready
			if (!playerComponent.isReady()) return false;
		}

		return true;
	}

	// Transition methods

	public void startGame(){
		state = GameState.Countdown;
	}

	public void EndGame(){
		state = GameState.Concluded;
	}

	// Gets a reference to the world the minigame is running in
	public static World getWorld(){
		return Universe.get().getDefaultWorld();
	}

}
