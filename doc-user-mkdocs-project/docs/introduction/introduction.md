# TSTool TimesheetsCom Plugin / Introduction #

*   [Introduction](#introduction)
*   [TSTool use with TimesheetsCom Web Services](#tstool-use-with-timesheetscom-web-services)

----------------------

## Introduction ##

TSTool is a powerful software tool that automates time series processing and product generation.
It was originally developed for the State of Colorado to process data for river basin modeling and has since
been enhanced to work with many data sources including:

*   United States Geological Survey (USGS) web service and file formats
*   Natural Resources Conservation Service (NRCS) web services
*   Regional Climate Center (RCC) Applied Climate Information Service (ACIS) web services
*   US Army Corps of Engineers DSS data files
*   others

TSTool is maintained by the Open Water Foundation,
which also enhances the software based on project needs.

*   See the latest [TSTool Documentation](https://opencdss.state.co.us/tstool/latest/doc-user/) to learn about core TSTool features.
*   See the [TSTool Download website](https://opencdss.state.co.us/tstool/) for the most recent software versions and documentation.
*   See the [TimesheetsCom Plugin download page](https://software.openwaterfoundation.org/tstool-timesheetscom-plugin/).

## TSTool use with TimesheetsCom Web Services ##

The [`timesheets.com`](https://timesheets.com) cloud-hosted timesheet system manages timesheet data.

See the following resources:

*   [`timesheets.com` Getting Started with the API](https://support2.timesheets.com/knowledge-base/getting-started/)
*   [`timesheets.com` Web Service API Documentation](https://secure05v.timesheets.com/api/public/v1/index.cfm?docs)

The [TimesheetsCom datastore documentation](../datastore-ref/TimesheetsCom/TimesheetsCom.md) describes how TSTool integrates with the API.

The [`ReadTimesheetsCom`](../command-ref/ReadTimesheetsCom/ReadTimesheetsCom.md) command can be used to read time series,
in addition to time series identifiers that are generated from the main TSTool interface.
