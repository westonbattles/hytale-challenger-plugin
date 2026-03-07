package com.westonbattles.challenger;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.commands.HideBossUICommand;
import com.westonbattles.challenger.commands.ShowBossUICommand;
import com.westonbattles.challenger.components.PlayerComponent;
import com.westonbattles.challenger.events.OpenGuiListener;
import com.westonbattles.challenger.events.TestEvent;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class ChallengerPlugin extends JavaPlugin {

    private static ChallengerPlugin instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType<EntityStore, PlayerComponent> playerComponent;

    public ChallengerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        //Events
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, OpenGuiListener::openGui);
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, TestEvent::onPlayerReady);
        //Commands
        this.getCommandRegistry().registerCommand(new ShowBossUICommand("showbossui", "shows the boss ui"));
        this.getCommandRegistry().registerCommand(new HideBossUICommand("hidebossui", "hides the boss ui"));
        //Components
        this.playerComponent = this.getEntityStoreRegistry().registerComponent(PlayerComponent.class, PlayerComponent::new);
        //Interactions
        //this.getCodecRegistry(Interaction.CODEC).register("template_interaction", TemplateInteraction.class, TemplateInteraction.CODEC);
    }

    public ComponentType<EntityStore, PlayerComponent> getPlayerComponentType() {
        return playerComponent;
    }
    public static ChallengerPlugin get() {
        return instance;
    }
}