package com.ruby.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotRecorderMixin {

	@ModifyVariable(
			method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private static Consumer<Component> wrapConsumer(Consumer<Component> original) {
		return component -> {
			Minecraft mc = Minecraft.getInstance();

			mc.execute(() -> {
				File dir = new File(mc.gameDirectory, "screenshots");
				File latest = findLatestScreenshot(dir);

				// ベース文
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
			});
		};
	}

	private static File findLatestScreenshot(File dir) {
		File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
		if (files == null || files.length == 0) return null;

		File latest = files[0];
		for (File f : files) {
			if (f.lastModified() > latest.lastModified()) {
				latest = f;
			}
		}
		return latest;
	}
}
