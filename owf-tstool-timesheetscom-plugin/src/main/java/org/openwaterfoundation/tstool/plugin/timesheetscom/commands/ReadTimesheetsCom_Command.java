// ReadTimesheetsCom_Command - This class initializes, checks, and runs the ReadTimesheetsCom() command.

/* NoticeStart

OWF TSTool timesheetscom Plugin
Copyright (C) 2023-2024 Open Water Foundation

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

package org.openwaterfoundation.tstool.plugin.timesheetscom.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.openwaterfoundation.tstool.plugin.timesheetscom.datastore.TimesheetsComDataStore;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.AccountCode;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.Customer;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.Project;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableData;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableRecord;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.ReportProjectCustomizableReportData;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.timesheetscom.dao.User;
import org.openwaterfoundation.tstool.plugin.timesheetscom.ui.TimesheetsCom_TimeSeries_InputFilter_JPanel;

import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.MissingObjectEvent;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class initializes, checks, and runs the ReadTimesheetsCom() command.
*/
public class ReadTimesheetsCom_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Number of where clauses shown in the editor and available as parameters.
*/
private int __numWhere = 6;

/**
Data values for boolean parameters.
*/
protected String _False = "False";
protected String _True = "True";

/**
Data values for IncludeHours parameter.
Also uses Archived and All.
*/
protected String _New = "New";

/**
Data values for DataFlag parameter.
*/
protected String _Archived = "Archived";
protected String _Archived0 = "Archived0";
protected String _Archived1 = "Archived1";

/**
 * Project status:
 * - also use Archived from above
 */
protected String _Active = "Active";
protected String _All = "All";

/**
Data values for IfMissing parameter.
*/
protected String _Ignore = "Ignore";
protected String _Warn = "Warn";

/**
The table of account codes that is created in discovery mode.
*/
private DataTable discoveryAccountCodeTable = null;

/**
The table of customers that is created in discovery mode.
*/
private DataTable discoveryCustomerTable = null;

/**
The table of projects that is created in discovery mode.
*/
private DataTable discoveryProjectTable = null;

/**
The table of project time that is created in discovery mode.
*/
private DataTable discoveryProjectTimeTable = null;

/**
The table of users that is created in discovery mode.
*/
private DataTable discoveryUserTable = null;

/**
The table of work notes that is created in discovery mode (when not operating on an existing table).
*/
private DataTable discoveryWorkTable = null;

/**
List of time series read during discovery.
These are TS objects but with mainly the metadata (TSIdent) filled in.
*/
private List<TS> __discoveryTSList = null;

/**
Constructor.
*/
public ReadTimesheetsCom_Command () {
	super();
	setCommandName ( "ReadTimesheetsCom" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String routine = getClass().getSimpleName() + "checkCommandParameters";
	String warning = "";
    String message;

    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    String DataStore = parameters.getValue ( "DataStore" );
    String DataType = parameters.getValue ( "DataType" );
    String Interval = parameters.getValue ( "Interval" );
    String CustomerName = parameters.getValue ( "CustomerName" );
    String ProjectName = parameters.getValue ( "ProjectName" );
    String UserLastName = parameters.getValue ( "UserLastName" );
    String UserFirstName = parameters.getValue ( "UserFirstName" );
    String InputStart = parameters.getValue ( "InputStart" );
    String InputEnd = parameters.getValue ( "InputEnd" );
    String IncludeHours = parameters.getValue ( "IncludeHours" );
    //String ProjectStatus = parameters.getValue ( "ProjectStatus" );
    String DataFlag = parameters.getValue ( "DataFlag" );
    String IfMissing = parameters.getValue ( "IfMissing" );
    String AppendWorkTable = parameters.getValue ( "AppendWorkTable" );
    String Debug = parameters.getValue ( "Debug" );
    String InputFiltersCheck = parameters.getValue ( "InputFiltersCheck" ); // Passed in from the editor, not an actual parameter.
    String Where1 = parameters.getValue ( "Where1" );
    String Where2 = parameters.getValue ( "Where2" );
    String Where3 = parameters.getValue ( "Where3" );
    String Where4 = parameters.getValue ( "Where4" );
    String Where5 = parameters.getValue ( "Where5" );

	if ( (DataStore == null) || DataStore.isEmpty() ) {
        message = "The datastore must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the datastore." ) );
	}

	// TODO SAM 2023-01-02 Need to check the WhereN parameters.

	if ( (InputStart != null) && !InputStart.equals("") &&
		!InputStart.equalsIgnoreCase("InputStart") &&
		!InputStart.equalsIgnoreCase("InputEnd") && (InputStart.indexOf("${") < 0)) { // }
		try {
			DateTime.parse(InputStart);
		}
		catch ( Exception e ) {
            message = "The input start date/time \"" + InputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a date/time or InputStart." ) );
		}
	}

	if ( (InputEnd != null) && !InputEnd.equals("") &&
		!InputEnd.equalsIgnoreCase("InputStart") &&
		!InputEnd.equalsIgnoreCase("InputEnd") && (InputEnd.indexOf("${") < 0)) { // }
		try {
			DateTime.parse( InputEnd );
		}
		catch ( Exception e ) {
            message = "The input end date/time \"" + InputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify a date/time or InputEnd." ) );
		}
	}

    if ( (IncludeHours != null) && !IncludeHours.equals("") &&
        !IncludeHours.equalsIgnoreCase(_All) && !IncludeHours.equalsIgnoreCase(_Archived) && !IncludeHours.equalsIgnoreCase(_New) ) {
        message = "The IncludeHours parameter \"" + IncludeHours +
        "\" must be " + _All + " (default), " + _Archived + ", or " + _New + ".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _All + " (default), " + _Archived + ", or " + _New + " (default)." ) );
    }

	/*
    if ( (ProjectStatus != null) && !ProjectStatus.equals("") &&
        !ProjectStatus.equalsIgnoreCase(_Active) && !ProjectStatus.equalsIgnoreCase(_Archived) && !ProjectStatus.equalsIgnoreCase(_All) ) {
        message = "The ProjectStatus parameter \"" + ProjectStatus +
        "\" must be " + _Active + ", " + _Archived + ", or " + _All + ".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _Active + ", " + _Archived + ", or " + _All + "." ) );
    }
    */

    if ( (DataFlag != null) && !DataFlag.equals("") &&
        !DataFlag.equalsIgnoreCase(_Archived) && !DataFlag.equalsIgnoreCase(_Archived0) && !DataFlag.equalsIgnoreCase(_Archived1) ) {
        message = "The DataFlag parameter \"" + DataFlag +
        "\" must be " + _Archived + ", " + _Archived0 + ", or " + _Archived1 + ".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _Archived + ", " + _Archived0 + ", or " + _Archived1 + "." ) );
    }

    if ( (IfMissing != null) && !IfMissing.equals("") &&
        !IfMissing.equalsIgnoreCase(_Warn) && !IfMissing.equalsIgnoreCase(_Ignore) ) {
        message = "The IfMissing parameter \"" + IfMissing +
        "\" must be " + _Ignore + " or " + _Warn + " (default).";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _Ignore + " or " + _Warn + " (default if blank)." ) );
    }

	if ( (AppendWorkTable != null) && !AppendWorkTable.equals("") &&
		!AppendWorkTable.equalsIgnoreCase(_False) && !AppendWorkTable.equalsIgnoreCase(_True) ) {
        message = "The AppendWorkTable parameter value is invalid.";
		warning += "\n" + message;
           status.addToLog ( CommandPhaseType.INITIALIZATION,
               new CommandLogRecord(CommandStatusType.FAILURE,
                   message, "Specify " + _False + " (default) or " + _True ) );
	}

	if ( (Debug != null) && !Debug.equals("") &&
		!Debug.equalsIgnoreCase(_False) && !Debug.equalsIgnoreCase(_True) ) {
        message = "The Debug parameter value is invalid.";
		warning += "\n" + message;
           status.addToLog ( CommandPhaseType.INITIALIZATION,
               new CommandLogRecord(CommandStatusType.FAILURE,
                   message, "Specify " + _False + " (default) or " + _True ) );
	}

	// Make sure that some parameters are specified so that a query of all data is disallowed.

	int whereCount = 0;
	if ( (Where1 != null) && !Where1.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where2 != null) && !Where2.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where3 != null) && !Where3.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where4 != null) && !Where4.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where5 != null) && !Where5.startsWith(";") ) {
		++whereCount;
	}

	boolean readSingle = false;
	boolean readMult = false;
	if ( (CustomerName != null) && !CustomerName.isEmpty() ) {
		// Querying one time series.
		readSingle = true;

		// The data type cannot be a wild card.
		if ( DataType.equals("*") ) {
            message = "The data type cannot be * when matching a single time series.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a specific data type." ) );
		}
	}
	if ( whereCount > 0 ) {
		// Querying multiple time series.
		readMult = true;
	}
	if ( Message.isDebugOn ) {
		Message.printStatus(2, routine, "CustomerName=" + CustomerName + " whereCount=" + whereCount + " readSingle=" + readSingle + " readMult=" + readMult);
		Message.printStatus(2, routine, "Where1=" + Where1 + " Where2=" + Where2 + " Where3=" + Where3 + " Where4=" + Where4 + " Where5=" + Where5);
	}

	if ( readSingle && readMult ) {
		// Can only read one or multiple.
        message = "Parameters are specified to match a single time series and multiple time series (but not both).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify parameters to match a single time series OR multiple time series." ) );
	}
	if ( !readSingle && !readMult ) {
		// OK if the DataType is not *.
		if ( DataType.equals("*") ) {
			// Not enough parameters are specified.
        	message = "Parameters must be specified to match a single time series OR multiple time series (reading ALL time series is prohibited).";
			warning += "\n" + message;
        	status.addToLog ( CommandPhaseType.INITIALIZATION,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Specify parameters to match a single time series OR multiple time series.  At a minimum, specify the data type." ) );
		}
	}

	// Make sure the interval is specified if reading one time series.
	if ( readSingle && ((Interval == null) || Interval.isEmpty() || Interval.equals("*"))) {
        message = "The interval must be specified when reading a single time series (wildcard cannot be used).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify an interval to use for the single time series." ) );
	}

	// Make sure that project name is specified.
	if ( readSingle && ((ProjectName == null) || ProjectName.isEmpty() || ProjectName.equals("*"))) {
        message = "The project name must be specified when reading a single time series (wildcard cannot be used).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a project name to use for the single time series." ) );
	}

	// Make sure that user last name is specified.
	if ( readSingle && ((UserLastName == null) || UserLastName.isEmpty() || UserLastName.equals("*"))) {
        message = "The user last name must be specified when reading a single time series (wildcard cannot be used).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a user last name to use for the single time series." ) );
	}

	// Make sure that user first name is specified.
	if ( readSingle && ((UserFirstName == null) || UserFirstName.isEmpty() || UserFirstName.equals("*"))) {
        message = "The user first name must be specified when reading a single time series (wildcard cannot be used).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a user first name to use for the single time series." ) );
	}

    // If any issues were detected in the input filter add to the message string.
    if ( (InputFiltersCheck != null) && !InputFiltersCheck.isEmpty() ) {
    	warning += InputFiltersCheck;
    }

    // Check for invalid parameters.
    List<String> validList = new ArrayList<>();
    // Top.
    validList.add ( "DataStore" );
    //validList.add ( "TSID" );
    validList.add ( "DataType" );
    validList.add ( "Interval" );
    // Match single.
    validList.add ( "CustomerName" );
    validList.add ( "ProjectName" );
    validList.add ( "UserLastName" );
    validList.add ( "UserFirstName" );
    // Match 1+.
    int numFilters = 25; // Make a big number so all are allowed.
    for ( int i = 1; i <= numFilters; i++ ) {
        validList.add ( "Where" + i );
    }
    // Query filters.
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
    validList.add ( "IncludeHours" );
    //validList.add ( "ProjectStatus" );
    // Output.
    validList.add ( "Alias" );
    validList.add ( "DataFlag" );
    validList.add ( "IfMissing" );
    validList.add ( "WorkTableID" );
    validList.add ( "AppendWorkTable" );
    validList.add ( "Debug" );
    // Tables.
    validList.add ( "AccountCodeTableID" );
    validList.add ( "CustomerTableID" );
    validList.add ( "ProjectTableID" );
    validList.add ( "ProjectTimeTableID" );
    validList.add ( "UserTableID" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}

    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
 * Create the account code table.
 * @param dataStore the datastore containing global data
 * @param userTableID the identifier for the account code table
 */
