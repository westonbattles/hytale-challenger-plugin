package com.westonbattles.challenger.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;
import com.westonbattles.challenger.game.GameManager;

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
		GameManager gm = ChallengerPlugin.get().getGameManager();
		List<PlayerRef> players = gm.getPlayers();

		if (players.isEmpty()) {
			ctx.sendMessage(Message.raw("No players in GameManager.players"));
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.runAsync(() -> {
			ctx.sendMessage(Message.raw("GameManager.players (" + players.size() + "):"));
			for (int i = 0; i < players.size(); i++) {
				PlayerRef playerRef = players.get(i);
				PlayerComponent pc = gm.getPlayerComponent(playerRef);

				if (pc == null) continue;

				ctx.sendMessage(Message.raw(String.format("  [%d] %-20s | Role: %-12s | Ready: %s", i, playerRef.getUsername(), pc.getRole(), pc.isReady())));
			}
		}, gm.getWorld());
	}
}
