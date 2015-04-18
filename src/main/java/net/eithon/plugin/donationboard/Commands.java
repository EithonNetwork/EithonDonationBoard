package net.eithon.plugin.donationboard;

import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
	private static Commands singleton = null;
	private static final String SHIFT_COMMAND = "/donationboard shift";
	private static final String PRINT_COMMAND = "/donationboard print";
	private static final String LOAD_COMMAND = "/donationboard load";
	private static final String SAVE_COMMAND = "/donationboard save";
	private static final String REGISTER_COMMAND = "/donationboard register <player>";
	private static final String GOTO_COMMAND = "/donationboard goto";
	private static final String DONATE_COMMAND = "/donationboard donate <player> <E-tokens> <amount>";

	private static String mandatoryWorld;

	private Commands() {
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(EithonPlugin eithonPlugin){		
		Configuration config = eithonPlugin.getConfiguration();
		mandatoryWorld = config.getString("MandatoryWorld", "");
	}

	void disable() {
	}

	public boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}
		String command = args[0].toLowerCase();
		if (command.equals("donate")) {
			Commands.get().donateCommand(sender, args);
		} else {

			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player!");
				return false;
			}

			Player player = (Player) sender;

			if (command.equals("shift")) {
				Commands.get().shiftCommand(player, args);
			} else if (command.equals("print")) {
				Commands.get().printCommand(player, args);
			} else if (command.equals("load")) {
				Commands.get().loadCommand(player, args);
			} else if (command.equals("save")) {
				Commands.get().saveCommand(player, args);
			} else if (command.equals("register")) {
				Commands.get().saveCommand(player, args);
			} else if (command.equals("goto")) {
				Commands.get().gotoCommand(player, args);
			} else if (command.equals("stats")) {
				Commands.get().statsCommand(player, args);
			} else {
				sender.sendMessage("Could not understand command.");
				return false;
			}
		}
		return true;
	}

	void shiftCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.shift")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(SHIFT_COMMAND);
			return;
		}

		BoardController.get().shiftLeft();
	}

	void printCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.print")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(PRINT_COMMAND);
			return;
		}

		BoardController.get().print(player);
	}

	public void statsCommand(Player player, String[] args) {
		BoardController.get().stats(player);
	}

	void loadCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.load")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(LOAD_COMMAND);
			return;
		}

		BoardController.get().loadNow();
	}

	void saveCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.save")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(SAVE_COMMAND);
			return;
		}

		BoardController.get().saveNow();
	}

	@SuppressWarnings("deprecation")
	void registerCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.register")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 2)) {
			player.sendMessage(REGISTER_COMMAND);
			return;
		}

		Player registerPlayer = player;
		if (args.length > 1) {
			registerPlayer = Bukkit.getPlayer(args[1]);
		}

		BoardController.get().register(registerPlayer);
	}

	void gotoCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.register")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(GOTO_COMMAND);
			return;
		}

		player.teleport(BoardController.get().getBoardLocation());
	}

	@SuppressWarnings("deprecation")
	public void donateCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!verifyPermission(player, "donationboard.donate")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 3, 4)) {
			sender.sendMessage(DONATE_COMMAND);
			return;
		}

		Player donatePlayer = null;
		try {
			UUID id = UUID.fromString(args[1]);
			donatePlayer = Bukkit.getPlayer(id);
		} catch (Exception e) {
		}
		if (donatePlayer == null) donatePlayer = Bukkit.getPlayer(args[1]);
		if (donatePlayer == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		int tokens = 0;
		try {
			tokens = Integer.parseUnsignedInt(args[2]);
		} catch (Exception e) {
			sender.sendMessage(String.format("Number of tokens could not be understood: %s", args[2]));
			return;
		}

		double amount = 0.0;
		if (args.length > 3) {
			try {
				amount = Double.parseDouble(args[3]);
			} catch (Exception e) {
				sender.sendMessage(String.format("Amount paid could not be understood: %s", args[3]));
				return;
			}
		}

		BoardController.get().donate(donatePlayer, tokens, amount);
	}


	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}

	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}

	private static boolean isInMandatoryWorld(World world) 
	{
		if (mandatoryWorld == null) return true;
		return world.getName().equalsIgnoreCase(mandatoryWorld);
	}
}
