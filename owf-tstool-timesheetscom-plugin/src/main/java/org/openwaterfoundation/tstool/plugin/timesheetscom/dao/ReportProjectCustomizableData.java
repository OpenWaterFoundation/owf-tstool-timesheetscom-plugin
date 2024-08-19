// ReportProjectCustomizable - results from report/project/customizable service

/* NoticeStart

OWF TSTool timesheetscom Plugin
Copyright (C) 2023 - 2024 Open Water Foundation

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
	private Float hoursAsFloat = null;

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
		// 'workDate' is by default in format "January, 21 2021 00:00:00" so parse for DateTime.
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

		// Convert hours to float.
		this.hoursAsFloat = Float.valueOf(this.hours);

		// Convert the project ID to an integer.
		this.projectIdAsInteger = Integer.valueOf(this.projectId);
	}

	/**
	 * Return the account code ID.
	 * @return the account code ID
	 */
	public String getAccountCodeId () {
		return this.accountCodeId;
	}

	/**
	 * Return the account code name.
	 * @return the account code name
	 */
	public String getAccountCodeName () {
		return this.accountCodeName;
	}

	/**
	 * Return the approved flag.
	 * @return the approved flag
	 */
	public String getApproved () {
		return this.approved;
	}

	/**
	 * Return the archived flag.
	 * @return the archived flag
	 */
	public String getArchived () {
		return this.archived;
	}

	/**
	 * Return whether billable.
	 * @return whether billable
	 */
	public String getBillable () {
		return this.billable;
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
	 * Return the customer number.
	 * @return the customer number
	 */
	public String getCustomerNumber () {
		return this.customerNumber;
	}

	/**
	 * Return the department.
	 * @return the department
	 */
	public String getDepartment () {
		return this.department;
	}

	/**
	 * Return the employee number.
	 * @return the employee number
	 */
	public String getEmployeeNumber () {
		return this.employeeNumber;
	}

	/**
	 * Return the employee type.
	 * @return the employee type
	 */
	public String getEmployeeType () {
		return this.employeeType;
	}

	/**
	 * Return the user first name.
	 * @return the user first name
	 */
	public String getFirstName () {
		return this.firstName;
	}

	/**
	 * Return the hours as a string
	 * @return the hours as a string
	 */
	public String getHours () {
		return this.hours;
	}

	/**
	 * Return the hours as a floating point number.
	 * @return the hours as a floating point number.
	 */
	public Float getHoursAsFloat () {
		return this.hoursAsFloat;
	}

	/**
	 * Return the job title.
	 * @return the job title
	 */
	public String getJobTitle () {
		return this.jobTitle;
	}

	/**
	 * Return the last change date.
	 * @return the last change date
	 */
	public String getLastChangeDate () {
		return this.lastChangeDate;
	}

	/**
	 * Return the user last name.
	 * @return the user last name
	 */
	public String getLastName () {
		return this.lastName;
	}

	/**
	 * Return the pay type.
	 * @return the pay type
	 */
	public String getPayType () {
		return this.payType;
	}

	/**
	 * Return the project ID.
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
	 * Return the project name.
	 * @return the project name
	 */
	public String getProjectName () {
		return this.projectName;
	}

	/**
	 * Return the record ID.
	 * @return the record ID
	 */
	public String getRecordId () {
		return this.recordId;
	}

	/**
	 * Return the signed.
	 * @return the signed
	 */
	public String getSigned () {
		return this.signed;
	}

	/**
	 * Return the user ID.
	 * @return the user ID
	 */
	public String getUserId () {
		return this.userId;
	}

	/**
	 * Get the work date as as string.
	 * @return the work date as as string
	 */
	public String getWorkDate() {
		return this.workDate;
	}

	/**
	 * Get the work date as DateTime.
	 */
	public DateTime getWorkDateAsDateTime () {
		return this.workDateTime;
	}

	/**
	 * Get the work description.
	 */
	public String getWorkDescription () {
		return this.workDescription;
	}

}