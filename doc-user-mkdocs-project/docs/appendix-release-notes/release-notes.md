# TSTool / TimesheetsCom Data Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.

*   [TSTool core product release notes](http://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/).
*   [TSTool Version Compatibility](#tstool-version-compatibility)
*   [Release Note Details](#release-note-details)

----

## TSTool Version Compatibility ##

The following table lists TSTool and plugin software version compatibility.

**<p style="text-align: center;">
TSTool and Plugin Version Compatibility
</p>**

| **Plugin Version** | **Required TSTool Version** | **Comments** |
| -- | -- | -- |
| 2.0.0 | >=  15.0.0 | TSTool and plugin updated to Java 11, new plugin manager. |
| < 2.0.0 | >= 14.6.0 | |

## Release Note Details ##

Plugin release notes are listed below.
The repository issue for release note item is shown where applicable.

*   [Version 2.0.0](#version-200)
*   [Version 1.1.7](#version-117)
*   [Version 1.1.6](#version-116)
*   [Version 1.1.5](#version-115)
*   [Version 1.1.4](#version-114)
*   [Version 1.1.3](#version-113)
*   [Version 1.1.2](#version-112)
*   [Version 1.1.1](#version-111)
*   [Version 1.1.0](#version-110)
*   [Version 1.0.1](#version-101)
*   [Version 1.0.0](#version-100)

----------

## Version 2.0.0 ##

**Major release to use Java 11.**

*   ![change](change.png) Update the plugin to use Java 11:
    +   The Java version is consistent with TSTool 15.0.0.
    *   The plugin installation now uses a version folder,
        which allows multiple versions of the plugin to be installed at the same time,
        for use with different versions of TSTool.

## Version 1.1.7 ##

**Maintenance release to improve command editor.**

*   ![change](change.png) [#18] Update the [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md)
    command:
    +   Fix the editor to show `${Property}` values in choices for customer, project, user last and first name.
        User-supplied values are shown in addition to matching timesheet data.    

## Version 1.1.6 ##

**Maintenance release to support workflows.**

*   ![change](change.png) [#17] Update the [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md)
    command:
    +    Allow the work notes table to be created without outputting time series.
    +    Add the `Project` to the work notes table.
    +    Change work notes `Person` to `User` to match Timesheets.com naming.
    +    Change work notes `Description` to `Notes`.

## Version 1.1.5 ##

**Maintenance release to support workflows.**

*   ![change](change.png) [#16] Update time series properties to include:
    +   `dataStart` and `dataEnd` to indicate the period for data
    +   `dataCount` the count of hour values > `.001`

## Version 1.1.4 ##

**Maintenance release to respond to API changes.**

*   ![change](change.png) [#15] The `/report/project/customizable` web service changed.
    These changes caused the project hours query to return zero records.  The plugin has been updated.
    +   The `ProjectRecordBillableStatus` query parameter changed to `Billable`.
    +   The `ProjectRecordStatus` query parameter changed to `RecordStatus`.

## Version 1.1.3 ##

**Maintenance release to improve handling of work notes.**

*   Update the [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command:
    +   ![change](change.png) [#14] Change so that the work notes, if output,
        are constrained to the `InputStart` and `InputEnd`.

## Version 1.1.2 ##

**Maintenance release to improve functionity for billing analysis workflows.**

*   Update the [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command:
    +   ![bug](bug.png) [#13] Fix bug where the `Where` filters were not working correctly.
    +   ![change](change.png) Add the `Alias` parameter to the command editor (previously, was accidentally omitted).
    +   ![change](change.png) Add the `OutputTimeSeries` parameter to control whether time series are output.
        Using `False` allows reading and outputting tables such as user and project list without outputting time series.
    +   ![change](change.png) Add the project default billing rate and created date to the time series catalog and set as time series properties.

## Version 1.1.1 ##

**Maintenance release to respond to API changes.**

*   ![change](change.png) [#10] The `/report/project/customizable` web service changed
    the `AllEmployees` query parameter to `AllUsers`, which caused the project hours query to return zero records.
    The plugin has been updated.

## Version 1.1.0 ##

**Feature release to improve error-handling, data caching, and data checks.**

*   ![change](change.png) [#3] Improve error handling when a web service request has an error:
    +   Errors are now shown in the TSTool user interface for commands with errors.
    +   If any global data cannot be read because the maximum wait has been reached or other issue,
        error will be shown for commands in the TSTool user interface. 
*   ![change](change.png) [#5] The query filter now provides project status,
    which allows filtering time series by whether active or archived.
    This is implemented in the main query area and the
    [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command.
*   ![change](change.png) Update the [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command:
    +   [#6] Command parameters have been added to save the global data cache,
        which is useful to allow processing the tables, archiving backups, etc.:
        -   `AccountCodeTableID`
        -   `CustomerTableID`
        -   `ProjectTableID`
        -   `ProjectTimeTableID`
        -   `UserTableID`
    *   [#7] Add the `WorkTableID` parameter to save the work notes for output time series to a table.
        Use the `ProjectTimeTableID` parameter to save work notes for all data records.
    *   [#8] The `DataFlag` parameter has been added to allow the time series data flag to be set
        to the data record's archive status (`1` = active, `0` = archived).
    *   [#9] Add the `IncludeHours` parameter to control whether archived and/or new hours are read.
*   ![bug](bug.png) [#4] Data caching now handles HTTP 420 (too many requests) returned by web services:
    +   A wait and retry has been implemented, where the wait increases to an upper limit.
        This should ensure that data are always read, although refreshing cached data may be slower at times due to the retries.
    +   All data objects read from `timesheets.com` implement the wait/retry.
    +   The datastore configuration file property `RequestDayLimit` has been added to specify the limit on days for a request,
        which can be used if the limit has been increased for the account by `timesheets.com`.
        This will increase the performance.

## Version 1.0.1 ##

**Maintenance release to improve performance.**

*   ![change](change.png) [#1] Update to automatically refresh cached data after an hour
    to ensure that current data are available.

## Version 1.0.0 ##

**Feature release - initial production release.**

*   ![new](new.png) [1.0.0] Initial production release:
    +   Main TSTool window includes browsing features to list TimesheetsCom time series.
    +   [TSID for TimesheetsCom](../command-ref/TSID/TSID.md) are recognized to read time series with default parameters.
    +   The [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command is provided to automate
        reading 1+ time series.
    +   Documentation is available for the [TimesheetsCom datastore](../datastore-ref/TimesheetsCom/TimesheetsCom.md).
