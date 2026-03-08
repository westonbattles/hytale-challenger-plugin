package com.westonbattles.challenger.game;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.westonbattles.challenger.ChallengerPlugin;
import com.westonbattles.challenger.components.PlayerComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GameSystems {

	public static class CheckGameStartSystem extends RefChangeSystem<EntityStore, PlayerComponent> {

		@Nullable
		@Override
		public Query<EntityStore> getQuery() {
			return ChallengerPlugin.get().getPlayerComponentType();
		}

		@Nonnull
		@Override
		public ComponentType<EntityStore, PlayerComponent> componentType() {
			return ChallengerPlugin.get().getPlayerComponentType();
		}

		@Override
		public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerComponent playerComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
		}

		@Override
		public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PlayerComponent playerComponent, @Nonnull PlayerComponent t1, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
		}

		@Override
		public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerComponent playerComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
		}


	}

}
