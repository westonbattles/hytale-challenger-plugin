package com.westonbattles.challenger.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class NotificationCommand extends AbstractCommand {

	public NotificationCommand(@Nonnull String name, @Nonnull String description) {
		super(name, description);
	}

	private final RequiredArg<PlayerRef> playerRefArg = this.withRequiredArg("player", "player to show the notification to", ArgTypes.PLAYER_REF);


	@NullableDecl
	@Override
	protected CompletableFuture<Void> execute(@NonNullDecl CommandContext commandContext) {
		PlayerRef playerRef = playerRefArg.get(commandContext);

		PacketHandler packetHandler = playerRef.getPacketHandler();
		Message primaryMessage = Message.raw("This is the primary message").color("#00FF00");
		Message secondaryMessage = Message.raw("This is the secondary message").color("#228B22");

//        ItemWithAllMetadata iconItem = new ItemStack("Weapon_Sword_Mithril", 1).toPacket();
//        NotificationUtil.sendNotification(packetHandler, primaryMessage, secondaryMessage, iconItem);

		return CompletableFuture.runAsync(() -> {EventTitleUtil.showEventTitleToPlayer(playerRef, primaryMessage, secondaryMessage, true, null, 5, 1, 1);});
	}
}