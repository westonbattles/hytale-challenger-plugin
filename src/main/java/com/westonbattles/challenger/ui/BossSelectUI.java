package com.westonbattles.challenger.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Objects;

public class BossSelectUI extends InteractiveCustomUIPage<BossSelectUI.Data> {

    private static final String P1_SELECT_BUTTON_ID = "Player1BossSelectButton";
    private static final String P2_SELECT_BUTTON_ID = "Player2BossSelectButton";
    private static final String RANDOM_PLAYER_SELECT_BUTTON_ID = "RandomBossSelectButton";

    public BossSelectUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, Data.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("BossSelect.ui");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#"+P1_SELECT_BUTTON_ID, EventData.of("ClickedButton", P1_SELECT_BUTTON_ID), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#"+P2_SELECT_BUTTON_ID, EventData.of("ClickedButton", P2_SELECT_BUTTON_ID), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#"+RANDOM_PLAYER_SELECT_BUTTON_ID, EventData.of("ClickedButton", RANDOM_PLAYER_SELECT_BUTTON_ID), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        Player player = Objects.requireNonNull(store.getComponent(ref, Player.getComponentType()));
		switch (data.clickedButton) {
			case P1_SELECT_BUTTON_ID -> player.sendMessage(Message.raw("TODO: Set boss to player 1"));
			case P2_SELECT_BUTTON_ID -> player.sendMessage(Message.raw("TODO: Set boss to player 2"));
			case RANDOM_PLAYER_SELECT_BUTTON_ID -> player.sendMessage(Message.raw("TODO: Set boss to random player"));
		}
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ClickedButton", Codec.STRING), (data, s) -> data.clickedButton = s, data -> data.clickedButton).add()
                .build();

        private String clickedButton;
    }
}