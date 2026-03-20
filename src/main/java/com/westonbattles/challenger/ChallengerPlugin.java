package com.westonbattles.challenger;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.commands.*;
import com.westonbattles.challenger.commands.debug.*;
import com.westonbattles.challenger.components.PlayerComponent;
import com.westonbattles.challenger.game.GameManager;
import com.westonbattles.challenger.game.GameSystems;
import com.westonbattles.challenger.listeners.PlayerDisconnectListener;
import com.westonbattles.challenger.listeners.PlayerReadyListener;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class ChallengerPlugin extends JavaPlugin {

    private static ChallengerPlugin instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private GameManager gameManager;

    private ComponentType<EntityStore, PlayerComponent> playerComponent;

    public ChallengerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {

        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.gameManager = new GameManager();

        // Registries
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        //Listeners
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerReadyListener::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerDisconnectListener::onPlayerDisconnect);
        //Commands
        this.getCommandRegistry().registerCommand(new BossUIShowCommand("showbossui", "shows the boss ui"));
        this.getCommandRegistry().registerCommand(new BossUIHideCommand("hidebossui", "hides the boss ui"));
        this.getCommandRegistry().registerCommand(new ListPlayersCommand("listplayers", "Prints the contents of GameManager.players"));
        this.getCommandRegistry().registerCommand(new ReadyCommand("ready", "toggle ready"));
        this.getCommandRegistry().registerCommand(new MakeBossCommand("makeboss", ""));
        this.getCommandRegistry().registerCommand(new ResetPlayerCommand("resetplayer", ""));
        this.getCommandRegistry().registerCommand(new NotificationCommand("notification", ""));
        //Components
        this.playerComponent = entityStoreRegistry.registerComponent(PlayerComponent.class, PlayerComponent::new);
        //Systems
        entityStoreRegistry.registerSystem(new GameSystems.CountdownSystem());

        //Interactions
        //this.getCodecRegistry(Interaction.CODEC).register("template_interaction", TemplateInteraction.class, TemplateInteraction.CODEC);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ComponentType<EntityStore, PlayerComponent> getPlayerComponentType() {
        return playerComponent;
    }
    public static ChallengerPlugin get() {
        return instance;
    }
}