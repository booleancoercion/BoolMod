package dev.boolco.boolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.pb4.stylednicknames.NicknameHolder;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond;
import me.lucko.spark.api.statistic.types.DoubleStatistic;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Comparator;
import java.util.List;

public class BoolMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("boolmod");

    private Spark spark = null;

    @Override
    public void onInitialize() {
        boolean isStyledNicknamesLoaded = FabricLoader.getInstance().isModLoaded("styled-nicknames");
        boolean isSparkLoaded = FabricLoader.getInstance().isModLoaded("spark");

        CommandRegistrationCallback.EVENT
                .register((dispatcher, dedicated) -> dispatcher.register(literal("list").then(literal("json")
                        .executes(context -> {
                            MinecraftServer server = context.getSource().getServer();

                            JsonObject obj = new JsonObject();
                            obj.addProperty("current_players", server.getCurrentPlayerCount());
                            obj.addProperty("max_players", server.getMaxPlayerCount());

                            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                            players.sort(Comparator.comparing(player -> player.getName().asString()));

                            JsonArray arr = new JsonArray();
                            for (ServerPlayerEntity player : players) {
                                JsonObject playerObject = new JsonObject();
                                playerObject.addProperty("name", player.getName().asString());
                                if (isStyledNicknamesLoaded) {
                                    playerObject.addProperty("nickname", NicknameHolder.of(player).sn_get());
                                    playerObject.add("nickname_styled", Text.Serializer.toJsonTree(NicknameHolder.of(player).sn_getOutput()));
                                }
                                playerObject.addProperty("uuid", player.getUuidAsString());

                                arr.add(playerObject);
                            }

                            obj.add("list", arr);

                            if (isSparkLoaded) {
                                if (spark == null) {
                                    spark = SparkProvider.get();
                                }

                                DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
                                assert tps != null;
                                JsonArray tpsArray = new JsonArray();

                                for (TicksPerSecond window : TicksPerSecond.values()) {
                                    double polled = tps.poll(window);
                                    tpsArray.add(Math.round(polled * 100) / 100.0);
                                }

                                obj.add("tps", tpsArray);
                            }

                            context.getSource().sendFeedback(
                                    new LiteralText(obj.toString()),
                                    false);

                            return 1;
                        }))));
    }
}
