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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	 * Customer identifier as an integer.
	 */
	@JsonIgnore
	private int customerIdAsInteger = -1;
	
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
	 * "CREATEDDATE"
	 */
	@JsonProperty("CREATEDDATE")
	private String createdDate = "";
	
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
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
		this.customerIdAsInteger = Integer.valueOf(this.customerId);
	}

	/**
	 * Return address1
	 * @return address1
	 */
	public String getAddress1 () {
		return this.address1;
	}

	/**
	 * Return address2
	 * @return address2
	 */
	public String getAddress2 () {
		return this.address2;
	}

	/**
	 * Return businessFax
	 * @return businessFax
	 */
	public String getBusinessFax () {
		return this.businessFax;
	}

	/**
	 * Return businessPhone
	 * @return businessPhone
	 */
	public String getBusinessPhone () {
		return this.businessPhone;
	}

	/**
	 * Return city
	 * @return city
	 */
	public String getCity () {
		return this.city;
	}

	/**
	 * Return comments
	 * @return comments
	 */
	public String getComments () {
		return this.comments;
	}

	/**
	 * Return companyId
	 * @return companyId
	 */
	public String getCompanyId () {
		return this.companyId;
	}

	/**
	 * Return contactEmail
	 * @return contactEmail
	 */
	public String getContactEmail () {
		return this.contactEmail;
	}

	/**
	 * Return contactName
	 * @return contactName
	 */
	public String getContactName () {
		return this.contactName;
	}

	/**
	 * Return contactPhone
	 * @return contactPhone
	 */
	public String getContactPhone () {
		return this.contactPhone;
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
	 * Return the customer ID.
	 * @return the customer ID 
	 */
	public String getCustomerId () {
		return this.customerId;
	}
	
	/**
	 * Return the customer ID as an integer.
	 * @return the customer ID as an integer 
	 */
	public int getCustomerIdAsInteger () {
		return this.customerIdAsInteger;
	}
	
	/**
	 * Return the customer name.
	 * @return the customer name 
	 */
	public String getCustomerName () {
		return this.customerName;
	}
	
	/**
	 * Return the customer name plain.
	 * @return the customer name plain
	 */
	public String getCustomerNamePlain () {
		return this.customerNamePlain;
	}
	
	/**
	 * Return the customer number.
	 * @return the customer number 
	 */
	public String getCustomerNumber () {
		return this.customerNumber;
	}
	
	/**
	 * Return the customer status.
	 * @return the customer status 
	 */
	public String getCustomerStatus () {
		return this.customerStatus;
	}
	
	/**
	 * Return whether read only.
	 * @return whether read only 
	 */
	public String getReadOnly () {
		return this.readOnly;
	}

	/**
	 * Return state.
	 * @return state
	 */
	public String getState () {
		return this.state;
	}
	
	/**
	 * Return zip.
	 * @return zip
	 */
	public String getZip () {
		return this.zip;
	}

	/**
 	* Lookup a customer given its identifier.
 	* @param customerList list of Customer to search
 	* @param id customer ID to match
 	* @return matching customer or null if not found
 	*/
	public static Customer lookupCustomerForCustomerId ( List<Customer> customerList, int id ) {
		if ( customerList == null ) {
			return null;
		}
		if ( id < 0 ) {
			return null;
		}
		for ( Customer customer : customerList ) {
			int customerId = customer.getCustomerIdAsInteger();
			if ( (customerId > 0) && (customerId == id) ) {
				return customer;
			}
		}
		// Not found.
		return null;
	}
	
}