private DataTable createAccountCodeTable ( TimesheetsComDataStore dataStore, String accountCodeTableID ) {
	DataTable table = new DataTable ();
	table.setTableID ( accountCodeTableID );

	// Main columns.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODENAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODEDESCRIPTION", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODESTATUS", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODEID", -1, -1), null);
	// Less important.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODENAMEPLAIN", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATEDDATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORUSERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "DEFAULTPAYRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "DEFAULTUSERPAYRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "USERPAYRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "READONLY", -1, -1), null);
	
	int errorCount = 0;
	for ( AccountCode code : dataStore.getAccountCodeCache() ) {
		try {
			TableRecord rec = new TableRecord();
			// Main columns.
			rec.addFieldValue(code.getAccountCodeName());
			rec.addFieldValue(code.getAccountCodeDescription());
			rec.addFieldValue(code.getAccountCodeStatus());
			rec.addFieldValue(code.getAccountCodeId());
			// Less imporant.
			rec.addFieldValue(code.getAccountCodeNamePlain());
			rec.addFieldValue(code.getCreatedDate());
			rec.addFieldValue(code.getCreatorUserId());
			rec.addFieldValue(code.getCreatorName());
			rec.addFieldValue(code.getDefaultPayRate());
			rec.addFieldValue(code.getDefaultUserPayRate());
			rec.addFieldValue(code.getUserPayRate());
			rec.addFieldValue(code.getReadOnly());
			table.addRecord(rec);
		}
		catch ( Exception e ) {
			// Should not happen and could generate a lot of output.
			if ( Message.isDebugOn ) {
				String routine = getClass().getSimpleName() + ".createAccountCodeTable";
				Message.printWarning(3, routine, "Error adding record to account code table.");
				Message.printWarning(3, routine, e);
			}
			++errorCount;
		}
	}
	
	if ( errorCount > 0 ) {
		throw new RuntimeException ( "Error creating account code table - run with debug and review log file." );
	}

	// Sort by the account code name.
	String [] sortColumns = { "ACCOUNTCODENAME" };
	int [] sortOrder = null;
	table.sortTable(sortColumns, sortOrder);

	return table;
}

/**
 * Create the customer table.
 * @param dataStore the datastore containing global data
 * @param userTableID the identifier for the customer table
 */
