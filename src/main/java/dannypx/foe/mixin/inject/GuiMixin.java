package dannypx.foe.mixin.inject;

import dannypx.foe.handler.fetch.TitleHandler;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.handler.logic.XpHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import dannypx.foe.config.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void injectSetTitle(Component title, CallbackInfo ci) {
        if(LoadingHandler.instance().isLoadingDone()
                && Configs.mixinConfig.guiMixinSetTitle.get()
        ) {
            TitleHandler.instance().setTitle(title);
        }
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void injectSetSubtitle(Component subtitle, CallbackInfo ci) {
        if(LoadingHandler.instance().isLoadingDone()
                && Configs.mixinConfig.guiMixinSetSubtitle.get()
        ) {
            TitleHandler.instance().setSubTitle(subtitle);
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"),  cancellable = true)
    private void injectRenderSelectedItemName(GuiGraphics guiGraphics, CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.guiMixinRenderSelectedItemName.get()
        ) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
    private void injectRenderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.guiMixinRenderScoreBoardSidebar.get()
        ) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void injectRenderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.guiMixinRenderItemHotbar.get()
        ) {
            ci.cancel();
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void foer$captureXp(Component message, boolean tinted, CallbackInfo ci) {
        if (ConnectionHandler.instance().isOnServer() & Configs.mainConfig.enableMod.get()) {
            XpHandler.instance().onOverlay(message);
        }
    }
}
