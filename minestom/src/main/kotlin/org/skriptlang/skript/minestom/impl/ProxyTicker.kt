package org.skriptlang.skript.minestom.impl

import org.bukkit.scheduler.Ticker

class ProxyTicker(
	private val ticker: Ticker,
	private val onTick: (Runnable) -> Unit
) : Ticker {
	override fun initialize(tick: Runnable) {
		return ticker.initialize {
			onTick.invoke(tick)
			tick.run()
		}
	}
}