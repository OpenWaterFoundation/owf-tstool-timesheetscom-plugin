# TSTool / Troubleshooting #

Troubleshooting TSTool for TimesheetsCom involves confirming that the core product and plugin are performing as expected.
Issues may also be related to `timesheets.com` data.

*   [Troubleshooting Core TSTool Product](#troubleshooting-core-tstool-product)
*   [Troubleshooting TimesheetsCom TSTool Integration](#troubleshooting-timesheetscom-tstool-integration)
    +   [***Commands(Plugin)*** Menu Contains Duplicate Commands](#commandsplugin-menu-contains-duplicate-commands)
    +   [Web Service Datastore Returns no Data](#web-service-datastore-returns-no-data)

------------------

## Troubleshooting Core TSTool Product ##

See the main [TSTool Troubleshooting documentation](https://opencdss.state.co.us/tstool/latest/doc-user/troubleshooting/troubleshooting/).

## Troubleshooting TimesheetsCom TSTool Integration ##

The following are typical issues that are encountered when using TSTool with the TimesheetsCom plugin.
The ***View / Datastores*** menu item will display the status of datastores.
The ***Tools / Diagnostics - View Log File...*** menu item will display the log file.

### ***Commands(Plugin)*** Menu Contains Duplicate Commands ###

If the ***Commands(Plugin)*** menu contains duplicate commands,
TSTool is finding multiple plugin `jar` files.
To fix, check the `plugins` folder and subfolders for the software installation folder
and the user's `.tstool/NN/plugins` folder.
Remove extra jar files, leaving only the version that is desired (typically the most recent version).

### Web Service Datastore Returns no Data ###

If the web service datastore returns no data, check the following:

1.  Review the TSTool log file for errors.
    Typically a message will indicate an HTTP error code for the URL that was requested.
2.  Copy and paste the URL into a web browser to confirm the error.
    The browser will typically show a specific web service error message such as a
    missing query parameter or typo.
3.  See the [`timesheets.com` API documentation](https://secure05v.timesheets.com/api/public/v1/index.cfm?docs)
    to check whether the URL is correct.
4.  Contact `timesheets.com` support for the data publisher to determine whether data limits are in place.

If the issue cannot be resolved, contact the [Open Water Foundation](https://openwaterfoundation.org/about-owf/staff/).
