package net.eithon.plugin.donationboard.logic;

import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.IJson;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.donationboard.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerInfo implements IJson<PlayerInfo>, IUuidAndName  {
	private static EithonPlugin currentEithonPlugin;
	
	private EithonPlayer _eithonPlayer;
	private int _remainingDonationTokens;
	private double _totalMoneyDonated;
	private long _totalTokensDonated;
	private int _perkLevelStartAtOne;
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
		this._perkLevelStartAtOne = 0;
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
		this._eithonPlayer = EithonPlayer.getFromJson(jsonObject.get("player"));
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
		verbose("shouldGetPerks", "_remainingDonationTokens=%d, _isDonatorOnTheBoard=%s, _hasBeenToBoard=%s",
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
	
	public void setPerkLevel(int levelStartAtOne) { this._perkLevelStartAtOne = levelStartAtOne; }

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

	private void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player != null) {
			player.sendMessage(message);
		}
	}

	public String toString()
	{
		return String.format("%s (%d tokens): perklevel %d", this.getName(), this._remainingDonationTokens, this._perkLevelStartAtOne);
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		currentEithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
