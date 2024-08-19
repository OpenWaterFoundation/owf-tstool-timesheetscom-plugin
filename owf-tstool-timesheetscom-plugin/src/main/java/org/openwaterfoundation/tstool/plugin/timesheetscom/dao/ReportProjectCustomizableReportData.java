// ReportData

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

import RTi.Util.Time.DateTime;

/**
 * timesheets.com "report/project/customizable" object ReportData object,
 * which includes a list of reports as ReportProjectCustomizableRecord.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReportProjectCustomizableReportData {
	// Alphabetize.

	/**
	 * "TotalHours"
	 */
	@JsonProperty("TotalHours")
	private String totalHours = "";

	/**
	 * "Records":
	 * - is is actually just one record, not a list
	 */
	@JsonProperty("Records")
	private ReportProjectCustomizableRecord record = null;

	/**
	 * Start date for the query, not currently used for anything.
	 */
	@JsonIgnore
	DateTime startDate = null;

	/**
	 * End date for the query, not currently used for anything.
	 */
	@JsonIgnore
	DateTime endDate = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public ReportProjectCustomizableReportData () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading from the API.
	 */
	public void cleanData () {
		// The report will include one record.
		this.record.cleanData();
	}

	/**
	 * Return the ReportProjectCustomizable record.
	 * @return the ReportProjectCustomizable record
	 */
	public ReportProjectCustomizableRecord getReportProjectCustomizableRecord () {
		return this.record;
	}

	/**
	 * Set the start date for the query.
	 */
	public void setStartDate ( DateTime startDate ) {
		this.startDate = startDate;
	}

	/**
	 * Set the end date for the query.
	 */
	public void setEndDate ( DateTime endDate ) {
		this.endDate = endDate;
	}

	/**
	 * Return the number of records in the report.
	 * @return the number of records in the report
	 */
	public int size () {
		return record.size();
	}

	/**
	 * Return the total number of records in a list of reports.
	 * @return the number of records in the report
	 */
	public static int size ( List<ReportProjectCustomizableReportData> reportProjectCustomizableReportDataList ) {
		int size = 0;
		for ( ReportProjectCustomizableReportData data : reportProjectCustomizableReportDataList ) {
			size += data.size();
		}
		return size;
	}

}