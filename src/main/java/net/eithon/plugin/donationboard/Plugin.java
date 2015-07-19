package net.eithon.plugin.donationboard;

import java.time.LocalTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.IRepeatable;
import net.eithon.plugin.donationboard.logic.BoardController;
import net.eithon.plugin.donationboard.logic.PlayerInfo;

public final class Plugin extends EithonPlugin {
	BoardController _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new BoardController(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		EventListener eventListener = new EventListener(this, this._controller);
		repeatShift();	
		PlayerInfo.initialize(this);
		super.activate(commandHandler, eventListener);
	}


	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
	}

	private void repeatShift() {
		final Plugin thisObject = this;
		AlarmTrigger.get().repeatEveryDay("Donation board daily shift", LocalTime.of(7,0,0), 
				new IRepeatable() {
			@Override
			public boolean repeat() {
				if (thisObject._controller == null) return false;
				thisObject._controller.shiftLeft();
				return true;
			}
		});
	}
}
