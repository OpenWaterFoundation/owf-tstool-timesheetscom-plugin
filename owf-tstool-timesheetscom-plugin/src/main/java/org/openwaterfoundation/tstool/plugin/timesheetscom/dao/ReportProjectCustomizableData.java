// ReportProjectCustomizable - results from report/project/customizable service

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
 * timesheets.com "report/project/customizable" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReportProjectCustomizableData {
	// Alphabetize.
	
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
	 * "APPROVED"
	 */
	@JsonProperty("APPROVED")
	private String approved = "";
	
	/**
	 * "ARCHIVED"
	 */
	@JsonProperty("ARCHIVED")
	private String archived = "";
	
	/**
	 * "BILLABLE"
	 */
	@JsonProperty("BILLABLE")
	private String billable = "";
	
	/**
	 * "CUSTOMERID"
	 */
	@JsonProperty("CUSTOMERID")
	private String customerId = "";
	
	/**
	 * "CUSTOMERNAME"
	 */
	@JsonProperty("CUSTOMERNAME")
	private String customerName = "";
	
	/**
	 * "CUSTOMERNUMBER"
	 */
	@JsonProperty("CUSTOMERNUMBER")
	private String customerNumber = "";
	
	/**
	 * "DEPARTMENT"
	 */
	@JsonProperty("DEPARTMENT")
	private String department = "";
	
	/**
	 * "EMPLOYEENUMBER"
	 */
	@JsonProperty("EMPLOYEENUMBER")
	private String employeeNumber = "";
	
	/**
	 * "EMPLOYEETYPE"
	 */
	@JsonProperty("EMPLOYEETYPE")
	private String employeeType = "";
	
	/**
	 * "FIRSTNAME"
	 */
	@JsonProperty("FIRSTNAME")
	private String firstName = "";
	
	/**
	 * "HOURS"
	 */
	@JsonProperty("HOURS")
	private String hours = "";
	
	/**
	 * Hours as a floating point number.
	 */
	@JsonIgnore
	private Float hoursFloat = null;
	
	/**
	 * "JOBTITLE"
	 */
	@JsonProperty("JOBTITLE")
	private String jobTitle = "";
	
	/**
	 * "LASTCHANGEDATE"
	 */
	@JsonProperty("LASTCHANGEDATE")
	private String lastChangeDate = "";
	
	/**
	 * "LASTNAME"
	 */
	@JsonProperty("LASTNAME")
	private String lastName = "";
	
	/**
	 * "PAYTYPE"
	 */
	@JsonProperty("PAYTYPE")
	private String payType = "";
	
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
	 * "RECORDID"
	 */
	@JsonProperty("RECORDID")
	private String recordId = "";
	
	/**
	 * "SIGNED"
	 */
	@JsonProperty("SIGNED")
	private String signed = "";
	
	/**
	 * "USERID"
	 */
	@JsonProperty("USERID")
	private String userId = "";
	
	/**
	 * "WORKDATE"
	 */
	@JsonProperty("WORKDATE")
	private String workDate = "";
	
	/**
	 * Word date as a DateTime, to help with time series processing.
	 */
	@JsonIgnore
	private DateTime workDateTime = null;
	
	/**
	 * "WORKDESCRIPTION"
	 */
	@JsonProperty("WORKDESCRIPTION")
	private String workDescription = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public ReportProjectCustomizableData () {
	}
	
	/**
	 * Clean the data:
	 * - create workDateTime
	 * - create hoursInteger
	 */
	public void cleanData () {
		// workDate is by default in format "January, 21 2021 00:00:00"
		// so parse for DateTime.
		String [] parts = this.workDate.replace(",", "").trim().split(" ");
		int monthNumber = -1;
		for ( int i = 0; i < 12; i++ ) {
			String monthName = TimeUtil.MONTH_NAMES[i];
			if ( monthName.equalsIgnoreCase(parts[0]) ) {
				monthNumber = i + 1;
				break;
			}
		}
		this.workDateTime = DateTime.parse(
			String.format("%4.4s-%02d-%02d", parts[2], monthNumber, Integer.valueOf(parts[1]) ) );
		
		// Convert hours to integer.
		this.hoursFloat = Float.valueOf(this.hours);
	}
	
	/**
	 * Return the customer ID.
	 * @return the customer ID 
	 */
	public String getCustomerId () {
		return this.customerId;
	}
	
	/**
	 * Return the customer name.
	 * @return the customer name 
	 */
	public String getCustomerName () {
		return this.customerName;
	}
	
	/**
	 * Return the user first name.
	 * @return the user first name
	 */
	public String getFirstName () {
		return this.firstName;
	}
	
	/**
	 * Return the hours as a floating point number.
	 * @return the hours as a floating point number.
	 */
	public Float getHoursFloat () {
		return this.hoursFloat;
	}
	
	/**
	 * Return the user last name.
	 * @return the user last name
	 */
	public String getLastName () {
		return this.lastName;
	}
	
	/**
	 * Return the project ID.
	 * @return the project ID
	 */
	public String getProjectId () {
		return this.projectId;
	}
	
	/**
	 * Return the project name.
	 * @return the project name
	 */
	public String getProjectName () {
		return this.projectName;
	}
	
	/**
	 * Return the user ID.
	 * @return the user ID 
	 */
	public String getUserId () {
		return this.userId;
	}
	
	/**
	 * Get the work date as DateTime.
	 */
	public DateTime getWorkDateTime () {
		return this.workDateTime;
	}
	
}