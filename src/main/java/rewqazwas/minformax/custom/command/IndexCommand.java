package rewqazwas.minformax.custom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.PlayerIndex;

import java.util.ArrayList;
import java.util.List;

import static rewqazwas.minformax.custom.utility.Utils.clearContent;

public class IndexCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("minformax")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("index")
                        .then(Commands.literal("add")
                                .then(Commands.argument("index", StringArgumentType.string())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModDataReloadListener.MOB_DROPS.keySet(), builder))
                                        .executes(IndexCommand::addIndex)
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(IndexCommand::addIndex)
                                        )
                                )
                        )
                        .then(Commands.literal("add_all")
                                .executes(IndexCommand::addAllIndexes)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(IndexCommand::addAllIndexes)
                                )
                        )
                        .then(Commands.literal("clear")
                                .executes(IndexCommand::clearIndex)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(IndexCommand::clearIndex)
                                )
                        )
                )
        );
    }

    private static int addIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (IllegalArgumentException e) {
            player = context.getSource().getPlayerOrException();
        }

        String index = StringArgumentType.getString(context, "index");
        ServerPlayer finalPlayer = player;

        if (ModDataReloadListener.MOB_DROPS.containsKey(index)) {
            PlayerIndex.add(finalPlayer, index);
            finalPlayer.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex(finalPlayer), finalPlayer.level()));
            context.getSource().sendSuccess(() -> Component.literal("Added index: " + index + " for " + finalPlayer.getName().getString()), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Index not found: " + index));
            return 0;
        }
    }

    private static int addAllIndexes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (IllegalArgumentException e) {
            player = context.getSource().getPlayerOrException();
        }

        List<String> allIndexes = new ArrayList<>(ModDataReloadListener.MOB_DROPS.keySet());
        ServerPlayer finalPlayer = player;

        for (String index : allIndexes) {
            PlayerIndex.add(finalPlayer, index);
        }

        finalPlayer.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex(finalPlayer), finalPlayer.level()));
        context.getSource().sendSuccess(() -> Component.literal("Added all " + allIndexes.size() + " indexes for " + finalPlayer.getName().getString()), true);
        return allIndexes.size();
    }

    private static int clearIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (IllegalArgumentException e) {
            player = context.getSource().getPlayerOrException();
        }
        ServerPlayer finalPlayer = player;

        PlayerIndex.clear(finalPlayer);
        finalPlayer.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex(finalPlayer), finalPlayer.level()));
        context.getSource().sendSuccess(() -> Component.literal("Cleared index for " + finalPlayer.getName().getString()), true);
        return 1;
    }
}
