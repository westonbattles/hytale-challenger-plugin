package com.westonbattles.challenger.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ReadyDummiesCommand extends AbstractPlayerCommand {

	public final String[] playerNames = {"Waluigi", "pink", "simon", "ir0n", "Archmage"};

	public ReadyDummiesCommand(@Nonnull String name, @Nonnull String description) {
		super(name, description);
	}

	@Override
	protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
		CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
		for (int i = 0; i < playerNames.length; i++) {
			final int idx = i;
			chain = chain.thenCompose(v -> {
				String command = "dummy chat " + playerNames[idx] + " /ready";
				return CommandManager.get().handleCommand(playerRef, command);
			});
		}
	}

}