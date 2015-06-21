package net.eithon.plugin.donationboard.logic;

import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.IJson;
import net.eithon.library.plugin.Configuration;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.donationboard.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerInfo implements IJson<PlayerInfo>, IUuidAndName  {
	private static EithonPlugin currentEithonPlugin;

	private EithonPlayer _eithonPlayer;
	private int _numberOfLevels;
	private int _remainingDonationTokens;
	private double _totalMoneyDonated;
	private long _totalTokensDonated;
	private int _perkLevel;
	private boolean _isDonatorOnTheBoard;
	private boolean _hasBeenToBoard;

	public static void initialize(EithonPlugin eithonPlugin)
	{
		currentEithonPlugin = eithonPlugin;
	}

	public PlayerInfo(Player player)
	{
		this._eithonPlayer = new EithonPlayer(player);
		this._remainingDonationTokens = 0;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
		Configuration config = currentEithonPlugin.getConfiguration();
		this._numberOfLevels = config.getInt("Levels", 5);
	}

	PlayerInfo() {
	}

	@Override
	public PlayerInfo factory() {
		return new PlayerInfo();
	}

	@Override
	public PlayerInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJSon(jsonObject.get("player"));
		this._remainingDonationTokens = ((Long) jsonObject.get("remainingDonationTokens")).intValue();
		this._totalTokensDonated = ((Long) jsonObject.get("totalTokensDonated")).intValue();
		this._totalMoneyDonated = (double) jsonObject.get("totalMoneyDonated");
		return this;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("player", this._eithonPlayer.toJson());
		json.put("remainingDonationTokens", this._remainingDonationTokens);
		json.put("totalTokensDonated", this._totalTokensDonated);
		json.put("totalMoneyDonated", this._totalMoneyDonated);
		return json;
	}

	public int getRemainingDonationTokens() {
		return this._remainingDonationTokens;
	}

	public long getTotalTokensDonated() {
		return this._totalTokensDonated;
	}

	public double getTotalMoneyDonated() {
		return this._totalMoneyDonated;
	}

	public boolean shouldGetPerks() {
		debug("shouldGetPerks", "_remainingDonationTokens=%d, _isDonatorOnTheBoard=%s, _hasBeenToBoard=%s",
				this._remainingDonationTokens, this._isDonatorOnTheBoard?"TRUE":"FALSE", this._hasBeenToBoard?"TRUE":"FALSE");
		return (this._remainingDonationTokens > 0) || this._isDonatorOnTheBoard || this._hasBeenToBoard;
	}

	public boolean shouldBeAutomaticallyPromoted() {
		return (this._remainingDonationTokens > 0) || this._isDonatorOnTheBoard;
	}

	public void addDonationTokens(int tokens, double amount) {
		this._remainingDonationTokens+=tokens;
		this._totalTokensDonated += tokens;
		this._totalMoneyDonated += amount;
		sendMessage(String.format("You now have %d E-tokens to use on the donation board.", getRemainingDonationTokens()));
	}

	public void usedOneToken() {
		if (this._remainingDonationTokens < 0) {
			this._remainingDonationTokens = 0;
			return;
		}			
		this._remainingDonationTokens--;
		if (this._remainingDonationTokens < 0) this._remainingDonationTokens = 0;
		if (this._remainingDonationTokens == 0) {
			Config.M.noTokensLeft.sendMessage(getPlayer());
		} else {
			Config.M.tokensLeft.sendMessage(getPlayer(), this._remainingDonationTokens);
		}
	}

	public String getName() {
		return this._eithonPlayer.getName();
	}

	public Player getPlayer() {
		return this._eithonPlayer.getPlayer();
	}

	public UUID getUniqueId() {
		return this._eithonPlayer.getUniqueId();
	}

	public void demoteOrPromote(int toLevel, boolean reset) {
		debug("demoteOrPromote", "Enter. toLevel=%d, reset=%s", toLevel, reset?"TRUE":"FALSE");
		int perkLevelBeforeReset = this._perkLevel;
		if (reset) {
			debug("demoteOrPromote", "Reset");
			resetPerkLevel(true);
			this._perkLevel = 0;
		}
		int currentPerkLevel = this._perkLevel;
		debug("demoteOrPromote", "currentPerkLevel=%d", currentPerkLevel);
		if (toLevel < currentPerkLevel) {
			debug("demoteOrPromote", "Demote to %d", toLevel);
			demote(toLevel, perkLevelBeforeReset);
		} else if (toLevel > currentPerkLevel) {
			debug("demoteOrPromote", "Promote to %d", toLevel);
			promote(toLevel, perkLevelBeforeReset);
		}
		debug("demoteOrPromote", "Leave");
	}

	public void setIsDonatorOnTheBoard(boolean isDonatorOnTheBoard)
	{
		this._isDonatorOnTheBoard = isDonatorOnTheBoard;
	}

	public void markAsHasBeenToBoard()
	{
		this._hasBeenToBoard = true;
	}

	public void resetHasBeenToBoard()
	{
		this._hasBeenToBoard = false;
	}

	private void promote(int toLevel, int currentLevel) {
		debug("promote", "Enter. toLevel=%d, currentLevel=%d", toLevel, currentLevel);
		if (!shouldGetPerks()) {
			debug("promote", "Must visit board");
			Config.M.visitBoard.sendMessage(this.getPlayer(), toLevel);
			debug("promote", "Leave");
			return;
		}
		for (int level = this._perkLevel + 1; level <= toLevel; level++) {
			addGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel > currentLevel) {
			Config.M.levelRaised.sendMessage(getPlayer(), toLevel);
		}
		debug("promote", "Leave");
	}

	private void demote(int toLevel, int currentLevel) {
		debug("demote", "Enter. toLevel=%d, currentLevel=%d", toLevel, currentLevel);
		for (int level = this._perkLevel; level > toLevel; level--) {
			removeGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel < currentLevel) {
			Config.M.levelLowered.sendMessage(getPlayer(), toLevel);
		}
		debug("demote", "Enter. toLevel=%d, currentLevel=%d", toLevel, currentLevel);
	}

	private void resetPerkLevel(boolean force) {
		if (force) this._perkLevel = this._numberOfLevels;
		demote(0, 0);
	}

	private void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player != null) {
			player.sendMessage(message);
		}
	}

	private void addGroup(int level) {
		Config.C.addGroup.execute(this.getName(), level);
	}

	private void removeGroup(int level) {
		Config.C.removeGroup.execute(this.getName(), level);
	}

	public String toString()
	{
		return String.format("%s (%d tokens): perklevel %d", this.getName(), this._remainingDonationTokens, this._perkLevel);
	}
	
	private static void debug(String method, String format, Object... args) {
		currentEithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, format, args);
	}
}
