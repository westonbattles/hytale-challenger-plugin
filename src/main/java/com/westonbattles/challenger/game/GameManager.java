package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.commands.player.effect.PlayerEffectApplyCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class GameManager {

	public GameState state = GameState.Waiting;
	private final List<PlayerRef> players = new ArrayList<>();

	public void addPlayer(@Nonnull PlayerRef playerRef) {

		// Make sure we aren't adding a player that is already here
		if (players.contains(playerRef)) return;

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Failed adding " + playerRef.getUsername() +" to player list: playerRef.getReference() was null.");
			return;
		}

		// Add playerComponent to player
		PlayerComponent playerComponent = new PlayerComponent();
		// putComponent appears to automatically replace the component if the entity already has it
		getStore().putComponent(ref, ChallengerPlugin.get().getPlayerComponentType(), playerComponent);

		// Add player to player list
		players.add(playerRef);
	}

	public void removePlayer(@Nonnull PlayerRef playerRef) {

		// Make sure the player we are trying to remove is actually in the list of players
		if (!players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Failed removing " + playerRef.getUsername() +" from player list as they are not in the list.");
			return;
		}

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Failed removing " + playerRef.getUsername() +" from player list: playerRef.getReference() was null.");
			return;
		}

		// Remove the player component of the player
		this.getStore().removeComponentIfExists(ref, ChallengerPlugin.get().getPlayerComponentType());

		players.remove(playerRef);
	}

	@Nullable
	public PlayerRole getRole(PlayerRef playerRef) {

		PlayerComponent playerComponent = getPlayerComponent(playerRef);
		if (playerComponent == null) return null;

		return playerComponent.getRole();

	}

	/**
	 * Loop through all players and check if they are ready
	 *
	 * Readiness is determined based on their playerComponent isReady variable. Returns true if 0 players are playing
	 */
	public boolean playersReady(){

		for (PlayerRef playerRef : players) {

			PlayerComponent playerComponent = getPlayerComponent(playerRef);

			// If the playerComponent is null we just remove the player from the list of players idk what would cause this to happen but oh well
			if (playerComponent == null) {
				ChallengerPlugin.LOGGER.atSevere().log(playerRef.getUsername() + " playerComponent is null: Removing them from player list");
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

	@Nullable
	private PlayerComponent getPlayerComponent(PlayerRef playerRef) {
		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Failed getting ref for " + playerRef.getUsername());
			return null;
		}

		PlayerComponent playerComponent = getStore().getComponent(ref, ChallengerPlugin.get().getPlayerComponentType());
		if (playerComponent == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Failed getting PlayerComponent for " + playerRef.getUsername());
			players.remove(playerRef);
			return null;
		}
		return playerComponent;
	}

	// Helpful Getters
	public Store<EntityStore> getStore() {
		return getWorld().getEntityStore().getStore();
	}
	// Gets a reference to the world the minigame is running in
	public World getWorld(){
		return Universe.get().getDefaultWorld();
	}

}
