// TimesheetsDataStore - class that implements the timesheetscom plugin datastore

/* NoticeStart

OWF TSTool timeshetscom Plugin
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

package org.openwaterfoundation.tstool.plugin.timesheetscom.datastore;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openwaterfoundation.tstool.plugin.timesheetscom.PluginMeta;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.AccountCode;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.Customer;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.Project;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableReportData;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableData;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableDataComparator;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableRecord;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ServerConstants;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.TimeSeriesCatalogComparator;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.User;
import org.openwaterfoundation.tstool.plugin.timesheetscom.ui.TimesheetsCom_TimeSeries_CellRenderer;
import org.openwaterfoundation.tstool.plugin.timesheetscom.ui.TimesheetsCom_TimeSeries_InputFilter_JPanel;
import org.openwaterfoundation.tstool.plugin.timesheetscom.ui.TimesheetsCom_TimeSeries_TableModel;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dto.HttpCodeException;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dto.JacksonToolkit;

import com.fasterxml.jackson.databind.JsonNode;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.Message.Message;
import RTi.Util.String.MultiKeyStringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

/**
 * Datastore for timesheets.com web services.
 */
public class TimesheetsComDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Standard request parameters:
	 * - for now don't use
	 */
	private final String COMMON_REQUEST_PARAMETERS = "";

	// TODO smalers 2023-11-13 why is this separate from the built-in datastore properties?
	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	Map<String,Object> pluginProperties = new LinkedHashMap<>();

	/**
	 * Global account codes.
	 */
	List<AccountCode> accountCodeList = new ArrayList<>();

	/**
	 * Global customer list.
	 */
	List<Customer> customerList = new ArrayList<>();

	/**
	 * Global project list.
	 */
	List<Project> projectList = new ArrayList<>();

	/**
	 * Global report/project/customizable data:
	 * - necessary because need to evaluate all data records to get the list of time series
	 *
	 * The data model is as follows:
	 *
	 * ReportProjectCustomizableReportDataList:
	 * - list of ReportProjectCustomizableReportData (typically one entry)
	 *   - each has one ReportProjectCustomizableRecord
	 *     - includes a list of ReportProjectCustomizableData
	 */
	List<ReportProjectCustomizableReportData> reportProjectCustomizableReportDataList = new ArrayList<>();

	/**
	 * List of all daily time sheet data records from the above.
	 */
	List<ReportProjectCustomizableData> allTimesheetData = new ArrayList<>();
	
	/**
	 * Expiration time at which global data will be refreshed.
	 */
	OffsetDateTime globalDataExpirationTime = null;

	/**
	 * Expiration time offset in seconds:
	 * - the 'globalDataExpirationTime' will be set this far into the future when global data are read
	 * - don't use a time that is too short because timesheets.com may restrict access due to high retries
	 */
	long globalDataExpirationOffset = 3600;

	/**
	 * Global location ID list, used to streamline creating lists for UI choices,
	 * determined when the tscatalogList is read.
	 */
	List<String> locIdList = new ArrayList<>();
	
	/**
	 * Global data read problems:
	 * - if not empty, this should be set as an error in the ReadTimesheetsCom command to indicate incomplete data
	 */
	List<String> globalDataProblems = new ArrayList<>();

	/**
	 * Global server constants.
	 */
	Map<String,Object> serverConstants = new LinkedHashMap<>();

	/**
	 * Global time series catalog, used to streamline creating lists for UI choices.
	 */
	List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

	/**
	 * Global user list.
	 */
	List<User> userList = new ArrayList<>();

	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store
	@param description name for the data store
	@param dmi DMI instance to use for the data store.
	*/
	public TimesheetsComDataStore ( String name, String description, URI serviceRootURI, PropList props ) {
		String routine = getClass().getSimpleName() + ".TimesheetsComDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootURI );
	    // Set the properties in the datastore.
	    setProperties ( props );

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation timesheetscom (timesheets.com) data web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with timesheets.com web services.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}
	
	/**
	 * Check global data to evaluate whether it has expired.
	 * If the global data are expired, read it again.
	 */
	public void checkGlobalDataExpiration () {
		String routine = getClass().getSimpleName() + ".checkGlobalDataExpiration";
		OffsetDateTime now = OffsetDateTime.now();
		if ( (this.globalDataExpirationTime != null) && now.isAfter(this.globalDataExpirationTime) ) {
			// Global data have expired so read it again.
			Message.printStatus(2, routine, "Global data have expired.  Reading current data.");
			readGlobalData();
		}
	}

	/**
 	* Check the web service requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore timesheets-owf version >= 1.5.5
 	* @require datastore timesheets-owf ?configproperty propname? == Something
 	*
 	* @enabledif datastore timesheets-owf version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "TimesheetsComDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore timesheets-owf version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore timesheets-owf configuration
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("xxx") ) {
				// Leave this code in as an example.
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'xxx' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether timesheets.com datastore has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "timesheets.com configuration 'xxx' value is not defined." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "timesheets.com configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "timesheets.com configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2023-11-11 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nsdataws-mhfd version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}

	}

	/**
	 * Create the time series catalog.
	 * @param dataList list of all timesheet data records
	 */
	private void createTimeSeriesCatalog ( List<ReportProjectCustomizableData> dataList ) {
		String routine = this.getClass().getSimpleName() + ".createTimeSeriesCatalog";

		// Start with the top-level report/project/customizable list and check whether a time series
		// has been added.
		Message.printStatus(2, routine, "Processing " + dataList.size() + " time series records into time series catalog.");
		for ( ReportProjectCustomizableData data : dataList ) {
			if ( data.getHoursAsFloat() > .001 ) {
				// Don't include zero hours because some project hours may have been zeroed out to correct issues.
				if ( TimeSeriesCatalog.findForData ( this.tscatalogList, data ) == null ) {
					// Time series was not found so add it.
					this.tscatalogList.add ( new TimeSeriesCatalog ( data, Project.findForProjectId(this.projectList, data.getProjectId()) ) );
				}
			}
		}
		Message.printStatus(2, routine, "Created " + this.tscatalogList.size() + " time series catalog.");
	}

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		TimesheetsCom_TimeSeries_InputFilter_JPanel ifp = new TimesheetsCom_TimeSeries_InputFilter_JPanel(this, 4);
		return ifp;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		// First query the database for the specified input.
		List<TimeSeriesCatalog> tsmetaList = readTimeSeriesMeta ( dataType, timeStep, ifp );
		return getTimeSeriesListTableModel(tsmetaList);
	}

	/**
 	* Create a work table with standard columns.
 	* @param workTableID identifier for the table to be created
 	* @return new table with standard columns
 	*/
	public DataTable createWorkTable ( String workTableID ) {
    	DataTable table = new DataTable();
    	table.setTableID ( workTableID );
    	// Currently columns are hand-coded so don't need to handle dynamically.
    	//int workTableDateColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "Date", -1, -1), null);
    	//int workUserColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_STRING, "Person", -1, -1), null);
    	//int workHoursColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_FLOAT, "Hours", -1, 1), null);
    	//int workDescriptionColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_STRING, "Description", -1, -1), null);
    	return table;
	}

    /**
     * Get the global list of account codes.
     * @return the global list of account codes
     */
    public List<AccountCode> getAccountCodeCache () {
    	return this.accountCodeList;
    }

	// TODO smalers 2023-11-18 maybe create this list up front as global data.
	/**
	 * Get all the ReportProjectCustomizableData
	 * @return all the ReportProjectCustomizableData
	 */
	private List<ReportProjectCustomizableData> getAllReportProjectCustomizableData () {
		List<ReportProjectCustomizableData> dataList = new ArrayList<>();

		// Get the array of "ReportData", which is an array:
		// - should have
		for ( ReportProjectCustomizableReportData reportData : this.reportProjectCustomizableReportDataList ) {
			ReportProjectCustomizableRecord record = reportData.getReportProjectCustomizableRecord();
			dataList.addAll ( record.getReportProjectCustomizableDataList() );
		}
		return dataList;
	}

	/**
	 * Return the API key used in the header of requests.
	 */
	public String getApiKey () {
		//Object prop = this.pluginProperties.get("ApiKey");
		Object prop = getProperties().getValue("ApiKey");
		if ( prop == null ) {
			return null;
		}
		else {
			return (String)prop;
		}
	}

	/**
	 * Return the authentication string used in the header of requests.
	 */
	public String getAuthorization () {
		//Object prop = this.pluginProperties.get("Authorization");
		Object prop = getProperties().getValue("Authorization");
		if ( prop == null ) {
			return null;
		}
		else {
			return (String)prop;
		}
	}

    /**
     * Get the global list of cached customers.
     * @return the global list of cached customers
     */
    public List<Customer> getCustomerCache () {
    	return this.customerList;
    }

	/**
	 * Get the unique customer names for the specified data type and interval.
	 * @param dataType the data type to match, can be null, empty, or "*"
	 * @param interval the data interval to match, can be null, empty, or "*"
	 */
    public List<String> getCustomerNamesForDataTypeAndInterval ( String dataType, String interval ) {
    	List<String> customerNames = new ArrayList<>();
    	boolean doCheckDataType = false;
    	if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
    		doCheckDataType = true;
    	}
    	boolean doCheckInterval = false;
    	if ( (interval != null) && !interval.isEmpty() && !interval.equals("*") ) {
    		doCheckInterval = true;
    	}
    	// Loop through the TimeSeriesCatalog and find the unique customer names.
    	for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
    		if ( doCheckDataType ) {
    			if ( !dataType.equals(tscatalog.getDataType()) ) {
    				// Data type does not match.
    				continue;
    			}
    		}
    		if ( doCheckInterval ) {
    			if ( !interval.equalsIgnoreCase(tscatalog.getDataInterval()) ) {
    				// Data interval does not match.
    				continue;
    			}
    		}
    		// If here add if not already added.
    		boolean found = false;
    		String customerName = tscatalog.getCustomerName();
    		for ( String customerName2 : customerNames ) {
    			if ( customerName2.equals(customerName) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			customerNames.add(customerName);
    		}
    	}
    	
    	Collections.sort ( customerNames, String.CASE_INSENSITIVE_ORDER );
    	return customerNames;
    }

    /**
     * Get the global data expiration offset in seconds.
     * @return the global data expiration offset in seconds
     */
    public long getGlobalDataExpirationOffset () {
    	return this.globalDataExpirationOffset;
    }

    /**
     * Get the global data expiration time.
     * @return the global data expiration time
     */
    public OffsetDateTime getGlobalDataExpirationTime () {
    	return this.globalDataExpirationTime;
    }

    /**
     * Get the global data problems.
     * @return the global data problems list
     */
    public List<String> getGlobalDataProblems () {
    	return this.globalDataProblems;
    }

	/**
	 * Get the HTTP request properties (HTTP headers).
	 * This must be added to all HTTP requests.
	 * @return a dictionary of HTTP request headers.
	 */
	public MultiKeyStringDictionary getHttpRequestProperties () {
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("apikey", getApiKey());
		requestProperties.add("x-ts-authorization", getAuthorization());
		requestProperties.add("accept", "application/json" );
		return requestProperties;
	}

	/**
	 * Get the list of location identifier strings used in the UI.
	 * The list is determined from the cached list of time series catalog.
	 * @param dataType to match, or * or null to return all, should be a value of stationparameter_no
	 * @return a unique sorted list of the location identifiers (station_no)
	 */
	public List<String> getLocIdStrings ( String dataType ) {
		if ( (dataType == null) || dataType.isEmpty() || dataType.equals("*") ) {
			// Return the cached list of all locations.
			return this.locIdList;
		}
		else {
			// Get the list of locations from the cached list of time series catalog
			List<String> locIdList = new ArrayList<>();
			boolean found = false;
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {

				if ( !tscatalog.getDataType().equals(dataType) ) {
					// Requested data type does not match.
					continue;
				}

				found = false;
				for ( String locId2 : locIdList ) {
					if ( locId2.equals(tscatalog.getLocId()) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					locIdList.add(tscatalog.getLocId());
				}
			}
			Collections.sort(locIdList, String.CASE_INSENSITIVE_ORDER);
			return locIdList;
		}
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(), entry.getValue());
    	}
		return pluginProperties;
	}

    /**
     * Get the global list of cached projects.
     * @return the global list of cached projects
     */
    public List<Project> getProjectCache () {
    	return this.projectList;
    }

	/**
	 * Get the unique project names for the specified data type, interval, and customer name.
	 * @param dataType the data type to match, can be null, empty, or "*"
	 * @param interval the data interval to match, can be null, empty, or "*"
	 * @param customerName the customer name to match, can be null, empty, or "*"
	 */
    public List<String> getProjectNamesForDataTypeAndIntervalAndCustomerName (
    	String dataType, String interval, String customerName ) {
    	List<String> projectNames = new ArrayList<>();
    	boolean doCheckDataType = false;
    	if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
    		doCheckDataType = true;
    	}
    	boolean doCheckInterval = false;
    	if ( (interval != null) && !interval.isEmpty() && !interval.equals("*") ) {
    		doCheckInterval = true;
    	}
    	boolean doCheckCustomerName = false;
    	if ( (customerName != null) && !customerName.isEmpty() && !customerName.equals("*") ) {
    		doCheckCustomerName = true;
    	}
    	// Loop through the TimeSeriesCatalog and find the unique project names.
    	for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
    		if ( doCheckDataType ) {
    			if ( !dataType.equals(tscatalog.getDataType()) ) {
    				// Data type does not match.
    				continue;
    			}
    		}
    		if ( doCheckInterval ) {
    			if ( !interval.equalsIgnoreCase(tscatalog.getDataInterval()) ) {
    				// Data interval does not match.
    				continue;
    			}
    		}
    		if ( doCheckCustomerName ) {
    			if ( !customerName.equalsIgnoreCase(tscatalog.getCustomerName()) ) {
    				// Customer name does not match.
    				continue;
    			}
    		}
    		// If here add if not already added.
    		boolean found = false;
    		String projectName = tscatalog.getProjectName();
    		for ( String projectName2 : projectNames ) {
    			if ( projectName2.equals(projectName) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			projectNames.add(projectName);
    		}
    	}
    	
    	Collections.sort ( projectNames, String.CASE_INSENSITIVE_ORDER );
    	return projectNames;
    }

    /**
     * Get the cached ReportProjectCustomizableData.
     * This is a list of reports, which each have data records.
     * @return the cached ReportProjectCustomizableData.
     */
    public List<ReportProjectCustomizableReportData> getReportProjectCustomizableDataCache () {
    	return this.reportProjectCustomizableReportDataList;
    }

	/**
	 * Get the unique user first names for the specified data type, interval, customer name, project name,
	 * and user last name.
	 * @param dataType the data type to match, can be null, empty, or "*"
	 * @param interval the data interval to match, can be null, empty, or "*"
	 * @param customerName the customer name to match, can be null, empty, or "*"
	 * @param projectName the project name to match, can be null, empty, or "*"
	 * @param userLastName the user last name to match, can be null, empty, or "*"
	 */
    public List<String> getUserFirstNamesForDataTypeAndIntervalAndCustomerNameAndProjectNameAndUserLastName (
    	String dataType, String interval, String customerName, String projectName, String userLastName ) {
    	List<String> userFirstNames = new ArrayList<>();
    	boolean doCheckDataType = false;
    	if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
    		doCheckDataType = true;
    	}
    	boolean doCheckInterval = false;
    	if ( (interval != null) && !interval.isEmpty() && !interval.equals("*") ) {
    		doCheckInterval = true;
    	}
    	boolean doCheckCustomerName = false;
    	if ( (customerName != null) && !customerName.isEmpty() && !customerName.equals("*") ) {
    		doCheckCustomerName = true;
    	}
    	boolean doCheckProjectName = false;
    	if ( (projectName != null) && !projectName.isEmpty() && !projectName.equals("*") ) {
    		doCheckProjectName = true;
    	}
    	boolean doCheckUserLastName = false;
    	if ( (userLastName != null) && !userLastName.isEmpty() && !userLastName.equals("*") ) {
    		doCheckUserLastName = true;
    	}
    	// Loop through the TimeSeriesCatalog and find the unique user first names.
    	for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
    		if ( doCheckDataType ) {
    			if ( !dataType.equals(tscatalog.getDataType()) ) {
    				// Data type does not match.
    				continue;
    			}
    		}
    		if ( doCheckInterval ) {
    			if ( !interval.equalsIgnoreCase(tscatalog.getDataInterval()) ) {
    				// Data interval does not match.
    				continue;
    			}
    		}
    		if ( doCheckCustomerName ) {
    			if ( !customerName.equalsIgnoreCase(tscatalog.getCustomerName()) ) {
    				// Customer name does not match.
    				continue;
    			}
    		}
    		if ( doCheckProjectName ) {
    			if ( !projectName.equalsIgnoreCase(tscatalog.getProjectName()) ) {
    				// Project name does not match.
    				continue;
    			}
    		}
    		if ( doCheckUserLastName ) {
    			if ( !userLastName.equalsIgnoreCase(tscatalog.getUserLastName()) ) {
    				// User last name does not match.
    				continue;
    			}
    		}
    		// If here add if not already added.
    		boolean found = false;
    		String userFirstName = tscatalog.getUserFirstName();
    		for ( String userFirstName2 : userFirstNames ) {
    			if ( userFirstName2.equals(userFirstName) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			userFirstNames.add(userFirstName);
    		}
    	}
    	
    	Collections.sort ( userFirstNames, String.CASE_INSENSITIVE_ORDER );
    	return userFirstNames;
    }

	/**
	 * Get the unique project names for the specified data type, interval, and customer name.
	 * @param dataType the data type to match, can be null, empty, or "*"
	 * @param interval the data interval to match, can be null, empty, or "*"
	 * @param customerName the customer name to match, can be null, empty, or "*"
	 * @param projectName the project name to match, can be null, empty, or "*"
	 */
    public List<String> getUserLastNamesForDataTypeAndIntervalAndCustomerNameAndProjectName (
    	String dataType, String interval, String customerName, String projectName ) {
    	List<String> userLastNames = new ArrayList<>();
    	boolean doCheckDataType = false;
    	if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
    		doCheckDataType = true;
    	}
    	boolean doCheckInterval = false;
    	if ( (interval != null) && !interval.isEmpty() && !interval.equals("*") ) {
    		doCheckInterval = true;
    	}
    	boolean doCheckCustomerName = false;
    	if ( (customerName != null) && !customerName.isEmpty() && !customerName.equals("*") ) {
    		doCheckCustomerName = true;
    	}
    	boolean doCheckProjectName = false;
    	if ( (projectName != null) && !projectName.isEmpty() && !projectName.equals("*") ) {
    		doCheckProjectName = true;
    	}
    	// Loop through the TimeSeriesCatalog and find the unique user last names.
    	for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
    		if ( doCheckDataType ) {
    			if ( !dataType.equals(tscatalog.getDataType()) ) {
    				// Data type does not match.
    				continue;
    			}
    		}
    		if ( doCheckInterval ) {
    			if ( !interval.equalsIgnoreCase(tscatalog.getDataInterval()) ) {
    				// Data interval does not match.
    				continue;
    			}
    		}
    		if ( doCheckCustomerName ) {
    			if ( !customerName.equalsIgnoreCase(tscatalog.getCustomerName()) ) {
    				// Customer name does not match.
    				continue;
    			}
    		}
    		if ( doCheckProjectName ) {
    			if ( !projectName.equalsIgnoreCase(tscatalog.getProjectName()) ) {
    				// Project name does not match.
    				continue;
    			}
    		}
    		// If here add if not already added.
    		boolean found = false;
    		String userLastName = tscatalog.getUserLastName();
    		for ( String userLastName2 : userLastNames ) {
    			if ( userLastName2.equals(userLastName) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			userLastNames.add(userLastName);
    		}
    	}
    	
    	Collections.sort ( userLastNames, String.CASE_INSENSITIVE_ORDER );
    	return userLastNames;
    }

	/**
	 * Return the maximum number of days in a request.
	 * @return the maximum number of days in a request
	 */
	public int getRequestDayLimit () {
		Object prop = getProperties().getValue("RequestDayLimit");
		if ( prop == null ) {
			return 366;
		}
		else {
			return Integer.valueOf((String)prop);
		}
	}

	/**
	 * Return the list of time series catalog.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<TimeSeriesCatalog> getTimeSeriesCatalog(boolean readData) {
		if ( readData ) {
			String dataTypeReq = null;
			String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		String tsid = null;
			this.tscatalogList = readTimeSeriesCatalog(tsid, dataTypeReq, dataIntervalReq, ifp);
		}
		return this.tscatalogList;
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings ( String dataType ) {
		//boolean includeWildcards = true;
		boolean includeWildcards = false;
		return getTimeSeriesDataIntervalStrings(dataType, includeWildcards);
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings ( String dataType, boolean includeWildcards ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		List<String> dataIntervals = new ArrayList<>();
		Message.printStatus(2, routine, "Getting interval strings for data type \"" + dataType + "\"");

		// Currently the interval is always hour.
		dataIntervals.add("Day");

		// Sort the intervals:
		// - TODO smalers need to sort by time
		Collections.sort(dataIntervals,String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end

			dataIntervals.add("*");
			if ( dataIntervals.size() > 1 ) {
				// Also add at the beginning to simplify selections:
				// - could check for a small number like 5 but there should always be a few
				dataIntervals.add(0,"*");
			}
		}

		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the dataTypes.name properties from the stationSummaries web service request.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings ( String dataInterval ) {
		//boolean includeWildcards = true;
		boolean includeWildcards = false;
		return getTimeSeriesDataTypeStrings(dataInterval, includeWildcards );
	}

	/**
	 * Return the list of time series data type strings.
	 * These strings are the same as the parameter type list 'parametertype_name'.
	 */
	public List<String> getTimeSeriesDataTypeStrings ( String dataInterval, boolean includeWildcards ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataTypeStrings";

		boolean checkDataInterval = false;
		if ( (dataInterval != null) && !dataInterval.isEmpty() ) {
			checkDataInterval = true;
		}

		List<String> dataTypes = new ArrayList<>();

		// Get the unique list of data types from the time series catalog.

		boolean dataIntervalMatched = false;
		String dataType = null;
		for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
			if ( checkDataInterval ) {
				if ( !dataInterval.equals(tscatalog.getDataInterval()) ) {
					dataIntervalMatched = false;
				}
			}
			else {
				// Intervals re not being checked.
				dataIntervalMatched = true;
			}
			// Check whether the data type has been found before.
			boolean found = false;
			if ( dataIntervalMatched ) {
				// Data interval matched so OK to continue checking.
				dataType = tscatalog.getDataType();
				for ( String dataType2 : dataTypes ) {
					if ( dataType.equals(dataType2) ) {
						found = true;
						break;
					}
				}
			}
			if ( dataIntervalMatched && !found ) {
				// Add the data type from the TimeSeriesCatalog.
				dataTypes.add ( dataType );
			}
		}

		// Sort the names.
		Collections.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Add wildcard at the front and end - allows querying all data types for the location:
			// - always add so that people can get a full list
			// - adding at the top makes it easy to explore data without having to scroll to the end

			dataTypes.add("*");
			dataTypes.add(0,"*");
		}

		return dataTypes;
	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel,
		int row ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesIdentifierFromTableModel";
    	TimesheetsCom_TimeSeries_TableModel tm = (TimesheetsCom_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	//String locId = (String)tableModel.getValueAt(row,tm.COL_LOCATION_ID);
    	String source = "TimeSeriesCom"; // TODO smalers 2023-11-11 evaluate whether can get organization ID.
    	String dataType = (String)tableModel.getValueAt(row,tm.COL_DATA_TYPE);
    	String interval = (String)tableModel.getValueAt(row,tm.COL_DATA_INTERVAL);
    	String scenario = "";
    	String inputName = ""; // Only used for files.
    	TSIdent tsid = null;
		String datastoreName = this.getName();
		String locId = "";
   		locId = "" + tableModel.getValueAt(row,tm.COL_LOCID);
    	try {
    		tsid = new TSIdent(locId, source, dataType, interval, scenario, datastoreName, inputName );
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	return tsid;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return new TimesheetsCom_TimeSeries_CellRenderer ((TimesheetsCom_TimeSeries_TableModel)tableModel);
    }

    /**
     * Get the TableModel used for displaying the time series.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return new TimesheetsCom_TimeSeries_TableModel(this,(List<TimeSeriesCatalog>)data);
    }

    /**
     * Get the global list of cached users.
     * @return the global list of cached users
     */
    public List<User> getUserCache () {
    	return this.userList;
    }

    /**
     * Determine if a project is active.
     * @param projectIdNumber project identifier number
     * @return true if the project status is active, false otherwise
     */
    public boolean projectIsActive ( int projectIdNumber ) {
    	for ( Project project : this.projectList ) {
    		if ( project.getProjectIdAsInteger() == projectIdNumber ) {
    			// Found the matching project.
    			if ( project.getProjectStatusAsInteger() == 1 ) {
    				// Project is active.
    				return true;
    			}
    			else {
    				return false;
    			}
    		}
    	}
    	return false;
    }

    /**
     * Determine if a project is archived.
     * @param projectIdNumber project identifier number
     * @return true if the project status is archive, false otherwise
     */
    public boolean projectIsArchived ( int projectIdNumber ) {
    	for ( Project project : this.projectList ) {
    		if ( project.getProjectIdAsInteger() == projectIdNumber ) {
    			// Found the matching project.
    			if ( project.getProjectStatusAsInteger() == 0 ) {
    				// Project is archived.
    				return true;
    			}
    			else {
    				return false;
    			}
    		}
    	}
    	return false;
    }

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return true;
	}

	/**
 	* Read the item/accountcode objects.  Results look like:
 	*  {
  "errors" : [ ],
  "data" : {
    "maxrows" : "50",
    "startrow" : "1",
    "items" : {
      "RowCount" : 8,
      "Columns" : [ "ACCOUNTCODEID", "ACCOUNTCODENAME", "ACCOUNTCODENAMEPLAIN", "ACCOUNTCODEDESCRIPTION", "DEFAULTPAYRATE", "CREATEDDATE", "CREATORUSERID", "READONLY", "ACCOUNTCODESTATUS", "CREATORNAME", "USERPAYRATE", "DEFAULTUSERPAYRATE" ],
      "Data" : [ {
        "ACCOUNTCODEID" : "121832",
        "ACCOUNTCODENAME" : "Uncategorized",
        "ACCOUNTCODENAMEPLAIN" : "Uncategorized",
        "ACCOUNTCODEDESCRIPTION" : "Uncategorized",
        "DEFAULTPAYRATE" : "0.00",
        "CREATEDDATE" : "June, 27 2018 20:01:21",
        "CREATORUSERID" : "212163",
        "READONLY" : "1",
        "ACCOUNTCODESTATUS" : "1",
        "CREATORNAME" : "Joe Smith",
        "USERPAYRATE" : "",
        "DEFAULTUSERPAYRATE" : "0.00"
      }
 	* @return a list of AccountCode.
 	*/
	private List<AccountCode> readAccountCodes() throws IOException {
		String routine = getClass().getSimpleName() + ".readAccountCodes";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/items/accountcode";
		Message.printStatus(2, routine, "Reading account codes from: " + requestUrl);
		List<AccountCode> accountCodeList = new ArrayList<>();
		//String arrayName = "Data";
		//String arrayName = "maxrows";
		//String arrayName = "data";
		String [] elements = { "data", "items", "Data" };

		// If the HTTP request returns 420, need to wait and try again.
		int wait = 0;
		int waitMax = 600000;
		JsonNode jsonNode = null;
		while ( true ) {
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					this.debug, requestUrl, getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 1/2 second
						wait = 500;
					}
					else {
						// Double the wait.
						wait = (int)(wait * 2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read account codes data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Go to the top of the loop and try to read again.
					continue;
				}
			}
			if ( jsonNode == null ) {
				Message.printStatus(2, routine, "  Reading account codes returned null.");
				// Break out of the read loop.
				break;
			}
			else {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " account codes items.");
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					accountCodeList.add((AccountCode)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), AccountCode.class));
				}
				// Break out of the read loop.
				break;
			}
		}
		return accountCodeList;
	}

	/**
 	* Read the item/customer objects.  Results look like:
 	* {
  "errors" : [ ],
  "data" : {
    "maxrows" : "50",
    "startrow" : "1",
    "items" : {
      "RowCount" : 41,
      "Columns" : [ "CUSTOMERID", "COMPANYID", "CREATORUSERID", "CREATEDDATE", "CUSTOMERNAME", "CUSTOMERNAMEPLAIN", "CUSTOMERNUMBER", "ADDRESS1", "ADDRESS2", "CITY", "STATE", "ZIP", "BUSINESSPHONE", "BUSINESSFAX", "CONTACTNAME", "CONTACTPHONE", "CONTACTEMAIL", "COMMENTS", "CUSTOMERSTATUS", "READONLY", "CREATORNAME" ],
      "Data" : [ {
        "CUSTOMERID" : "144078",
        "COMPANYID" : "37471",
        "CREATORUSERID" : "212163",
        "CREATEDDATE" : "June, 27 2018 20:01:21",
        "CUSTOMERNAME" : "Unassigned",
        "CUSTOMERNAMEPLAIN" : "Unassigned",
        "CUSTOMERNUMBER" : "",
        "ADDRESS1" : "",
        "ADDRESS2" : "",
        "CITY" : "",
        "STATE" : "",
        "ZIP" : "",
        "BUSINESSPHONE" : "",
        "BUSINESSFAX" : "",
        "CONTACTNAME" : "",
        "CONTACTPHONE" : "",
        "CONTACTEMAIL" : "",
        "COMMENTS" : "",
        "CUSTOMERSTATUS" : "1",
        "READONLY" : "1",
        "CREATORNAME" : "Joe Smith"
      },
      ...
 	* @return a list of Customer.
 	*/
	private List<Customer> readCustomers() throws IOException {
		String routine = getClass().getSimpleName() + ".readCustomers";
		StringBuilder requestUrl = new StringBuilder(getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/items/customer");
		// Get active and archived customers.
		requestUrl.append("?Status=CUSTOMERSTATUS_ACTIVE,CUSTOMERSTATUS_ARCHIVED");
		// Get all rows.
		requestUrl.append("&MaxRows=1000");
		Message.printStatus(2, routine, "Reading customers from: " + requestUrl);
		List<Customer> customerList = new ArrayList<>();
		String [] elements = { "data", "items", "Data" };

		// If the HTTP request returns 420, need to wait and try again.
		int wait = 0;
		int waitMax = 600000;
		JsonNode jsonNode = null;
		while ( true ) {
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					this.debug, requestUrl.toString(), getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 1/2 second
						wait = 500;
					}
					else {
						// Double the wait.
						wait = (int)(wait * 2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read customer data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Go to the top of the loop and try to read again.
					continue;
				}
			}
			if ( jsonNode == null ) {
				Message.printStatus(2, routine, "  Reading customers returned null.");
				// Break out of the read loop.
				break;
			}
			else {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " customer items.");
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					Customer customer = (Customer)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Customer.class);
					// Clean the data:
					// - convert strings to other types
					customer.cleanData();
					customerList.add(customer);
				}
				// Break out of the read loop.
				break;
			}
		}
		return customerList;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );
		OffsetDateTime now = OffsetDateTime.now();
		this.globalDataExpirationTime = now.plusSeconds(this.globalDataExpirationOffset);
		Message.printWarning ( 2, routine, "Global data will expire at: " + this.globalDataExpirationTime );

		// Add to avoid Eclipse warning if 'debug' is not used.
		if ( debug ) {
		}

		// Clear the global data problems.
		this.globalDataProblems.clear();

		// Server constants:
		// - read first because may be used for later logic

		try {
			List<ServerConstants> serverConstantsList = readServerConstants();
			if ( serverConstantsList.size() == 1 ) {
				this.serverConstants = serverConstantsList.get(0).getServerConstants();
				Message.printStatus(2, routine, "Read " + this.serverConstants.size() + " server constants." );
			}
			else {
				Message.printWarning(3, routine, "Error reading global server constants (read " +
					serverConstantsList.size() + " 'data' objects.");
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global server constants (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global server constants data.");
		}

		// Account code objects.

		try {
			List<AccountCode> accountCodeList0 = readAccountCodes();
			if ( (accountCodeList0.size() == 0) && (this.accountCodeList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 account codes." );
				Message.printStatus(2, routine, "Keeping " + this.accountCodeList.size() + " previously read account code data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.accountCodeList = accountCodeList0;
				Message.printStatus(2, routine, "Read " + this.accountCodeList.size() + " account codes." );
				if ( Message.isDebugOn ) {
					for ( AccountCode ac : this.accountCodeList ) {
						Message.printStatus(2, routine, "Account code: " + ac );
					}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global account codes (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global account codes data.");
		}

		// Customer objects.

		try {
			List<Customer> customerList0 = readCustomers();
			if ( (customerList0.size() == 0) && (this.customerList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 customers." );
				Message.printStatus(2, routine, "Keeping " + this.customerList.size() + " previously read customer data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.customerList = customerList0;
				Message.printStatus(2, routine, "Read " + this.customerList.size() + " customers." );
				if ( Message.isDebugOn ) {
					//for ( Customer customer : this.customerList ) {
					//	Message.printStatus(2, routine, "Customer: " + customer );
					//}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global customers (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global customers data.");
		}

		// Project objects.

		try {
			List<Project> projectList0 = readProjects();
			if ( (projectList0.size() == 0) && (this.projectList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 projects." );
				Message.printStatus(2, routine, "Keeping " + this.projectList.size() + " previously read project data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.projectList = projectList0;
				Message.printStatus(2, routine, "Read " + this.projectList.size() + " projects." );
				if ( Message.isDebugOn ) {
					//for ( Project project : this.projectList ) {
					//	Message.printStatus(2, routine, "Project: " + project );
					//}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global projects (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global projects data.");
		}

		// User objects:
		// - this handles HTTP 420

		try {
			List<User> userList0 = readUsers();
			if ( (userList0.size() == 0) && (this.userList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 users." );
				Message.printStatus(2, routine, "Keeping " + this.userList.size() + " previously read user data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.userList = userList0;
				Message.printStatus(2, routine, "Read " + this.userList.size() + " users." );
				if ( Message.isDebugOn ) {
					//for ( User user : this.userList ) {
					//	Message.printStatus(2, routine, "User: " + user );
					//}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global users (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global user data.");
		}

		// Customizable project report:
		// - put at the end because often triggers HTTP 420 error that requires wait
		// - will be used in readTimeSeries

		try {
			List<ReportProjectCustomizableReportData> reportProjectCustomizableReportDataList0 = readReportProjectCustomizable();
			if ( (reportProjectCustomizableReportDataList0.size() == 0) && (this.reportProjectCustomizableReportDataList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 report customizable records." );
				Message.printStatus(2, routine, "Keeping " + this.reportProjectCustomizableReportDataList.size() + " previously read report data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.reportProjectCustomizableReportDataList = reportProjectCustomizableReportDataList0;
				Message.printStatus(2, routine, "Read " + this.reportProjectCustomizableReportDataList.size()
					+ " report customizable records with a total of "
					+ ReportProjectCustomizableReportData.size(this.reportProjectCustomizableReportDataList) + " hourly project records." );
				/*
				if ( Message.isDebugOn ) {
					for ( ReportProjectCustomizableReportData data : this.reportProjectCustomizableReportDataList ) {
						Message.printStatus(2, routine, "Report project customizable report data: " + data );
					}
				}
				*/
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading report project customizable (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global report project customizable data.");
		}

		// The time series catalog is created by examining all timesheet records because there is no other
		// way to get a distinct list of records.
		// The cached list is used to create choices for the UI in order to ensure fast performance.
		// Therefore the slowdown is only at TSTool startup.
		try {
			// Create the time series catalog:
			this.allTimesheetData = getAllReportProjectCustomizableData ();
			createTimeSeriesCatalog ( this.allTimesheetData );
			Collections.sort(this.tscatalogList, new TimeSeriesCatalogComparator() );

			// Sort the lists.
			Collections.sort(this.locIdList,String.CASE_INSENSITIVE_ORDER);
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series catalog list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
	}


	/**
 	* Read the item/project objects.  Results look like:
 	*  {
  "errors" : [ ],
  "data" : {
    "maxrows" : "50",
    "startrow" : "1",
    "items" : {
      "RowCount" : 50,
      "Columns" : [ "PROJECTID", "PROJECTNAME", "PROJECTNAMEPLAIN", "PROJECTDESCRIPTION", "DEFAULTBILLRATE", "MINIMUMTIMEINCREMENT", "CREATEDDATE", "CREATORUSERID", "READONLY", "PROJECTSTATUS", "CREATORNAME", "USERBILLRATE", "DEFAULTUSERBILLRATE" ],
      "Data" : [ {
        "PROJECTID" : "261572",
        "PROJECTNAME" : "Unassigned",
        "PROJECTNAMEPLAIN" : "Unassigned",
        "PROJECTDESCRIPTION" : "Unassigned",
        "DEFAULTBILLRATE" : "0.00",
        "MINIMUMTIMEINCREMENT" : "0",
        "CREATEDDATE" : "June, 27 2018 20:01:21",
        "CREATORUSERID" : "212163",
        "READONLY" : "1",
        "PROJECTSTATUS" : "1",
        "CREATORNAME" : "Joe Smith",
        "USERBILLRATE" : "",
        ...
 	* @return a list of Project.
 	*/
	private List<Project> readProjects() throws IOException {
		String routine = getClass().getSimpleName() + ".readProjects";
		StringBuilder requestUrl = new StringBuilder ( getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/items/project" );
		// Get active and archived projects (default is archived).
		requestUrl.append ( "?Status=PROJECTSTATUS_ACTIVE,PROJECTSTATUS_ARCHIVED");
		// Get all the projects (default is 50).
		requestUrl.append ( "&MaxRows=1000");
		Message.printStatus(2, routine, "Reading projects from: " + requestUrl);
		List<Project> projectList = new ArrayList<>();
		String [] elements = { "data", "items", "Data" };
		boolean debug = this.debug;
		// Set debug = true to output the JSON to the log file.
		//boolean debug = true;

		// If the HTTP request returns 420, need to wait and try again.
		int wait = 0;
		int waitMax = 600000;
		JsonNode jsonNode = null;
		while ( true ) {
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					debug, requestUrl.toString(), getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 1/2 second
						wait = 500;
					}
					else {
						// Double the wait.
						wait = (int)(wait * 2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read project data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Go to the top of the loop and try to read again.
					continue;
				}
			}
			if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " project items.");
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					projectList.add((Project)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Project.class));
				}
				// Clean the data:
				// - convert strings to other types
				for ( Project project : projectList ) {
					project.cleanData();
				}
				// Break out of the read loop.
				break;
			}
			else {
				Message.printStatus(2, routine, "  Reading projects returned null.");
				// Break out of the read loop.
				break;
			}
		}
		return projectList;
	}

	/**
 	* Read the report/project/customizable objects.
 	* The full period back to January 1, 2015 is read.
 	* Results look like:
 	*  {
  "errors" : [ ],
  "processing" : {
    "writetostorage" : "false",
    "queued" : "false",
    "reportuuid" : ""
  },
  "report" : {
      ...
      "ReportData" : [ {
      "TotalHours" : "14.70",
      "Records" : {
        "RowCount" : 7,
        "Columns" : [ "USERID", "FIRSTNAME", "LASTNAME", "JOBTITLE", "EMPLOYEETYPE", "EMPLOYEENUMBER", "PAYTYPE", "DEPARTMENT", "CUSTOMERID", "CUSTOMERNAME", "CUSTOMERNUMBER", "PROJECTID", "PROJECTNAME", "ACCOUNTCODEID", "ACCOUNTCODENAME", "BILLABLE", "WORKDESCRIPTION", "RECORDID", "WORKDATE", "SIGNED", "APPROVED", "ARCHIVED", "HOURS", "LASTCHANGEDATE" ],
        "Data" : [ {
          "USERID" : "212173",
          "FIRSTNAME" : "Steve",
          "LASTNAME" : "Malers",
          "JOBTITLE" : "Chief Technology Officer",
          "EMPLOYEETYPE" : "1",
          "EMPLOYEENUMBER" : "2",
          "PAYTYPE" : "SALARY",
          "DEPARTMENT" : "",
          "CUSTOMERID" : "144145",
          "CUSTOMERNAME" : "AZ-County-Maricopa",
          "CUSTOMERNUMBER" : "",
          "PROJECTID" : "261817",
          "PROJECTNAME" : "Project - AZ-County-Maricopa - Support",
          "ACCOUNTCODEID" : "121843",
          "ACCOUNTCODENAME" : "BILL-SUPPORT",
          "BILLABLE" : "1",
          "WORKDESCRIPTION" : "Email related to web services and Esri integration",
          "RECORDID" : "25086757",
          "WORKDATE" : "February, 24 2022 00:00:00",
          "SIGNED" : "0",
          "APPROVED" : "1",
          "ARCHIVED" : "1",
          "HOURS" : "0.20",
          "LASTCHANGEDATE" : "September, 14 2023 22:48:13"
        }
  ...
 	* @return a list of Project.
 	*/
	private List<ReportProjectCustomizableReportData> readReportProjectCustomizable() throws IOException {
		String routine = getClass().getSimpleName() + ".readReportProjectCustomizable";
		List<ReportProjectCustomizableReportData> reportDataList = new ArrayList<>();

		OffsetDateTime now = OffsetDateTime.now();
		// End year for the request.
		int endYear = 0;
		// Increment for the year, based on the datastore RequestDayLimit property, set by timesheets.com.
		int yearIncrement = getRequestDayLimit()/366;
		if ( yearIncrement*366 > getRequestDayLimit() ) {
			// Make sure roundoff does not cause the increment to be longer than the number of days allowed.
			--yearIncrement;
		}
		Message.printStatus(2, routine, "Year increment for reading customizable report data = " + yearIncrement +
			" (based on RequestDayLimit datastore property).");
		int yearMin = 2015;
		int ndataYear = 0;
		// Used to break out of the year read loop.
		boolean haveData = true;
		boolean firstYear = true;
		// Milliseconds to wait between requests, needed because the service complains if too many requests occur too close together.
		int wait = 0;
		// Maximum milliseconds to wait so that the software does not hang:
		// - 10 minutes, but hopefully will never be that high
		int waitMax = 600000;
		while ( true ) {
			// Loop backward in time by calendar year:
			// - the API only allows reading one year;
			//   although it could be extended, use one year queries for general use
			// - break when there are no records returned
			// - if queries occur too fast, the server will return a 420 error so build in a wait if that occurs
			String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/report/project/customizable";
			String startDate = null;
			String endDate = null;
			if ( firstYear ) {
				endYear = now.getYear();
				firstYear = false;
			}
			else {
				// Decrement the year by the increment.
				endYear -= yearIncrement;
			}
			if ( endYear < yearMin ) {
				// Break to make sure not an infinite loop.
				Message.printStatus(2, routine, "  Trying to read before minimum year " + endYear + " - end reading customizable reports.");
				break;
			}
			ndataYear = 0;
			// Add one year since processing complete years.
			startDate = String.format("%04d-01-01", endYear - yearIncrement + 1);
			DateTime startDate_DateTime = DateTime.parse(startDate);
			endDate = String.format("%04d-12-31", endYear );
			DateTime endDate_DateTime = DateTime.parse(endDate);
			Message.printStatus(2, routine, "Reading /report/project/customizable for " + startDate + " to " + endDate );
			requestUrl +=
				// Period.
				"?StartDate=" + startDate +
				"&EndDate=" + endDate +
				// All other parameters, alphabetized.
				// All account codes.
				"&AllAccountCodes=1" +
				// All customers.
				"&AllCustomers=1" +
				// All employees:
				// - changed to AllUsers in API version 1.0.1?
				//"&AllEmployees=1" +
				// All users:
				// - replaces AllEmployees in API version 1.0.1?
				"&AllUsers=1" +
				// Includes all active projects:
				// - seems to include archived projects also
				"&AllProjects=1" +
				// For now, don't rely on the approved status.
				"&Approved=RECORD_UNAPPROVED,RECORD_APPROVED" +
				// Not sure that this matters but helpful when reviewing raw data.
				"&GroupType=Project" +
				// Include the work description by default.
				"&IncludeWorkDescription=1" +
				// For now, don't rely on the billable status.
				"&ProjectRecordBillableStatus=RECORD_BILLABLE,RECORD_UNBILLABLE" +
				// All records, including archived.
				"&ProjectRecordStatus=PROJECTRECORDSTATUS_ALL" +
				"&ReportType=Detailed" +
				// For now, don't rely on the signed status.
				"&Signed=RECORD_UNSIGNED,RECORD_SIGNED";
			Message.printStatus(2, routine, "Reading report project customizable from: " + requestUrl);
			String [] elements = { "report", "ReportData" };
			JsonNode jsonNode = null;
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					this.debug, requestUrl, getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 30 seconds based on some testing, but unable to totally nail down
						wait = 30000;
					}
					else  {
						// Increase the wait by 20%.
						wait = (int)(wait * 1.2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read all project customizable data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Increment the year since going backwards and go to the top of the loop again.
					endYear = endYear + yearIncrement;
					continue;
				}
			}
			if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " ReportData objects for " + startDate + " to " + endDate + ".");
				ndataYear = 0;
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					ReportProjectCustomizableReportData reportData =
						(ReportProjectCustomizableReportData)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), ReportProjectCustomizableReportData.class);
					int ndata = reportData.getReportProjectCustomizableRecord().getReportProjectCustomizableDataList().size();
					Message.printStatus(2, routine, "    Read " + ndata + " individual timesheet Data objects for " + startDate + " to " + endDate + ".");
					if ( ndata == 0 ) {
						// No data records in the year:
						// - assume no more data
						haveData = false;
						Message.printStatus(2, routine, "    No data for " + startDate + " to " + endDate + " so assuming the end of data.");
						break;
					}
					ndataYear += ndata;
					// Start and end are for information and troubleshooting.
					reportData.setStartDate ( startDate_DateTime );
					reportData.setEndDate ( endDate_DateTime );
					// Clean the data, including converting strings to other data types.
					reportData.cleanData();
					// Add the report data to the full data list.
					reportDataList.add(reportData);
				}
				Message.printStatus(2, routine, "    Read " + ndataYear + " total timesheet Data objects for " + startDate + " to " + endDate + ".");
				if ( !haveData ) {
					// Break out of the outside loop:
					// - may have no data due to not reading archived data
					//break;
				}
			}
			else {
				Message.printStatus(2, routine, "  Reading report project customizable for " + startDate + " to " + endDate + " returned null or zero objects.");
				// May have a complete year with no data so continue looping until the minimum year.
			}
		}
		return reportDataList;
	}

	/**
 	* Read the server/constants objects.  Results look like:
 	*  {
  		"errors" : [ ],
  		"data" : {
    		"TIMECLASS_HOLIDAY" : "5",
    		"PAYROLL_CODE_PROCESS_VALIDATION_ERROR" : "8",
    		"TIMELABEL_SALARIED" : "Unpayable/Salaried",
    		"VENDORSTATUS_ACTIVE" : "1",
    		...
    	}
    }
 	* @return a list of ServerConstant.
 	*/
	private List<ServerConstants> readServerConstants() throws IOException {
		String routine = getClass().getSimpleName() + ".readServerConstants";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/server/constants";
		Message.printStatus(2, routine, "Reading server constants from: " + requestUrl);
		List<ServerConstants> serverConstantsList = new ArrayList<>();
		// Constants are in the top-level "data" array.
		String [] elements = null;

		// If the HTTP request returns 420, need to wait and try again.
		int wait = 0;
		int waitMax = 600000;
		JsonNode jsonNode = null;
		while ( true ) {
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					this.debug, requestUrl, getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 1/2 second
						wait = 500;
					}
					else {
						// Double the wait.
						wait = (int)(wait * 2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read server constants data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Go to the top of the loop and try to read again.
					continue;
				}
			}
			if ( jsonNode == null ) {
				Message.printStatus(2, routine, "  Reading server constants returned null.");
				// Break out of the read loop.
				break;
			}
			else {
				// Will include top level "errors" and "data".
				//Message.printStatus(2, routine, "  Read " + jsonNode.size() + " server constants.");
				serverConstantsList.add((ServerConstants)JacksonToolkit.getInstance().treeToValue(jsonNode, ServerConstants.class));
				Map<String,Object> constants = serverConstantsList.get(0).getServerConstants();
				for ( Map.Entry<String, Object> entry : constants.entrySet() ) {
					Message.printStatus(2, routine, "Server constant " + entry.getKey() + " = " + entry.getValue() );
				}
				// Break out of the read loop.
				break;
			}
		}
		return serverConstantsList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * @param tsid time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	try {
    		HashMap<String,Object> props = null;
    		DataTable workTable = null;
    		return readTimeSeries ( tsid, readStart, readEnd, readData, props, workTable );
    	}
    	catch ( Exception e ) {
    		// Throw a RuntimeException since the method interface does not include an exception type.
    		Message.printWarning(2, routine, e);
    		throw new RuntimeException ( e );
    	}
    }

    /**
     * Read a single time series given its time series identifier.
     * @param tsidReq requested time series identifier.
     * The output time series may be different depending on the requested properties.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> "Debug" - if Boolean true, turn on debug for the query</li>
     * <li> "Hours" - if "Archived" only include archived data, if "New", only include new (unarchived) data, default is include all data</li>
     * <li> "DataFlag" - if "Archived" set the data flag to ARCHIVED, if "Archived0", only set if 0, if "Archived1", only set if 1.</li>
     * <li> "ProjectStatus" - "Active" (default), "Archived", or "All" - NOT ENABLED
     * </ul>
     * @param workTable if null, table to set work notes
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsidReq, DateTime readStart, DateTime readEnd,
    	boolean readData, HashMap<String,Object> readProperties, DataTable workTable ) throws Exception {
    	// Check whether the global data have expired and reread if necessary.
    	checkGlobalDataExpiration();
    	
    	//String routine = getClass().getSimpleName() + ".readTimeSeries";

    	// Get the properties of interest.
    	
    	// Whether to include new and/or archived data.
    	if ( readProperties == null ) {
    		// Create an empty hashmap if necessary to avoid checking for null below.
    		readProperties = new HashMap<>();
    	}
    	boolean doArchived = true;
    	boolean doNew = true;
    	Object propValueO = readProperties.get("IncludeHours" );
    	if ( propValueO != null )  {
    		String propValue = (String)propValueO;
    		if ( propValue.equalsIgnoreCase("Archived") ) {
    			doArchived = true;
    			doNew = false;
    		}
    		else if ( propValue.equalsIgnoreCase("New") ) {
    			doArchived = false;
    			doNew = true;
    		}
    	}

    	// Whether to set the data flag to the archived flag.
    	boolean doFlagArchived = false;
    	boolean doFlagArchived0 = false;
    	boolean doFlagArchived1 = false;
    	propValueO = readProperties.get("DataFlag" );
    	if ( propValueO != null )  {
    		String propValue = (String)propValueO;
    		if ( propValue.equalsIgnoreCase("Archived") ) {
    			doFlagArchived = true;
    		}
    		else if ( propValue.equalsIgnoreCase("Archived0") ) {
    			doFlagArchived0 = true;
    		}
    		else if ( propValue.equalsIgnoreCase("Archived1") ) {
    			doFlagArchived1 = true;
    		}
    	}

    	// Find the matching TimeSeriesCatalog.

    	TimeSeriesCatalog tscatalog = TimeSeriesCatalog.findForTSID ( this.tscatalogList, tsidReq );
    	if ( tscatalog == null ) {
    		throw new Exception ("Unable to find TSID=\"" + tsidReq + "\" in the time series catalog.");
    	}

    	TS ts = null;

    	// Create the time series and set properties.

   		try {
   			ts = TSUtil.newTimeSeries(tsidReq, true);
   		}
   		catch ( Exception e ) {
   			throw new RuntimeException ( e );
   		}
   		// Set the properties.
   		try {
   			ts.setIdentifier(tsidReq);
   		}
   		catch ( Exception e ) {
   			throw new RuntimeException ( e );
   		}

    	// Get the data records.

    	// Set the period to bounding data records.
    	if ( readStart != null ) {
    		ts.setDate1Original(readStart);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the start down to include a full interval.
    			readStart.round(-1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate1(readStart);
    	}
    	if ( readEnd != null ) {
    		ts.setDate2Original(readEnd);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the end up to include a full interval
    			readEnd.round(1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate2(readEnd);
    	}

    	// Set standard properties:
    	// - use station name for the description because the station parameter name seems to be terse
		ts.setDescription( tscatalog.getProjectName() );
		ts.setDataUnits("Hours");
		ts.setDataUnitsOriginal("Hours");
		ts.setMissing(Double.NaN);

		// Set the time series properties:
		// - these can then be accessed in workflows using ${ts:Property} syntax
		setTimeSeriesProperties ( ts, tscatalog );

    	if ( readData ) {
    		// Also read the time series values:
    		// - first get the matching data records
   			List<ReportProjectCustomizableData> dataList = readTimeSeries_GetData ( tscatalog, this.allTimesheetData );
   			if ( dataList.size() > 0 ) {
   				// Set the original dates to what is available in the full dataset.
   				ts.setDate1Original(dataList.get(0).getWorkDateAsDateTime());
   				ts.setDate2Original(dataList.get(dataList.size() - 1).getWorkDateAsDateTime());

   				// Set the start and end if not specified above.
   				if ( readStart == null ) {
   					ts.setDate1(dataList.get(0).getWorkDateAsDateTime());
   				}
   				if ( readEnd == null ) {
   					ts.setDate2(dataList.get(dataList.size() - 1).getWorkDateAsDateTime());
   				}

    			// Allocate the time series data array.
   				ts.allocateDataSpace();

   				// Transfer the TimeSeriesValue list to the TS data.

   				readTimeSeries_TransferData ( ts, dataList,
   					doArchived, doNew,
   					doFlagArchived, doFlagArchived0, doFlagArchived1,
   					workTable );
   				//Message.printStatus(2,routine, "Transferring " + timeSeriesValueList.size() + " time series values.");
   			}
    	}

    	return ts;
    }

    /**
     * Get the timesheet records for a time series.
     * @param ts time series to be filled
     * @param tscatalog time series catalog for the time series
     * @param dataList list of all timesheet data records
     * @return the data records specific to the time series
     */
   	private List<ReportProjectCustomizableData> readTimeSeries_GetData ( TimeSeriesCatalog tscatalog,
   		List<ReportProjectCustomizableData> dataList ) {
   		List<ReportProjectCustomizableData> matchedDataList = new ArrayList<>();
   		// Loop through all the data records.

   		for ( ReportProjectCustomizableData data : dataList ) {
   			if ( !data.getCustomerId().equals(tscatalog.getCustomerId()) ) {
   				continue;
   			}
   			if ( !data.getProjectId().equals(tscatalog.getProjectId()) ) {
   				continue;
   			}
   			if ( !data.getUserId().equals(tscatalog.getUserId()) ) {
   				continue;
   			}
   			// If here the data matched.
   			matchedDataList.add(data);
   		}

   		// Sort the data records ascending by the work date.
   		Collections.sort ( matchedDataList, new ReportProjectCustomizableDataComparator() );
   		return matchedDataList;
   	}

    /**
     * Transfer the timesheet records to time series.
     * Only non-zero hours are transferred.
     * @param ts time series to be filled
     * @param dataList list of timesheet data records
     * @param doArchived if true, include archived hours in the output, otherwise don't include
     * @param doNew if true, include new hours in the output, otherwise don't include
     * @param doFlagArchived set the data flag to the ARCHIVED value
     * @param doFlagArchived0 set the data flag to the ARCHIVED value, if 0
     * @param doFlagArchived1 set the data flag to the ARCHIVED value, if 1
     * @param workTable data table containing work notes
     */
   	private void readTimeSeries_TransferData (
   		TS ts, List<ReportProjectCustomizableData> dataList,
   		boolean doArchived, boolean doNew,
   		boolean doFlagArchived, boolean doFlagArchived0, boolean doFlagArchived1,
   		DataTable workTable ) {
   		// Check whether the time series identifier includes "Unassigned" and if so print a warning to the log file to help track down.
   		String routine = null;
   		boolean isUnassigned = false;
   		if ( ts.getIdentifierString().contains("Unassigned") ) {
   			isUnassigned = true;
   			routine = getClass().getSimpleName() + ".readTimeSeries_TransferData";
   		}
   		else if ( Message.isDebugOn ) {
   			routine = getClass().getSimpleName() + ".readTimeSeries_TransferData";
   		}
   		String flag = null;
   		DateTime dt = null;
   		double value;
   		String archived = null;
   		for ( ReportProjectCustomizableData data : dataList ) {
   			// First get the existing data value.
   			flag = null;
   			dt = data.getWorkDateAsDateTime();
   			value = ts.getDataValue(dt);
   			if ( value < .001 ) {
   				// Treat as zero:
   				// - don't add to the time series or table below because it is likely a timesheet correction.
   				continue;
   			}
   			
   			archived = data.getArchived();

   			// Determine whether to include the data.
   			if ( doArchived && doNew ) {
   				// All data are included.
   			}
   			else if ( doArchived && !archived.equals("1") ) {
   				// Don't include because not archived.
   				continue;
   			}
   			else if ( doNew && !archived.equals("0") ) {
   				// Don't include because not new.
   				continue;
   			}

   			// Determine the data flag from ARCHIVED:
   			// - ARCHIVED is 0 or 1
   			if ( doFlagArchived ) {
   				flag = archived;
   			}
   			else if ( doFlagArchived0 && archived.equals("0") ) {
   				flag = archived;
   			}
   			else if ( doFlagArchived1 && archived.equals("1") ) {
   				flag = archived;
   			}
   			
   			// Set the data value.
   			
   			if ( ts.isDataMissing(ts.getDataValue(dt)) ) {
   				// Current time series value is missing:
   				// - set the value
   				if ( flag == null ) {
   					ts.setDataValue(dt, data.getHoursAsFloat());
   				}
   				else {
   					ts.setDataValue(dt, data.getHoursAsFloat(), flag, -1);
   				}
   			}
   			else {
   				// Add to the existing value.
   				if ( flag == null ) {
   					ts.setDataValue(dt, (value + data.getHoursAsFloat()) );
   				}
   				else {
   					ts.setDataValue(dt, (value + data.getHoursAsFloat()), flag, -1 );
   				}
   			}
   			
   			// Print a warning if unassigned.
   			if ( isUnassigned ) {
   				Message.printWarning(3, routine, "Have unassigned data for ts \"" + ts.getIdentifierString() +
   					"\" date=" + dt + " value=" + data.getHoursAsFloat() );
   			}
   			
   			// If the work table is not null, add a table record:
   			// - the positions are hard-coded and standard
   			
   			if ( workTable != null ) {
   				try {
   					TableRecord rec = new TableRecord();
   					// Can use the original DateTime since it won't be changed.
   					rec.addFieldValue(dt);
   					rec.addFieldValue(data.getLastName() + ", " + data.getFirstName() );
   					rec.addFieldValue(Float.valueOf(data.getHoursAsFloat()));
   					rec.addFieldValue(data.getWorkDescription());
					workTable.addRecord(rec);
   				}
   				catch ( Exception e ) {
   					// Should not happen and could generate a lot of output.
   					if ( Message.isDebugOn ) {
   						Message.printWarning(3, routine, "Error adding record to work table.");
   						Message.printWarning(3, routine, e);
   					}
   				}
   			}
   		}
   	}

	/**
	 * Read time series catalog, which uses the cached data from the "/report/project/customizable" web service query.
	 * @param tsidReq requested time series identifier
	 * @param dataTypeReq requested data type (e.g., "TotalHours") or "*" to read all data types,
	 *        or null to use default of "*".
	 * @param dataIntervalReq requested data interval (e.g., "1Day") or "*" to read all intervals,
	 *        or null to use default of "*".
	 * @param ifp input filter panel with "where" conditions
	 */
	public List<TimeSeriesCatalog> readTimeSeriesCatalog ( String tsidReq, String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
		//String routine = getClass().getSimpleName() + ".readTimeSeriesCatalog";
    	// Check whether the global data have expired and reread if necessary.
    	checkGlobalDataExpiration();

		// Indicate whether the data type should be matched.
		boolean doDataTypeReq = false;
		if ( (dataTypeReq != null) && !dataTypeReq.isEmpty() && !dataTypeReq.equals("*") ) {
			doDataTypeReq = true;
		}

		// Indicate whether the data interval should be matched.
		boolean doDataIntervalReq = false;
		if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
			doDataIntervalReq = true;
		}

		boolean readFromCache = true;
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

		if ( readFromCache ) {
			// Read the time series list from the cache.
			boolean matched = false;
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
				// Default is matched until a specified criteria is not met.
				matched = true;

				if ( doDataTypeReq ) {
					if ( !dataTypeReq.equals(tscatalog.getDataType())) {
						matched = false;
					}
				}

				if ( doDataIntervalReq ) {
					if ( !dataIntervalReq.equals(tscatalog.getDataInterval())) {
						matched = false;
					}
				}

				// Filter the time series list based on the input filters.
				if ( ifp != null ) {
	        		int nfg = ifp.getNumFilterGroups ();
	        		InputFilter filter;
	        		// Loop through the filter groups.
	        		for ( int ifg = 0; ifg < nfg; ifg++ ) {
	        			// Get the filter that is active for the group.
				    	filter = ifp.getInputFilter(ifg);
				    	String operator = ifp.getOperator(ifg);
				    	String whereLabel = filter.getWhereLabelPersistent();
				    	if ( whereLabel.equals("customerName") ) {
				    		if ( !filter.matches(tscatalog.getCustomerName(), operator, false) ) {
				    			matched = false;
				    			break;
				    		}
				    	}
				    	else if ( whereLabel.equals("projectName") ) {
				    		if ( !filter.matches(tscatalog.getProjectName(), operator, false) ) {
				    			matched = false;
				    			break;
				    		}
				    	}
				    	else if ( whereLabel.equals("projectStatus") ) {
				    		// Only operator of equals is enabled so just compare.
				    		String value = filter.getInputInternal();
				    		if ( value.equals("Active") && !tscatalog.getProjectStatus().equals("1") ) {
				    			matched = false;
				    			break;
				    		}
				    		else if ( value.equals("Archived") && !tscatalog.getProjectStatus().equals("0") ) {
				    			matched = false;
				    			break;
				    		}
				    	}
				    	else if ( whereLabel.equals("userFirstName") ) {
				    		if ( !filter.matches(tscatalog.getUserFirstName(), operator, false) ) {
				    			matched = false;
				    			break;
				    		}
				    	}
				    	else if ( whereLabel.equals("userLastName") ) {
				    		if ( !filter.matches(tscatalog.getUserLastName(), operator, false) ) {
				    			matched = false;
				    			break;
				    		}
				    	}
	        		}
				}

				// If matched, add to the list.
				if ( matched ) {
					tscatalogList.add ( tscatalog );
				}
			}
		}

		// The list will already be sorted based on the global catalog list.

		return tscatalogList;
	}

    /**
     * Read time series metadata.
     * Currently, all timesheets.com catalog data are cached at startup.
     * @param dataTypeReq requested data type
     * @param dataIntervalReq requested data interval
     * @param ifp input filter panel from UI
     */
    List<TimeSeriesCatalog> readTimeSeriesMeta ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
    	// Remove note from data type, if used.
	   	int pos = dataTypeReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataTypeReq = dataTypeReq.substring(0, pos);
	   	}
    	// Remove note from data type, if used.
	   	pos = dataIntervalReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataIntervalReq = dataIntervalReq.substring(0, pos).trim();
	   	}

	   	// By default all time series are included in the catalog:
	   	// - the filter panel options can be used to constrain
	   	// - the time series identifier is not used
	   	String tsidReq = null;
	    return readTimeSeriesCatalog ( tsidReq, dataTypeReq, dataIntervalReq, ifp );
	}

	/**
 	* Read the 'users' objects.  Results look like:
 	* {
  "errors" : [ ],
  "data" : {
    "maxrows" : "50",
    "startrow" : "1",
    "users" : {
      "RowCount" : 11,
      "Columns" : [ "USERID", "LASTNAME", "FIRSTNAME", "ACCESS", "ADMINUSERID", "EMPLOYEENUMBER", "JOBTITLE", "USERSTATUS", "CONCATNAME" ],^M
      "Data" : [ {
        "USERID" : "238167",
        "LASTNAME" : "Smith",
        "FIRSTNAME" : "Joe",
        "ACCESS" : "EMP",
        "ADMINUSERID" : "212163",
        "EMPLOYEENUMBER" : "8",
        "JOBTITLE" : "Network Support Technician",
        "USERSTATUS" : "1",
        "CONCATNAME" : "Smith, Joe"
      },
 	* @return a list of User.
 	*/
	private List<User> readUsers() throws IOException {
		String routine = getClass().getSimpleName() + ".readUsers";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "/users";
		Message.printStatus(2, routine, "Reading users from: " + requestUrl);
		List<User> userList = new ArrayList<>();
		String [] elements = { "data", "users", "Data" };

		// If the HTTP request returns 420, need to wait and try again.
		int wait = 0;
		int waitMax = 600000;
		while ( true ) {
			JsonNode jsonNode = null;
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl (
					this.debug, requestUrl, getHttpRequestProperties(), elements );
			}
			catch ( HttpCodeException e ) {
				Message.printStatus(2, routine, "HTTP code " + e.getCode() + " was returned indicating need to space out requests.");
				if ( e.getCode() == 420 ) {
					// The request is being made too fast so build in a wait.
					if ( wait == 0 ) {
						// Milliseconds:
						// - start with 1/2 second
						wait = 500;
					}
					else {
						// Double the wait.
						wait = (int)(wait * 2);
					}
					if ( wait > waitMax ) {
						// Have gone past the maximum wait.  Throw an exception rather than waiting a long time.
						String message = "HTTP code 420 wait retry is > limit " + waitMax + " ms - can't read user data.";
						Message.printWarning(3, routine, message);
						throw new IOException ( message );
					}
					// Wait the number of seconds.
					Message.printStatus(2, routine, "HTTP code 420 returned.  Waiting " + wait + " ms and then retrying the request.");
					try {
						Thread.sleep(wait);
					}
					catch ( InterruptedException e2 ) {
						// Should not occur.
					}
					// Go to the top of the loop and try to read again.
					continue;
				}
			}
			if ( jsonNode == null ) {
				Message.printStatus(2, routine, "  Reading users returned null.");
			}
			else {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " user items.");
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					userList.add((User)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), User.class));
				}
				// Break out of the read loop.
				break;
			}
		}
		return userList;
	}


    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
    	return "";
    }

    /**
     * Set the time series properties from the TimeSeriesCatalog.
     * @param ts time series to set properties
     * @param tscatalog time series catalog to get properties
     */
    private void setTimeSeriesProperties ( TS ts, TimeSeriesCatalog tscatalog ) {
    	// Set all the timesheets.com properties that are known for the time series.

    	// Customer properties.
    	ts.setProperty("customerId", tscatalog.getCustomerId());
    	ts.setProperty("customerName", tscatalog.getCustomerName());

    	// Project properties.
    	ts.setProperty("projectId", tscatalog.getProjectId());
    	ts.setProperty("projectName", tscatalog.getProjectName());

    	// User properties.
    	ts.setProperty("userFirstName", tscatalog.getUserFirstName());
    	ts.setProperty("userLastName", tscatalog.getUserLastName());
    }

}