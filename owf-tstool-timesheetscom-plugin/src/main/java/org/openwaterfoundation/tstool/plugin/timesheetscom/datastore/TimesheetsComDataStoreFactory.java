// TimesheetsComDataStoreFactory - class to create a TimesheetsComDataStore instance

/* NoticeStart

OWF TSTool TimesheetsCom Plugin
Copyright (C) 2023 Open Water Foundation

OWF TSTool TimesheetsCom plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool TimesheetsCom Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool TimesheetsCom Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.timesheetscom.datastore;

import java.net.URI;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreFactory;

public class TimesheetsComDataStoreFactory implements DataStoreFactory {

	/**
	Create a TimesheetsComDataStore instance.
	@param props datastore configuration properties, such as read from the configuration file
	*/
	public DataStore create ( PropList props ) {
	    String name = props.getValue ( "Name" );
	    String description = props.getValue ( "Description" );
	    if ( description == null ) {
	        description = "";
	    }
	    String serviceRootURI = props.getValue ( "ServiceRootURI" );
	    if ( serviceRootURI == null ) {
	    	System.out.println("TimesheetsComDataStore ServiceRootURI is not defined in the datastore configuration file.");
	    }
	    String apiKey = props.getValue ( "ApiKey" );
	    if ( apiKey == null ) {
	    	System.out.println("TimesheetsComDataStore ApiKey is not defined in the datastore configuration file.");
	    }
	    String authorization = props.getValue ( "Authorization" );
	    if ( authorization == null ) {
	    	System.out.println("TimesheetsComDataStore Authorization is not defined in the datastore configuration file.");
	    }
	    try {
	        DataStore ds = new TimesheetsComDataStore ( name, description, new URI(serviceRootURI), props );
	        return ds;
	    }
	    catch ( Exception e ) {
	        Message.printWarning(3,"",e);
	        throw new RuntimeException ( e );
	    }
	}
}
