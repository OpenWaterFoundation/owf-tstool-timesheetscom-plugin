// TimesheetsCom_TimeSeries_InputFilter_JPanel - panel to filter time series queries

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

package org.openwaterfoundation.tstool.plugin.timesheetscom.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.timesheetscom.datastore.TimesheetsComDataStore;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying timesheets.com web services.
*/
@SuppressWarnings("serial")
public class TimesheetsCom_TimeSeries_InputFilter_JPanel extends InputFilter_JPanel {

	/**
	Test datastore, for connection.
	*/
	private TimesheetsComDataStore datastore = null;

	/**
	Constructor for case when no datastore is configured - default panel.
	@param label label for the panel
	*/
	public TimesheetsCom_TimeSeries_InputFilter_JPanel ( String label ) {
		super(label);
	}

	/**
	Constructor.
	@param dataStore the data store to use to connect to the test database.  Cannot be null.
	@param numFilterGroups the number of filter groups to display
	*/
	public TimesheetsCom_TimeSeries_InputFilter_JPanel ( TimesheetsComDataStore dataStore, int numFilterGroups ) {
	    super();
	    this.datastore = dataStore;
	    if ( this.datastore != null ) {
	        setFilters ( numFilterGroups );
	    }
	}

	/**
	Set the filter data.  This method is called at setup and when refreshing the list with a new subject type.
	For all cases, use the InputFilter constructor "whereLabelPersistent" to ensure that the TSTool ReadTimesheetsCom command
	will show a nice filter name.
	*/
	public void setFilters ( int numFilterGroups ) {
		String routine = getClass().getSimpleName() + ".setFilters";

		// Read the data to populate filter choices.

		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		try {
			// By default all time series are included in the catalog:
			// - this allows providing query filters that are found in the time series list
			// - use the saved global data rather than rereading to improve performance
			tscatalogList = datastore.getTimeSeriesCatalog ( false );
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "Exception reading the timesheets.com time series list");
			Message.printWarning(2, routine, e);
		}

		// The internal names for filters match the web service query parameters.

	    List<InputFilter> filters = new ArrayList<>();

	    // Always add blank to top of filter
	    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank.

	    // Loop through the time series catalog records and extract unique values for filters.
	    List<String> customerNameChoices = new ArrayList<>();
	    List<String> projectNameChoices = new ArrayList<>();
	    List<String> firstNameChoices = new ArrayList<>();
	    List<String> lastNameChoices = new ArrayList<>();

	    String customerName = null;
	    String projectName = null;
	    String firstName = null;
	    String lastName = null;
	    boolean found = false;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Get the values for lists.

	    	// Customers.
	    	customerName = tscatalog.getCustomerName();

	    	// Projects.
	    	projectName = tscatalog.getProjectName();

	    	// Users.
	    	firstName = tscatalog.getUserFirstName();
	    	lastName = tscatalog.getUserLastName();

