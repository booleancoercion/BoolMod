package dev.boolco.boolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;

import eu.pb4.stylednicknames.NicknameHolder;

import static net.minecraft.commands.Commands.*;

import java.util.ArrayList;
import java.util.Comparator;

public class BoolMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("boolmod");

    private boolean isStyledNicknamesLoaded = false;

    @Override
    public void onInitialize() {
        isStyledNicknamesLoaded = FabricLoader.getInstance().isModLoaded("styled-nicknames");

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(literal("list").then(literal("json").executes(this::runListJson)));
                });
    }

    private int runListJson(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        PlayerList playerList = server.getPlayerList();

        JsonObject obj = new JsonObject();
        obj.addProperty("current_players", playerList.getPlayerCount());
        obj.addProperty("max_players", playerList.getMaxPlayers());

        ArrayList<ServerPlayer> players = new ArrayList<ServerPlayer>(playerList.getPlayers());
        players.sort(Comparator.comparing(player -> player.getName().getString()));

        JsonArray arr = new JsonArray();
        for (ServerPlayer player : players) {
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("name", player.getName().getString());
            if (isStyledNicknamesLoaded) {
                playerObject.addProperty("nickname", NicknameHolder.of(player).styledNicknames$get());
            }
            playerObject.addProperty("uuid", player.getStringUUID());

            arr.add(playerObject);
        }

        obj.add("list", arr);

        context.getSource().sendSuccess(() -> {
            return Component.literal(obj.toString());
        }, true);

        return 1;
    }
}
