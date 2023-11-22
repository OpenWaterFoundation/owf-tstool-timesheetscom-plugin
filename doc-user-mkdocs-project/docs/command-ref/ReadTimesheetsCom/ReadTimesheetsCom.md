# TSTool / Command / ReadTimesheetsCom #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
    +   [Match Single Time Series](#match-single-time-series)
    +   [Match 1+ Time Series](#match-1-time-series)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
    +   [Example Daily Time Series](#example-daily-time-series)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The `ReadTimesheetsCom` command reads one or more time series from `timesheets.com` web services:

*   Read a single time series by matching a TSTool time series identifier (TSID).
*   Read 1+ time series using filters similar to the main TSTool window.

See the [TimesheetsCom Data Web Services Appendix](../../datastore-ref/TimesheetsCom/TimesheetsCom.md)
for more information about `timesheets.com` web service integration and limitations.
The command is designed to utilize web service query criteria to process large numbers of time series,
for example to produce real-time information products and perform historical data analysis and quality control.

See also the 
[TSID for TimesheetsCom](../TSID/TSID.md) time series identifier command,
which reads time series for a single time series.

The ***Data type***, ***Data interval***, and ***Where*** command parameters and input fields
are similar to those in the main TSTool interface.
However, whereas the main TSTool interface first requires a query to find the
matching time series list and interactive select to copy specific time series identifiers into the ***Commands*** area,
the `ReadTimesheetsCom` command automates reading the time series list and the corresponding data for the time series.
Using the `ReadTimesheetsCom` command can greatly shorten command files and simplify command logic
when processing many time series.
However, because the command can process many time series and web services are impacted by network speed,
running the command can take a while to complete for large datasets.

Data for the location and other time series metadata,
as shown in the main TSTool interface, are set as time series properties, using web service data values.
Right-click on a time series in the TSTool ***Results*** area and then use the
***Time Series Properties*** menu to view time series properties.
These properties can be transferred to a table with the
[`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/)
command and processed further with other table commands.

All time series use `Day` interval, with values being the number of hours worked in a day for a project.

## Command Editor ##

The following dialog is used to edit the command and illustrates the syntax for the command.
Two options are available for matching time series.

### Match Single Time Series ###

The following example illustrates how to read a single time series by specifying the data type and interval (top)
and customer, project, and user name (***Match Single Time Series*** tab).
This approach is similar to using the general
[`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/)
command but offers parameters specific to `timesheets.com` web services.

**<p style="text-align: center;">
![Read single TimesheetsCom time series](ReadTimesheetsCom-single.png)
</p>**

**<p style="text-align: center;">
`ReadTimesheetsCom` Command Editor to Read a Single Time Series (<a href="../ReadTimesheetsCom-single.png">see full-size image)</a>
</p>**

### Match 1+ Time Series ###

The following figure illustrates how to query multiple time series.
For example, this can be used to process all time series of a data type in the system
or all time series for a location.

**<p style="text-align: center;">
![Read one or more TimesheetsCom time series](ReadTimesheetsCom-multiple.png)
</p>**

**<p style="text-align: center;">
`ReadTimesheetsCom` Command Editor to Read Multiple Time Series (<a href="../ReadTimesheetsCom-multiple.png">see full-size image)</a>
</p>**

## Command Syntax ##

The command syntax is as follows:

```text
ReadTimesheetsCom(Parameter="Value",...)
```

**<p style="text-align: center;">
Command Parameters
</p>**

|**Tab**|**Parameter**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|**Description**|**Default**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
|--------------|-----------------|-----------------|--|
|All|`DataStore`<br>**required**|The TimesheetsCom datastore name to use for the web services connection, as per datastore configuration files (see the [TimesheetsCom Web Services Datastore appendix](../../datastore-ref/TimesheetsCom/TimesheetsCom.md)). | None - must be specified. |
||`DataType`<br>**required**|The data type to be queried, currently always `ProjectHours`. | `*` to read all the time series. |
||`Interval`<br>**required**|The data interval for the time series, currently always `Day` | `*` - to read all the time series. |
|***Match Single Time Series***|`CustomerName`<br>**required**|The customer name to match. | None - must be specified to read a single time series. |
| |`ProjectName`<br>**required**|The project name to match. | None - must be specified to read a single time series. |
| |`UserLastName`<br>**required**|The user last name to match. | None - must be specified to read a single time series. |
| |`UserFirstName`<br>**required**|The user first name to match. | None - must be specified to read a single time series. |
||`TSID`| A view-only value that indicates the time series identifier that will result from the input parameters when a single time series is queried. | |
|***Match 1+ Time Series***|`WhereN`|When reading 1+ time series, the “where” clauses to be applied.  The filters match the values in the Where fields in the command editor dialog and the TSTool main interface.  The parameters should be named `Where1`, `Where2`, etc., with a gap resulting in the remaining items being ignored.  The format of each value is:<br>`Item;Operator;Value`<br>Where `Item` indicates a data field to be filtered on, `Operator` is the type of constraint, and `Value` is the value to be checked when querying.|If not specified, the query will not be limited and very large numbers of time series may be queried.|
|All|`Alias`<br>|The alias to assign to the time series, as a literal string or using the special formatting characters listed by the command editor.  The alias is a short identifier used by other commands to locate time series for processing, as an alternative to the time series identifier (`TSID`).|None – alias not assigned.|
||`InputStart`|Start of the period to query, specified as a date/time with a precision that matches the requested data interval.|Read all available data.|
||`InputEnd`|End of the period to query, specified as a date/time with a precision that matches the requested data interval.|Read all available data.|
||`Debug`| Used for troubleshooting:  `False` or `True`. | `False` |

## Examples ##

See the [TimesheetsCom](../../datastore-ref/TimesheetsCom/TimesheetsCom.md) datastore documentation.

## Troubleshooting ##

If listing time series or retrieving data has errors, try the following:

1.  Review the TSTool log file (see ***Tools / Diagnositics - View Log File*** for current and startup log file.)
2.  Confirm that the authorization information in the datastore configuration file is correct.
3.  Use command line `curl` to query the URL.

## See Also ##

*   [`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/) command
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command
*   [TSID for TimesheetsCom](../TSID/TSID.md) command
*   [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/) command