private DataTable createCustomerTable ( TimesheetsComDataStore dataStore, String customerTableID ) {
	DataTable table = new DataTable ();
	table.setTableID ( customerTableID );

	// Main columns.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERSTATUS", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "COMMENTS", -1, -1), null);
	// Less important.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNAMEPLAIN", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "COMPANYID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNUMBER", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CONTACTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CONTACTEMAIL", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CONTACTPHONE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORUSERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATEDDATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ADDRESS1", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ADDRESS2", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CITY", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "STATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ZIP", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "BUSINESSPHONE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "BUSINESSFAX", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "READONLY", -1, -1), null);

	int errorCount = 0;
	for ( Customer customer : dataStore.getCustomerCache() ) {
		try {
			TableRecord rec = new TableRecord();
			// Main columns.
			rec.addFieldValue(customer.getCustomerName());
			rec.addFieldValue(customer.getCustomerStatus());
			rec.addFieldValue(customer.getCustomerId());
			rec.addFieldValue(customer.getComments());
			// Less important.
			rec.addFieldValue(customer.getCustomerNamePlain());
			rec.addFieldValue(customer.getCompanyId());
			rec.addFieldValue(customer.getCustomerNumber());
			rec.addFieldValue(customer.getContactName());
			rec.addFieldValue(customer.getContactEmail());
			rec.addFieldValue(customer.getContactPhone());
			rec.addFieldValue(customer.getCreatorName());
			rec.addFieldValue(customer.getCreatorUserId());
			rec.addFieldValue(customer.getCreatedDate());
			rec.addFieldValue(customer.getAddress1());
			rec.addFieldValue(customer.getAddress2());
			rec.addFieldValue(customer.getCity());
			rec.addFieldValue(customer.getState());
			rec.addFieldValue(customer.getZip());
			rec.addFieldValue(customer.getBusinessPhone());
			rec.addFieldValue(customer.getBusinessFax());
			rec.addFieldValue(customer.getReadOnly());
			table.addRecord(rec);
		}
		catch ( Exception e ) {
			// Should not happen and could generate a lot of output.
			if ( Message.isDebugOn ) {
				String routine = getClass().getSimpleName() + ".createCustomerTable";
				Message.printWarning(3, routine, "Error adding record to customer table.");
				Message.printWarning(3, routine, e);
			}
			++errorCount;
		}

	}

	if ( errorCount > 0 ) {
		throw new RuntimeException ( "Error creating customer table - run with debug and review log file." );
	}

	// Sort by the customer name.
	String [] sortColumns = { "CUSTOMERNAME" };
	int [] sortOrder = null;
	table.sortTable(sortColumns, sortOrder);

	return table;
}

/**
 * Create the project table.
 * @param dataStore the datastore containing global data
 * @param userTableID the identifier for the project table
 */
private DataTable createProjectTable ( TimesheetsComDataStore dataStore, String projectTableID ) {
	DataTable table = new DataTable ();
	table.setTableID ( projectTableID );

	// Main columns.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTSTATUS", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTDESCRIPTION", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "MINIMUMTIMEINCREMENT", -1, -1), null);
	// Less important.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTNAMEPLAIN", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATEDDATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CREATORUSERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "DEFAULTBILLRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "DEFAULTUSERBILLRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "USERBILLRATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "READONLY", -1, -1), null);

	int errorCount = 0;
	for ( Project project : dataStore.getProjectCache() ) {
		try {
			TableRecord rec = new TableRecord();
			// Main columns.
			Customer customer =	Customer.lookupCustomerForCustomerId(dataStore.getCustomerCache(), project.getCustomerIdAsInteger() );
			if ( customer == null ) {
				rec.addFieldValue(customer);
			}
			else {
				rec.addFieldValue(customer.getCustomerName());
			}
			rec.addFieldValue(project.getProjectName());
			rec.addFieldValue(project.getProjectStatus());
			rec.addFieldValue(project.getProjectDescription());
			rec.addFieldValue(project.getProjectId());
			rec.addFieldValue(project.getMinimumTimeIncrement());
			// Less important.
			rec.addFieldValue(project.getProjectNamePlain());
			rec.addFieldValue(project.getCreatedDate());
			rec.addFieldValue(project.getCreatorName());
			rec.addFieldValue(project.getCreatorUserId());
			rec.addFieldValue(project.getDefaultBillRate());
			rec.addFieldValue(project.getDefaultUserBillRate());
			rec.addFieldValue(project.getUserBillRate());
			rec.addFieldValue(project.getReadOnly());
			table.addRecord(rec);
		}
		catch ( Exception e ) {
			// Should not happen and could generate a lot of output.
			//if ( Message.isDebugOn ) {
				String routine = getClass().getSimpleName() + ".createAccountCodeTable";
				Message.printWarning(3, routine, "Error adding record to account code table.");
				Message.printWarning(3, routine, e);
			//}
			++errorCount;
		}
	}

	if ( errorCount > 0 ) {
		throw new RuntimeException ( "Error creating project table - run with debug and review log file." );
	}
	
	// Sort by the project name and then project status descending so that active are listed first.
	String [] sortColumns = { "PROJECTNAME", "PROJECTSTATUS" };
	int [] sortOrder = { 1, -1 };
	table.sortTable(sortColumns, sortOrder);
	
	return table;
}

/**
 * Create the project time table.
 * @param dataStore the datastore containing global data
 * @param userTableID the identifier for the project table
 */
private DataTable createProjectTimeTable ( TimesheetsComDataStore dataStore, String projectTimeTableID ) {
	DataTable table = new DataTable ();
	table.setTableID ( projectTimeTableID );

	// Main columns.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PROJECTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODENAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "LASTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "FIRSTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "WORKDATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "HOURS", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "WORKDESCRIPTION", -1, -1), null);
	// Less important.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCOUNTCODEID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "APPROVED", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ARCHIVED", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "BILLABLE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CUSTOMERNUMBER", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "DEPARTMENT", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "EMPLOYEENUMBER", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "EMPLOYEETYPE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "JOBTITLE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "LASTCHANGEDATE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "PAYTYPE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "RECORDID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "SIGNED", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "USDERID", -1, -1), null);
	
	int errorCount = 0;
	for ( ReportProjectCustomizableReportData report: dataStore.getReportProjectCustomizableDataCache() ) {
		ReportProjectCustomizableRecord record = report.getReportProjectCustomizableRecord();
		for ( ReportProjectCustomizableData data : record.getReportProjectCustomizableDataList() ) {
			try {
				TableRecord rec = new TableRecord();
				// Main columns.
				rec.addFieldValue(data.getCustomerName());
				rec.addFieldValue(data.getProjectName());
				rec.addFieldValue(data.getAccountCodeName());
				rec.addFieldValue(data.getLastName());
				rec.addFieldValue(data.getFirstName());
				rec.addFieldValue(data.getWorkDate());
				rec.addFieldValue(data.getHours());
				rec.addFieldValue(data.getWorkDescription());
				// Less important.
				rec.addFieldValue(data.getAccountCodeId());
				rec.addFieldValue(data.getApproved());
				rec.addFieldValue(data.getArchived());
				rec.addFieldValue(data.getBillable());
				rec.addFieldValue(data.getCustomerId());
				rec.addFieldValue(data.getCustomerNumber());
				rec.addFieldValue(data.getDepartment());
				rec.addFieldValue(data.getEmployeeNumber());
				rec.addFieldValue(data.getEmployeeType());
				rec.addFieldValue(data.getJobTitle());
				rec.addFieldValue(data.getLastChangeDate());
				rec.addFieldValue(data.getPayType());
				rec.addFieldValue(data.getRecordId());
				rec.addFieldValue(data.getSigned());
				rec.addFieldValue(data.getUserId());
				table.addRecord(rec);
			}
			catch ( Exception e ) {
				// Should not happen and could generate a lot of output.
				if ( Message.isDebugOn ) {
					String routine = getClass().getSimpleName() + ".createUserTable";
					Message.printWarning(3, routine, "Error adding record to user table.");
					Message.printWarning(3, routine, e);
				}
				++errorCount;
			}
		}

	}

	if ( errorCount > 0 ) {
		throw new RuntimeException ( "Error creating project time table - run with debug and review log file." );
	}

	return table;
}

