package ch.njol.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.events.wrapper.PlayerEventWrapper
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import net.minestom.server.entity.Player
import org.bukkit.event.Event

class EffMessage : Effect() {
    companion object {
        init {
            println("effmessage")
            Skript.registerEffect(EffMessage::class.java, "(message|send) %strings% [recipients:to %players%]")
        }
    }

    lateinit var messages: Expression<String>
    var recipients: Expression<Player>? = null

    override fun init(exprs: Array<out Expression<*>>, parseMark: Int, p2: Kleenean?, parseResult: SkriptParser.ParseResult): Boolean {
        println("initializing effmessage")
        messages = exprs[0] as Expression<String>

		if (parseResult.hasTag("recipients"))
        	recipients = exprs[1] as Expression<Player>

        return true
    }

    override fun execute(event: Event?) {
        for (message in messages.getAll(event)) {
			val recipients = recipients?.getAll(event)

			if (recipients == null && event is PlayerEventWrapper<*>)
				event.event.player.sendMessage(message)
			else if (recipients == null) {}
			else for (recipient in recipients)
				recipient.sendMessage(message)
		}
    }

    override fun toString(event: Event?, debug: Boolean): String {
        val messages = messages.getAll(event)
        val recipients = recipients?.getAll(event)
        return "send ${messages.toList()} to ${recipients?.toList()?.map { it.name }}"
    }
}