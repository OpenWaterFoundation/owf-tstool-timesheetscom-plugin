// Customer - results from items/customer service

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
 * timesheets.com "items/customer" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Customer {
	// Alphabetize.
	
	/**
	 * "ADDRESS1"
	 */
	@JsonProperty("ADDRESS1")
	private String address1 = "";
	
	/**
	 * "ADDRESS2"
	 */
	@JsonProperty("ADDRESS2")
	private String address2 = "";
	
	/**
	 * "BUSINESSFAX"
	 */
	@JsonProperty("BUSINESSFAX")
	private String businessFax = "";
	
	/**
	 * "BUSINESSPHONE"
	 */
	@JsonProperty("BUSINESSPHONE")
	private String businessPhone = "";
	
	/**
	 * "CITY"
	 */
	@JsonProperty("CITY")
	private String city = "";

	/**
	 * "COMMENTS"
	 */
	@JsonProperty("COMMENTS")
	private String comments = "";
	
	/**
	 * "CONTACTEMAIL"
	 */
	@JsonProperty("CONTACTEMAIL")
	private String contactEmail = "";
	
	/**
	 * "CONTACTNAME"
	 */
	@JsonProperty("CONTACTNAME")
	private String contactName = "";
	
	/**
	 * "CONTACTPHONE"
	 */
	@JsonProperty("CONTACTPHONE")
	private String contactPhone = "";
	
	/**
	 * "CREATORNAME"
	 */
	@JsonProperty("CREATORNAME")
	private String creatorName = "";
	
	/**
	 * "CUSTOMERID"
	 */
	@JsonProperty("CUSTOMERID")
	private String customerId = "";
	
	/**
	 * "CUSTOMERSTATUS"
	 */
	@JsonProperty("CUSTOMERSTATUS")
	private String customerStatus = "";
	
	/**
	 * "COMPANYRID"
	 */
	@JsonProperty("COMPANYID")
	private String companyId = "";
	
	/**
	 * "CREATEDATE"
	 */
	@JsonProperty("CREATEDATE")
	private String creaateDate = "";
	
	/**
	 * "CREATORUSERID"
	 */
	@JsonProperty("CREATORUSERID")
	private String creatorUserId = "";
	
	/**
	 * "CUSTOMERNAME"
	 */
	@JsonProperty("CUSTOMERNAME")
	private String customerName = "";
	
	/**
	 * "CUSTOMERNAMEPLAIN"
	 */
	@JsonProperty("CUSTOMERNAMEPLAIN")
	private String customerNamePlain = "";
	
	/**
	 * "CUSTOMERNUMBER"
	 */
	@JsonProperty("CUSTOMERNUMBER")
	private String customerNumber = "";
	
	/**
	 * "READONLY"
	 */
	@JsonProperty("READONLY")
	private String readOnly = "";
	
	/**
	 * "STATE"
	 */
	@JsonProperty("STATE")
	private String state = "";
	
	/**
	 * "ZIP"
	 */
	@JsonProperty("ZIP")
	private String zip = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public Customer () {
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