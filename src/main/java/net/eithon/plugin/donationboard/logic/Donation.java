package net.eithon.plugin.donationboard.logic;

import org.bukkit.OfflinePlayer;

class Donation {
	private OfflinePlayer _player;

	public Donation()
	{
		this._player = null;
	}
	
	public void setEmpty()
	{
		this._player = null;
	}

	public void setDonation(OfflinePlayer player)
	{
		this._player = player;

	}

	boolean isEmpty() {
		return this._player == null;
	}

	boolean isDonation() {
		return !isEmpty();
	}

	String getPlayerName() {
		return this._player.getName();
	}
	
	OfflinePlayer getOfflinePlayer()
	{
		return this._player;
	}
	
	public String toString() {
		return getPlayerName();
	}

	public boolean isSame(Donation donationInfo) {
		return this._player.getUniqueId() == donationInfo._player.getUniqueId();
	}

	public boolean isSamePlayer(OfflinePlayer player) {
		return this._player.getUniqueId() == player.getUniqueId();
	}

	public void copy(Donation from) {
		this._player = from._player;
	}
}
