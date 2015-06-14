package net.eithon.plugin.donationboard.logic;

import java.io.File;
import java.time.LocalDateTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.plugin.donationboard.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
public class BoardController {
	private static String mandatoryWorld;

	private PlayerCollection<PlayerInfo> _knownPlayers;

	private BoardModel _model;
	private BoardView _view;
	private EithonPlugin _eithonPlugin = null;

	public BoardController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._knownPlayers = new PlayerCollection<PlayerInfo>(new PlayerInfo());	
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
		if (day == 1) broadCastDonation(player);
		playersNeedToRevisitBoard();
		delayedSave();
		delayedRefresh();
	}

	private void broadCastDonation(Player player) {
		Config.M.playerHasDonated.broadcastMessage(player.getDisplayName());
	}

	public void initialize(Player player, Block clickedBlock) {
		this._model = new BoardModel(Config.V.numberOfDays, Config.V.numberOfLevels);
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
			this._eithonPlugin.getEithonLogger().warning("The donation board payload was empty.");
			return;
		}
		this._view = BoardView.createFromJson((JSONObject)payload.get("view"));
		this._model = new BoardModel(Config.V.numberOfDays, Config.V.numberOfLevels);
		this._view.updateBoardModel(this._model);
		this._knownPlayers.fromJson(payload.get("players"));
	}
	
	private void FindDonators() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.setIsDonatorOnTheBoard(false);
		}
		for (int day = 0; day <= Config.V.numberOfDays; day++) {
			for (int level = 0; level <= Config.V.numberOfLevels; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				Player player = donation.getPlayer();
				if (player == null) continue;
				PlayerInfo playerInfo = getOrAddPlayerInfo(player);
				playerInfo.setIsDonatorOnTheBoard(true);
			}
		}
	}	

	private boolean isDonator(Player player) {
		for (int day = 0; day <= Config.V.numberOfDays; day++) {
			for (int level = 0; level <= Config.V.numberOfLevels; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				if (donation.getPlayer() == player) return true;
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

	public void register(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.markAsHasBeenToBoard();
		maybePromotePlayer(player, true);
	}

	public void donate(Player player, int tokens, double amount) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.addDonationTokens(tokens, amount);
		maybePromotePlayer(player, false);
		delayedSave();
	}

	private void playersNeedToRevisitBoard() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			if (!playerInfo.shouldBeAutomaticallyPromoted())  {
				playerInfo.resetHasBeenToBoard();
			}
		}	
	}

	private void maybePromotePlayer(Player player, boolean forceReset) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		int toLevel = this._model.getDonationLevel(1);
		playerInfo.demoteOrPromote(toLevel, forceReset);
	}

	public void playerJoined(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (isDonator(player)) {
			playerInfo.setIsDonatorOnTheBoard(true);
		}
		maybePromotePlayer(player, true);
	}

	public void playerTeleportedToBoard(Player player, Location from) 
	{
		if (!isInMandatoryWorld(player.getWorld())) return;
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo.shouldGetPerks()) return;
		LocalDateTime alarm = LocalDateTime.now().plusSeconds(Config.V.perkClaimAfterSeconds);
		AlarmTrigger.get().setAlarm(String.format("%s can claim perk", player.getName()),
				alarm,
				new Runnable() {
			public void run() {
				if (isInMandatoryWorld(player.getWorld())) {
					register(player);
				}
			}
		});	
	}

	void refreshNow() {
		if (this._model == null) return;
		this._view.refresh(this._model);
		FindDonators();
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
		this._model.markOnlyThis(day, level, player.getName());
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

	private void updatePerkLevel(int toLevel) 
	{
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.demoteOrPromote(toLevel, false);
		}	
	}

	private PlayerInfo getOrAddPlayerInfo(Player player) {
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

	static boolean isInMandatoryWorld(World world) 
	{
		if (mandatoryWorld == null) return true;
		return world.getName().equalsIgnoreCase(mandatoryWorld);
	}
}
