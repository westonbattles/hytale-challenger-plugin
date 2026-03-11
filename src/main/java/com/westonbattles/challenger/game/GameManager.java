package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
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
import java.util.Random;
import java.util.UUID;


public class GameManager {

	public GameState state = GameState.Waiting;
	private final List<PlayerRef> players = new ArrayList<>();

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

	/**
	 * Include world so we can get the proper store
	 */
	public void removePlayer(@Nonnull PlayerRef playerRef) {
		// Make sure the player we are trying to remove is actually in the list of players
		if (!players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Cannot remove player \"" + playerRef.getUsername() +"\": not in game");
			return;
		}

		Ref<EntityStore> ref = playerRef.getReference();
		if (ref == null || !ref.isValid()) {
			ChallengerPlugin.LOGGER.atSevere().log("Cannot remove player \"" + playerRef.getUsername() +"\": playerRef.getReference() was null or invalid");
			return;
		}

		ChallengerPlugin.LOGGER.atInfo().log("Removing player '" + playerRef.getUsername() +"' from the game");

		// Remove the player component of the player
		ref.getStore().removeComponentIfExists(ref, ChallengerPlugin.get().getPlayerComponentType());

		players.remove(playerRef);

		// If the player who left is the only one who wasn't ready, the game should be started so we need to preform this check
		// shouldStart() accesses the minigame world's store, so it must run on that world's
		getWorld().execute(() -> {
				if (shouldStart()) startCountdown();
		});
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

	public void startCountdown(){
		state = GameState.Countdown;
		ChallengerPlugin.LOGGER.atInfo().log("STARTING COUNTDOWN");
	}

	public void startGame(){
		state = GameState.Active;
		ChallengerPlugin.LOGGER.atInfo().log("STARTING GAME");
		int boss = new Random().nextInt(players.size());

		for (int i = 0; i < players.size(); i++) {
			if (i == boss) makeBoss(players.get(i));
			else makeChallenger(players.get(i));
		}
	}

	public void makeBoss(@Nonnull PlayerRef playerRef) {
		PlayerComponent pc = ensureAndGetPlayerComponent(playerRef);
		Ref<EntityStore> ref = playerRef.getReference();
		assert ref != null && ref.isValid();
		Store<EntityStore> store = ref.getStore();

		pc.setRole(PlayerRole.Boss);

		String bossModelId = "Boss";
		ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(bossModelId);
		if (modelAsset == null) {
			ChallengerPlugin.LOGGER.atWarning().log(String.format("Could not set boss model for %s: does model '%s' exist?", playerRef.getUsername(), bossModelId));
			return;
		}

		Model model = Model.createScaledModel(modelAsset, 1.0f);
		store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
	}

	private void makeChallenger(@Nonnull PlayerRef playerRef) {
		PlayerComponent pc = ensureAndGetPlayerComponent(playerRef);
		pc.setRole(PlayerRole.Challenger);
	}

	public void resetPlayer(@Nonnull PlayerRef playerRef) {
		PlayerComponent pc = ensureAndGetPlayerComponent(playerRef);
		Ref<EntityStore> ref = playerRef.getReference();
		assert ref != null && ref.isValid();
		Store<EntityStore> store = ref.getStore();

		pc.setRole(PlayerRole.Unassigned);

		PlayerSkinComponent skinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
		if (skinComponent != null) {
			PlayerSkinComponent playerSkinComponent = store.ensureAndGetComponent(ref, PlayerSkinComponent.getComponentType());
			Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
			store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
			playerSkinComponent.setNetworkOutdated();
		}
	}

	@Nullable
	public static PlayerRef getPlayerRef(@Nonnull Player player) {
		Ref<EntityStore> ref = player.getReference();
		if (ref == null || !ref.isValid()) return null;
		return ref.getStore().getComponent(ref, PlayerRef.getComponentType());
	}

	@Nullable
	public PlayerComponent getPlayerComponent(PlayerRef playerRef) {
		if (!getWorld().getWorldConfig().getUuid().equals(playerRef.getWorldUuid())) return null;

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

	@Nonnull
	public PlayerComponent ensureAndGetPlayerComponent(PlayerRef playerRef) {

		assert getWorld().getWorldConfig().getUuid().equals(playerRef.getWorldUuid());

		Ref<EntityStore> ref = playerRef.getReference();
		assert ref != null && ref.isValid();

		return getStore().ensureAndGetComponent(ref, ChallengerPlugin.get().getPlayerComponentType());
	}

	public Store<EntityStore> getStore() {
		return getStoreFromWorld(getWorld());
	}

	public static Store<EntityStore> getStoreFromWorld(World world) {
		Store<EntityStore> store = world.getEntityStore().getStore();
		store.assertThread(); // Store must be called from a world thread
		return store;
	}


	/** Gets a reference to the world the minigame is running in
	 */
	public World getWorld(){
		return Universe.get().getDefaultWorld();
	}

	public List<PlayerRef> getPlayers() {
		return players;
	}

	public void setState(GameState state) {this.state = state;}

}
