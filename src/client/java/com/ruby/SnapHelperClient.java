package com.ruby;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

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

		boolean success;

		// ================================
		// OS 判定
		// ================================
		if (isWindows()) {
			success = copyImageWindows(image);
		} else if (isMac()) {
			success = copyImageMac(image);
		} else if (isLinux()) {
			success = copyImageLinux(image);
		} else {
			mc.gui.getChat().addMessage(Component.literal("❌ 未対応のOSです"));
			return 0;
		}

		if (!success) {
			mc.gui.getChat().addMessage(Component.literal("❌ クリップボードへのコピーに失敗しました"));
			return 0;
		}

		return 1;
	}

	private static boolean isWindows() {
		return osName().contains("win");
	}

	private static boolean isMac() {
		return osName().contains("mac");
	}

	private static boolean isLinux() {
		return osName().contains("nux") || osName().contains("linux");
	}

	private static String osName() {
		return System.getProperty("os.name").toLowerCase(Locale.ROOT);
	}

	// =========================
	// Windows
	// =========================
	private static boolean copyImageWindows(File image) {
		try {
			Process process = new ProcessBuilder(
					"powershell",
					"-NoProfile",
					"-Command",
					"Set-Clipboard",
					"-Path",
					image.getAbsolutePath()
			).start();

			return process.waitFor() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =========================
	// macOS
	// =========================
	private static boolean copyImageMac(File image) {
		try {
			String path = image.getAbsolutePath().replace("\"", "\\\"");

			Process process = new ProcessBuilder(
					"osascript",
					"-e",
					"set the clipboard to (read (POSIX file \"" + path + "\") as TIFF)"
			).start();

			return process.waitFor() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =========================
	// Linux
	// =========================
	private static boolean copyImageLinux(File image) {
		try {
			if (System.getenv("WAYLAND_DISPLAY") != null) {
				return runProcess(
						"wl-copy",
						"--type",
						"image/png",
						image.getAbsolutePath()
				);
			}

			if (System.getenv("DISPLAY") != null) {
				return runProcess(
						"xclip",
						"-selection",
						"clipboard",
						"-t",
						"image/png",
						"-i",
						image.getAbsolutePath()
				);
			}

			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean runProcess(String... command) throws Exception {
		Process process = new ProcessBuilder(command).start();
		return process.waitFor() == 0;
	}
}
