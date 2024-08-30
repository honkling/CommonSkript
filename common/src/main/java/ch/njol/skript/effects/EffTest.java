package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EffTest extends Effect {
	static {
		Skript.registerEffect(EffTest.class, "test effect");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test effect";
	}

	@Override
	protected void execute(Event event) {
		Skript.info("TEST TEST!!!");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}
}
