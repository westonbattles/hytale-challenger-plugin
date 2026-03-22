package com.westonbattles.challenger.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.game.GameManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bson.types.Code;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BossSelectUI extends InteractiveCustomUIPage<BossSelectUI.Data> {

    private static final String PLAYER_DROPDOWN_ID = "PlayerList";
    private static final String EXIT_BUTTON_ID = "CloseButton";
    private static final String RANDOM_PLAYER_SELECT_BUTTON_ID = "RandomBossSelectButton";

    public BossSelectUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, Data.CODEC);
    }

    // TODO: Update dropdown function that dynamically updates the dropdown for things like a player leaving

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("BossSelect.ui");

        GameManager gameManager = ChallengerPlugin.get().getGameManager();

        List<DropdownEntryInfo> players = new ObjectArrayList<>();

        for (PlayerRef playerRef : gameManager.getPlayers()) {
            players.add(new DropdownEntryInfo(LocalizableString.fromString(playerRef.getUsername()), playerRef.getUuid().toString()));
        }

        uiCommandBuilder.set("#PlayerList.Entries", players);
        uiCommandBuilder.set("#PlayerList.Value", gameManager.getBoss().getUuid().toString());


        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#"+PLAYER_DROPDOWN_ID, EventData.of("@SelectedPlayer", "#"+PLAYER_DROPDOWN_ID+".Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#"+RANDOM_PLAYER_SELECT_BUTTON_ID, EventData.of("ClickedButton", RANDOM_PLAYER_SELECT_BUTTON_ID), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#"+EXIT_BUTTON_ID, EventData.of("ClickedButton", EXIT_BUTTON_ID), false);
    }

    public void updateSelection() {
        String bossUuid = ChallengerPlugin.get().getGameManager().getBoss().getUuid().toString();

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#PlayerList.Value", bossUuid);
        sendUpdate(uiCommandBuilder, false); // false = don't clear existing UI
    }

    public void updateEntries() {
        GameManager gameManager = ChallengerPlugin.get().getGameManager();
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();

        List<DropdownEntryInfo> players = new ObjectArrayList<>();

        for (PlayerRef playerRef : gameManager.getPlayers()) {
            players.add(new DropdownEntryInfo(LocalizableString.fromString(playerRef.getUsername()), playerRef.getUuid().toString()));
        }

        uiCommandBuilder.set("#PlayerList.Entries", players);
        sendUpdate(uiCommandBuilder, false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);


        GameManager gameManager = ChallengerPlugin.get().getGameManager();
        Player player = Objects.requireNonNull(store.getComponent(ref, Player.getComponentType()));

        switch (data.clickedButton) {
            case RANDOM_PLAYER_SELECT_BUTTON_ID -> {
                gameManager.setBossIndex(-1);
                updateSelection();
            }
            case EXIT_BUTTON_ID -> player.getPageManager().setPage(ref, store, Page.None);
        }

        if (data.selectedPlayer != null) {
            gameManager.setBossIndex(UUID.fromString(data.selectedPlayer));
        }
        //sendUpdate();
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ClickedButton", Codec.STRING), (data, s) -> data.clickedButton = s, data -> data.clickedButton).add()
                .append(new KeyedCodec<>("@SelectedPlayer", Codec.STRING), (data, s) -> data.selectedPlayer = s, data -> data.selectedPlayer).add()
                .build();

        private String clickedButton;
        private String selectedPlayer;
    }
}