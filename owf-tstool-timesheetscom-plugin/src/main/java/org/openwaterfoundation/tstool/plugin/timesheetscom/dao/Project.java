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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * timesheets.com "items/project" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Project {
	// Alphabetize.
	
	/**
	 * "CREATEDDATE"
	 */
	@JsonProperty("CREATEDDATE")
	private String createdDate = "";
	
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
	
	// TODO smalers 2024-03-21 did this used to be included.  I don't see it now so how does a project relate to a customer?
	/**
	 * "CUSTOMERID"
	 */
	@JsonProperty("CUSTOMERID")
	private String customerId = "";

	/**
	 * Customer ID as an integer.
	 */
	private int customerIdAsInteger = -1;
	
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
	 * "PROJECTID" as an integer.
	 */
	@JsonIgnore
	private int projectIdAsInteger = -1;
	
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
	 * - "1" is active
	 * - "0" is archived
	 */
	@JsonProperty("PROJECTSTATUS")
	private String projectStatus = "";
	
	/**
	 * "PROJECTSTATUS" as an integer.
	 */
	@JsonIgnore
	private int projectStatusAsInteger = -1;
	
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
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
		try {
			this.customerIdAsInteger = Integer.valueOf(this.customerId);
		}
		catch ( NumberFormatException e ) {
			this.customerIdAsInteger = -1;
		}
		this.projectIdAsInteger = Integer.valueOf(this.projectId);
		this.projectStatusAsInteger = Integer.valueOf(this.projectStatus);
	}

	/**
	 * Find a project given its project identifier as a string.
	 * @param projectList list of projects to search
	 * @param projectId project identifier to match
	 * @return the matching project, or null if not matched
	 */
	public static Project findForProjectId (List<Project> projectList, String projectId ) {
		if ( projectList == null ) {
			return null;
		}
		if ( projectId == null ) {
			return null;
		}
		for ( Project project : projectList ) {
			if ( project.getProjectId().equals(projectId) ) {
				return project;
			}
		}
		// Not matched.
		return null;
	}

	/**
	 * Return the created date.
	 * @return the created date 
	 */
	public String getCreatedDate () {
		return this.createdDate;
	}

	/**
	 * Return the creator name.
	 * @return the creator name 
	 */
	public String getCreatorName () {
		return this.creatorName;
	}
	
	/**
	 * Return the creator user ID.
	 * @return the creator user ID 
	 */
	public String getCreatorUserId () {
		return this.creatorUserId;
	}
	
	/**
	 * Return the customer ID
	 * @return the customer ID
	 */
	public String getCustomerId () {
		return this.customerId;
	}

	/**
	 * Return the customer ID as an integer
	 * @return the customer ID as an integer.
	 */
	public int getCustomerIdAsInteger () {
		return this.customerIdAsInteger;
	}
	
	/**
	 * Return the default bill rate.
	 * @return the default bill rate
	 */
	public String getDefaultBillRate () {
		return this.defaultBillRate;
	}
	
	/**
	 * Return the default user bill rate.
	 * @return the default user bill rate
	 */
	public String getDefaultUserBillRate () {
		return this.defaultUserBillRate;
	}
	
	/**
	 * Return the minimum time increment
	 * @return the minimum time increment
	 */
	public String getMinimumTimeIncrement () {
		return this.minimumTimeIncrement;
	}
	
	/**
	 * Return the project description
	 * @return the project description
	 */
	public String getProjectDescription () {
		return this.projectDescription;
	}
	
	/**
	 * Return the project ID
	 * @return the project ID
	 */
	public String getProjectId () {
		return this.projectId;
	}
	
	/**
	 * Get the project ID as an integer.
	 * @return the project ID as an integer
	 */
	public int getProjectIdAsInteger () {
		return this.projectIdAsInteger;
	}
	
	/**
	 * Return the project name
	 * @return the project name
	 */
	public String getProjectName () {
		return this.projectName;
	}
	
	/**
	 * Return the project name, plain
	 * @return the project name, plain
	 */
	public String getProjectNamePlain () {
		return this.projectNamePlain;
	}
	
	/**
	 * Return the project status
	 * @return the project status
	 */
	public String getProjectStatus () {
		return this.projectStatus;
	}
	
	/**
	 * Get the project status as an integer.
	 * @return the project status as an integer
	 */
	public int getProjectStatusAsInteger () {
		return this.projectStatusAsInteger;
	}
	
	/**
	 * Return whether read only.
	 * @return whether read only 
	 */
	public String getReadOnly () {
		return this.readOnly;
	}
	
	/**
	 * Return the user bill rate
	 * @return the user bill rate
	 */
	public String getUserBillRate () {
		return this.userBillRate;
	}
	
}