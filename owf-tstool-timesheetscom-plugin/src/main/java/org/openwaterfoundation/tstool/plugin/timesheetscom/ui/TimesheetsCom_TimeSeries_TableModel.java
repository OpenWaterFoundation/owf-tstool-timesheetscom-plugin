// TimeseriesCom_TimeSeries_TableModel - table model for the time series catalog

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

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import org.openwaterfoundation.tstool.plugin.timesheetscom.datastore.TimesheetsComDataStore;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.TimeSeriesCatalog;

/**
This class is a table model for time series header information for timesheets.com web services time series.
By default the sheet will contain row and column numbers.
*/
@SuppressWarnings({ "serial", "rawtypes" })
public class TimesheetsCom_TimeSeries_TableModel extends JWorksheet_AbstractRowTableModel {

	/**
	Number of columns in the table model.
	*/
	private final int COLUMNS = 19;

	//public final int COL_LOCATION_ID = 0;
	public final int COL_LOCID = 0;
	public final int COL_DATA_TYPE = 1;
	public final int COL_DATA_INTERVAL = 2;
	public final int COL_CUSTOMER_NAME = 3;
	public final int COL_PROJECT_NAME = 4;
	public final int COL_PROJECT_CREATED_DATE = 5;
	public final int COL_PROJECT_STATUS = 6;
	public final int COL_DATA_START = 7;
	public final int COL_DATA_END = 8;
	public final int COL_DATA_COUNT = 9;
	public final int COL_PROJECT_DEFAULT_BILL_RATE = 10;
	public final int COL_LAST_NAME = 11;
	public final int COL_FIRST_NAME = 12;
	public final int COL_DATA_UNITS = 13;
	public final int COL_CUSTOMER_ID = 14;
	public final int COL_PROJECT_ID = 15;
	public final int COL_USER_ID = 16;
	public final int COL_PROBLEMS = 17;
	public final int COL_DATASTORE = 18;

	/**
	Datastore corresponding to datastore used to retrieve the data.
	*/
	TimesheetsComDataStore datastore = null;

	/**
	Data are a list of TimeSeriesCatalog.
	*/
	private List<TimeSeriesCatalog> timeSeriesCatalogList = null;

	/**
	Constructor.  This builds the model for displaying the given TimesheetsCom time series data.
	@param dataStore the data store for the data
	@param data the list of TimesheetsCom TimeSeriesCatalog that will be displayed in the table.
	@throws Exception if an invalid results passed in.
	*/
	@SuppressWarnings("unchecked")
	public TimesheetsCom_TimeSeries_TableModel ( TimesheetsComDataStore dataStore, List<? extends Object> data ) {
		if ( data == null ) {
			_rows = 0;
		}
		else {
		    _rows = data.size();
		}
	    this.datastore = dataStore;
		_data = data; // Generic
		// TODO SAM 2016-04-17 Need to use instanceof here to check.
		this.timeSeriesCatalogList = (List<TimeSeriesCatalog>)data;
	}

