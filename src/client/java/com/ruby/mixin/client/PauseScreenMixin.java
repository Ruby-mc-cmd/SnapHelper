package com.ruby.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void snaphelper$onInit(CallbackInfo ci) {

        int baseX = this.width / 2 - 100;
        int baseY = this.height / 4 + 120;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("\uE000")
                                .withStyle(Style.EMPTY
                                        .withFont(new FontDescription.Resource(
                                                Identifier.parse("snaphelper:char")


                                        ))),
                        button -> openScreenshotFolder()
                ).bounds(
                        baseX + 200 + 4,
                        baseY - 16,
                        20,
                        20
                ).build()
        );
    }

    private void openScreenshotFolder() {
        Minecraft mc = Minecraft.getInstance();
        File dir = new File(mc.gameDirectory, "screenshots");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        Util.getPlatform().openFile(dir);
    }
}
