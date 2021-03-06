package net.eithon.plugin.donationboard.logic;

import java.io.File;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.json.FileContent;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.permissions.PermissionGroupLadder;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.ICountDownListener;
import net.eithon.library.title.Title;
import net.eithon.plugin.donationboard.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
public class BoardController {
	private PlayerCollection<PlayerInfo> _knownPlayers;

	private BoardModel _model;
	private BoardView _view;
	private EithonPlugin _eithonPlugin = null;
	private PermissionGroupLadder _perkLevelLadder;

	public BoardController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._knownPlayers = new PlayerCollection<PlayerInfo>(new PlayerInfo());
		this._perkLevelLadder = new PermissionGroupLadder(
				eithonPlugin,
				true, Config.V.perkLevelGroups);
		loadNow();
	}

	void disable() {
		updatePerkLevel(0);
		this._view = null;
		this._knownPlayers = new PlayerCollection<PlayerInfo>(new PlayerInfo());
		this._eithonPlugin = null;
	}

	public void increasePerkLevel(Player player, Block block) {
		if (!playerHasTokens(player)) {
			Config.M.needTokens.sendMessage(player);
			Config.M.howToGetTokens.sendMessage(player);
			return;
		}
		decreasePlayerDonationTokens(player);
		int day = markAsDonated(player, block);
		if (day == 1) {
			broadCastDonation(player);
			playersNeedToRevisitBoard();
		}
		delayedSave();
		delayedRefresh();
	}

	private void broadCastDonation(Player player) {
		Config.M.playerHasDonated.broadcastToThisServer(player.getDisplayName());
	}

	public void initialize(Player player, Block clickedBlock) {
		this._model = new BoardModel(Config.V.numberOfDays, Config.V.perkLevelGroups.length);
		this._view = new BoardView(clickedBlock);
		this._model.createFirstLineOfButtons();
		delayedSave();
		delayedRefresh();
	}

	public void shiftLeft() {
		this._model.shiftLeft();
		playersNeedToRevisitBoard();
		delayedRefresh();
	}

	@SuppressWarnings("unchecked")
	public void saveNow()
	{
		if (this._view == null) return;
		File jsonFile = new File(this._eithonPlugin.getDataFolder(), "donations.json");
		JSONObject payload = new JSONObject();
		payload.put("view", this._view.toJson());
		payload.put("players", this._knownPlayers.toJson());

		FileContent fileContent = new FileContent("donationBoard", 1, payload);
		fileContent.save(jsonFile);
	}

	public void loadNow() {
		File file = new File(this._eithonPlugin.getDataFolder(), "donations.json");
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) return;
		JSONObject payload = (JSONObject)fileContent.getPayload();
		if (payload == null) {
			this._eithonPlugin.logWarn("The donation board payload was empty.");
			return;
		}
		this._view = BoardView.createFromJson((JSONObject)payload.get("view"));
		this._model = new BoardModel(Config.V.numberOfDays, Config.V.perkLevelGroups.length);
		this._view.updateBoardModel(this._model);
		this._knownPlayers.fromJson(payload.get("players"));
	}

	private void findDonators() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.setIsDonatorOnTheBoard(false);
		}
		for (int day = 0; day <= Config.V.numberOfDays; day++) {
			for (int level = 0; level <= Config.V.perkLevelGroups.length; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				OfflinePlayer player = donation.getOfflinePlayer();
				if (player == null) continue;
				PlayerInfo playerInfo = getOrAddPlayerInfo(player);
				playerInfo.setIsDonatorOnTheBoard(true);
			}
		}
	}	

	private boolean isDonator(Player player) {
		if (this._model == null) return false;
		for (int day = 0; day <= Config.V.numberOfDays; day++) {
			for (int level = 0; level <= Config.V.perkLevelGroups.length; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				if (donation.getOfflinePlayer() == player) return true;
			}
		}
		return false;
	}	

	public void print(Player player) {
		//this._model.print(player);
		for (PlayerInfo playerInfo : this._knownPlayers) {
			player.sendMessage(playerInfo.toString());
		}
	}

	public boolean register(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.markAsHasBeenToBoard();
		boolean wasPromoted = maybePromotePlayer(playerInfo);
		if (!wasPromoted && this._model != null) {
			Config.M.noChangeInPerkLevel.sendMessage(player, this._model.getDonationLevel(1));
		}
		return wasPromoted;
	}

	public void donate(Player player, int tokens, double amount) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.addDonationTokens(tokens, amount);
		maybePromotePlayer(playerInfo);
		delayedSave();
	}

	private void playersNeedToRevisitBoard() {
		int levelStartAtOne = this._model.getDonationLevel(1);
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.resetHasBeenToBoard();		
			maybePromotePlayer(playerInfo);
			if (!playerInfo.shouldBeAutomaticallyPromoted()) {
				Player player = playerInfo.getPlayer();
				if (player == null) continue;
				Config.M.visitBoard.sendMessage(player, levelStartAtOne);
			}
		}	
	}

	public void playerJoined(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (isDonator(player)) {
			playerInfo.setIsDonatorOnTheBoard(true);
		}
		maybePromotePlayer(playerInfo);
	}

	private boolean maybePromotePlayer(PlayerInfo playerInfo) {
		if (this._model == null) return false;
		int levelStartAtOne = playerInfo.shouldGetPerks() ? this._model.getDonationLevel(1) : 0;
		return updatePerkLevel(playerInfo, levelStartAtOne);
	}

	private boolean updatePerkLevel(PlayerInfo playerInfo, int levelStartAtOne) 
	{
		Player player = playerInfo.getPlayer();
		if (player == null) return false;

		boolean hasGroupNewBefore = PermissionsFacade.hasPermissionGroup(player, "New");
		boolean changed = this._perkLevelLadder.updatePermissionGroups(player, levelStartAtOne);
		if (hasGroupNewBefore) {
			boolean hasGroupNewAfter = PermissionsFacade.hasPermissionGroup(player, "New");
			if (hasGroupNewBefore && !hasGroupNewAfter) {
				verbose("maybePromotePlayer", "%s", "Permission group New had disappeared, adding it again");
				PermissionsFacade.addPermissionGroup(player, "New");
			}
		}
		playerInfo.setPerkLevel(levelStartAtOne);
		if (changed) Config.M.levelChanged.sendMessage(player, levelStartAtOne);
		return changed;
	}

	private void updatePerkLevel(int levelStartAtOne) 
	{
		for (PlayerInfo playerInfo : this._knownPlayers) {
			updatePerkLevel(playerInfo, levelStartAtOne);
		}	
	}

	public void delayedTeleportCheck(Player player)  {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				playerTeleportedToBoard(player);
			}
		}, 20);
	}

	void playerTeleportedToBoard(Player player) 
	{
		verbose("playerTeleportedToBoard", "Enter player %s", player.getName());
		if (!isInMandatoryWorld(player.getWorld())) {	
			verbose("playerTeleportedToBoard", "World %s is not accepted", player.getWorld().getName());
			verbose("playerTeleportedToBoard", "Leave");
			return;
		}
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (playerInfo.shouldGetPerks()) {	
			verbose("playerTeleportedToBoard", "Player %s will get a perk update without waiting.", player.getName());
			register(player);
			verbose("playerTeleportedToBoard", "Leave");
			return;
		}
		verbose("playerTeleportedToBoard", "Start countdown");
		Title.get().CountDown(this._eithonPlugin, player, Config.V.perkClaimAfterSeconds, new ICountDownListener() {
			public boolean isCancelled(long remainingIntervals) {
				verbose("playerTeleportedToBoard.CountDown", "Checking if still in correct world");
				return !isInMandatoryWorld(player.getWorld());
			}
			public void afterDoneTask() {
				verbose("playerTeleportedToBoard.CountDown", "Player has visited the board.");
				register(player);
			}
			public void afterCancelTask() {
				verbose("playerTeleportedToBoard.CountDown", "Visit board cancelled.");
			}
		});
		verbose("playerTeleportedToBoard", "Leave");
	}

	void refreshNow() {
		if (this._model == null) return;
		this._view.refresh(this._model);
		findDonators();
		updatePerkLevel();
	}

	private void updatePerkLevel() 
	{
		int toLevel = this._model.getDonationLevel(1);
		updatePerkLevel(toLevel);	
	}

	private boolean playerHasTokens(Player player)
	{
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo != null) {
			if (playerInfo.getRemainingDonationTokens() > 0) return true;
		}
		return false;
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				refreshNow();
			}
		});
	}

	private int markAsDonated(Player player, Block block) {
		int day = this._view.calculateDay(block);
		int level = this._view.calculateLevel(block);
		this._model.markOnlyThis(day, level, player);
		return day;
	}

	private void decreasePlayerDonationTokens(Player player) {
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		playerInfo.usedOneToken();
	}

	private void delayedSave() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveNow();
			}
		});
	}

	private PlayerInfo getOrAddPlayerInfo(OfflinePlayer player) {
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo == null) {
			playerInfo = new PlayerInfo(player);
			this._knownPlayers.put(player, playerInfo);
		}
		return playerInfo;
	}

	public Location getBoardLocation() {
		return this._view.getLocation();
	}

	public void stats(CommandSender sender) {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			stats(sender, playerInfo);
		}
	}

	public void stats(CommandSender sender, Player player)
	{
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo == null) {
			sender.sendMessage(String.format("%s has no donation information.", player.getName()));
			return;
		}
		stats(sender, playerInfo);
	}

	public void stats(CommandSender sender, PlayerInfo playerInfo)
	{
		sender.sendMessage(String.format("%s has %d E-tokens, of %d (%.2f�) in total.", 
				playerInfo.getName(),
				playerInfo.getRemainingDonationTokens(),
				playerInfo.getTotalTokensDonated(),
				playerInfo.getTotalMoneyDonated()));	
	}

	public void resetPlayer(Player player) {
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		this._perkLevelLadder.reset(player);
		if (playerInfo == null) return;
		this._knownPlayers.remove(player);
	}

	public boolean isInMandatoryWorld(World world) 
	{
		if (Config.V.mandatoryWorld == null) {
			this._eithonPlugin.logWarn("No mandatory world set");
			return true;
		}
		boolean sameName = world.getName().equalsIgnoreCase(Config.V.mandatoryWorld);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE,
				"Current world: \"%s\". Mandatory world: \"%s\". Same = %s", 
				world.getName(), Config.V.mandatoryWorld, 
				sameName ? "TRUE" : "FALSE");
		return sameName;
	}

	void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
