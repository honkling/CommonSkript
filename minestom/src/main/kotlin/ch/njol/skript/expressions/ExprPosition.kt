package ch.njol.skript.expressions

import ch.njol.skript.classes.Changer
import ch.njol.skript.expressions.base.PropertyExpression
import ch.njol.skript.expressions.base.SimplePropertyExpression
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity

class ExprPosition : SimplePropertyExpression<Entity, Pos>() {
	override fun convert(from: Entity?): Pos? {
		return from?.position
	}

	override fun getReturnType(): Class<out Pos> {
		return Pos::class.java
	}

	override fun getPropertyName(): String {
		return "position"
	}
}