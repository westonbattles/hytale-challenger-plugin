package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.commands.player.effect.PlayerEffectApplyCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
import java.util.UUID;


public class GameManager {

	public GameState state = GameState.Waiting;
	private final List<PlayerRef> players = new ArrayList<>();

	public void addPlayer(@Nonnull Player player) {
		PlayerRef playerRef = getPlayerRef(player);
		if (playerRef == null) return;
		addPlayer(playerRef);
	}

	public void addPlayer(@Nonnull PlayerRef playerRef) {

		UUID worldUUID = playerRef.getWorldUuid();
		assert worldUUID != null;
		assert playerRef.getWorldUuid().equals(getWorld().getWorldConfig().getUuid());

		// Make sure we aren't adding a player that is already here
		if (players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Cannot add player \"" + playerRef.getUsername() +"\": already in game");
			return;
		}

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Cannot add player \"" + playerRef.getUsername() +"\": playerRef.getReference() was null");
			return;
		}

		// Add playerComponent to player
		PlayerComponent playerComponent = new PlayerComponent();
		// putComponent appears to automatically replace the component if the entity already has it
		getStore().putComponent(ref, ChallengerPlugin.get().getPlayerComponentType(), playerComponent);

		// Add player to player list
		players.add(playerRef);
	}

	public void removePlayer(@Nonnull Player player) {
		PlayerRef playerRef = getPlayerRef(player);
		if (playerRef == null) return;
		removePlayer(playerRef, player.getWorld());
	}

	private void removePlayer(@Nonnull PlayerRef playerRef) {

		UUID worldUUID = playerRef.getWorldUuid();
		assert worldUUID != null;
		assert playerRef.getWorldUuid().equals(getWorld().getWorldConfig().getUuid());

		removePlayer(playerRef, getWorld());
	}

	// Include world so we can get the proper store
	public void removePlayer(@Nonnull PlayerRef playerRef, World world) {
		// Make sure the player we are trying to remove is actually in the list of players
		if (!players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Cannot remove player \"" + playerRef.getUsername() +"\": not in game");
			return;
		}

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Cannot remove player \"" + playerRef.getUsername() +"\": playerRef.getReference() was null");
			return;
		}

		// Remove the player component of the player
		this.getStoreFromWorld(world).removeComponentIfExists(ref, ChallengerPlugin.get().getPlayerComponentType());

		players.remove(playerRef);
	}

	public void removePlayerFromListOnly(@Nonnull PlayerRef playerRef) {
		players.remove(playerRef);
	}

	/**
	 * Loop through all players and check if they are ready
	 * Readiness is determined based on their playerComponent isReady variable. Returns true if 0 players are playing
	 */
	public boolean playersReady(){

		for (PlayerRef playerRef : players) {

			PlayerComponent playerComponent = getPlayerComponent(playerRef);

			// If the playerComponent is null we just remove the player from the list of players idk what would cause this to happen but oh well
			if (playerComponent == null) {
				ChallengerPlugin.LOGGER.atSevere().log(playerRef.getUsername() + " playerComponent is null: Removing them from game");
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
	private PlayerRef getPlayerRef(@Nonnull Player player) {
		return getPlayerRef(player, getWorld());
	}

	@Nullable
	public PlayerRef getPlayerRef(@Nonnull Player player, @Nonnull World world) {
		Ref<EntityStore> ref = player.getReference();
		if (ref == null) return null;
		return getStoreFromWorld(world).getComponent(ref, PlayerRef.getComponentType());
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

	public Store<EntityStore> getStoreFromWorld(World world) {
		return world.getEntityStore().getStore();
	}

	// Gets a reference to the world the minigame is running in
	public World getWorld(){
		return Universe.get().getDefaultWorld();
	}

	@Nullable
	public PlayerRole getRole(PlayerRef playerRef) {

		PlayerComponent playerComponent = getPlayerComponent(playerRef);
		if (playerComponent == null) return null;

		return playerComponent.getRole();

	}

	public List<PlayerRef> getPlayers() {
		return players;
	}

}
