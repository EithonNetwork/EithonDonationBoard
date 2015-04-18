package net.eithon.plugin.donationboard;

import org.bukkit.entity.Player;

public class BoardModel {
	public int _numberOfDays;
	public int _numberOfLevels;

	private Donation[][] _donations;

	BoardModel(int numberOfDays, int numberOfLevels) {
		this._numberOfDays = numberOfDays;
		this._numberOfLevels = numberOfLevels;
		this._donations = new Donation[this._numberOfDays][this._numberOfLevels];
		resetBoard();
	}

	public int getNumberOfDays()
	{
		return this._numberOfDays;
	}

	public int getNumberOfLevels()
	{
		return this._numberOfLevels;
	}

	public void createFirstLineOfButtons() {
		for (int dayIndex = 0; dayIndex < this._numberOfDays; dayIndex++) {
			initializeNewDayInternal(dayIndex);
		}
	}

	public void shiftLeft() {
		// Copy values from right
		for (int dayIndex = 0; dayIndex < this._numberOfDays-1; dayIndex++) {
			for (int levelIndex = 0; levelIndex < this._numberOfLevels; levelIndex++) {
				this._donations[dayIndex][levelIndex].copy(this._donations[dayIndex+1][levelIndex]);
			}
		}			
		// Initialize the last day
		initializeNewDayInternal(this._numberOfDays-1);
	}

	public BoardModel clone() {
		BoardModel newClone = new BoardModel(this._numberOfDays, this._numberOfLevels);
		for (int dayIndex = 0; dayIndex < this._numberOfDays ; dayIndex++) {
			for (int levelIndex = 0; levelIndex < this._numberOfLevels; levelIndex++) {
				newClone._donations[dayIndex][levelIndex].copy(this._donations[dayIndex][levelIndex]);
			}
		}
		return newClone;
	}

	public void markOnlyThis(int day, int level, String playerName) {
		markOnlyThisInternal(day-1, level-1, playerName);
	}

	public Donation getDonationInfo(int day, int level) {
		return getDonationInfoInternal(day-1, level-1);
	}

	public int getDonationLevel(int day) {
		return getDonationLevelInternal(day-1)+1;
	}

	public void print(Player player) {
		for (int dayIndex = 0; dayIndex < this._numberOfDays; dayIndex++) {
			int day = dayIndex+1;
			for (int levelIndex = 0; levelIndex < this._numberOfLevels; levelIndex++) {
				int level = levelIndex+1;
				String message = String.format("%d,%d: %s", day, level, this._donations[dayIndex][levelIndex].toString());
				if (player != null) player.sendMessage(message);
				else System.out.println(message);
			}
		}
	}

	private void initializeNewDayInternal(int dayIndex) {
		for (int levelIndex = 0; levelIndex < this._numberOfLevels; levelIndex++) {
			this._donations[dayIndex][levelIndex].setEmpty();
		}
	}

	private void markOnlyThisInternal(int dayIndex, int levelIndex, String playerName) {
		if (!isInsideBoardInternal(dayIndex, levelIndex)) return;
		if (playerName == null)
		{
			this._donations[dayIndex][levelIndex].setEmpty();		
		} else {
			this._donations[dayIndex][levelIndex].setDonation(playerName);			
		}
	}

	private Donation getDonationInfoInternal(int dayIndex, int levelIndex) {
		if (!isInsideBoardInternal(dayIndex, levelIndex)) return null;
		return this._donations[dayIndex][levelIndex];
	}

	private boolean isInsideBoardInternal(int day, int level) {
		if (day < 0) return false;
		if (day >= this._numberOfDays) return false;
		if (level < 0) return false;
		if (level >= this._numberOfLevels) return false;
		return true;
	}

	private void resetBoard() {
		for (int dayIndex = 0; dayIndex < this._numberOfDays; dayIndex++) {
			for (int levelIndex = 0; levelIndex < this._numberOfLevels; levelIndex++) {
				this._donations[dayIndex][levelIndex] = new Donation();
			}
		}
	}

	private int getDonationLevelInternal(int dayIndex) {
		int donationLevel = -1;
		for (int level = 0; level < this._numberOfLevels; level++) {
			if (!this._donations[dayIndex][level].isDonation()) break;
			donationLevel = level;
		}
		return donationLevel;
	}
}
