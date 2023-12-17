# TSTool / TimesheetsCom Data Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.
See the [TSTool release notes](http://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/).

Plugin release notes are listed below.
The repository issue for release note item is shown where applicable.

*   [Changes in Version 1.0.1](#changes-in-version-101)
*   [Changes in Version 1.0.0](#changes-in-version-100)

----------

## Changes in Version 1.0.1 ##

**Maintenance release to improve performance.**

*   ![change](change.png) [#1] Update to automatically refresh cached data after an hour
    to ensure that current data are available.

## Changes in Version 1.0.0 ##

**Feature release - initial production release.**

*   ![new](new.png) [1.0.0] Initial production release:
    +   Main TSTool window includes browsing features to list TimesheetsCom time series.
    +   [TSID for TimesheetsCom](../command-ref/TSID/TSID.md) are recognized to read time series with default parameters.
    +   The [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command is provided to automate
        reading 1+ time series.
    +   Documentation is available for the [TimesheetsCom datastore](../datastore-ref/TimesheetsCom/TimesheetsCom.md).
