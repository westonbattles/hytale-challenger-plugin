package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;
import com.westonbattles.challenger.ui.BossSelectUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.foreign.Arena;
import java.net.spi.InetAddressResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class GameManager {

	public GameState state = GameState.Waiting;
	private final List<PlayerRef> players = new ArrayList<>();
	private int bossIndex = 0;

	private static double[] LOBBY_SPAWN_POS = {7.5, 16.0, 188.5};
	private static double[] ARENA_CENTER_POS = {7.5, 5.0, 127.5};

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

		Player player = getStore().getComponent(ref, Player.getComponentType());
		if (player == null) return;

		Inventory inventory = player.getInventory();
		inventory.clear();
		ItemStack settingsItem = new ItemStack("settings");
		inventory.getHotbar().setItemStackForSlot((short) 8, settingsItem);



		// Refresh all the currently open boss select ui so we can add player to boss select lists of all lists that are currently opennnnenenenen
		getWorld().execute(this::updateAllBossSelectUIs);
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

		// if the boss left we gotta find a different boss chat
		if (bossIndex == players.size()) bossIndex = 0;

		// If the player who left is the only one who wasn't ready, the game should be started so we need to preform this check
		// shouldStart() accesses the minigame world's store, so it must run on that world's
		getWorld().execute(() -> {
				updateAllBossSelectUIs();
				if (shouldStart()) startCountdown();
		});
	}

	public boolean removePlayerFromListOnly(@Nonnull PlayerRef playerRef) {
		if (bossIndex == players.size()-1) bossIndex = 0;
		getWorld().execute(this::updateAllBossSelectUIs);
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

		Vector3d arenaCenter = new Vector3d(ARENA_CENTER_POS[0], ARENA_CENTER_POS[1], ARENA_CENTER_POS[2]);

		int distanceFromBoss = 7;
		int spawnIndex = 0;
		double angleDifference = (2*Math.PI) / (players.size()-1);

		for (int i = 0; i < players.size(); i++) {

			// Clear inventory
			Ref<EntityStore> ref = players.get(i).getReference();
			assert ref != null && ref.isValid();
			Player player = getStore().getComponent(ref, Player.getComponentType());
			assert player != null;
			Inventory inventory = player.getInventory();
			inventory.clear();

			if (i == bossIndex) {
				makeBoss(players.get(i));

				// Teleport boss to center of arena
				Vector3f rot =  new Vector3f(0, 0, 0);
				Teleport teleport = Teleport.createForPlayer(arenaCenter, rot);
				ref.getStore().addComponent(ref, Teleport.getComponentType(), teleport);
			}
			else {
				makeChallenger(players.get(i));

				// Teleport players in a ring around the boss
				Vector3d spawnPosition = new Vector3d(
						distanceFromBoss * Math.cos(angleDifference * spawnIndex),
						0.0,
						distanceFromBoss * Math.sin(angleDifference * spawnIndex)
				);
				spawnPosition.add(arenaCenter); // Translate spawn positions to be around the boss

				Vector3f rot =  new Vector3f(0, 0, 0); // prob not hard to figure out
				Teleport teleport = Teleport.createForPlayer(spawnPosition, rot);
				ref.getStore().addComponent(ref, Teleport.getComponentType(), teleport);


				// Update index
				spawnIndex += 1;
			}
		}
	}

	public void concludeGame() {
		state = GameState.Concluded;
	}

	public void endGame() {
		state = GameState.Waiting;
		for (PlayerRef playerRef : players) {
			// Reset player role to unassigned (and reset models and items)
			resetPlayer(playerRef);

			// Get reference
			Ref<EntityStore> ref = playerRef.getReference();
			if (ref == null || !ref.isValid()) continue;

			// Teleport player
			Vector3d transform = new Vector3d(LOBBY_SPAWN_POS[0], LOBBY_SPAWN_POS[1], LOBBY_SPAWN_POS[2]);
			Vector3f rot =  new Vector3f(0, 0, 0);
			Teleport teleport = Teleport.createForPlayer(transform, rot);
			ref.getStore().addComponent(ref, Teleport.getComponentType(), teleport);
		}
	}

	public void makeBoss() {
		makeBoss(players.get(bossIndex));
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

		// Reset after model is applied so particles get sent on next tick
		ChallengerPlugin.get().getBossParticleSystem().reset();
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

		// reset player model if player was boss
		if (pc.getRole() == PlayerRole.Boss) {
			PlayerSkinComponent skinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
			if (skinComponent != null) {
				PlayerSkinComponent playerSkinComponent = store.ensureAndGetComponent(ref, PlayerSkinComponent.getComponentType());
				Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
				store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
				playerSkinComponent.setNetworkOutdated();
			}
		}

		pc.setRole(PlayerRole.Unassigned);
		if (pc.isReady()) pc.toggleReady();

		// Reset inventory to spawn inventory (settings cog)
		Player player = ref.getStore().getComponent(ref, Player.getComponentType());
		if (player == null) return;
		Inventory inventory = player.getInventory();
		inventory.clear();
		ItemStack settingsItem = new ItemStack("settings");
		inventory.getHotbar().setItemStackForSlot((short) 8, settingsItem);
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

	public PlayerRef getBoss() {
		return players.get(bossIndex);
	}

	public int getBossIndex() { return bossIndex; }

	public void setBossIndex (int index) {
		assert -1 <= index && index < players.size();

		if (index == -1) {
			Random rand = new Random();
			this.bossIndex = rand.nextInt(players.size());
		}
		else {
			this.bossIndex = index;
		}
	}

	public void setBossIndex (UUID uuid) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUuid().equals(uuid)) {
				bossIndex = i;
				return;
			}
		}
		ChallengerPlugin.LOGGER.atSevere().log("Could not set boss index: Player not in player list!");
	}

	public void setBossIndex (@Nonnull PlayerRef playerRef) {

		if (!players.contains(playerRef)) {
			ChallengerPlugin.LOGGER.atWarning().log("Cannot set boss index for player \"" + playerRef.getUsername() +"\": not in game");
			return;
		}

		bossIndex = players.indexOf(playerRef);

	}

	public void updateAllBossSelectUIs() {
		for (PlayerRef playerRef : players) {
			Ref<EntityStore> ref = playerRef.getReference();
			if (ref == null) continue;
			Player player = getStore().getComponent(ref, Player.getComponentType());
			if (player == null) continue;

			CustomUIPage page = player.getPageManager().getCustomPage();
			if (page == null) continue; // player doesn't have ui open i think
			if (page.getClass() == BossSelectUI.class) {
				BossSelectUI bossSelectUI = (BossSelectUI) page;
				bossSelectUI.updateSelection();
				bossSelectUI.updateEntries();
			}
		}
	}

	public void setState(GameState state) {this.state = state;}

}
