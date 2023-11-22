// ReportProjectCustomizableRecord - the "Records" object

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * timesheets.com "report/project/customizable" object Records object, which includes a list of Data.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReportProjectCustomizableRecord {
	// Alphabetize.
	
	/**
	 * "RowCount"
	 */
	//@JsonProperty("RowCount")
	//private Integer rowCount = null;
	
	/**
	 * "Data"
	 */
	@JsonProperty("Data")
	private List<ReportProjectCustomizableData> dataList = new ArrayList<>();
	
	/**
	 * Default constructor used by Jackson.
	 */
	public ReportProjectCustomizableRecord () {
	}
	
	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading from the API.
	 */
	public void cleanData () {
		for ( ReportProjectCustomizableData data : dataList ) {
			data.cleanData();
		}
	}

	/**
	 * Return the list of ReportProjectCustomizableData.
	 * @return the list of ReportProjectCustomizableData
	 */
	public List<ReportProjectCustomizableData> getReportProjectCustomizableDataList () {
		return this.dataList;
	}

	/**
	 * Return the number of records in the report.
	 * #return the number of records in the report
	 */
	public int size () {
		return this.dataList.size();
	}
}
