package dev.boolco.boolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static net.minecraft.server.command.CommandManager.*;

import java.util.List;

public class BoolMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("boolmod");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT
                .register((dispatcher, dedicated) -> dispatcher.register(literal("list").then(literal("json")
                        .executes(context -> {
                            MinecraftServer server = context.getSource().getServer();

                            JsonObject obj = new JsonObject();
                            obj.addProperty("current_players", server.getCurrentPlayerCount());
                            obj.addProperty("max_players", server.getMaxPlayerCount());

                            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                            players.sort((player1, player2) -> {
                                return player1.getName().asString().compareTo(player2.getName().asString());
                            });

                            JsonArray arr = new JsonArray();
                            for (ServerPlayerEntity player : players) {
                                JsonObject playerObject = new JsonObject();
                                playerObject.addProperty("name", player.getName().asString());
                                playerObject.addProperty("uuid", player.getUuidAsString());
                                arr.add(playerObject);
                            }

                            obj.add("list", arr);

                            context.getSource().sendFeedback(
                                    new LiteralText(obj.toString()),
                                    false);

                            return 1;
                        }))));
    }
}
