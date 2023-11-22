// AccountCode - results from items/accountcode service

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
 * timesheets.com "items/accountcount" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AccountCode {
	// Alphabetize.
	
	/**
	 * "ACCOUNTCODEDESCRIPTION"
	 */
	@JsonProperty("ACCOUNTCODEDESCRIPTION")
	private String accountCodeDescription = "";
	
	/**
	 * "ACCOUNTCODEID"
	 */
	@JsonProperty("ACCOUNTCODEID")
	private String accountCodeId = "";
	
	/**
	 * "ACCOUNTCODENAME"
	 */
	@JsonProperty("ACCOUNTCODENAME")
	private String accountCodeName = "";
	
	/**
	 * "ACCOUNTCODENAMEPLAIN"
	 */
	@JsonProperty("ACCOUNTCODENAMEPLAIN")
	private String accountCodeNamePlain = "";

	/**
	 * "ACCOUNTCODESTATUS"
	 */
	@JsonProperty("ACCOUNTCODESTATUS")
	private String accountCodeStatus = "";

	/**
	 * "CREATEDATE"
	 */
	@JsonProperty("CREATEDATE")
	private String createDate = "";
	
	/**
	 * "CREATEUSERID"
	 */
	@JsonProperty("CREATEUSERID")
	private String createUserId = "";
	
	/**
	 * "CREATORNAME"
	 */
	@JsonProperty("CREATORNAME")
	private String creatorName = "";
	
	/**
	 * "DEFAULTPAYRATE"
	 */
	@JsonProperty("DEFAULTPAYRATE")
	private String defaultPayRate = "";
	
	/**
	 * "DEFAULTUSERPAYRATE"
	 */
	@JsonProperty("DEFAULTUSERPAYRATE")
	private String defaultUserPayRate = "";
	
	/**
	 * "READONLY"
	 */
	@JsonProperty("READONLY")
	private String readOnly = "";
	
	/**
	 * "USERPAYRATE"
	 */
	@JsonProperty("USERPAYRATE")
	private String userPayRate = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public AccountCode () {
	}

	/**
	 * Return the account code ID.
	 * @return the account code ID
	 */
	public String getAccountCodeId () {
		return this.accountCodeId;
	}

	/**
	 * Return the account code description.
	 * @return the account code description
	 */
	public String getAccountCodeDescription () {
		return this.accountCodeDescription;
	}

	/**
	 * Return the account code name.
	 * @return the account code name
	 */
	public String getAccountCodeName () {
		return this.accountCodeName;
	}

	/**
	 * Return the account code name plain.
	 * @return the account code name plain
	 */
	public String getAccountCodeNamePlain () {
		return this.accountCodeNamePlain;
	}

	/**
	 * Return the account code status.
	 * @return the account code status
	 */
	public String getAccountCodeStatus () {
		return this.accountCodeStatus;
	}

	/**
	 * Return the create date.
	 * @return the create date
	 */
	public String getCreateDate () {
		return this.createDate;
	}
	
	/**
	 * Return the create user ID.
	 * @return the create user ID
	 */
	public String getCreateUserId () {
		return this.createUserId;
	}
	
	/**
	 * Return the creator name.
	 * @return the creator name 
	 */
	public String getCreatorName () {
		return this.creatorName;
	}
	
	/**
	 * Return the default pay rate.
	 * @return the default pay rate.
	 */
	public String getDefaultPayRate () {
		return this.defaultPayRate;
	}
	
	/**
	 * Return the default user pay rate.
	 * @return the default user pay rate.
	 */
	public String getDefaultUserPayRate () {
		return this.defaultUserPayRate;
	}

	/**
	 * Return whether read only.
	 * @return whether read only 
	 */
	public String getReadOnly () {
		return this.readOnly;
	}
	
	/**
	 * Return the user pay rate.
	 * @return the user pay rate
	 */
	public String getUserPayRate () {
		return this.userPayRate;
	}
	
	/**
	 * Return a string representation of the object, useful for troubleshooting.
	 */
	/*
	public String toString() {
		return "key=" + this.key + " code=\"" + this.code + "\" description=\"" +
			this.description + "\" color=\"" + this.color + "\"";
	}
	*/
	
}