package org.openwaterfoundation.tstool.plugin.timesheetscom.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort timesheets.com TimeSeriesCatalog.
 * The locId is sorted and then by user last name.
 */
public class TimeSeriesCatalogComparator implements Comparator<TimeSeriesCatalog> {

	/**
	 * Constructor.
	 */
	public TimeSeriesCatalogComparator () {
	}
	
	/**
	 * Compare two TimeSeriesCatalog.
	 * If tscatalogA is < tscatalogB, return -1.
	 * If tscatalogA = tscatalogB, return 0.
	 * If tscatalogA is > tscatalogB, return 1
	 * @param tscatalogA the first TimeSeriesCatalog to compare
	 * @param tscatalogB the second TimeSeriesCatalog to compare
	 */
	public int compare(TimeSeriesCatalog tscatalogA, TimeSeriesCatalog tscatalogB) {
		int compareResult = tscatalogA.getLocId().compareTo ( tscatalogB.getLocId() );
		if ( compareResult != 0 ) {
			// locId are not the same so return the result.
			return compareResult;
		}
		else {
			// locId are the same so compare the user last name.
			return tscatalogA.getUserLastName().compareTo ( tscatalogB.getUserLastName() );
		}
	}
}