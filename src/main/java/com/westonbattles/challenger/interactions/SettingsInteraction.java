package com.westonbattles.challenger.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SimpleInteraction;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.ui.BossSelectUI;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SettingsInteraction extends SimpleInstantInteraction {

	public static final BuilderCodec<SettingsInteraction> CODEC = BuilderCodec.builder(
			SettingsInteraction.class, SettingsInteraction::new, SimpleInstantInteraction.CODEC
	).build();

	@Override
	protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {

		CommandBuffer<EntityStore> commandBuffer= interactionContext.getCommandBuffer();

		assert commandBuffer != null;

		Ref<EntityStore> ref = interactionContext.getEntity();
		Player player = commandBuffer.getComponent(ref, Player.getComponentType());
		PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());

		assert player != null;
		assert playerRef != null;

		player.getPageManager().openCustomPage(ref, commandBuffer.getStore(), new BossSelectUI(playerRef, CustomPageLifetime.CanDismiss));


		//Universe.get().sendMessage(Message.raw("INTERACTION"));
		ChallengerPlugin.LOGGER.atInfo().log(String.valueOf(interactionType));

	}
}