/**
 * Create the user table.
 * @param dataStore the datastore containing global data
 * @param userTableID the identifier for the user table
 */
private DataTable createUserTable ( TimesheetsComDataStore dataStore, String userTableID ) {
	DataTable table = new DataTable ();
	table.setTableID ( userTableID );

	// Main columns.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "LASTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "FIRSTNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "USERSTATUS", -1, -1), null);
	// Less important.
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "CONCATNAME", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "JOBTITLE", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ACCESS", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "ADMINUSERID", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "EMPLOYEENUMBER", -1, -1), null);
	table.addField(new TableField(TableField.DATA_TYPE_STRING, "USERID", -1, -1), null);
	//table.addField(new TableField(TableField.DATA_TYPE_STRING, "USERNAME", -1, -1), null);
	
	int errorCount = 0;
	for ( User user : dataStore.getUserCache() ) {
		try {
			TableRecord rec = new TableRecord();
			// Main columns.
			rec.addFieldValue(user.getLastName());
			rec.addFieldValue(user.getFirstName());
			rec.addFieldValue(user.getUserStatus());
			// Less important.
			rec.addFieldValue(user.getConcatName());
			rec.addFieldValue(user.getJobTitle());
			rec.addFieldValue(user.getAccess());
			rec.addFieldValue(user.getAdminUserId());
			rec.addFieldValue(user.getEmployeeNumber());
			rec.addFieldValue(user.getUserId());
			//rec.addFieldValue(user.getUserName());
			table.addRecord(rec);
		}
		catch ( Exception e ) {
			// Should not happen and could generate a lot of output.
			if ( Message.isDebugOn ) {
				String routine = getClass().getSimpleName() + ".createUserTable";
				Message.printWarning(3, routine, "Error adding record to user table.");
				Message.printWarning(3, routine, e);
			}
			++errorCount;
		}

	}

	if ( errorCount > 0 ) {
		throw new RuntimeException ( "Error creating user table - run with debug and review log file." );
	}
	
	return table;
}

/**
 * Create properties for reading time series.
 * @param debug whether to run web service queries in debug
 * @param dataFlag how to set the data flag (empty/null, "Archived", "Archived0", "Archived1")
 * @param includeHours data to include (empty/null, "Archived", "New", "All")
 */
