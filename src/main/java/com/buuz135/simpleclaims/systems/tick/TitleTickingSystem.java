package com.buuz135.simpleclaims.systems.tick;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;
import java.util.HashMap;

public class TitleTickingSystem extends EntityTickingSystem<EntityStore> {

    private HashMap<String, Message> titles;

    public TitleTickingSystem() {
        this.titles = new HashMap<>();
    }

    @Override
    public void tick(float v, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        var chunk = Message.raw("Wilderness").color(Color.GREEN);
        var chunkInfo = ClaimManager.getInstance().getChunkRawCoords(player.getWorld().getName(), (int) playerRef.getTransform().getPosition().getX(), (int) playerRef.getTransform().getPosition().getZ());
        if (chunkInfo != null){
            var party = ClaimManager.getInstance().getPartyById(chunkInfo.getPartyOwner());
            if (party != null) chunk = Message.raw(party.getName()).color(Color.WHITE);
        }
        var current = this.titles.getOrDefault(playerRef.getUuid().toString(), Message.raw(""));
        if (!current.getRawText().equals(chunk.getRawText())) {
            this.titles.put(playerRef.getUuid().toString(), chunk);
            EventTitleUtil.showEventTitleToPlayer(playerRef, chunk, Message.raw("Claims"), false, null, 2, 0.5f, 0.5f);
        }

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
