package rewqazwas.minformax.custom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
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
                                )
                        )
                        .then(Commands.literal("add_all")
                                .executes(IndexCommand::addAllIndexes)
                        )
                )
        );
    }

    private static int addIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String index = StringArgumentType.getString(context, "index");
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (ModDataReloadListener.MOB_DROPS.containsKey(index)) {
            PlayerIndex.add(player, index);
            player.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex(player), player.level()));
            context.getSource().sendSuccess(() -> Component.literal("Added index: " + index), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Index not found: " + index));
            return 0;
        }
    }

    private static int addAllIndexes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> allIndexes = new ArrayList<>(ModDataReloadListener.MOB_DROPS.keySet());
        
        for (String index : allIndexes) {
            PlayerIndex.add(player, index);
        }
        
        player.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex(player), player.level()));
        context.getSource().sendSuccess(() -> Component.literal("Added all " + allIndexes.size() + " indexes"), true);
        return allIndexes.size();
    }
}
