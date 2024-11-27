// TimeSeriesCatalog - object to list time series

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

import java.util.ArrayList;
import java.util.List;

import RTi.TS.TSIdent;
import RTi.Util.Time.DateTime;

/**
 * Class to store time series catalog (metadata) for timesheets.com TSTool time series list.
 * This is a combination of standard time series properties used in TSTool and timesheets.com data.
 * More data may be included and shown in the table model while evaluating the web services
 * and will be removed or disabled later.
 * The types are as one would expect, whereas the 'TimeSeries' object uses strings as per web service JSON types.
 */
public class TimeSeriesCatalog {

	// General data, provided by TSTool, extracted/duplicated from timesheets.com services:
	// - use data from the ReportProjectCustomizable unless otherwise noted (e.g., project data)
	private String locId = "";
	private String dataInterval = "";
	private String dataType = "";
	private String dataUnits = "";

	// Customer data, listed alphabetically.
	private String customerId = "";
	private String customerName = "";

	// Project data, listed alphabetically.
	private Float projectDefaultBillRate = null;  // Looked up from the project.
	private String projectId = "";
	private String projectName = "";
	private String projectCreatedDate = "";
	private String projectStatus = "";  // Looked up from the project.

	// User data, listed alphabetically.
	private String userId = "";
	private String userFirstName = "";
	private String userLastName = "";
	
	// Time series, not in timesheets.com objects.
	private DateTime dataStart = null;
	private DateTime dataEnd = null;
	private int dataCount = 0;

	// List of problems, one string per issue.
	private List<String> problems = null; // Initialize to null to save memory ... must check elsewhere when using.

	/**
	 * Has ReadTSCatalog.checkData() resulted in problems being set?
	 * This is used when there are issues with non-unique time series identifiers.
	 * For example if two catalog are returned for a stationNumId, dataType, and dataInterval,
	 * each of the tscatalog is processed in checkData().  The will each be processed twice.
	 * This data member is set to true the first time so that the 'problems' list is only set once
	 * in TSCatalogDAO.checkData().
	 */
	private boolean haveCheckDataProblemsBeenSet = false;

	/**
	 * Constructor.
	 */
	public TimeSeriesCatalog () {
	}

	/**
	 * Copy constructor.
	 * @param timeSeriesCatalog instance to copy
	 */
	public TimeSeriesCatalog ( TimeSeriesCatalog timeSeriesCatalog ) {
		// Do a deep copy by default as per normal Java conventions.
		this(timeSeriesCatalog, true);
	}

	/**
	 * Copy constructor.
	 * @param timeSeriesCatalog instance to copy
	 * @param deepCopy indicates whether an exact deep copy should be made (true)
	 * or a shallow copy that is typically used when defining a derived catalog record.
	 * For example, use deepCopy=false when copying a scaled catalog entry for a rated time series.
	 */
	public TimeSeriesCatalog ( TimeSeriesCatalog timeSeriesCatalog, boolean deepCopy ) {
		// List in the same order as internal data member list.
		this.locId = timeSeriesCatalog.locId;
		this.dataInterval = timeSeriesCatalog.dataInterval;
		this.dataType = timeSeriesCatalog.dataType;
		this.dataUnits = timeSeriesCatalog.dataUnits;

		// Customer data, listed alphabetically.
		this.customerName = timeSeriesCatalog.customerName;

		// Project data, listed alphabetically.
		this.projectCreatedDate = timeSeriesCatalog.projectCreatedDate;
		this.projectDefaultBillRate = timeSeriesCatalog.projectDefaultBillRate;
		this.projectId = timeSeriesCatalog.projectId;
		this.projectName = timeSeriesCatalog.projectName;
		this.projectStatus = timeSeriesCatalog.projectStatus;

		// User data, listed alphabetically.
		this.userFirstName = timeSeriesCatalog.userFirstName;
		this.userLastName = timeSeriesCatalog.userLastName;
		this.userId = timeSeriesCatalog.userId;

		// Time series data.
		this.dataStart = timeSeriesCatalog.dataStart;
		this.dataEnd = timeSeriesCatalog.dataEnd;
		this.dataCount = timeSeriesCatalog.dataCount;

		if ( deepCopy ) {
			// Time series catalog problems.
			if ( timeSeriesCatalog.problems == null ) {
				this.problems = null;
			}
			else {
				// Create a new list.
				this.problems = new ArrayList<>();
				for ( String s : timeSeriesCatalog.problems ) {
					this.problems.add(s);
				}
			}
		}
		else {
			// Default is null problems list.
		}
	}

