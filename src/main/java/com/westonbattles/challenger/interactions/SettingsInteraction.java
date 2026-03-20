package com.westonbattles.challenger.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SimpleInteraction;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SettingsInteraction extends SimpleInstantInteraction {

	public static final BuilderCodec<SettingsInteraction> CODEC = BuilderCodec.builder(
			SettingsInteraction.class, SettingsInteraction::new, SimpleInstantInteraction.CODEC
	).build();

	@Override
	protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
		CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "notification wisped");
	}
}
