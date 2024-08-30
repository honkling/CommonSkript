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
package ch.njol.skript;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.update.Updater;

/**
 * Skript's update checker.
 */
public class SkriptUpdater extends Updater {
	
	public final static Message m_not_started = new Message("updater.not started");
	public final static Message m_checking = new Message("updater.checking");
	public final static Message m_check_in_progress = new Message("updater.check in progress");
	public final static Message m_updater_disabled = new Message("updater.updater disabled");
	public final static ArgsMessage m_check_error = new ArgsMessage("updater.check error");
	public final static Message m_running_latest_version = new Message("updater.running latest version");
	public final static Message m_running_latest_version_beta = new Message("updater.running latest version (beta)");
	public final static ArgsMessage m_update_available = new ArgsMessage("updater.update available");
	public final static ArgsMessage m_downloading = new ArgsMessage("updater.downloading");
	public final static Message m_download_in_progress = new Message("updater.download in progress");
	public final static ArgsMessage m_download_error = new ArgsMessage("updater.download error");
	public final static ArgsMessage m_downloaded = new ArgsMessage("updater.downloaded");
	public final static Message m_internal_error = new Message("updater.internal error");
	public final static Message m_custom_version = new Message("updater.custom version");
	public final static Message m_nightly = new Message("updater.nightly build");
	
	SkriptUpdater() {
		super(loadManifest());
	}
	
	/**
	 * Loads the release manifest from Skript jar.
	 * @return Release manifest.
	 */
	private static ReleaseManifest loadManifest() {
		String manifest;
		try (InputStream is = Skript.getInstance().getResource("release-manifest.json");
				Scanner s = new Scanner(is)) {
			s.useDelimiter("\\\\A");
			manifest = s.next();
		} catch (IOException e) {
			throw new IllegalStateException("Skript is missing release-manifest.json!");
		}
		assert manifest != null;
		return ReleaseManifest.load(manifest);
	}
}
