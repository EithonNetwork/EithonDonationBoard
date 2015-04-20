package net.eithon.plugin.donationboard;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {	
		public static String mandatoryWorld;
		public static int numberOfDays;
		public static int numberOfLevels;
		public static long perkClaimAfterSeconds;

		static void load(Configuration config) {
			mandatoryWorld = config.getString("MandatoryWorld", "");
			numberOfDays = config.getInt("Days", 31);
			numberOfLevels = config.getInt("Levels", 5);
			perkClaimAfterSeconds = config.getInt("PerkClaimAfterSeconds", 10);
		}
	}
	public static class C {
		public static ConfigurableCommand addGroup;
		public static ConfigurableCommand removeGroup;

		static void load(Configuration config) {
			addGroup = config.getConfigurableCommand("commands.AddGroup", 2,
					"perm player %s addgroup PerkLevel%d");
			removeGroup = config.getConfigurableCommand("commands.RemoveGroup", 2,
					"perm player %s removegroup PerkLevel%d");
		}

	}
	public static class M {
		public static ConfigurableMessage needTokens;
		public static ConfigurableMessage howToGetTokens;
		public static ConfigurableMessage playerHasDonated;
		public static ConfigurableMessage visitBoard;
		public static ConfigurableMessage levelRaised;
		public static ConfigurableMessage levelLowered;
		public static ConfigurableMessage noTokensLeft;
		public static ConfigurableMessage tokensLeft;

		static void load(Configuration config) {
			needTokens = config.getConfigurableMessage("messages.NeedTokens", 0,
					"You must have E-tokens to raise the perk level.");
			howToGetTokens = config.getConfigurableMessage("messages.HowToGetTokens", 0,
					"You get E-tokens by donating money at http://eithon.org/donate.");
			playerHasDonated = config.getConfigurableMessage("messages.PlayerHasDonated", 1,
					"Player %s has made a donation for today!");
			visitBoard = config.getConfigurableMessage("messages.VisitBoard", 1,
					"If you visit the donationboard, you can raise your perk level to %d.");
			levelRaised = config.getConfigurableMessage("messages.PerkLevelRaised", 1,
					"Your perk level has been raised to %d.");
			levelLowered = config.getConfigurableMessage("messages.PerkLevelLowered", 1,
					"Your perk level has been lowered to %d.");
			noTokensLeft = config.getConfigurableMessage("messages.NoTokensLeft", 0,
					"You have no E-tokens left.");
			tokensLeft = config.getConfigurableMessage("TokensLeftMessage", 1,
					"You have %d remaining E-tokens.");
		}		
	}

}
