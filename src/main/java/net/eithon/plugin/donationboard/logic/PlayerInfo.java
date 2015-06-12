package net.eithon.plugin.donationboard.logic;

import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.Converter;
import net.eithon.library.json.IJson;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
import net.eithon.plugin.donationboard.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerInfo implements IJson<PlayerInfo>, IUuidAndName  {
	private static EithonPlugin currentEithonPlugin;

	private String _name;
	private UUID _id;
	private Player _player;
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
		Configuration config = currentEithonPlugin.getConfiguration();
	}

	public PlayerInfo(Player player)
	{
		this._player = player;
		this._name = player.getName();
		this._id = player.getUniqueId();
		this._remainingDonationTokens = 0;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
		Configuration config = currentEithonPlugin.getConfiguration();
		this._numberOfLevels = config.getInt("Levels", 5);
	}

	public PlayerInfo(UUID uniqueId, int remainingDonationTokens, long totalTokensDonated, double totalAmountDonated)
	{
		this._player = null;
		this._name = null;
		this._id = uniqueId;
		this._remainingDonationTokens = remainingDonationTokens;
		this._totalTokensDonated = totalTokensDonated;
		this._totalMoneyDonated = totalAmountDonated;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	public PlayerInfo(UUID uniqueId, String name, int remainingDonationTokens, long totalTokensDonated, double totalAmountDonated)
	{
		this._player = null;
		this._name = name;
		this._id = uniqueId;
		this._remainingDonationTokens = remainingDonationTokens;
		this._totalTokensDonated = totalTokensDonated;
		this._totalMoneyDonated = totalAmountDonated;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
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
		this._id = Converter.toPlayerId((JSONObject) jsonObject.get("player"));
		this._name = Converter.toPlayerName((JSONObject) jsonObject.get("player"));
		this._remainingDonationTokens = (int) jsonObject.get("remainingDonationTokens");
		this._totalTokensDonated = (long) jsonObject.get("totalTokensDonated");
		this._totalMoneyDonated = (double) jsonObject.get("totalMoneyDonated");
		return this;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("player", Converter.fromPlayer(this._id, this._name));
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
		if (this._name == null)
		{
			Player player = this.getPlayer();
			if (player == null) return null;
			this._name = player.getName();
		}
		return this._name;
	}

	public Player getPlayer() {
		if (this._player == null)
		{
			this._player = Bukkit.getPlayer(getUniqueId());
		}
		return this._player;
	}

	public UUID getUniqueId() {
		return this._id;
	}

	public void demoteOrPromote(int toLevel, boolean reset) {
		int perkLevelBeforeReset = this._perkLevel;
		if (reset) {
			resetPerkLevel(true);
			this._perkLevel = 0;
		}
		int currentPerkLevel = this._perkLevel;
		if (toLevel < currentPerkLevel) {
			demote(toLevel, perkLevelBeforeReset);
		} else if (toLevel > currentPerkLevel) {
			promote(toLevel, perkLevelBeforeReset);
		}
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
		if (!shouldGetPerks()) {
			Config.M.visitBoard.sendMessage(this.getPlayer(), toLevel);
			return;
		}
		for (int level = this._perkLevel + 1; level <= toLevel; level++) {
			addGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel > currentLevel) {
			Config.M.levelRaised.sendMessage(getPlayer(), toLevel);
		}
	}

	private void demote(int toLevel, int currentLevel) {
		for (int level = this._perkLevel; level > toLevel; level--) {
			removeGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel < currentLevel) {
			Config.M.levelLowered.sendMessage(getPlayer(), toLevel);
		}
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
}
