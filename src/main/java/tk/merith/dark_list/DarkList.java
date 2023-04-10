package tk.merith.dark_list;

import eu.midnightdust.lib.config.MidnightConfig;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.config.QuiltConfig;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.merith.dark_list.config.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DarkList implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("DarkList");

	public static HashMap<String, String> modList = new HashMap<String, String>();
	@Override
	public void onInitialize(ModContainer mod) {
		boolean isServer = false;
		if (MinecraftQuiltLoader.getEnvironmentType().name().equals("SERVER")) {
			isServer = true;
		}
		// create packet identifier
		Identifier packetId = new Identifier("dark_list", "modlist");
		if (!isServer) {
			for (ModContainer modContainer : QuiltLoader.getAllMods()) {
				modList.put(modContainer.metadata().id(), modContainer.metadata().version().toString());
			}
			ClientLoginNetworking.registerGlobalReceiver(packetId, (client, handler, buf, responseSender) -> {
				PacketByteBuf buffer = PacketByteBufs.create();
				buffer.writeMap(modList, PacketByteBuf::writeString, PacketByteBuf::writeString);
				LOGGER.info("DarkList: Sending modlist to server");
				return CompletableFuture.completedFuture(buffer);
			});
		} else {
			// CONF: handle config shit

			MidnightConfig.init("DarkList", Config.class);

			// CONF: break blacklist into hashmap
			HashMap<String, String> blacklist = new HashMap<>();
			 String[] brokenList = Config.modList.split(",");
			for (String item : brokenList) {
				String[] itemSplit = item.split(":");
				if (itemSplit.length < 2) {
					blacklist.put(itemSplit[0], "*");
				} else {
					blacklist.put(itemSplit[0], itemSplit[1]);
				}
			};

			// Print The config


			// handle packet info
			ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
				sender.sendPacket(packetId, PacketByteBufs.empty());
			});
			ServerLoginNetworking.registerGlobalReceiver(packetId, (server, handler, understood, buf, synchronizer, responseSender) -> {
				if (!understood) {
					LOGGER.warn("DarkList: User Connected without DarkList installed");
					if (Config.enforceDarkList) {
						LOGGER.warn("DarkList: Kicking User");
						handler.disconnect(Text.of("Sorry, you need DarkList installed to join this server"));
					}
				} else {
				LOGGER.info("DarkList: Received modlist from client");
				HashMap<String, String> clientModList = new HashMap<String, String>();
				clientModList.putAll(buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString));
				if (Config.debug) {
					LOGGER.info("DarkList: {}", clientModList);
				};

				String NotAllowedMods = "";
				for (Map.Entry<String, String> entry : clientModList.entrySet()) {
					String modid = entry.getKey();
					String version = entry.getValue();
					if (blacklist.containsKey(modid)) {
						NotAllowedMods += modid + ":" + version + ", ";
					}
				}
				if (NotAllowedMods.length() > 0) {
					NotAllowedMods = NotAllowedMods.substring(0, NotAllowedMods.length() - 1);
					LOGGER.info("DarkList: Attempted to join with the following Mods: {})", NotAllowedMods);
					handler.disconnect(Text.of("You are not allowed to join the server with the following Mods: "+NotAllowedMods));
				}
			}
			});
		}
	}
}
