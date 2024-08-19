package org.openwaterfoundation.tstool.plugin.timesheetscom.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort timesheets data by work date.
 */
public class ReportProjectCustomizableDataComparator implements Comparator<ReportProjectCustomizableData> {

	/**
	 * Constructor.
	 */
	public ReportProjectCustomizableDataComparator () {
	}

	/**
	 * Compare two ReportProjectCustomizableDataComparator.
	 * If dataA is < dataB, return -1.
	 * If dataA = dataB, return 0.
	 * If dataA is > dataB, return 1
	 * @param dataA the first ReportProjectCustomizableData to compare
	 * @param dataB the second ReportProjectCustomizableData to compare
	 */
	public int compare ( ReportProjectCustomizableData dataA, ReportProjectCustomizableData dataB ) {
		if ( dataA.getWorkDateAsDateTime().lessThan(dataB.getWorkDateAsDateTime()) ) {
			return -1;
		}
		else if ( dataA.getWorkDateAsDateTime().greaterThan(dataB.getWorkDateAsDateTime()) ) {
			return 1;
		}
		else {
			return 0;
		}
	}
}