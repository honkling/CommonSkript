package ch.njol.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.expressions.base.EventValueExpression
import ch.njol.skript.expressions.base.WrapperExpression
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.Literal
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.Utils
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import org.bukkit.event.Event

class ExprEventExpression : WrapperExpression<Any>() {
    companion object {
        init {
            println("register")
            Skript.registerExpression(ExprEventExpression::class.java, Any::class.java, ExpressionType.PROPERTY, "[the] event-%*classinfo%")
        }
    }

    override fun init(exprs: Array<out Expression<*>>, p1: Int, p2: Kleenean?, parser: SkriptParser.ParseResult): Boolean {
        println("initializing expreventexpression")
        val classInfo = (exprs[0] as Literal<ClassInfo<*>>).single
        val clazz = classInfo.c

        val plural = Utils.getEnglishPlural(parser.expr).second
        val eventValue = EventValueExpression(if (plural == true) CollectionUtils.arrayType(clazz) else clazz)
        expr = eventValue
        return eventValue.init()
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return expr.toString(event, debug)
    }
}
