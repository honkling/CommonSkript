package ch.njol.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import org.bukkit.event.Event

class EffTeleport : Effect() {
	companion object {
		init {
			Skript.registerEffect(EffTeleport::class.java, "teleport %entities% to %position%")
		}
	}

	lateinit var entities: Expression<Entity>
	lateinit var position: Expression<Pos>

	override fun init(
		expressions: Array<out Expression<*>>,
		matchedPattern: Int,
		isDelayed: Kleenean?,
		parseResult: SkriptParser.ParseResult?
	): Boolean {
		entities = expressions[0] as Expression<Entity>
		position = expressions[1] as Expression<Pos>
		return true
	}

	override fun execute(event: Event) {
		val entities = entities.getAll(event)
		val position = position.getSingle(event)!!

		for (entity in entities)
			entity.teleport(position)
	}

	override fun toString(event: Event?, debug: Boolean): String {
		val entities = entities.getAll(event)
		val position = position.getSingle(event)
		return "teleport ${entities.joinToString(", ")} to $position"
	}

}