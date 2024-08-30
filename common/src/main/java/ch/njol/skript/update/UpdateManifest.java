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

import java.net.URL;

/**
 * Returned by an update checker when an update is available.
 */
public class UpdateManifest {
	
	/**
	 * Release id, for example "2.3".
	 */
	public final String id;
	
	/**
	 * When the release was published.
	 */
	public final String date;
	
	/**
	 * Patch notes for the update.
	 */
	public final String patchNotes;
	
	/**
	 * Download URL for the update.
	 */
	public final URL downloadUrl;

	public UpdateManifest(String id, String date, String patchNotes, URL downloadUrl) {
		this.id = id;
		this.date = date;
		this.patchNotes = patchNotes;
		this.downloadUrl = downloadUrl;
	}
}