	    	// Only add if not already in the lists.
	    	found = false;
	    	for ( String customerName0 : customerNameChoices ) {
	    		if ( customerName.equals(customerName0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		customerNameChoices.add("" + customerName);
	    	}

	    	found = false;
	    	for ( String projectName0 : projectNameChoices ) {
	    		if ( projectName.equals(projectName0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		projectNameChoices.add("" + projectName);
	    	}

	    	found = false;
	    	for ( String firstName0 : firstNameChoices ) {
	    		if ( firstName.equals(firstName0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		firstNameChoices.add("" + firstName);
	    	}

	    	found = false;
	    	for ( String lastName0 : lastNameChoices ) {
	    		if ( lastName.equals(lastName0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		lastNameChoices.add("" + lastName);
	    	}

	    }

	    // Project status choices:
	    // - these do not match the tscatlog data and have to be handled specifically

	    List<String> projectStatusChoices = new ArrayList<>();
	    projectStatusChoices.add ( "Active" );
	    projectStatusChoices.add ( "Archived" );
	    projectStatusChoices.add ( "All" );

	    // Sort the choices.
	    Collections.sort(customerNameChoices,String.CASE_INSENSITIVE_ORDER);
	    Collections.sort(projectNameChoices,String.CASE_INSENSITIVE_ORDER);
	    Collections.sort(firstNameChoices,String.CASE_INSENSITIVE_ORDER);
	    Collections.sort(lastNameChoices,String.CASE_INSENSITIVE_ORDER);

	    filters.add(new InputFilter("Customer - Name",
            "customerName", "customerName", "customerName",
            StringUtil.TYPE_STRING, customerNameChoices, customerNameChoices, true));

	    filters.add(new InputFilter("Project - Name",
            "projectName", "projectName", "projectName",
            StringUtil.TYPE_STRING, projectNameChoices, projectNameChoices, true));

	    InputFilter filter = new InputFilter("Project - Status",
            "projectStatus", "projectStatus", "projectStatus",
            StringUtil.TYPE_STRING, projectStatusChoices, projectStatusChoices, true);
	    filter.removeConstraint(InputFilter.INPUT_STARTS_WITH);
	    filter.removeConstraint(InputFilter.INPUT_ENDS_WITH);
	    filter.removeConstraint(InputFilter.INPUT_CONTAINS);
	    filters.add(filter);

	    filters.add(new InputFilter("User - First Name",
            "userFirstName", "userFirstName", "userFirstName",
            StringUtil.TYPE_STRING, firstNameChoices, firstNameChoices, true));

	    filters.add(new InputFilter("User - Last Name",
            "userLastName", "userLastName", "userLastName",
            StringUtil.TYPE_STRING, lastNameChoices, lastNameChoices, true));

	    /*
	    Collections.sort(stationNoChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Station - Number",
            "station_no", "stationNo", "station_no",
            StringUtil.TYPE_STRING, stationNoChoices, stationNoChoices, true));

	    Collections.sort(stationParameterNameChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Station Parameter - Name",
            "stationparameter_name", "stationParameterName", "stationparameter_name",
            StringUtil.TYPE_STRING, stationParameterNameChoices, stationParameterNameChoices, true));

	    Collections.sort(tsIdChoices,String.CASE_INSENSITIVE_ORDER);
	    filter = new InputFilter("Time series - ID",
	        "ts_id", "tsId", "ts_id",
	        StringUtil.TYPE_INTEGER, tsIdChoices, tsIdChoices, false);
	    filter.removeConstraint(InputFilter.INPUT_GREATER_THAN);
	    filter.removeConstraint(InputFilter.INPUT_GREATER_THAN_OR_EQUAL_TO);
	    filter.removeConstraint(InputFilter.INPUT_LESS_THAN);
	    filter.removeConstraint(InputFilter.INPUT_LESS_THAN_OR_EQUAL_TO);
	    filters.add(filter);

	    Collections.sort(tsNameChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Time series - Name",
            "ts_name", "tsName", "ts_name",
            StringUtil.TYPE_STRING, tsNameChoices, tsNameChoices, true));

	    Collections.sort(tsPathChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Time series - Path",
            "ts_path", "tsPath", "ts_path",
            StringUtil.TYPE_STRING, tsPathChoices, tsPathChoices, true));

	    Collections.sort(tsShortNameChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Time series - Name (short)",
            "ts_shortname", "tsShortName", "ts_shortname",
            StringUtil.TYPE_STRING, tsShortNameChoices, tsShortNameChoices, true));
            */

	  	setToolTipText("<html>Specify one or more input filters to limit query, will be ANDed.</html>");

	    int numVisible = 14;
	    setInputFilters(filters, numFilterGroups, numVisible);
	}

	/**
	Return the data store corresponding to this input filter panel.
	@return the data store corresponding to this input filter panel.
	*/
	public TimesheetsComDataStore getDataStore ( ) {
	    return this.datastore;
	}

}