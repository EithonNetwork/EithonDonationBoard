package net.eithon.plugin.donationboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;

public final class Plugin extends EithonPlugin {
	private BoardController _controller;

	@Override
	public void onEnable() {
		this._controller = new BoardController(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		EventListener eventListener = new EventListener(this, this._controller);
		super.enable(commandHandler, eventListener);
		AlarmTrigger.get().enable(this);
		setShiftTimer();	
		PlayerInfo.initialize(this);
	}


	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
		AlarmTrigger.get().disable();
	}
	
	private void setShiftTimer() {
		LocalDateTime alarmTime = null;
		LocalDateTime alarmToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(7,0,0));
		LocalDateTime alarmTomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(7,0,0));
		if (LocalDateTime.now().isBefore(alarmToday)) alarmTime = alarmToday;
		else alarmTime = alarmTomorrow;
		AlarmTrigger.get().setAlarm("Donation board daily shift",
				alarmTime, 
				new Runnable() {
			public void run() {
				keepOnShifting();
			}
		});
	}

	protected void keepOnShifting() {
		if (this._controller == null) return;
		this._controller.shiftLeft();
		setShiftTimer();
	}
}
