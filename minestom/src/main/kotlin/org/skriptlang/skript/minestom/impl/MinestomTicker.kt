package org.skriptlang.skript.minestom.impl

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import org.bukkit.scheduler.Ticker

object MinestomTicker : Ticker {
	override fun initialize(tick: Runnable) {
		MinecraftServer.getSchedulerManager().submitTask {
			tick.run()
			TaskSchedule.nextTick()
		}
	}
}