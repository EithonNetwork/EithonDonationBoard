package net.eithon.plugin.donationboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin implements Listener {
	private Events _events;

	@Override
	public void onEnable() {
		EithonPlugin eithonPlugin = EithonPlugin.get(this);
		eithonPlugin.enable();
		this._events = new Events();
		this._events.enable(eithonPlugin);
		BoardController.get().enable(eithonPlugin);
		Commands.get().enable(eithonPlugin);
		AlarmTrigger.get().enable(this);
		setShiftTimer();	
		getServer().getPluginManager().registerEvents(this._events, this);
		PlayerInfo.initialize(eithonPlugin);
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
		BoardController.get().shiftLeft();
		setShiftTimer();
	}

	@Override
	public void onDisable() {
		BoardController.get().disable();
		Commands.get().disable();
		AlarmTrigger.get().disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return Commands.get().onCommand(sender, args);
	}
}