	/**
	 * Constructor for report project customizable record.
	 * This is called for the first encounter with a unique record (otherwise the data are added to an existing catalog).
	 * @param data report project customizable data record to add as a time series catalog entry
	 * @param project the project matching the data
	 */
	public TimeSeriesCatalog ( ReportProjectCustomizableData data, Project project ) {
		// General time series properties.
		this.locId = "'" + data.getCustomerName() + "/" + data.getProjectName() + "/"
			+ data.getLastName() + "," + data.getFirstName() + "'";
		// Use "ProjectHours" for data type since it comes from the timesheets.com project hours.
		this.dataType = "ProjectHours";
		this.dataInterval = "Day";
		this.dataUnits = "Hours";

		// Customer data.
		this.customerId = data.getCustomerId();
		this.customerName = data.getCustomerName();

		// Project data:
		// - need to look up status from the projects
		this.projectId = data.getProjectId();
		this.projectName = data.getProjectName();
		if ( project != null ) {
			this.projectCreatedDate = project.getCreatedDate();
			this.projectDefaultBillRate = project.getDefaultBillRateAsFloat();
			this.projectStatus = project.getProjectStatus();
		}

		// User data.
		this.userId = data.getUserId();
		this.userFirstName = data.getFirstName();
		this.userLastName = data.getLastName();
		
		// Time series data are set independently because they depend on the full data period.
	}

	/**
	 * Add a problem to the problem list.
	 * @param problem Single problem string.
	 */
	public void addProblem ( String problem ) {
		if ( this.problems == null ) {
			this.problems = new ArrayList<>();
		}
		this.problems.add(problem);
	}

	/**
	 * Clear the problems.
	 * @return
	 */
	public void clearProblems() {
		if ( this.problems != null ) {
			this.problems.clear();
		}
	}

	/**
	 * Add report data.  The data start, end, and count are update.
	 * @param data a report record
	 */
	public void updateForReportData ( ReportProjectCustomizableData data ) {
		// Check the start.
		if ( this.dataStart == null ) {
			this.dataStart = data.getWorkDateAsDateTime();
		}
		else if ( data.getWorkDateAsDateTime().lessThan(this.dataStart) ) {
			this.dataStart = data.getWorkDateAsDateTime();
		}

		// Check the end.
		if ( this.dataEnd == null ) {
			this.dataEnd = data.getWorkDateAsDateTime();
		}
		else if ( data.getWorkDateAsDateTime().greaterThan(this.dataEnd) ) {
			this.dataEnd = data.getWorkDateAsDateTime();
		}
		
		// Increment the data count.
		if ( data.getHoursAsFloat() > .001 ) {
			++this.dataCount;
		}
	}

	/**
	 * Create an index list for TimeSeriesCatalog data list, using stationNumId as the index.
	 * This is a list of lists, with outermost list being the stationNumId.
	 * It is assumed that the catalog is sorted by stationNumId, which should be the case
	 * due to logic in the 'tscatalog' service.
	 * @param tscatalogList list of TimeSeriesCatalog to create an index for.
	 * @return the indexed TimeSeriesCatalog
	 */
	/*
	public static List<IndexedDataList<Integer,TimeSeriesCatalog>> createIndex ( List<TimeSeriesCatalog> tscatalogList ) {
		List<IndexedDataList<Integer,TimeSeriesCatalog>> indexList = new ArrayList<>();
		// Loop through the TimeSeriesCatalog list.
		Integer stationNumIdPrev = null;
		boolean newStationNumId = false;
		Integer stationNumId = null;
		IndexedDataList<Integer,TimeSeriesCatalog> stationTimeSeriesCatalogList = null;
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			stationNumId = tscatalog.getStationNumId();
			newStationNumId = false;
			if ( stationNumIdPrev == null ) {
				// First station.
				newStationNumId = true;
			}
			else if ( ! stationNumId.equals(stationNumIdPrev) ) {
				// Station does not match previous so need to add to index.
				newStationNumId = true;
			}
			// Set the previous stationNumId for the next iteration.
			stationNumIdPrev = stationNumId;
			if ( newStationNumId ) {
				// New station:
				// - create a new list and add to the index list
				// - use the statinNumId for primary identifier and stationId for secondary identifier
				//stationTimeSeriesCatalogList = new IndexedDataList<>(stationNumId, tscatalog.getStationId());
				indexList.add(stationTimeSeriesCatalogList);
			}
			// Add the station to the current list being processed.
			stationTimeSeriesCatalogList.add(tscatalog);
		}
		return indexList;
	}
	*/

