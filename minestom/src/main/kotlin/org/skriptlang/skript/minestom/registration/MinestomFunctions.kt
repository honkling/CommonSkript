package org.skriptlang.skript.minestom.registration

import ch.njol.skript.lang.function.Functions
import ch.njol.skript.lang.function.Parameter
import ch.njol.skript.lang.function.SimpleJavaFunction
import ch.njol.skript.registrations.DefaultClasses
import net.minestom.server.coordinate.Pos

fun registerFunctions() {
	Functions.registerFunction(object : SimpleJavaFunction<Pos>("position", arrayOf(
		Parameter("x", DefaultClasses.NUMBER, true, null),
		Parameter("y", DefaultClasses.NUMBER, true, null),
		Parameter("z", DefaultClasses.NUMBER, true, null),
		Parameter("yaw", DefaultClasses.NUMBER, true, null),
		Parameter("pitch", DefaultClasses.NUMBER, true, null),
	), positionType, true) {
		override fun executeSimple(params: Array<out Array<Any>>): Array<Pos> {
			return arrayOf(Pos(
				(params[0][0] as Number).toDouble(),
				(params[1][0] as Number).toDouble(),
				(params[2][0] as Number).toDouble(),
				(params[3][0] as Number).toFloat(),
				(params[4][0] as Number).toFloat()
			))
		}
	})
}