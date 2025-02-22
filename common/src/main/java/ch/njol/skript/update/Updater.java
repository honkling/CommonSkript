/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Task;

/**
 * Extensible updater system. Note: starts disabled, must be enabled using
 * {@link #setEnabled(boolean)}.
 */
public abstract class Updater {
	
	/**
	 * Release that is currently in use.
	 */
	private final ReleaseManifest currentRelease;
	
	/**
	 * Update checker used by this build.
	 */
	private final UpdateChecker updateChecker;
	
	/**
	 * Release channel currently in use.
	 */
	@Nullable
	private volatile ReleaseChannel releaseChannel;
	
	/**
	 * Current state of the updater.
	 */
	private volatile UpdaterState state;
	
	/**
	 * Status of the release.
	 */
	private volatile ReleaseStatus releaseStatus;
	
	/**
	 * How often to check for updates. 0 to not check automatically at all.
	 */
	private volatile long checkFrequency;
	
	/**
	 * Update manifest, if it exists.
	 */
	@Nullable
	private volatile UpdateManifest updateManifest;
	
	/**
	 * Whether this checker is enabled or not. Disabled checkers never report
	 * any updates or make network connections.
	 */
	private volatile boolean enabled;
	
	protected Updater(ReleaseManifest manifest) {
		this.currentRelease = manifest;
		this.updateChecker = manifest.createUpdateChecker();
		this.state = UpdaterState.NOT_STARTED;
		this.releaseStatus = ReleaseStatus.UNKNOWN;
		this.enabled = false;
	}
	
	/**
	 * Fetches the update manifest. Release channel must have been set before
	 * this is done. Note that this will not have side effects to this Updater
	 * instance.
	 * @return Future that will contain update manifest or null if no updates
	 * are available in current channel.
	 */
	public CompletableFuture<UpdateManifest> fetchUpdateManifest() {
		if (!enabled) {
			CompletableFuture<UpdateManifest> future = CompletableFuture.completedFuture(null);
			assert future != null;
			return future;
		}
		ReleaseChannel channel = releaseChannel;
		if (channel == null) {
			throw new IllegalStateException("release channel must be specified");
		}
		// Just check that channel name is in update name
		return updateChecker.check(currentRelease, channel);
	}

	public ReleaseManifest getCurrentRelease() {
		return currentRelease;
	}
	
	public void setReleaseChannel(ReleaseChannel channel) {
		this.releaseChannel = channel;
	}
	
	/**
	 * Sets update check frequency.
	 * @param ticks Frequency in ticks.
	 */
	public void setCheckFrequency(long ticks) {
		this.checkFrequency = ticks;
	}
	
	public UpdaterState getState() {
		return state;
	}
	
	public ReleaseStatus getReleaseStatus() {
		return releaseStatus;
	}
	
	@Nullable
	public UpdateManifest getUpdateManifest() {
		return updateManifest;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
