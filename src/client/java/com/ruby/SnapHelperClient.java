package com.ruby;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ruby.mixin.client.ImageSelection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SnapHelperClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					literal("snaphelper:copy_image")
							.then(argument("path", StringArgumentType.greedyString())
									.executes(this::copyScreenshotByPath)
							)
			);
		});
	}

	private int copyScreenshotByPath(CommandContext<?> ctx) {
		Minecraft mc = Minecraft.getInstance();

		String rawPath = StringArgumentType.getString(ctx, "path");

		File image = new File(rawPath).getAbsoluteFile();

		// ================================
		// ファイル判定
		// ================================
		if (!image.exists() || !image.isFile()) {
			mc.gui.getChat().addMessage(Component.literal("❌ ファイルが存在しません"));
			return 0;
		}

		try {
			// ================================
			// canonical化
			// ================================
			File canonicalImage = image.getCanonicalFile();
			File screenshotsDir = new File(mc.gameDirectory, "screenshots").getCanonicalFile();

			// ================================
			// screenshots配下のみ許可
			// ================================
			if (!canonicalImage.toPath().startsWith(screenshotsDir.toPath())) {
				mc.gui.getChat().addMessage(Component.literal("❌ screenshots フォルダ配下のみ使用できます"));
				return 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
			mc.gui.getChat().addMessage(Component.literal("❌ パス判定でエラーが発生しました"));
			return 0;
		}

		// ================================
		// 拡張子チェック
		// ================================
		String lower = image.getName().toLowerCase(Locale.ROOT);
		if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
			mc.gui.getChat().addMessage(Component.literal("❌ 対応形式は png / jpg のみです"));
			return 0;
		}

        // ================================
        // 処理を共通化
        // ================================
        if (copyImageNative(image)) {
            return 1;
        } else {
            mc.gui.getChat().addMessage(Component.literal("❌ クリップボードへのコピーに失敗しました"));
            return 0;
        }
    }

    // ================================
    // AWT APIを使用して画像をクリップボードにコピー
    // ================================
    private boolean copyImageNative(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return false;

            ImageSelection selection = new ImageSelection(image);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
