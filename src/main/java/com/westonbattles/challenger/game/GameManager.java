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
			ChallengerPlugin.LOGGER.atWarning().log("Cannot add player '" + playerRef.getUsername() +"': already in game");
			return;
		}

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null) {
			ChallengerPlugin.LOGGER.atSevere().log("Cannot add player '" + playerRef.getUsername() +"': playerRef.getReference() was null");
			return;
		}

		// Add playerComponent to player
		PlayerComponent playerComponent = new PlayerComponent();
		// putComponent appears to automatically replace the component if the entity already has it
		getStore().putComponent(ref, ChallengerPlugin.get().getPlayerComponentType(), playerComponent);

		ChallengerPlugin.LOGGER.atInfo().log("Adding player '" + playerRef.getUsername() +"' to the game");

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

		ChallengerPlugin.LOGGER.atInfo().log("Removing player '" + playerRef.getUsername() +"' from the game");

		// Remove the player component of the player
		this.getStoreFromWorld(world).removeComponentIfExists(ref, ChallengerPlugin.get().getPlayerComponentType());

		players.remove(playerRef);

		// If the player who left is the only one who wasn't ready, the game should be started so we need to preform this check
		if (shouldStart()) startGame();
	}

	public boolean removePlayerFromListOnly(@Nonnull PlayerRef playerRef) {
		return players.remove(playerRef); // safe if playerRef not in players
	}

	/**
	 * Loop through all players and check if they are ready
	 * Readiness is determined based on their playerComponent isReady variable. Returns true if 0 players are playing
	 */
	public boolean playersReady(){

		for (PlayerRef playerRef : players) {

			// If playerComponent doesn't exist for this player for whatever reason, this function will ad it
			PlayerComponent playerComponent = ensureAndGetPlayerComponent(playerRef);

			// We return false unless every player is ready
			if (!playerComponent.isReady()) return false;
		}

		return true;
	}

	public boolean shouldStart() {
		return state == GameState.Waiting && players.size() >= 2 && playersReady();
	}

	// Transition methods
	public void startGame(){
		state = GameState.Countdown;
		ChallengerPlugin.LOGGER.atInfo().log("STARTING GAME");
	}

	public void EndGame(){
		state = GameState.Waiting;
		ChallengerPlugin.LOGGER.atInfo().log("ENDING GAME");
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
	public PlayerComponent getPlayerComponentOrNull(PlayerRef playerRef) {

		assert getWorld().getWorldConfig().getUuid().equals(playerRef.getWorldUuid());

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

	public PlayerComponent ensureAndGetPlayerComponent(PlayerRef playerRef) {

		assert getWorld().getWorldConfig().getUuid().equals(playerRef.getWorldUuid());

		Ref<EntityStore> ref = playerRef.getReference();
		assert ref != null;

		return getStore().ensureAndGetComponent(ref, ChallengerPlugin.get().getPlayerComponentType());
	}

	// Helpful Getters
	public Store<EntityStore> getStore() {
		return getWorld().getEntityStore().getStore();
	}

	public Store<EntityStore> getStoreFromWorld(World world) {
		Store<EntityStore> store = world.getEntityStore().getStore();
		store.assertThread(); // Store must be called from a world thread
		return store;
	}

	// Gets a reference to the world the minigame is running in

	public World getWorld(){
		return Universe.get().getDefaultWorld();
	}

	public List<PlayerRef> getPlayers() {
		return players;
	}

}
