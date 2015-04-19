package net.eithon.plugin.donationboard.logic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class Donation {
	private String _playerName;

	public Donation()
	{
		this._playerName = null;
	}
	
	public void setEmpty()
	{
		this._playerName = null;
	}

	public void setDonation(Player player)
	{
		this._playerName = player.getName();

	}

	public void setDonation(String playerName)
	{
		this._playerName = playerName;

	}

	boolean isEmpty() {
		return this._playerName == null;
	}

	boolean isDonation() {
		return !isEmpty();
	}

	String getPlayerName() {
		return this._playerName;
	}
	
	@SuppressWarnings("deprecation")
	Player getPlayer()
	{
		if (this._playerName == null) return null;
		return Bukkit.getPlayer(this._playerName);
	}
	
	public String toString() {
		return getPlayerName();
	}

	public boolean isSame(Donation donationInfo) {
		return this._playerName == donationInfo._playerName;
	}

	public void copy(Donation from) {
		this._playerName = from._playerName;
	}
}
