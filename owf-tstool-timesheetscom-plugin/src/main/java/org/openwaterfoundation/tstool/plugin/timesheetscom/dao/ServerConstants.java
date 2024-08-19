// ServerConstants - results from server/constants service

/* NoticeStart

OWF TSTool timesheetscom Plugin
Copyright (C) 2023 Open Water Foundation

OWF TSTool timesheetscom Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool timesheetscom Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool timesheetscom Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.timesheetscom.dao;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * timesheets.com "server/constants" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ServerConstants {
	/**
	 * "data"
	 */
	@JsonProperty("data")
	Map<String,Object> serverConstants = new LinkedHashMap<>();

	/**
	 * Default constructor used by Jackson.
	 */
	public ServerConstants () {
	}

	/**
	 * Return the server constants map.
	 * @return the server constants map
	 */
	public Map<String,Object> getServerConstants () {
		return this.serverConstants;
	}
}