private HashMap<String,Object> createReadProperties ( boolean debug, String dataFlag, String includeHours ) { //, String projectStatus ) {
	HashMap<String,Object> readProperties = new HashMap<>();
	if ( debug ) {
		readProperties.put("Debug", new Boolean(true) );
	}
	if ( (dataFlag != null) && !dataFlag.isEmpty() ) {
		readProperties.put("DataFlag", dataFlag );
	}
	if ( (includeHours != null) && !includeHours.isEmpty() ) {
		readProperties.put("IncludeHours", includeHours );
	}
	/*
	if ( (projectStatus != null) && !projectStatus.isEmpty() ) {
		readProperties.put("ProjectStatus", projectStatus );
	}
	*/
	return readProperties;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent ) {
	List<String> tableIDChoices = TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
        (TSCommandProcessor)getCommandProcessor(), this);
	// The command will be modified if changed.
	return (new ReadTimesheetsCom_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Return the account code table that is read by this class when run in discovery mode.
@return the account code table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryAccountCodeTable() {
    return this.discoveryAccountCodeTable;
}

/**
Return the customer table that is read by this class when run in discovery mode.
@return the customer table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryCustomerTable() {
    return this.discoveryCustomerTable;
}

/**
Return the project table that is read by this class when run in discovery mode.
@return the project table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryProjectTable() {
    return this.discoveryProjectTable;
}

/**
Return the project time table that is read by this class when run in discovery mode.
@return the project time table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryProjectTimeTable() {
    return this.discoveryProjectTimeTable;
}

/**
Return the user table that is read by this class when run in discovery mode.
@return the user table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryUserTable() {
    return this.discoveryUserTable;
}

/**
Return the work notes table that is read by this class when run in discovery mode.
@return the work notes table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryWorkTable() {
    return this.discoveryWorkTable;
}

/**
Return the list of time series read in discovery phase.
*/
private List<TS> getDiscoveryTSList () {
    return this.__discoveryTSList;
}

/**
Return the list of data objects read by this object in discovery mode.
The following classes can be requested:  TS
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c ) {
   	List<TS> discovery_TS_List = getDiscoveryTSList ();
  	TS datats = null;
  	if ( (discovery_TS_List != null) && (discovery_TS_List.size() > 0) ) {
  		// Get the first time series so can compare class below.
  		datats = discovery_TS_List.get(0);
  	}
    DataTable accountCodeTable = getDiscoveryAccountCodeTable();
    DataTable customerTable = getDiscoveryCustomerTable();
    DataTable projectTable = getDiscoveryProjectTable();
    DataTable projectTimeTable = getDiscoveryProjectTimeTable();
    DataTable userTable = getDiscoveryUserTable();
    DataTable workTable = getDiscoveryWorkTable();
    // Also check the base class.
    if ( (datats != null) && ((c == TS.class) || (c == datats.getClass())) ) {
    	// Asking for a list of time series.
       	if ( (discovery_TS_List == null) || (discovery_TS_List.size() == 0) ) {
           	return null;
       	}
       	// Since all time series must be the same interval, check the class for the first one (e.g., MonthTS).
       	// Can return immediately.
        return (List<T>)discovery_TS_List;
    }

    // The following handles the tables:
    // - currently only time series and tables can be requested
   	List<T> tableList = new ArrayList<>();
    if ( (accountCodeTable != null) && (c == accountCodeTable.getClass()) ) {
        tableList.add ( (T)accountCodeTable );
    }
    if ( (customerTable != null) && (c == customerTable.getClass()) ) {
        tableList.add ( (T)customerTable );
    }
    if ( (projectTable != null) && (c == projectTable.getClass()) ) {
        tableList.add ( (T)projectTable );
    }
    if ( (projectTimeTable != null) && (c == projectTimeTable.getClass()) ) {
        tableList.add ( (T)projectTimeTable );
    }
    if ( (userTable != null) && (c == userTable.getClass()) ) {
        tableList.add ( (T)userTable );
    }
    if ( (workTable != null) && (c == workTable.getClass()) ) {
        tableList.add ( (T)workTable );
    }
    if ( tableList.size() == 0 ) {
    	// No tables so return null.
        return null;
    }
    else {
    	// Have tables so return.
    	return tableList;
    }
}

// parseCommand is in parent class.

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String routine = getClass().getSimpleName() + ".runCommandInternal", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
	TSCommandProcessor tsprocessor = (TSCommandProcessor)processor;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);

    Boolean clearStatus = new Boolean(true); // Default.
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen.
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}

    boolean readData = true;
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTSList ( null );
        setDiscoveryAccountCodeTable ( null );
        setDiscoveryCustomerTable ( null );
        setDiscoveryProjectTable ( null );
        setDiscoveryProjectTimeTable ( null );
        setDiscoveryUserTable ( null );
        setDiscoveryWorkTable ( null );
        readData = false;
    }

	String DataType = parameters.getValue("DataType");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    DataType = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, DataType);
	}
	String Interval = parameters.getValue("Interval");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    Interval = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, Interval);
	}
	String CustomerName = parameters.getValue("CustomerName");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    CustomerName = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, CustomerName);
	}
	String ProjectName = parameters.getValue("ProjectName");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    ProjectName = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ProjectName);
	}
	String UserLastName = parameters.getValue("UserLastName");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    UserLastName = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, UserLastName);
	}
	String UserFirstName = parameters.getValue("UserFirstName");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    UserFirstName = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, UserFirstName);
	}
	String InputStart = parameters.getValue("InputStart");
	if ( (InputStart == null) || InputStart.isEmpty() ) {
		// Global input start.
		InputStart = "${InputStart}";
	}
    String InputEnd = parameters.getValue("InputEnd");
	if ( (InputEnd == null) || InputEnd.isEmpty() ) {
		// Global input end.
		InputEnd = "${InputEnd}";
	}
    String IncludeHours = parameters.getValue ("IncludeHours" );
	if ( commandPhase == CommandPhaseType.RUN ) {
	    IncludeHours = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, IncludeHours);
	}
    //String ProjectStatus = parameters.getValue ("ProjectStatus" );
	//if ( commandPhase == CommandPhaseType.RUN ) {
	    //ProjectStatus = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ProjectStatus);
	//}
	//if ( (ProjectStatus == null) || ProjectStatus.isEmpty() ) {
	//	ProjectStatus = this._Active; // Default;
	//}
	String WorkTableID = parameters.getValue("WorkTableID");
	boolean doWorkTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    WorkTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, WorkTableID);
	}
	if ( (WorkTableID != null) && !WorkTableID.isEmpty() ) {
		doWorkTable = true;
	}
	String AppendWorkTable = parameters.getValue("AppendWorkTable");
	boolean doAppendWorkTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    AppendWorkTable = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, AppendWorkTable);
	}
	if ( (AppendWorkTable != null) && AppendWorkTable.equalsIgnoreCase(_True) ) {
		doAppendWorkTable = true;
	}
    String DataFlag = parameters.getValue ("DataFlag" );
	if ( commandPhase == CommandPhaseType.RUN ) {
	    DataFlag = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, DataFlag);
	}
    String IfMissing = parameters.getValue ("IfMissing" );
    boolean ifMissingWarn = true;  // Default.
    if ( (IfMissing != null) && IfMissing.equalsIgnoreCase(_Ignore) ) {
        ifMissingWarn = false;  // Ignore when time series are not found.
    }
    String Debug = parameters.getValue("Debug");
    boolean debug = false;
	if ( (Debug != null) && Debug.equalsIgnoreCase("true") ) {
		debug = true;
	}

	String AccountCodeTableID = parameters.getValue("AccountCodeTableID");
	boolean doAccountCodeTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    AccountCodeTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, AccountCodeTableID);
	}
	if ( (AccountCodeTableID != null) && !AccountCodeTableID.isEmpty() ) {
		doAccountCodeTable = true;
	}
	String CustomerTableID = parameters.getValue("CustomerTableID");
	boolean doCustomerTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    CustomerTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, CustomerTableID);
	}
	if ( (CustomerTableID != null) && !CustomerTableID.isEmpty() ) {
		doCustomerTable = true;
	}
	String ProjectTableID = parameters.getValue("ProjectTableID");
	boolean doProjectTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    ProjectTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ProjectTableID);
	}
	if ( (ProjectTableID != null) && !ProjectTableID.isEmpty() ) {
		doProjectTable = true;
	}
	String ProjectTimeTableID = parameters.getValue("ProjectTimeTableID");
	boolean doProjectTimeTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    ProjectTimeTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ProjectTimeTableID);
	}
	if ( (ProjectTimeTableID != null) && !ProjectTimeTableID.isEmpty() ) {
		doProjectTimeTable = true;
	}
	String UserTableID = parameters.getValue("UserTableID");
	boolean doUserTable = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    UserTableID = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, UserTableID);
	}
	if ( (UserTableID != null) && !UserTableID.isEmpty() ) {
		doUserTable = true;
	}

    DateTime InputStart_DateTime = null;
    DateTime InputEnd_DateTime = null;
	DataTable workTable = null;
	if ( commandPhase == CommandPhaseType.RUN ) {
		try {
			InputStart_DateTime = TSCommandProcessorUtil.getDateTime ( InputStart, "InputStart", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}
		try {
			InputEnd_DateTime = TSCommandProcessorUtil.getDateTime ( InputEnd, "InputEnd", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}

		// Get the table to process.

		PropList request_params = null;
		CommandProcessorRequestResultsBean bean = null;
		if ( (WorkTableID != null) && !WorkTableID.equals("") ) {
			// Get the table to be updated/created.
			request_params = new PropList ( "" );
			request_params.set ( "TableID", WorkTableID );
			try {
				bean = processor.processRequest( "GetTable", request_params);
				PropList bean_PropList = bean.getResultsPropList();
				Object o_Table = bean_PropList.getContents ( "Table" );
				if ( o_Table != null ) {
					// Found the table so no need to create it.
					workTable = (DataTable)o_Table;
				}
			}
			catch ( Exception e ) {
				message = "Error requesting GetTable(TableID=\"" + WorkTableID + "\") from processor.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			}
		}

	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Get the datastore here because it is needed to create the table.
	
    String DataStore = parameters.getValue ( "DataStore" );
    TimesheetsComDataStore dataStore = null;
	if ( (DataStore != null) && !DataStore.equals("") ) {
	    // User has indicated that a datastore should be used.
	    DataStore dataStore0 = ((TSCommandProcessor)getCommandProcessor()).getDataStoreForName( DataStore, TimesheetsComDataStore.class );
        if ( dataStore0 != null ) {
            Message.printStatus(2, routine, "Selected datastore is \"" + dataStore0.getName() + "\"." );
			dataStore = (TimesheetsComDataStore)dataStore0;
        }
    }
	if ( dataStore == null ) {
           message = "Cannot get TimesheetsComDataStore for \"" + DataStore + "\".";
           Message.printWarning ( 2, routine, message );
           status.addToLog ( commandPhase,
               new CommandLogRecord(CommandStatusType.FAILURE,
                   message, "Verify that a TimesheetsComDataStore datastore is properly configured." ) );
           throw new RuntimeException ( message );
    }

	// Create the work notes table if requested:
	// - do up front because it is populated as the data are processed
	if ( doWorkTable ) {
		if ( commandPhase == CommandPhaseType.DISCOVERY ) {
			if ( workTable == null ) {
				// Did not find table so is being created in this command.
				// Create an empty table and set the ID.
				workTable = new DataTable();
				workTable.setTableID ( WorkTableID );
				setDiscoveryWorkTable ( workTable );
			}
			else {
				// The table was created in a previous command so don't need to add here.
			}
		}
		else if ( commandPhase == CommandPhaseType.RUN ) {
        	if ( (workTable == null) || !doAppendWorkTable ) {
        		// Did not find the table above so create it.
        		if ( workTable == null ) {
        			Message.printStatus(2, routine, "Was not able to match existing table \"" + WorkTableID + "\" so creating a new table.");
        		}
        		else {
        			Message.printStatus(2, routine, "Appending to existing table \"" + WorkTableID + "\".");
        		}
        		
        		// Create table with standard columns.
        		workTable = dataStore.createWorkTable ( WorkTableID );

        		// Set the table in the processor.

        		PropList request_params = new PropList ( "" );
        		request_params.setUsingObject ( "Table", workTable );
        		try {
        			processor.processRequest( "SetTable", request_params);
        		}
        		catch ( Exception e ) {
        			message = "Error requesting SetTable(Table=...) from processor.";
        			Message.printWarning(warning_level,
        				MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
        			status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                           message, "Report problem to software support." ) );
        		}
        	}
		}
	}

	// Set up properties for the read:
	// - OK if null values

	//PropList readProps = new PropList ( "ReadProps" );

	// Now try to read.

	// List for time series results:
	// - will be added to for one time series read or replaced if a list is read
	List<TS> tslist = new ArrayList<>();
	try {
        String Alias = parameters.getValue ( "Alias" );
        //String TSID = parameters.getValue ( "TSID" );
        if ( dataStore != null ) {
			// Have a datastore so try to read.
        	// See if a Where has been specified by checking for the first Where clause.
			String WhereN = parameters.getValue ( "Where" + 1 );
			//if ( (WhereN == null) || WhereN.isEmpty() ) { // }
			if ( (CustomerName != null) && !CustomerName.isEmpty() ) {
				// Have single customer name so try to read the single matching time series.
				TSIdent tsident = new TSIdent();
				if ( CustomerName != null ) {
					tsident.setLocation("'" + CustomerName + "/" + ProjectName + "/" + UserLastName + "," + UserFirstName + "'");
				}
				tsident.setSource("TimesheetsCom");
				if ( (DataType != null) && !DataType.isEmpty() && !DataType.equals("*") ) {
					tsident.setType(DataType);
				}
				if ( (Interval != null) && !Interval.isEmpty() && !Interval.equals("*") ) {
					tsident.setInterval(Interval);
				}
				String TSID = tsident.getIdentifier();
				// Currently can only read up to Day interval:
				// - TODO smalers 2023-01-16 enable once know how to handle
				boolean doRead = true;
				if ( (tsident.getIntervalBase() != TimeInterval.DAY) || (tsident.getIntervalMult() != 1) ) {
					message = "Can only read day interval for \"" + TSID + "\".";
					Message.printWarning ( 2, routine, message );
	                status.addToLog ( commandPhase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "The timesheets.com API only provides daily interval data." ) );
	                doRead = false;
				}
				if ( doRead ) {
					// OK to try reading.
					Message.printStatus ( 2, routine, "Reading a single TimesheetsCom web service time series \"" + TSID + "\"" );
					TS ts = null;
					try {
						HashMap<String,Object> readProperties = createReadProperties ( debug, DataFlag, IncludeHours ); //, ProjectStatus );
						if ( (commandPhase == CommandPhaseType.DISCOVERY) && TSID.contains("${") ) {
							// Create a single time series with the identifier.
							ts = TSUtil.newTimeSeries(TSID, true);
						}
						else {
							// In run mode so try reading the time series.
							ts = dataStore.readTimeSeries ( TSID, InputStart_DateTime, InputEnd_DateTime, readData, readProperties, workTable );
						}
					}
					catch ( Exception e ) {
				    	ts = null;
						message = "Unexpected error reading TimesheetsCom time series \"" + TSID + "\" (" + e + ").";
				    	if ( ifMissingWarn ) {
				    		Message.printWarning ( 2, routine, message );
	                       	status.addToLog ( commandPhase,
	                           	new CommandLogRecord(CommandStatusType.FAILURE,
	                               	message, "Verify the time series identifier." ) );
	                      	throw new RuntimeException ( message );
					   	}
					   	else {
					       	// Just show for info purposes.
	                       	status.addToLog ( commandPhase,
	                           	new CommandLogRecord(CommandStatusType.INFO,
	                               	message, "Verify the time series identifier." ) );
					   	}
					}
					finally {
				    	if ( ts == null ) {
				        	// Generate an event for listeners.
				        	notifyCommandProcessorEventListeners(new MissingObjectEvent(TSID,Class.forName("RTi.TS.TS"),"Time Series", this));
				    	}
					}
					if ( ts != null ) {
						// Set the alias.
				    	if ( Alias != null ) {
				    		if ( (commandPhase == CommandPhaseType.DISCOVERY) ) {
				    			// Set alias as is, may contain ${Property}.
				    			ts.setAlias ( Alias );
				    		}
				    		else {
				    			// Expand the alias before setting.
				    			ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString(
			                    	processor, ts, Alias, status, commandPhase) );
				    		}
				    	}
						tslist.add ( ts );
					}
				}
	        } // End reading a single time series.
			else {
	            // Read 1+ time series using the input filters.
				// Get the input needed to process the file.
				Message.printStatus(2, routine, "Reading multiple TimesheetsCom time series using input filter.");
				String InputName = parameters.getValue ( "InputName" );
				if ( InputName == null ) {
					InputName = "";
				}
				List<String> whereNList = new ArrayList<>();
				int nfg = 0; // Used below.
				// User may have skipped a where and left a blank so loop over a sufficiently large number of where parameters
				// to get the non-blank filters.
				for ( int ifg = 0; ifg < 25; ifg++ ) {
					WhereN = parameters.getValue ( "Where" + (ifg + 1) );
					if ( WhereN != null ) {
						++nfg;
						whereNList.add ( WhereN );
					}
				}

				// Initialize an input filter based on the data type.

				InputFilter_JPanel filterPanel = null;

				// Create the input filter panel.
				String dataTypeReq = "";
			    if ( dataTypeReq.indexOf("-") > 0 ) {
			        dataTypeReq = StringUtil.getToken(DataType,"-",0,1).trim();
			    }
			    else {
			        dataTypeReq = DataType.trim();
			    }

				filterPanel = new TimesheetsCom_TimeSeries_InputFilter_JPanel ( dataStore, 5 );

				// Populate with the where information from the command:
				// - the first part of the where should match the "whereLabelPersistent" used when constructing the input filter
				// - the TimesheetsCom internal field is used to help users correlate the TSTool filter to TimesheetsCom web services

				String filterDelim = ";";
				for ( int ifg = 0; ifg < nfg; ifg++ ) {
					WhereN = whereNList.get(ifg);
	                if ( WhereN.length() == 0 ) {
	                    continue;
	                }
	                // If the Where does not exactly match a choice,
	                // will need to add to the filter because it is not a predefined choice.
	                // For example, this could be used with string operations.
	                // It is OK if the filter is already in the list.
	                InputFilter filter = filterPanel.getInputFilter(ifg);
	                if ( filter.getInputComponent() instanceof SimpleJComboBox  ) {
	                	SimpleJComboBox combobox = (SimpleJComboBox)filter.getInputComponent();
	                	if ( combobox.isEditable() ) {
	                		// TODO smalers 2023-11-21 might need to set in the choices to avoid an error.
	                	}
	                }
					// Set the filter:
	                // - will exactly match a choice
	                // - also allow setting the text in the SimpleJComboBox
					try {
						//boolean setTextIfNoChoiceMatches = true;
	                    //filterPanel.setInputFilter( ifg, WhereN, filterDelim, setTextIfNoChoiceMatches );
	                    filterPanel.setInputFilter( ifg, WhereN, filterDelim );
					}
					catch ( Exception e ) {
	                    message = "Error setting where information using \"" + WhereN + "\"";
						Message.printWarning ( 2, routine,message);
						Message.printWarning ( 3, routine, e );
						++warning_count;
	                    status.addToLog ( commandPhase,
	                        new CommandLogRecord(CommandStatusType.FAILURE,
	                            message, "Report the problem to software support - also see the log file." ) );
					}
				}

				// Read the list of objects from which identifiers can be obtained.

				Message.printStatus ( 2, routine, "Getting the list of time series..." );

				// Create empty lists for catalogs from each major data category.
				List<TimeSeriesCatalog> tsCatalogList = new ArrayList<>();

				// Read the catalog.
				int size = 0;
				if ( commandPhase == CommandPhaseType.RUN ) {
					try {
						String tsid = null;
						tsCatalogList = dataStore.readTimeSeriesCatalog ( tsid, dataTypeReq, Interval, filterPanel );
						size = tsCatalogList.size();
					}
					catch ( Exception e ) {
						// Probably no data.
					}

					// Filter based on project active/archived.
					
					// Default is to only read time series for active projects.
					/*
					boolean doProjectActive = true;
					boolean doProjectArchived = false;
					if ( ProjectStatus.equalsIgnoreCase("Active") ) {
						doProjectActive = true;
						doProjectArchived = false;
					}
					else if ( ProjectStatus.equalsIgnoreCase("Archived") ) {
						doProjectActive = false;
						doProjectArchived = true;
					}
					else if ( ProjectStatus.equalsIgnoreCase("All") ) {
						doProjectActive = true;
						doProjectArchived = true;
					}
				
					if ( !doProjectActive || !doProjectArchived ) {
						// Need to check whether projects are active/archived.
						// Loop backwards so the list size can be changed.
						for ( int i = tsCatalogList.size() - 1; i >= 0; --i ) {
							int projectId = Integer.valueOf(tsCatalogList.get(i).getProjectId());
    		
							if ( doProjectActive ) {
								// Only process active projects.
								if ( !dataStore.projectIsActive(projectId) ) {
									tsCatalogList.remove(i);
								}
							}
							else if ( doProjectArchived ) {
								// Only process archived projects.
								if ( !dataStore.projectIsArchived(projectId) ) {
									tsCatalogList.remove(i);
								}
							}
						}
						// Reset the size.
						size = tsCatalogList.size();
					}
					*/
	
					// Make sure that size is set.
	       			if ( size == 0 ) {
						Message.printStatus ( 2, routine,"No TimesheetsCom web service time series were found." );
			        	// Warn if nothing was retrieved (can be overridden to ignore).
						if ( ifMissingWarn ) {
							message = "No time series were read from the TimesheetsCom web service.";
							Message.printWarning ( warning_level,
								MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
							status.addToLog ( commandPhase,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Data may not be in database." +
									"  Previous messages may provide more information." ) );
						}
		            	// Generate an event for listeners.
		            	// FIXME SAM 2008-08-20 Need to put together a more readable id for reporting.
	                	//notifyCommandProcessorEventListeners(
	                	//    new MissingObjectEvent(DataType + ", " + Interval + ", see command for user-specified criteria",
	                	//        Class.forName("RTi.TS.TS"),"Time Series", this));
						return;
	       			}

					// Else, convert each header object to a TSID string and read the time series.

					Message.printStatus ( 2, "", "Reading " + size + " time series..." );

					String tsidentString = null; // TSIdent string.
					TS ts; // Time series to read.
					TimeSeriesCatalog tsCatalog;
					HashMap<String,Object> readProperties = createReadProperties ( debug, DataFlag, IncludeHours ); //, ProjectStatus );
					for ( int i = 0; i < size; i++ ) {
						// Check to see if reading time series should be canceled because the command has been canceled.
						if ( tsprocessor.getCancelProcessingRequested() ) {
							// The user has requested that command processing should be canceled.
							// Check here in this command because a very large query could take a long time before a single command finishes.
							Message.printStatus(2, routine, "Cancel processing based on user request.");
							break;
						}
						// List in order of likelihood to improve performance.
						tsidentString = null; // Do this in case there is no active match.
						tsCatalog = (TimeSeriesCatalog)tsCatalogList.get(i);
						String customerName = tsCatalog.getCustomerName();
						String projectName = tsCatalog.getProjectName();
						String userLastName = tsCatalog.getUserLastName();
						String userFirstName = tsCatalog.getUserFirstName();
						String dataSource = "TimesheetsCom";
						String dataType = tsCatalog.getDataType();
						String interval = tsCatalog.getDataInterval();
						if ( (interval == null) || interval.equals("*") ) {
							// Don't set the interval so called code can determine.
							interval = "";
						}
						tsidentString =
							"'" + customerName + "/" + projectName + "/" + userLastName + "," + userFirstName + "'"
							+ "." + dataSource
							+ "." + dataType
							+ "." + interval;
		            	// Update the progress.
						message = "Reading TimesheetsCom web service time series " + (i + 1) + " of " + size + " \"" + tsidentString + "\"";
	                	notifyCommandProgressListeners ( i, size, (float)-1.0, message );
						try {
					    	ts = dataStore.readTimeSeries (
								tsidentString,
								InputStart_DateTime,
								InputEnd_DateTime, readData, readProperties, workTable );
							// Add the time series to the temporary list.  It will be further processed below.
		                	if ( (ts != null) && (Alias != null) && !Alias.equals("") ) {
		                    	ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString(
		                        	processor, ts, Alias, status, commandPhase) );
		                	}
		                	// Allow null to be added here.
							tslist.add ( ts );
						}
						catch ( Exception e ) {
							message = "Unexpected error reading TimesheetsCom web service time series \"" + tsidentString + "\" (" + e + ").";
							Message.printWarning ( 2, routine, message );
							Message.printWarning ( 2, routine, e );
							++warning_count;
	                    	status.addToLog ( commandPhase,
	                        	new CommandLogRecord(CommandStatusType.FAILURE,
	                           	message, "Report the problem to software support - also see the log file." ) );
						}
					}
				} // Command phase RUN.
			} // End reading using input filters.
		}

        int size = 0;
        if ( tslist != null ) {
            size = tslist.size();
        }

        if ( commandPhase == CommandPhaseType.RUN ) {
        	Message.printStatus ( 2, routine, "Read " + size + " TimesheetsCom web service time series." );
            if ( tslist != null ) {
                // Further process the time series.
                // This makes sure the period is at least as long as the output period.

                int wc = TSCommandProcessorUtil.processTimeSeriesListAfterRead( processor, this, tslist );
                if ( wc > 0 ) {
                    message = "Error post-processing TimesheetsCom web service time series after read.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }

                // Now add the list in the processor.

                int wc2 = TSCommandProcessorUtil.appendTimeSeriesListToResultsList ( processor, this, tslist );
                if ( wc2 > 0 ) {
                    message = "Error adding TimesheetsCom web service time series after read.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }
            }

            // Warn if nothing was retrieved (can be overridden to ignore).
            if ( (tslist == null) || (size == 0) ) {
				if ( ifMissingWarn ) {
					message = "No time series were read from the TimesheetsCom web service.";
					Message.printWarning ( warning_level,
						MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                    	status.addToLog ( commandPhase,
                    		new CommandLogRecord(CommandStatusType.FAILURE,
                    				message, "Data may not be in database.  See previous messages." ) );
				}
                // Generate an event for listeners.
                // TOD SAM 2008-08-20 Evaluate whether need here.
                //notifyCommandProcessorEventListeners(new MissingObjectEvent(DataType + ", " + Interval + filter_panel,this));
            }
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        	// This will typically only be populated when reading a single time series.
            setDiscoveryTSList ( tslist );
        }

        // Create and save the output tables if requested.

		if ( doAccountCodeTable ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				DataTable table = new DataTable();
				table.setTableID ( AccountCodeTableID );
				setDiscoveryAccountCodeTable ( table );
			}
			else if ( commandPhase == CommandPhaseType.RUN ) {
				// Create table with standard columns.
				DataTable table = null;
				try {
					table = createAccountCodeTable ( dataStore, AccountCodeTableID );
				}
				catch ( Exception e ) {
					message = "Error creating account code table.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					Message.printWarning(warning_level,routine,e);
					status.addToLog ( commandPhase,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Run with debug on and review the log file.") );
					table = null;
				}

				// Set the table in the processor.

				if ( table != null ) {
					PropList request_params = new PropList ( "" );
					request_params.setUsingObject ( "Table", table );
					try {
						processor.processRequest( "SetTable", request_params);
					}
					catch ( Exception e ) {
						message = "Error requesting SetTable(Table=...) from processor.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( commandPhase,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
					}
       			}
			}
		}

		if ( doCustomerTable ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				DataTable table = new DataTable();
				table.setTableID ( CustomerTableID );
				setDiscoveryCustomerTable ( table );
			}
			else if ( commandPhase == CommandPhaseType.RUN ) {
				// Create table with standard columns.
				DataTable table = null;
				try {
					table = createCustomerTable ( dataStore, CustomerTableID );
				}
				catch ( Exception e ) {
					message = "Error creating customer table.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					Message.printWarning(warning_level,routine,e);
					status.addToLog ( commandPhase,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Run with debug on and review the log file.") );
					table = null;
				}

				if ( table != null ) {
					// Set the table in the processor.

					PropList request_params = new PropList ( "" );
					request_params.setUsingObject ( "Table", table );
					try {
						processor.processRequest( "SetTable", request_params);
					}
					catch ( Exception e ) {
						message = "Error requesting SetTable(Table=...) from processor.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( commandPhase,
                       	new CommandLogRecord(CommandStatusType.FAILURE,
                          	message, "Report problem to software support." ) );
					}
				}
			}
		}

		if ( doProjectTable ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				DataTable table = new DataTable();
				table.setTableID ( ProjectTableID );
				setDiscoveryProjectTable ( table );
			}
			else if ( commandPhase == CommandPhaseType.RUN ) {
				// Create table with standard columns.
				DataTable table = null;
				try {
					table = createProjectTable ( dataStore, ProjectTableID );
				}
				catch ( Exception e ) {
					message = "Error creating project table.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					Message.printWarning(warning_level,routine,e);
					status.addToLog ( commandPhase,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Run with debug on and review the log file.") );
					table = null;
				}

				if ( table != null ) {
					// Set the table in the processor.

					PropList request_params = new PropList ( "" );
					request_params.setUsingObject ( "Table", table );
					try {
						processor.processRequest( "SetTable", request_params);
					}
					catch ( Exception e ) {
						message = "Error requesting SetTable(Table=...) from processor.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( commandPhase,
                       	new CommandLogRecord(CommandStatusType.FAILURE,
                          	message, "Report problem to software support." ) );
					}
       			}
			}
		}

		if ( doProjectTimeTable ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				DataTable table = new DataTable();
				table.setTableID ( ProjectTimeTableID );
				setDiscoveryProjectTimeTable ( table );
			}
			else if ( commandPhase == CommandPhaseType.RUN ) {
				// Create table with standard columns.
				DataTable table = null;
				try {
					table = createProjectTimeTable ( dataStore, ProjectTimeTableID );
				}
				catch ( Exception e ) {
					message = "Error creating project time table.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( commandPhase,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Run with debug on and review the log file.") );
					table = null;
				}

				if ( table != null ) {
					// Set the table in the processor.

					PropList request_params = new PropList ( "" );
					request_params.setUsingObject ( "Table", table );
					try {
						processor.processRequest( "SetTable", request_params);
					}
					catch ( Exception e ) {
						message = "Error requesting SetTable(Table=...) from processor.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( commandPhase,
                       		new CommandLogRecord(CommandStatusType.FAILURE,
                          	message, "Report problem to software support." ) );
					}
				}
       		}
		}
        
		if ( doUserTable ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				DataTable table = new DataTable();
				table.setTableID ( UserTableID );
				setDiscoveryUserTable ( table );
			}
			else if ( commandPhase == CommandPhaseType.RUN ) {
				// Create table with standard columns.
				DataTable table = null;
				try {
					table = createUserTable ( dataStore, UserTableID );
				}
				catch ( Exception e ) {
					message = "Error creating user table.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					Message.printWarning(warning_level,routine,e);
					status.addToLog ( commandPhase,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Run with debug on and review the log file.") );
					table = null;
				}

				if ( table != null ) {
					// Set the table in the processor.

					PropList request_params = new PropList ( "" );
					request_params.setUsingObject ( "Table", table );
					try {
						processor.processRequest( "SetTable", request_params);
					}
					catch ( Exception e ) {
						message = "Error requesting SetTable(Table=...) from processor.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
                       	routine, message );
						status.addToLog ( commandPhase,
                       	new CommandLogRecord(CommandStatusType.FAILURE,
                          message, "Report problem to software support." ) );
					}
       			}
			}
		}
        
        // Check whether the datastore had errors reading global data.
        
        List<String> problems = dataStore.getGlobalDataProblems();
        if ( (problems != null) && !problems.isEmpty() ) {
        	for ( String problem : problems ) {
               	status.addToLog ( commandPhase,
               		new CommandLogRecord(CommandStatusType.FAILURE,
            		problem, "Results may be incomplete.  Check the log file." ) );
               	Message.printWarning(3, routine, problem);
        	}
        }
        
	} // End if have datastore.
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message ="Unexpected error reading time series from the TimesheetsCom web service (" + e + ").";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
               message, "Report the problem to software support - also see the log file." ) );
		throw new CommandException ( message );
	}

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the account codes table that is read by this class in discovery mode.
@param account codes table the DataTable to receive output
*/
private void setDiscoveryAccountCodeTable ( DataTable table ) {
    this.discoveryAccountCodeTable = table;
}

