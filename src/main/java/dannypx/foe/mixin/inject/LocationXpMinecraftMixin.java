package dannypx.foe.mixin.inject;

import dannypx.foe.handler.logic.LocationXpHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class LocationXpMinecraftMixin {

    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void foer$hideAutomaticLocationXpMenu(
            Screen nextScreen,
            CallbackInfo ci
    ) {
        if (LocationXpHandler.instance()
                .shouldHideScreen(nextScreen)) {

            ci.cancel();
        }
    }
}