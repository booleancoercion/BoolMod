package dev.boolco.boolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.pb4.stylednicknames.NicknameHolder;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Comparator;
import java.util.List;

public class BoolMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("boolmod");

    @Override
    public void onInitialize() {
        boolean isStyledNicknamesLoaded = FabricLoader.getInstance().isModLoaded("styled-nicknames");

        CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("list").then(literal("json")
                        .executes(context -> {
                            MinecraftServer server = context.getSource().getServer();

                            JsonObject obj = new JsonObject();
                            obj.addProperty("current_players", server.getCurrentPlayerCount());
                            obj.addProperty("max_players", server.getMaxPlayerCount());

                            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                            players.sort(Comparator.comparing(player -> player.getName().getString()));

                            JsonArray arr = new JsonArray();
                            for (ServerPlayerEntity player : players) {
                                JsonObject playerObject = new JsonObject();
                                playerObject.addProperty("name", player.getName().getString());
                                if (isStyledNicknamesLoaded) {
                                    playerObject.addProperty("nickname", NicknameHolder.of(player).styledNicknames$get());
                                    playerObject.add("nickname_styled", Text.Serializer.toJsonTree(NicknameHolder.of(player).styledNicknames$getOutput()));
                                }
                                playerObject.addProperty("uuid", player.getUuidAsString());

                                arr.add(playerObject);
                            }

                            obj.add("list", arr);

                            context.getSource().sendMessage(Text.literal(obj.toString()));

                            return 1;
                        }))));
    }
}