/**
Set the customer table that is read by this class in discovery mode.
@param customer table the DataTable to receive output
*/
private void setDiscoveryCustomerTable ( DataTable table ) {
    this.discoveryCustomerTable = table;
}

/**
Set the project table that is read by this class in discovery mode.
@param project table the DataTable to receive output
*/
private void setDiscoveryProjectTable ( DataTable table ) {
    this.discoveryProjectTable = table;
}

/**
Set the project time table that is read by this class in discovery mode.
@param project time table the DataTable to receive output
*/
private void setDiscoveryProjectTimeTable ( DataTable table ) {
    this.discoveryProjectTimeTable = table;
}

/**
Set the user table that is read by this class in discovery mode.
@param user table the DataTable to receive output
*/
private void setDiscoveryUserTable ( DataTable table ) {
    this.discoveryUserTable = table;
}

/**
Set the work notes table that is read by this class in discovery mode.
@param work notes table the DataTable to receive output
*/
private void setDiscoveryWorkTable ( DataTable table ) {
    this.discoveryWorkTable = table;
}

/**
Set the list of time series read in discovery phase.
*/
private void setDiscoveryTSList ( List<TS> discoveryTSList ) {
    __discoveryTSList = discoveryTSList;
}

/**
Return the string representation of the command.
@param parameters parameters to include in the command
@return the string representation of the command
*/
public String toString ( PropList parameters ) {
	String [] parameterOrder1 = {
    	"DataStore",
    	"DataType",
    	"Interval",
    	// Match single.
    	"CustomerName",
    	"ProjectName",
    	"UserLastName",
    	"UserFirstName",
	};
  	// Match 1+.
	String delim = ";";
	List<String> whereParameters = new ArrayList<>();
    for ( int i = 1; i <= __numWhere; i++ ) {
    	String where = parameters.getValue("Where" + i);
    	if ( (where != null) && !where.isEmpty() && !where.startsWith(delim) ) {
    		whereParameters.add("Where" + i);
    	}
    }
	String [] parameterOrder2 = {
		// Filters.
		"InputStart",
		"InputEnd",
		"IncludeHours",
		//"ProjectStatus",
		// Output.
		"Alias",
		"DataFlag",
		"IfMissing",
		"Debug",
		// Work notes.
    	"WorkTableID",
    	"AppendWorkTable",
    	// Tables.
    	"AccountCodeTableID",
    	"CustomerTableID",
    	"ProjectTableID",
    	"ProjectTimeTableID",
    	"UserTableID"
	};

	// Format the final property list.
	String [] parameterOrder = new String[parameterOrder1.length + whereParameters.size() + parameterOrder2.length];
	int iparam = 0;
	for ( int i = 0; i < parameterOrder1.length; i++ ) {
		parameterOrder[iparam++] = parameterOrder1[i];
	}
	for ( int i = 0; i < whereParameters.size(); i++ ) {
		parameterOrder[iparam++] = whereParameters.get(i);
	}
	for ( int i = 0; i < parameterOrder2.length; i++ ) {
		parameterOrder[iparam++] = parameterOrder2[i];
	}
	return this.toString(parameters, parameterOrder);
}

}