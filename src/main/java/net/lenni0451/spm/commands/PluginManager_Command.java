package net.lenni0451.spm.commands;

import net.lenni0451.spm.PluginManager;
import net.lenni0451.spm.commands.subs.*;
import net.lenni0451.spm.commands.subs.types.ISubCommand;
import net.lenni0451.spm.commands.subs.types.ISubCommandMultithreaded;
import net.lenni0451.spm.messages.I18n;
import net.lenni0451.spm.utils.Logger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PluginManager_Command implements CommandExecutor {

    public static Map<String, ISubCommand> subCommands = new LinkedHashMap<>();

    static {
        //Sub commands need to be added with lowercase names!
        subCommands.put("help", new Help_Sub());
        subCommands.put("list", new List_Sub());
        subCommands.put("info", new Info_Sub());
        subCommands.put("enable", new Enable_Sub());
        subCommands.put("disable", new Disable_Sub());
        subCommands.put("restart", new Restart_Sub());
        subCommands.put("load", new Load_Sub());
        subCommands.put("unload", new Unload_Sub());
        subCommands.put("reload", new Reload_Sub());
        subCommands.put("commands", new Commands_Sub());
        subCommands.put("find", new Find_Sub());
        subCommands.put("gui", new Gui_Sub());
        subCommands.put("permissions", new Permissions_Sub());
        subCommands.put("dump", new Dump_Sub());
        subCommands.put("reloadconfig", new ReloadConfig_Sub());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pluginmanager.commands")) {
            Logger.sendPermissionMessage(sender);
            return true;
        }
        if (PluginManager.getInstance().getConfig().getBoolean("OnlyConsole") && !Bukkit.getConsoleSender().equals(sender)) {
            Logger.sendPrefixMessage(sender, I18n.t("pm.general.onlyConsole"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6--------------------------------------------------");
            for (Map.Entry<String, ISubCommand> entry : subCommands.entrySet()) {
                if (PluginManager.getInstance().getConfig().getBoolean("HideNoPermissionCommands")) {
                    if (!sender.hasPermission("pluginmanager.commands." + entry.getKey().toLowerCase())) continue;
                }
                for (String usage : entry.getValue().getUsage().split(Pattern.quote("\n"))) {
                    String message = " §7- §6pm " + entry.getKey() + " §7| §2" + usage;
                    if (sender instanceof Player) {
                        TextComponent textComponent = new TextComponent(message);
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pm " + entry.getKey()));
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§b/pm " + entry.getKey())}));
                        ((Player) sender).spigot().sendMessage(textComponent);
                    } else {
                        sender.sendMessage(message);
                    }
                }
            }
            sender.sendMessage("§6--------------------------------------------------");
        } else {
            String cmd = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);

            ISubCommand subCommand = subCommands.get(cmd.toLowerCase());
            if (subCommand == null) {
                Logger.sendPrefixMessage(sender, I18n.t("pm.commands.pluginmanager.subNotFound"));
            } else {
                final String[] _args = args;
                Runnable executeRun = () -> {
                    try {
                        if (!sender.hasPermission("pluginmanager.commands." + cmd.toLowerCase())) {
                            Logger.sendPermissionMessage(sender);
                        } else if (!subCommand.execute(sender, _args)) {
                            Logger.sendPrefixMessage(sender, I18n.t("pm.commands.pluginmanager.invalidUsage"));
                            for (String usage : subCommand.getUsage().split(Pattern.quote("\n"))) {
                                Logger.sendPrefixMessage(sender, I18n.t("pm.commands.pluginmanager.correctUsage", usage));
                            }
                        }
                    } catch (Throwable t) {
                        for (String s : I18n.mt("pm.commands.pluginmanager.unknownException")) {
                            Logger.sendPrefixMessage(sender, s);
                        }
                        t.printStackTrace();
                    }
                };
                if (subCommand instanceof ISubCommandMultithreaded) {
                    Bukkit.getScheduler().runTaskAsynchronously(PluginManager.getInstance(), executeRun);
                } else {
                    executeRun.run();
                }
            }
        }

        return true;
    }

}
