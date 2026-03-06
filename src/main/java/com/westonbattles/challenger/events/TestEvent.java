package com.westonbattles.challenger.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

public class TestEvent {

    /*
     Event Names:
     PlayerReadyEvent - fired when player is done loading into world
     PlayerMouseButtonEvent (currently unusable without visible mouse cursor)
     PlayerMouseMotionEvent (currently unusable)
    */

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Message.raw(player.getDisplayName() + " has joined the world"));
    }
}
