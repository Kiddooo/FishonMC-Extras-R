package dannypx.foe.mixin.inject;

import com.llamalad7.mixinextras.sugar.Local;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.CatchingHandler;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.logic.CrewHandler;
import dannypx.foe.handler.logic.LocationXpHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Unique
    private final List<UUID> uuids = new ArrayList<>();

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void injectHandlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        if (ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.clientPacketListenerMixinHandlePlayerInfoUpdate.get()
        ) {
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
                if (packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                    CrewHandler.instance().onPlayerJoin(entry.profileId());
                }
            }
        }
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void injectHandlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        if (ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.clientPacketListenerMixinHandlePlayerInfoRemove.get()
        ) {
            for (UUID uuid : packet.profileIds()) {
                CrewHandler.instance().onPlayerLeave(uuid);
            }
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void injectPostAddEntitySoundInstance(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket, CallbackInfo ci, @Local Entity entity) {
        if (entity instanceof Display.TextDisplay textDisplay
                && textDisplay.getText().getString().startsWith("CATCH SUMMARY")
                && !uuids.contains(textDisplay.getUUID())
        ) {
            String[] lines = textDisplay.getText().getString().split("\n");

            if (lines.length > 6) {
                Arrays.stream(lines)
                        .filter(line -> {
                            if (line.isEmpty()) return false;
                            char c = line.charAt(line.length() - 1);
                            return c >= 0xE000 && c <= 0xE999;
                        })
                        .findFirst().ifPresent(found -> CatchingHandler.instance().scanFishListener(found));

                uuids.add(textDisplay.getUUID());
            }
        }
    }

    @Inject(method = "handleOpenScreen", at = @At("TAIL"))
    private void foer$locationXpOpenScreen(
            ClientboundOpenScreenPacket packet,
            CallbackInfo ci
    ) {
        LocationXpHandler.instance().onOpenScreen(packet);
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    private void foer$locationXpContainerContent(
            ClientboundContainerSetContentPacket packet,
            CallbackInfo ci
    ) {
        LocationXpHandler.instance().onContainerContent(packet);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void foer$locationXpContainerSlot(
            ClientboundContainerSetSlotPacket packet,
            CallbackInfo ci
    ) {
        LocationXpHandler.instance().onContainerSlot(packet);
    }
}
