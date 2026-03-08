package com.westonbattles.challenger.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.westonbattles.challenger.ChallengerPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListPlayersCommand extends AbstractCommand {

	public ListPlayersCommand(@Nonnull String name, @Nonnull String description) {
		super(name, description);
	}

	@Nullable
	@Override
	protected CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
		List<PlayerRef> players = ChallengerPlugin.get().getGameManager().getPlayers();

		if (players.isEmpty()) {
			ctx.sendMessage(Message.raw("No players in GameManager.players"));
			return CompletableFuture.completedFuture(null);
		}

		ctx.sendMessage(Message.raw("GameManager.players (" + players.size() + "):"));
		for (int i = 0; i < players.size(); i++) {
			PlayerRef ref = players.get(i);
			ctx.sendMessage(Message.raw("  [" + i + "] " + ref.getUsername()));
		}

		return CompletableFuture.completedFuture(null);
	}
}
