# TSTool / Command / TSID for TimesheetsCom #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for TimesheetsCom causes a single time series to be read from TimesheetsCom web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [TimesheetsCom Datastore Appendix](../../datastore-ref/TimesheetsCom/TimesheetsCom.md) for information about TSID syntax.

See also the [`ReadTimesheetsCom`](../ReadTimesheetsCom/ReadTimesheetsCom.md) command,
which reads one or more time series and provides parameters for control over how data are read.

All `timesheets.com` data are treated as hourly data values stored in `Day` interval time series in TSTool.
The initial focus is on project records.
        
## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [TimesheetsCom Datastore Appendix](../../datastore-ref/TimesheetsCom/TimesheetsCom.md) for information about TSID syntax.

## Examples ##

See the [TimesheetsCom Datastore Appendix](../../datastore-ref/TimesheetsCom/TimesheetsCom.md) for information about TSID syntax.

## Troubleshooting ##

*   See the [`ReadTimesheetsCom` command troubleshooting](../ReadTimesheetsCom/ReadTimesheetsCom.md#troubleshooting) documentation.

## See Also ##

*   [`ReadTimesheetsCom`](../ReadTimesheetsCom/ReadTimesheetsCom.md) command for full control reading TimesheetsCom time series
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