	/**
	From AbstractTableModel.  Returns the class of the data stored in a given column.
	@param columnIndex the column for which to return the data class.
	*/
	@SuppressWarnings({ "unchecked" })
	public Class getColumnClass (int columnIndex) {
		switch (columnIndex) {
			// List in the same order as top of the class.
			//case COL_STATION_ELEVATION: return Double.class;
			default: return String.class; // All others.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of columns of data.
	@return the number of columns of data.
	*/
	public int getColumnCount() {
		return this.COLUMNS;
	}

	/**
	From AbstractTableMode.  Returns the name of the column at the given position.
	@return the name of the column at the given position.
	*/
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case COL_LOCID: return "Location Identifier";
			case COL_DATA_TYPE: return "Data Type";
			case COL_DATA_INTERVAL: return "Interval";
			//case COL_STATISTIC: return "Statistic";
			case COL_CUSTOMER_NAME: return "Customer Name";
			case COL_PROJECT_NAME: return "Project Name";
			case COL_PROJECT_CREATED_DATE: return "Project Created Date";
			case COL_PROJECT_STATUS: return "Project Status";
			case COL_DATA_START: return "Data Start";
			case COL_DATA_END: return "Data End";
			case COL_DATA_COUNT: return "Data Count";
			case COL_PROJECT_DEFAULT_BILL_RATE: return "Project Default Bill Rate";
			case COL_LAST_NAME: return "Last Name";
			case COL_FIRST_NAME: return "First Name";
			case COL_DATA_UNITS: return "Units";
			case COL_CUSTOMER_ID: return "Customer ID";
			case COL_PROJECT_ID: return "Project ID";
			case COL_USER_ID: return "User ID";
			case COL_PROBLEMS: return "Problems";
			case COL_DATASTORE: return "Datastore";

			default: return "";
		}
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public String[] getColumnToolTips() {
	    String[] toolTips = new String[this.COLUMNS];
	    toolTips[COL_LOCID] = "Location identifier (customer name, project name, and use name)";
	    toolTips[COL_DATA_TYPE] = "Time series data type (hours)";
	    toolTips[COL_DATA_INTERVAL] = "Time series data interval (ts_spacing converted to TSTool interval)";
	    //toolTips[COL_STATISTIC] = "NOT USED - statistic for regular interval data (included in main data type)";
	    toolTips[COL_CUSTOMER_NAME] = "Customer name";
	    toolTips[COL_PROJECT_NAME] = "Project name";
	    toolTips[COL_PROJECT_CREATED_DATE] = "Project created date";
	    toolTips[COL_PROJECT_STATUS] = "Project status";
	    toolTips[COL_DATA_START] = "Data start date";
	    toolTips[COL_DATA_END] = "Data end date";
	    toolTips[COL_DATA_COUNT] = "Data count > .001 hours.";
	    toolTips[COL_PROJECT_DEFAULT_BILL_RATE] = "Project default bill rate";
	    toolTips[COL_LAST_NAME] = "Last name";
	    toolTips[COL_FIRST_NAME] = "First name";
	    toolTips[COL_DATA_UNITS] = "Time series data value units";
	    toolTips[COL_CUSTOMER_ID] = "Customer ID";
	    toolTips[COL_PROJECT_ID] = "Project ID";
	    toolTips[COL_USER_ID] = "User ID";
		toolTips[COL_PROBLEMS] = "Problems";
		toolTips[COL_DATASTORE] = "Datastore name";
	    return toolTips;
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public int[] getColumnWidths() {
		int[] widths = new int[this.COLUMNS];
	    widths[COL_LOCID] = 60;
	    widths[COL_DATA_TYPE] = 10;
	    widths[COL_DATA_INTERVAL] = 6;
	    //widths[COL_STATISTIC] = 8;
	    widths[COL_CUSTOMER_NAME] = 25;
	    widths[COL_PROJECT_NAME] = 45;
	    widths[COL_PROJECT_CREATED_DATE] = 15;
	    widths[COL_PROJECT_STATUS] = 10;
	    widths[COL_DATA_START] = 8;
	    widths[COL_DATA_END] = 8;
	    widths[COL_DATA_COUNT] = 8;
	    widths[COL_PROJECT_DEFAULT_BILL_RATE] = 12;
	    widths[COL_LAST_NAME] = 8;
	    widths[COL_FIRST_NAME] = 8;
	    widths[COL_DATA_UNITS] = 4;
	    widths[COL_CUSTOMER_ID] = 8;
	    widths[COL_PROJECT_ID] = 6;
	    widths[COL_USER_ID] = 6;
		widths[COL_PROBLEMS] = 30;
		widths[COL_DATASTORE] = 10;
		return widths;
	}

	/**
	Returns the format to display the specified column.
	@param column column for which to return the format.
	@return the format (as used by StringUtil.formatString()).
	*/
	public String getFormat ( int column ) {
		switch (column) {
			case COL_PROJECT_DEFAULT_BILL_RATE: return "%.2f";
			default: return "%s"; // All else are strings.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of rows of data in the table.
	*/
	public int getRowCount() {
		return _rows;
	}

	/**
	From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
	@param row the row for which to return data.
	@param col the column for which to return data.
	@return the data that should be placed in the JTable at the given row and column.
	*/
	public Object getValueAt(int row, int col) {
		// Make sure the row numbers are never sorted.
		if (_sortOrder != null) {
			row = _sortOrder[row];
		}

		TimeSeriesCatalog timeSeriesCatalog = this.timeSeriesCatalogList.get(row);
		switch (col) {
			// OK to allow null because will be displayed as blank.
			case COL_LOCID: return timeSeriesCatalog.getLocId();
			case COL_DATA_TYPE: return timeSeriesCatalog.getDataType();
			case COL_DATA_INTERVAL: return timeSeriesCatalog.getDataInterval();
			//case COL_STATISTIC: return timeSeriesCatalog.getStatistic();
			// Data units come from the general value, which will be from point_type or rating.
			case COL_CUSTOMER_NAME: return timeSeriesCatalog.getCustomerName();
			case COL_PROJECT_NAME: return timeSeriesCatalog.getProjectName();
			case COL_PROJECT_CREATED_DATE: return timeSeriesCatalog.getProjectCreatedDate();
			case COL_PROJECT_STATUS: return timeSeriesCatalog.getProjectStatusAsWord();
			case COL_DATA_START: return timeSeriesCatalog.getDataStart();
			case COL_DATA_END: return timeSeriesCatalog.getDataEnd();
			case COL_DATA_COUNT: return timeSeriesCatalog.getDataCount();
			case COL_PROJECT_DEFAULT_BILL_RATE: return timeSeriesCatalog.getProjectDefaultBillRate();
			case COL_LAST_NAME: return timeSeriesCatalog.getUserLastName();
			case COL_FIRST_NAME: return timeSeriesCatalog.getUserFirstName();
			case COL_DATA_UNITS: return timeSeriesCatalog.getDataUnits();
			case COL_CUSTOMER_ID: return timeSeriesCatalog.getCustomerId();
			case COL_PROJECT_ID: return timeSeriesCatalog.getProjectId();
			case COL_USER_ID: return timeSeriesCatalog.getUserId();
			case COL_PROBLEMS: return timeSeriesCatalog.formatProblems();
			case COL_DATASTORE: return this.datastore.getName();
			default: return "";
		}
	}

}