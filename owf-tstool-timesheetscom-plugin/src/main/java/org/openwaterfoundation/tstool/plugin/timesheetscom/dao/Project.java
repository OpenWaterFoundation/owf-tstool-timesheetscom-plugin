// Project - results from items/project service

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * timesheets.com "items/project" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Project {
	// Alphabetize.
	
	/**
	 * "CREATEDATE"
	 */
	@JsonProperty("CREATEDATE")
	private String createDate = "";
	
	/**
	 * "CREATORNAME"
	 */
	@JsonProperty("CREATORNAME")
	private String creatorName = "";
	
	/**
	 * "CREATORUSERID"
	 */
	@JsonProperty("CREATORUSERID")
	private String creatorUserId = "";
	
	/**
	 * "CUSTOMERID"
	 */
	@JsonProperty("CUSTOMERID")
	private String customerId = "";
	
	/**
	 * "DEFAULTBILLRATE"
	 */
	@JsonProperty("DEFAULTBILLRATE")
	private String defaultBillRate = "";
	
	/**
	 * "DEFAULTUSERBILLRATE"
	 */
	@JsonProperty("DEFAULTUSERBILLRATE")
	private String defaultUserBillRate = "";
	
	/**
	 * "MINIMUMTIMEINCREMENT"
	 */
	@JsonProperty("MINIMUMTIMEINCREMENT")
	private String minimumTimeIncrement = "";
	
	/**
	 * "PROJECTDESCRIPTION"
	 */
	@JsonProperty("PROJECTDESCRIPTION")
	private String projectDescription = "";
	
	/**
	 * "PROJECTID"
	 */
	@JsonProperty("PROJECTID")
	private String projectId = "";
	
	/**
	 * "PROJECTNAME"
	 */
	@JsonProperty("PROJECTNAME")
	private String projectName = "";
	
	/**
	 * "PROJECTNAMEPLAIN"
	 */
	@JsonProperty("PROJECTNAMEPLAIN")
	private String projectNamePlain = "";
	
	/**
	 * "PROJECTSTATUS"
	 */
	@JsonProperty("PROJECTSTATUS")
	private String projectStatus = "";
	
	/**
	 * "READONLY"
	 */
	@JsonProperty("READONLY")
	private String readOnly = "";
	
	/**
	 * "USERBILLRATE"
	 */
	@JsonProperty("USERBILLRATE")
	private String userBillRate = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public Project () {
	}
	
	/**
	 * Return the creator name.
	 * @return the creator name 
	 */
	public String getCreatorName () {
		return this.creatorName;
	}
	
	/**
	 * Return whether read only.
	 * @return whether read only 
	 */
	public String getReadOnly () {
		return this.readOnly;
	}
	
}