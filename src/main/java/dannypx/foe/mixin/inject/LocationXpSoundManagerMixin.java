package dannypx.foe.mixin.inject;

import dannypx.foe.handler.logic.LocationXpHandler;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public abstract class LocationXpSoundManagerMixin {

    @Inject(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void foer$muteAutomaticCompendium(
            SoundInstance sound,
            CallbackInfoReturnable<SoundEngine.PlayResult> cir
    ) {
        if (LocationXpHandler.instance()
                .shouldMuteSound(sound)) {

            cir.setReturnValue(
                    SoundEngine.PlayResult.NOT_STARTED
            );
        }
    }
}