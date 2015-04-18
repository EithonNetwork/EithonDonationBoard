package net.eithon.plugin.donationboard;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Configuration;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class Events implements Listener {

	private static String mandatoryWorld;

	public void enable(EithonPlugin eithonPlugin) {
		Configuration config = eithonPlugin.getConfiguration();
		mandatoryWorld = config.getString("MandatoryWorld", "");
	}

	public void disable() {
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!isInMandatoryWorld(player.getWorld())) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		switch (event.getClickedBlock().getType()) {
		case STONE_BUTTON:
			BoardController.get().initialize(player, event.getClickedBlock());
			break;
		case WOOD_BUTTON:
			BoardController.get().increasePerkLevel(player, event.getClickedBlock());
			break;
		default:
			break;
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		BoardController.get().playerJoined(player);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!isInMandatoryWorld(event.getTo().getWorld())) return;
		Player player = event.getPlayer();
		BoardController.get().playerTeleportedToBoard(player, event.getFrom());
	}

	private static boolean isInMandatoryWorld(World world) 
	{
		if (mandatoryWorld == null) return true;
		return world.getName().equalsIgnoreCase(mandatoryWorld);
	}
}