	/**
	 * Search the time series catalog for a project record.
	 * @param tscatalogList list of TimeSeriesCatalog to search
	 * @param data report data to search
	 * @return the first matching catalog
	 */
	public static TimeSeriesCatalog findForData ( List<TimeSeriesCatalog> tscatalogList, ReportProjectCustomizableData data ) {
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			if ( tscatalog.getUserFirstName().equals(data.getFirstName())
				&& tscatalog.getUserLastName().equals(data.getLastName())
				&& tscatalog.getCustomerName().equals(data.getCustomerName())
				&& tscatalog.getProjectName().equals(data.getProjectName()) ) {
				// Found a match.
				return tscatalog;
			}
		}
		// No match.
		return null;
	}

	/**
	 * Search the time series catalog for a matching TSID.
	 * @param tscatalogList list of TimeSeriesCatalog to search
	 * @param tsid time series identifier to match
	 * @return the first matching catalog
	 */
	public static TimeSeriesCatalog findForTSID ( List<TimeSeriesCatalog> tscatalogList, String tsid ) {
		// TSID will have locId 'CustomerName'.'ProjectName'.'UserLast,UserFirst'.xxxx.DataType.DataInterval
		TSIdent tsident = null;
		try {
			tsident = new TSIdent ( tsid );
		}
		catch ( Exception e ) {
			return null;
		}
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			if ( !tsident.getLocation().equalsIgnoreCase(tscatalog.getLocId()) ) {
				// Data type does not match.
				continue;
			}
			if ( !tsident.getType().equalsIgnoreCase(tscatalog.getDataType()) ) {
				// Data type does not match.
				continue;
			}
			if ( !tsident.getInterval().equalsIgnoreCase(tscatalog.getDataInterval()) ) {
				// Data interval does not match:
				// - TODO smalers 2023-11-20 this currently does a simple check for "Day".
				continue;
			}
			// Matched the parts.
			return tscatalog;
		}
		// No match.
		return null;
	}

	/**
	 * Format problems into a single string.
	 * @return formatted problems.
	 */
	public String formatProblems() {
		if ( this.problems == null ) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for ( int i = 0; i < problems.size(); i++ ) {
			if ( i > 0 ) {
				b.append("; ");
			}
			b.append(problems.get(i));
		}
		return b.toString();
	}

	/**
	 * Return the customer ID.
	 * @return the customer ID
	 */
	public String getCustomerId ( ) {
		return this.customerId;
	}

	/**
	 * Return the customer name.
	 * @return the customer name
	 */
	public String getCustomerName ( ) {
		return this.customerName;
	}

	/**
	 * Return the data count.
	 * @return the data count. 
	 */
	public int getDataCount ( ) {
		return this.dataCount;
	}

	/**
	 * Return the data end.
	 * @return the data end. 
	 */
	public DateTime getDataEnd ( ) {
		return this.dataEnd;
	}

	/**
	 * Return the data start.
	 * @return the data start. 
	 */
	public DateTime getDataStart ( ) {
		return this.dataStart;
	}

	/**
	 * Return the data interval.
	 * @return the data interval
	 */
	public String getDataInterval ( ) {
		return this.dataInterval;
	}

	/**
	 * Return the data type.
	 * @return the data type
	 */
	public String getDataType ( ) {
		return this.dataType;
	}

	/**
	 * Return the data units.
	 * @return the data units
	 */
	public String getDataUnits ( ) {
		return this.dataUnits;
	}

	/**
	 * Get the list of distinct data intervals from the catalog, for example "IrregSecond", "15Minute".
	 * @param tscatalogList list of TimeSeriesCatalog to process.
	 * The list may have been filtered by data type previous to calling this method.
	 * @return a list of distinct data interval strings.
	 */
	public static List<String> getDistinctDataIntervals ( List<TimeSeriesCatalog> tscatalogList ) {
	    List<String> dataIntervalsDistinct = new ArrayList<>();
	    String dataInterval;
	    boolean found;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Data interval from the catalog, something like "IrregSecond", "15Minute", "1Hour", "24Hour".
	    	dataInterval = tscatalog.getDataInterval();
	    	if ( dataInterval == null ) {
	    		continue;
	    	}
	    	found = false;
	    	for ( String dataInterval2 : dataIntervalsDistinct ) {
	    		if ( dataInterval2.equals(dataInterval) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		// Add to the list of unique data types.
	    		dataIntervalsDistinct.add(dataInterval);
	    	}
	    }
	    return dataIntervalsDistinct;
	}

	/**
	 * Get the list of distinct data types from the catalog.
	 * @param tscatalogList list of TimeSeriesCatalog to process.
	 * @return a list of distinct data type strings.
	 */
	public static List<String> getDistinctDataTypes ( List<TimeSeriesCatalog> tscatalogList ) {
	    List<String> dataTypesDistinct = new ArrayList<>();
	    String dataType;
	    boolean found;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Data type from the catalog, something like "WaterLevelRiver".
	    	dataType = tscatalog.getDataType();
	    	if ( dataType == null ) {
	    		continue;
	    	}
	    	found = false;
	    	for ( String dataType2 : dataTypesDistinct ) {
	    		if ( dataType2.equals(dataType) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		// Add to the list of unique data types.
	    		dataTypesDistinct.add(dataType);
	    	}
	    }
	    return dataTypesDistinct;
	}

	/**
	 * Return whether checkData() has resulted in problems being set.
	 * @return whether checkData() has resulted in problems being set.
	 */
	public boolean getHaveCheckDataProblemsBeenSet () {
		return this.haveCheckDataProblemsBeenSet;
	}

	/**
	 * Return the location identifier.
	 * @return the location identifier
	 */
	public String getLocId ( ) {
		return this.locId;
	}

	/**
	 * Return the project ID.
	 * @return the project ID
	 */
	public String getProjectId ( ) {
		return this.projectId;
	}

	/**
	 * Return the project created date.
	 * @return the project created date
	 */
	public String getProjectCreatedDate ( ) {
		return this.projectCreatedDate;
	}

	/**
	 * Return the project name.
	 * @return the project name
	 */
	public String getProjectName ( ) {
		return this.projectName;
	}

	/**
	 * Return the project status ("1" is active, "0" is archived).
	 * @return the project status
	 */
	public String getProjectStatus ( ) {
		return this.projectStatus;
	}

	/**
	 * Return the project status word ("Active" is active, "Archived" is archived).
	 * @return the project status as a word.
	 */
	public String getProjectStatusAsWord ( ) {
		if ( this.projectStatus.equals("1") ) {
			return "Active";
		}
		else if ( this.projectStatus.equals("0") ) {
			return "Archived";
		}
		else {
			return "Unknown";
		}
	}

	/**
	 * Return the project default billing rate.
	 * @return the project default billing rate.
	 */
	public Float getProjectDefaultBillRate ( ) {
		return this.projectDefaultBillRate;
	}

	/**
	 * Return the user first name.
	 * @return the user first name
	 */
	public String getUserFirstName ( ) {
		return this.userFirstName;
	}

	/**
	 * Return the user ID.
	 * @return the user ID
	 */
	public String getUserId ( ) {
		return this.userId;
	}

	/**
	 * Return the user last name.
	 * @return the user last name
	 */
	public String getUserLastName ( ) {
		return this.userLastName;
	}

	/**
	 * Set the data last date (for last data).
	 * @param dataEnd last date for data
	 */
	public void setDataEnd ( DateTime dataEnd ) {
		this.dataEnd = dataEnd;
	}

	/**
	 * Set the time series catalog data interval.
	 * @param dataType time series data interval
	 */
	public void setDataInterval ( String dataInterval ) {
		this.dataInterval = dataInterval;
	}

	/**
	 * Set the data start date (for first data).
	 * @param dataStart first date for data
	 */
	public void setDataStart ( DateTime dataStart ) {
		this.dataStart = dataStart;
	}

	/**
	 * Set the time series catalog data type.
	 * @param dataType time series data type
	 */
	public void setDataType ( String dataType ) {
		this.dataType = dataType;
	}

	/**
	 * Set the time series catalog data units.
	 * @param dataType time series data units
	 */
	public void setDataUnits ( String dataUnits ) {
		this.dataUnits = dataUnits;
	}

	/**
	 * Set whether checkData() has resulted in problems being set.
	 * - TODO smalers 2020-12-15 not sure this is needed with the latest code.
	 *   Take out once tested out.
	 */
	public void setHaveCheckDataProblemsBeenSet ( boolean haveCheckDataProblemsBeenSet ) {
		this.haveCheckDataProblemsBeenSet = haveCheckDataProblemsBeenSet;
	}

	public void setLocId ( String locId ) {
		this.locId = locId;
	}

	/**
	 * Simple string to identify the time series catalog, for example for logging, using TSID format.
	 */
	public String toString() {
		return "" + this.locId + ".." + this.dataType + "." + this.dataInterval;
	}
}