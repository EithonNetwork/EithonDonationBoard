package net.eithon.plugin.donationboard;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String SHIFT_COMMAND = "/donationboard shift";
	private static final String PRINT_COMMAND = "/donationboard print";
	private static final String LOAD_COMMAND = "/donationboard load";
	private static final String SAVE_COMMAND = "/donationboard save";
	private static final String REGISTER_COMMAND = "/donationboard register <player>";
	private static final String GOTO_COMMAND = "/donationboard goto";
	private static final String DONATE_COMMAND = "/donationboard donate <player> <E-tokens> <amount>";
	private static final String STATS_COMMAND = "/donationboard stats";
	
	private BoardController _controller;

	public CommandHandler(EithonPlugin eithonPlugin, BoardController boardController){	
		this._controller = boardController;
	}

	public boolean onCommand(CommandParser commandParser) {
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1)) return true;
		
		String command = commandParser.getArgumentStringAsLowercase(0);
		if (command.equals("donate")) {
			donateCommand(commandParser);
		} else {
			if (commandParser.getPlayerOrInformSender() == null) return true;

			commandParser.setCurrentCommand(command);
			if (command.equals("shift")) {
				shiftCommand(commandParser);
			} else if (command.equals("print")) {
				printCommand(commandParser);
			} else if (command.equals("load")) {
				loadCommand(commandParser);
			} else if (command.equals("save")) {
				saveCommand(commandParser);
			} else if (command.equals("register")) {
				saveCommand(commandParser);
			} else if (command.equals("goto")) {
				gotoCommand(commandParser);
			} else if (command.equals("stats")) {
				statsCommand(commandParser);
			} else {
				commandParser.showCommandSyntax();
				return false;
			}
		}
		return true;
	}

	void shiftCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.shift")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		this._controller.shiftLeft();
	}

	void printCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.print")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		this._controller.print(commandParser.getPlayer());
	}

	public void statsCommand(CommandParser commandParser) {
		if (!commandParser.hasPermissionOrInformSender("donationboard.stats")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		this._controller.stats(commandParser.getPlayer());
	}

	void loadCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.load")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		this._controller.loadNow();
	}

	void saveCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.save")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.saveNow();
	}

	void registerCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.register")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;
		
		Player registerPlayer = commandParser.getArgumentPlayer(1, commandParser.getPlayer());
		if (registerPlayer == null) {
			commandParser.showCommandSyntax();
			return;
		}
		this._controller.register(registerPlayer);
	}

	void gotoCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.goto")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		commandParser.getPlayer().teleport(this._controller.getBoardLocation());
	}

	public void donateCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("donationboard.donate")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3, 4)) return;

		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayerOrInformSender(1, null);
		if (eithonPlayer == null) return;
		Player donatePlayer = eithonPlayer.getPlayer();
		int tokens = commandParser.getArgumentInteger(2, 0);
		double amount = commandParser.getArgumentDouble(3, 0.0);

		this._controller.donate(donatePlayer, tokens, amount);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command == null) {
			sender.sendMessage("Incomplete command.");
			return;
		}
		if (command.equalsIgnoreCase("shift")) {
			sender.sendMessage(SHIFT_COMMAND);
		} else if (command.equalsIgnoreCase("donate")) {
			sender.sendMessage(DONATE_COMMAND);
		} else if (command.equalsIgnoreCase("print")) {
			sender.sendMessage(PRINT_COMMAND);
		} else if (command.equalsIgnoreCase("load")) {
			sender.sendMessage(LOAD_COMMAND);
		} else if (command.equalsIgnoreCase("save")) {
			sender.sendMessage(SAVE_COMMAND);
		} else if (command.equalsIgnoreCase("register")) {
			sender.sendMessage(REGISTER_COMMAND);
		} else if (command.equalsIgnoreCase("goto")) {
			sender.sendMessage(GOTO_COMMAND);
		} else if (command.equalsIgnoreCase("stats")) {
			sender.sendMessage(STATS_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}		
	}
}
