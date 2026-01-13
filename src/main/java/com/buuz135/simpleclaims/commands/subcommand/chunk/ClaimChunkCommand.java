package com.buuz135.simpleclaims.commands.subcommand.chunk;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.tracking.ModifiedTracking;
import com.buuz135.simpleclaims.commands.CommandMessages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class ClaimChunkCommand extends AbstractAsyncCommand {

    public ClaimChunkCommand() {
        super("claim", "Claims the chunk where you are");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        CommandSender sender = commandContext.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                CompletableFuture<Void> future = new CompletableFuture<>();
                world.execute(() -> {
                    try {
                        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                        if (playerRef == null) {
                            commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                            future.complete(null);
                            return;
                        }
                        if (!ClaimManager.getInstance().canClaimInDimension(world)) {
                            player.sendMessage(CommandMessages.CANT_CLAIM_IN_THIS_DIMENSION);
                            future.complete(null);
                            return;
                        }
                        var party = ClaimManager.getInstance().getPartyFromPlayer(playerRef.getUuid());
                        if (party == null) {
                            party = ClaimManager.getInstance().createParty(player, playerRef);
                            player.sendMessage(CommandMessages.PARTY_CREATED);
                        }
                        var chunk = ClaimManager.getInstance().getChunkRawCoords(player.getWorld().getName(), (int) playerRef.getTransform().getPosition().getX(), (int) playerRef.getTransform().getPosition().getZ());
                        if (chunk != null) {
                            player.sendMessage(chunk.getPartyOwner().equals(party.getId()) ? CommandMessages.ALREADY_CLAIMED_BY_YOU : CommandMessages.ALREADY_CLAIMED_BY_ANOTHER_PLAYER);
                            future.complete(null);
                            return;
                        }
                        if (!ClaimManager.getInstance().hasEnoughClaimsLeft(party)) {
                            player.sendMessage(CommandMessages.NOT_ENOUGH_CHUNKS);
                            future.complete(null);
                            return;
                        }
                        ClaimManager.getInstance().claimChunkByRawCoords(player.getWorld().getName(), (int) playerRef.getTransform().getPosition().getX(), (int) playerRef.getTransform().getPosition().getZ(), party, player, playerRef);
                        player.sendMessage(CommandMessages.CLAIMED);
                        future.complete(null);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
                return future;
            } else {
                commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
