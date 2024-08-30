package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.custom.ScriptLoadEvent;
import ch.njol.skript.lang.util.SimpleEvent;

public class EvtScriptLoad extends SimpleEvent {
	static {
		Skript.registerEvent("Script Load", EvtScriptLoad.class, ScriptLoadEvent.class,
			"[script] (load|init|enable)");
	}

	@Override
	public boolean postLoad() {
		trigger.execute(new ScriptLoadEvent());
		return true;
	}
}
