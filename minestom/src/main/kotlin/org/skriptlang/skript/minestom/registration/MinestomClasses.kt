package org.skriptlang.skript.minestom.registration

import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.classes.Serializer
import ch.njol.skript.expressions.base.EventValueExpression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.registrations.Classes
import ch.njol.yggdrasil.Fields
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import java.util.*

private val uuid = Regex("(?i)[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")

lateinit var positionType: ClassInfo<Pos>

fun registerClasses() {
	doRegister()

	positionType = Classes.getExactClassInfo(Pos::class.java)!!
}

private fun doRegister() {
	Classes.registerClass(ClassInfo(Entity::class.java, "entity")
		.user("(entity|entities)")
		.name("Entity")
		.description("An entity.")
		.defaultExpression(EventValueExpression(Entity::class.java))
		.parser(object : Parser<Entity>() {
			override fun parse(s: String, context: ParseContext): Entity? {
				if (context != ParseContext.COMMAND && context != ParseContext.PARSE) {
					assert(false);
					return null;
				}

				if (s.isEmpty() || !uuid.matches(s))
					return null;

				val uuid = UUID.fromString(s)
				val instances = MinecraftServer.getInstanceManager().instances
				return instances.firstNotNullOfOrNull { it.getEntityByUuid(uuid) }
			}

			override fun canParse(context: ParseContext?): Boolean {
				return context == ParseContext.COMMAND || context == ParseContext.PARSE
			}

			override fun toString(p0: Entity, p1: Int): String {
				return p0.entityType.name()
			}

			override fun toVariableNameString(p0: Entity): String {
				return p0.uuid.toString()
			}
		}))

	Classes.registerClass(ClassInfo(Pos::class.java, "position")
		.user("positions?")
		.name("Position")
		.description("Holds the x, y, and z coordinates, as well as yaw and pitch.")
		.defaultExpression(EventValueExpression(Pos::class.java))
		.parser(object : Parser<Pos>() {
			override fun canParse(context: ParseContext?): Boolean {
				return false
			}

			override fun toVariableNameString(o: Pos): String {
				return "location(${o.x}, ${o.y}, ${o.z}, yaw = ${o.yaw}, pitch = ${o.pitch})"
			}

			override fun toString(o: Pos, flags: Int): String {
				return toVariableNameString(o)
			}
		})
		.serializer(object : Serializer<Pos>() {
			override fun serialize(o: Pos): Fields {
				val fields = Fields()
				fields.putPrimitive("x", o.x)
				fields.putPrimitive("y", o.y)
				fields.putPrimitive("z", o.z)
				fields.putPrimitive("yaw", o.yaw)
				fields.putPrimitive("pitch", o.pitch)
				return fields
			}

			override fun deserialize(fields: Fields): Pos {
				return Pos(
					fields.getPrimitive("x", Double::class.java),
					fields.getPrimitive("y", Double::class.java),
					fields.getPrimitive("z", Double::class.java),
					fields.getPrimitive("yaw", Float::class.java),
					fields.getPrimitive("pitch", Float::class.java),
				)
			}

			override fun deserialize(o: Pos?, f: Fields?) {
				assert(false)
			}

			override fun canBeInstantiated(): Boolean {
				return false
			}

			override fun mustSyncDeserialization(): Boolean {
				return true
			}
		}))

	Classes.registerClass(ClassInfo(Player::class.java, "player")
		.user("players?")
		.name("Players")
		.description("A player.")
		.defaultExpression(EventValueExpression(Player::class.java))
		.parser(object : Parser<Player>() {
			override fun parse(s: String, context: ParseContext): Player? {
				if (context != ParseContext.COMMAND && context != ParseContext.PARSE) {
					assert(false);
					return null;
				}

				if (s.isEmpty())
					return null;

				val connections = MinecraftServer.getConnectionManager()

				if (uuid.matches(s))
					return connections.getOnlinePlayerByUuid(UUID.fromString(s))

				return connections.findOnlinePlayer(s)
			}

			override fun canParse(context: ParseContext?): Boolean {
				return context == ParseContext.COMMAND || context == ParseContext.PARSE
			}

			override fun toString(p0: Player, p1: Int): String {
				return p0.username
			}

			override fun toVariableNameString(p0: Player): String {
				return p0.uuid.toString()
			}
		}))
}