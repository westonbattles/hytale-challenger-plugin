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

public class SpawnDummiesCommand extends AbstractPlayerCommand {

	public SpawnDummiesCommand(@Nonnull String name, @Nonnull String description) {
		super(name, description);
	}

	public final String[] playerNames = {"Waluigi", "pink", "simon", "ir0n", "Archmage"};
	private final double[][] playerSpawns = {
			{67.118, 16.0, 126.067, 100.692, -3.387},
			{60.320, 16.0, 124.238, -174.883, -3.235},
			{67.808, 16.0, 128.307, 83.006, -3.235},
			{64.281, 17.6, 124.573, 165.465, -35.120},
			{62.429, 16.0, 130.53, 42.407, -8.617}
	};

	@Override
	protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
		CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
		for (int i = 0; i < playerNames.length; i++) {
			final int idx = i;
			chain = chain.thenCompose(v -> {
				String command = "dummy create " + playerNames[idx] + " " + playerSpawns[idx][0] + " " + playerSpawns[idx][1] + " " + playerSpawns[idx][2] + " --pitch " + playerSpawns[idx][4]*(Math.PI / 180.0) + " --yaw " + playerSpawns[idx][3]*(Math.PI / 180.0);
				return CommandManager.get().handleCommand(playerRef, command);
			});
		}
	}

}