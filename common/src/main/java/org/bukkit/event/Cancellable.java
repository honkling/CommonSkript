package org.bukkit.event;

public interface Cancellable {
	public void setCancelled(boolean cancelled);
	public boolean isCancelled();
}
