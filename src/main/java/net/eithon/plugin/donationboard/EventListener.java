package net.eithon.plugin.donationboard;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.donationboard.logic.BoardController;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class EventListener implements Listener {


	private BoardController _controller;
	private EithonPlugin _eithonPlugin;

	public EventListener(EithonPlugin eithonPlugin, BoardController boardController) {	
		this._eithonPlugin = eithonPlugin;
		this._controller = boardController;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!isInMandatoryWorld(player.getWorld())) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		switch (event.getClickedBlock().getType()) {
		case STONE_BUTTON:
			this._controller.initialize(player, event.getClickedBlock());
			break;
		case WOOD_BUTTON:
			this._controller.increasePerkLevel(player, event.getClickedBlock());
			break;
		default:
			break;
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		this._controller.playerJoined(player);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!isInMandatoryWorld(event.getTo().getWorld())) return;
		Player player = event.getPlayer();
		this._controller.playerTeleportedToBoard(player, event.getFrom());
	}

	private boolean isInMandatoryWorld(World world) 
	{
		if (Config.V.mandatoryWorld == null) return true;
		boolean sameName = world.getName().equalsIgnoreCase(Config.V.mandatoryWorld);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE,
				"Current world: \"%s\". Mandatory world: \"%s\". Same = %s", 
				world.getName(), Config.V.mandatoryWorld, 
				sameName ? "TRUE" : "FALSE");
		return sameName;
	}
}
