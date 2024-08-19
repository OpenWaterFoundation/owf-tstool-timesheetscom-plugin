// User - results from the 'users' service

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
 * timesheets.com "users" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class User {
	// Alphabetize.

	/**
	 * "ACCESS"
	 */
	@JsonProperty("ACCESS")
	private String access = "";

	/**
	 * "ADMINUSERID"
	 */
	@JsonProperty("ADMINUSERID")
	private String adminUserId = "";

	/**
	 * "CONCATNAME"
	 */
	@JsonProperty("CONCATNAME")
	private String concatName = "";

	/**
	 * "EMPLOYEENUMBER"
	 */
	@JsonProperty("EMPLOYEENUMBER")
	private String employeeNumber = "";

	/**
	 * "FIRSTNAME"
	 */
	@JsonProperty("FIRSTNAME")
	private String firstName = "";

	/**
	 * "JOBTITLE"
	 */
	@JsonProperty("JOBTITLE")
	private String jobTitle = "";

	/**
	 * "LASTNAME"
	 */
	@JsonProperty("LASTNAME")
	private String lastName = "";

	/**
	 * "USERID"
	 */
	@JsonProperty("USERID")
	private String userId = "";

	/**
	 * "USERNAME"
	 */
	// TODO smalers 2024-03-21 is this used?
	//@JsonProperty("USERNAME")
	//private String userName = "";

	/**
	 * "USERSTATUS"
	 */
	@JsonProperty("USERSTATUS")
	private String userStatus = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public User () {
	}

	/**
	 * Get the access.
	 * @return the access
	 */
	public String getAccess () {
		return this.access;
	}

	/**
	 * Get the adminUserId.
	 * @return the adminUserId
	 */
	public String getAdminUserId () {
		return this.adminUserId;
	}

	/**
	 * Get the concatName.
	 * @return the concatName
	 */
	public String getConcatName () {
		return this.concatName;
	}

	/**
	 * Get the employeeNumber.
	 * @return the employeeNumber
	 */
	public String getEmployeeNumber () {
		return this.employeeNumber;
	}

	/**
	 * Get the firstName.
	 * @return the firstName
	 */
	public String getFirstName () {
		return this.firstName;
	}

	/**
	 * Get the jobTitle.
	 * @return the jobTitle
	 */
	public String getJobTitle () {
		return this.jobTitle;
	}

	/**
	 * Get the lastName.
	 * @return the lastName
	 */
	public String getLastName () {
		return this.lastName;
	}

	/**
	 * Get the userId.
	 * @return the userId
	 */
	public String getUserId () {
		return this.userId;
	}

	/**
	 * Get the userName.
	 * @return the userName
	 */
	//public String getUserName () {
	//	return this.userName;
	//}

	/**
	 * Get the userStatus.
	 * @return the userStatus
	 */
	public String getUserStatus () {
		return this.userStatus;
	}

}