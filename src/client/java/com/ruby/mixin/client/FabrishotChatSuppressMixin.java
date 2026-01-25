package com.ruby.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;

/**
 * Fabrishot 1.21.1 対応版
 * パッケージ: me.ramidzkh.fabrishot
 * ターゲットメソッド: printFileLink
 */
@Pseudo
@Mixin(targets = "me.ramidzkh.fabrishot.Fabrishot", remap = false)
public class FabrishotChatSuppressMixin {

    /**
     * printFileLink メソッドの実行を HEAD でキャンセルすることで、
     * チャットへのメッセージ追加を完全に阻止します。
     */
    @Inject(
            method = "printFileLink(Ljava/nio/file/Path;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onPrintFileLink(Path path, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
                    File dir = new File(mc.gameDirectory, "screenshots");
                    File latest = path.toFile();

                    MutableComponent line =
                            Component.translatable("snaphelper.screenshot.saved")
                                    .withStyle(Style.EMPTY
                                            .withBold(true)
                                    )
                                    .append(Component.literal("\n")
                                    );

                    if (latest != null) {

                        // ファイル名
                        line.append(
                                Component.literal("> " + latest.getName() + "\n")
                                        .withStyle(Style.EMPTY
                                                .withBold(false)
                                                .withUnderlined(false)
                                                .withColor(ChatFormatting.GRAY)
                                        )
                        );

                        // アイコン
                        line.append(
                                Component.literal("\uE000")
                                        .withStyle(Style.EMPTY
                                                .withFont(new FontDescription.Resource(
                                                                Identifier.parse("snaphelper:char")
                                                        )
                                                )
                                                .withUnderlined(true)
                                                .withBold(false)
                                                .withClickEvent(
                                                        new ClickEvent.OpenFile(latest)
                                                )
                                        )
                        );

                        // [ Open ]
                        line.append(
                                Component.literal("[Open]")
                                        .withStyle(Style.EMPTY
                                                .withColor(TextColor.fromRgb(0xFF69B4))
                                                .withUnderlined(true)
                                                .withBold(true)
                                                .withClickEvent(
                                                        new ClickEvent.OpenFile(latest)
                                                )
                                                .withHoverEvent(
                                                        new HoverEvent.ShowText(
                                                                Component.translatable("snaphelper.hovertext.open")
                                                        )
                                                )
                                        )
                        );

                        // [ File ]
                        line.append(
                                Component.literal("[File]")
                                        .withStyle(Style.EMPTY
                                                .withColor(ChatFormatting.AQUA)
                                                .withUnderlined(true)
                                                .withBold(true)
                                                .withClickEvent(
                                                        new ClickEvent.OpenFile(latest.getParent())
                                                )
                                                .withHoverEvent(
                                                        new HoverEvent.ShowText(
                                                                Component.translatable("snaphelper.hovertext.file")
                                                        )
                                                )
                                        )
                        );

                        // [ Copy ]
                        line.append(
                                Component.literal("[Copy]")
                                        .withStyle(Style.EMPTY
                                                .withColor(ChatFormatting.YELLOW)
                                                .withUnderlined(true)
                                                .withBold(true)
                                                .withClickEvent(
                                                        new ClickEvent.RunCommand(
                                                                "/snaphelper:copy_image " + latest.getAbsolutePath()
                                                        )
                                                )
                                                .withHoverEvent(
                                                        new HoverEvent.ShowText(
                                                                Component.translatable("snaphelper.hovertext.copy")
                                                        )
                                                )
                                        )
                        );

                        // [ Path ]
                        line.append(
                                Component.literal("[Path]")
                                        .withStyle(Style.EMPTY
                                                .withColor(ChatFormatting.GREEN)
                                                .withUnderlined(true)
                                                .withBold(true)
                                                .withClickEvent(
                                                        new ClickEvent.CopyToClipboard(
                                                                latest.getAbsolutePath()
                                                        )
                                                )
                                                .withHoverEvent(
                                                        new HoverEvent.ShowText(
                                                                Component.translatable("snaphelper.hovertext.path")
                                                        )
                                                )
                                        )
                        );
                    }
                    mc.gui.getChat().addMessage(line);
                }
        );
        ci.cancel();
    }
}