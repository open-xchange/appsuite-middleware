/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.report.client.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.admin.console.AbstractJMXTools;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;
import com.openexchange.report.appsuite.serialization.osgi.ReportSerializationActivator;
import com.openexchange.report.client.configuration.ReportConfiguration;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.MacDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.report.client.transport.TransportHandler;
import com.openexchange.tools.CSVWriter;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

/**
 * {@link ReportClientBase}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.0
 */
public class ReportClientBase extends AbstractJMXTools {

    protected static final String CSV_NOT_SUPPORTED_MSG = "CSV support for appsuite report style not available. Please execute again with additional parameter '-o'.";

    protected static final String ADVANCED_NOT_SUPPORTED_MSG = "Advanced output for appsuite report style not available. Please execute again with additional parameter '-o'.";

    protected static final String TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND = "Too many arguments. Using the default (display and send)";

    protected static final String NO_OPTION_SELECTED_USING_THE_DEFAULT_DISPLAY_AND_SEND = "No option selected. Using the default (display and send)";

    protected static final String NO_REPORT_FOUND_MSG = "No report found. Please generate a report first by using parameter -e or -x or having a look into the CLT help (-h)!";

    protected static final String NO_START_DATE_SET = "You can not run a report with an end date and no start date set!";

    private static final char OPT_SEND_ONLY_SHORT = 's';

    private static final String OPT_SEND_ONLY_LONG = "sendonly";

    private static final char OPT_DISPLAY_ONLY_SHORT = 'd';

    private static final String OPT_DISPLAY_ONLY_LONG = "displayonly";

    private static final char OPT_CSV_SHORT = 'c';

    private static final String OPT_CSV_LONG = "csv";

    private static final String OPT_ADVANCEDREPORT_LONG = "advancedreport";

    private static final char OPT_ADVANCEDREPORT_SHORT = 'a';

    private static final String OPT_SAVEREPORT_LONG = "savereport";

    private static final char OPT_SAVEREPORT_SHORT = 'f';

    private static final String OPT_SHOWCOMBINATION_LONG = "showaccesscombination";

    private static final char OPT_SHOWCOMBINATION_SHORT = 'b';

    // AppSuite options

    private static final char OPT_APPSUITE_RUN_REPORT_SHORT = 'e';

    private static final String OPT_APPSUITE_RUN_REPORT_LONG = "run-appsuite-report";

    private static final String OPT_APPSUITE_INSPECT_REPORTS_LONG = "inspect-appsuite-reports";

    private static final String OPT_APPSUITE_CANCEL_REPORTS_LONG = "cancel-appsuite-reports";

    private static final char OPT_APPSUITE_GET_REPORT_SHORT = 'g';

    private static final String OPT_APPSUITE_GET_REPORT_LONG = "get-appsuite-report";

    private static final char OPT_APPSUITE_REPORT_TYPE_SHORT = 't';

    private static final String OPT_APPSUITE_REPORT_TYPE_LONG = "report-type";

    private static final char OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT = 'x';

    private static final String OPT_APPSUITE_RUN_AND_DELIVER_REPORT_LONG = "run-and-deliver-report";

    private static final char OPT_RUN_AND_DELIVER_OLD_REPORT_SHORT = 'o';

    private static final String OPT_RUN_AND_DELIVER_OLD_REPORT_LONG = "run-and-deliver-old-report";

    private static final char OPT_APPSUITE_SET_TIMEFRAME_START_SHORT = 'S';

    private static final String OPT_APPSUITE_SET_TIMEFRAME_START_LONG = "timeframe-start";

    private static final char OPT_APPSUITE_SET_TIMEFRAME_END_SHORT = 'E';

    private static final String OPT_APPSUITE_SET_TIMEFRAME_END_LONG = "timeframe-end";

    private static final char OPT_OXAAS_SET_SINGLE_BRAND_SHORT = 'R';

    private static final String OPT_OXAAS_SET_SINGLE_BRAND_LONG = "single-tenant";

    private static final char OPT_OXAAS_SET_IGNORE_ADMINS_SHORT = 'A';

    private static final String OPT_OXAAS_SET_IGNORE_ADMINS_LONG = "ignore-admins";

    private static final char OPT_OXAAS_SET_DRIVE_METRICS_SHORT = 'D';

    private static final String OPT_OXAAS_SET_DRIVE_METRICS_LONG = "drive-metrics";

    private static final char OPT_OXAAS_SET_MAIL_METRICS_SHORT = 'M';

    private static final String OPT_OXAAS_SET_MAIL_METRICS_LONG = "mail-metrics";

    private CLIOption displayonly = null;

    private CLIOption sendonly = null;

    private CLIOption csv = null;

    private CLIOption advancedreport = null;

    private CLIOption savereport = null;

    // Appsuite Options

    private CLIOption showcombi = null;

    private CLIOption runAsReport = null;

    private CLIOption inspectAsReports = null;

    private CLIOption cancelAsReports = null;

    private CLIOption getAsReport = null;

    private CLIOption timeframeStart = null;

    private CLIOption timeframeEnd = null;

    // After changing to appsuite report as default -x does nothing. To be backward compatible -x will still be accepted.
    private CLIOption runAndDeliverAsReport = null;

    private CLIOption runAndDeliverOldReport = null;

    private CLIOption asReportType = null;

    // OXAAS options
    private CLIOption singleTenant = null;

    private CLIOption ignoreAdmins = null;

    private CLIOption driveMetrics = null;

    private CLIOption mailMetrics = null;

    public enum ReportMode {
        SENDONLY, DISPLAYONLY, SAVEONLY, MULTIPLE, DISPLAYANDSEND, NONE
    };

    @Override
    protected void furtherOptionsHandling(final AdminParser parser, final Map<String, String[]> env) {
        try {
            final String combi = (String) parser.getOptionValue(this.showcombi);
            if (null != combi) {
                displayCombinationAndExit(combi);
                return;
            }

            //determine the report mode
            ReportMode mode = ReportMode.NONE;
            boolean savereport = false;

            if (null != parser.getOptionValue(this.savereport)) {
                mode = ReportMode.SAVEONLY;
                savereport = true;
            }

            if (null != parser.getOptionValue(this.sendonly)) {
                if (ReportMode.NONE != mode) {
                    mode = ReportMode.MULTIPLE;
                } else {
                    mode = ReportMode.SENDONLY;
                    parser.parse(new String[] { "-x" });
                }
            }

            if (null != parser.getOptionValue(this.displayonly)) {
                if (ReportMode.SENDONLY == mode) {
                    mode = ReportMode.DISPLAYANDSEND;
                } else if (ReportMode.NONE != mode) {
                    mode = ReportMode.MULTIPLE;
                } else {
                    mode = ReportMode.DISPLAYONLY;
                }
            }

            // Does this report have a custom timeframe, if not, start- and end-date will be the same
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            boolean isCustomTimeframe = false;
            Date timeframeStart = new Date();
            Date timeframeEnd = new Date();
            if (parser.getOptionValue(this.timeframeStart) != null && mode != ReportMode.MULTIPLE) {
                timeframeStart = formatter.parse((String) parser.getOptionValue(this.timeframeStart));
                isCustomTimeframe = true;
                if (parser.getOptionValue(this.timeframeEnd) != null) {
                    timeframeEnd = formatter.parse((String) parser.getOptionValue(this.timeframeEnd));
                }
            }

            // If report-type is oxaas-extended, look for other report specific options
            String reportType = (String) parser.getOptionValue(this.asReportType);
            if (reportType == null) {
                reportType = "default";
            }
            boolean isSingleTenant = false;
            Long singeTenantId = 0l;
            boolean isIgnoreAdmin = false;
            boolean isShowDriveMetrics = false;
            boolean isShowMailMetrics = false;
            if (reportType.equals("oxaas-extended")) {
                if (parser.getOptionValue(singleTenant) != null) {
                    isSingleTenant = true;
                    singeTenantId = Long.parseLong(((String) parser.getOptionValue(singleTenant)));
                }
                if (parser.getOptionValue(ignoreAdmins) != null) {
                    isIgnoreAdmin = true;
                }
                if (parser.getOptionValue(driveMetrics) != null) {
                    isShowDriveMetrics = true;
                }
                if (parser.getOptionValue(mailMetrics) != null) {
                    isShowMailMetrics = true;
                }
            }
            
            ReportConfiguration reportConfiguration = new ReportConfiguration();
            String storagePath = reportConfiguration.getReportStorage().trim();
            int chunkSize = Integer.parseInt(reportConfiguration.getMaxChunkSize().trim());
            
            ReportConfigs reportConfigs = new ReportConfigs(reportType, false, isCustomTimeframe, timeframeStart.getTime(), timeframeEnd.getTime(), isSingleTenant, singeTenantId, isIgnoreAdmin, isShowDriveMetrics, isShowMailMetrics, storagePath, chunkSize);

            //Start the report generation
            System.out.println("Starting the Open-Xchange report client. Note that the report generation may take a little while.");
            final MBeanServerConnection initConnection = initConnection(env);

            // Run the new report style
            if (null == parser.getOptionValue(this.runAndDeliverOldReport)) {
                // the new report style does not support csv or advanced report option
                if (null != parser.getOptionValue(this.csv)) {
                    System.out.println(CSV_NOT_SUPPORTED_MSG);
                }
                if (null != parser.getOptionValue(this.advancedreport)) {
                    System.out.println(ADVANCED_NOT_SUPPORTED_MSG);
                }
                if (null != parser.getOptionValue(this.runAsReport)) {
                    runASReport(reportType, initConnection, reportConfigs);
                    inspectASReports(reportType, initConnection);
                    return;
                } else if (null != parser.getOptionValue(this.inspectAsReports)) {
                    inspectASReports(reportType, initConnection);
                    return;
                } else if (null != parser.getOptionValue(this.cancelAsReports)) {
                    cancelASReports(reportType, initConnection);
                    return;
                } else if (null != parser.getOptionValue(this.getAsReport)) {
                    getASReport(reportType, mode, savereport, initConnection);
                    return;
                } else {
                    // run and deliver AS report is no default
                    runAndDeliverASReport(mode, false, savereport, initConnection, reportConfigs);
                    return;
                }
            }

            // ... otherwise old report style

            final List<Total> totals = ObjectHandler.getTotalObjects(initConnection);
            List<ContextDetail> contextDetails = null;
            if (null != parser.getOptionValue(this.advancedreport)) {
                contextDetails = ObjectHandler.getDetailObjects(initConnection);
            }
            List<MacDetail> macDetails = ObjectHandler.getMacObjects(initConnection);
            final String[] versions = VersionHandler.getServerVersion();
            Map<String, String> serverConfiguration = ObjectHandler.getServerConfiguration(initConnection);
            final ClientLoginCount clc = ObjectHandler.getClientLoginCount(initConnection);
            final ClientLoginCount clcYear = ObjectHandler.getClientLoginCount(initConnection, true);

            switch (mode) {
                case SENDONLY:
                case SAVEONLY:
                    createTransportHandler().sendReport(totals, macDetails, contextDetails, serverConfiguration, versions, clc, clcYear, savereport);
                    break;

                case DISPLAYONLY:
                    print(totals, contextDetails, macDetails, serverConfiguration, versions, parser, clc, clcYear);
                    break;

                case NONE:
                    System.out.println(NO_OPTION_SELECTED_USING_THE_DEFAULT_DISPLAY_AND_SEND);
                case MULTIPLE:
                    if (ReportMode.NONE != mode) {
                        System.out.println(TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
                    }
                case DISPLAYANDSEND:
                default:
                    savereport = false;
                    createTransportHandler().sendReport(totals, macDetails, contextDetails, serverConfiguration, versions, clc, clcYear, savereport);
                    print(totals, contextDetails, macDetails, serverConfiguration, versions, parser, clc, clcYear);
                    break;
            }
        } catch (final MalformedObjectNameException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final NullPointerException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final JSONException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final InvalidAttributeValueException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final Exception e) {
            printServerException(e, parser);
            sysexit(1);
        }
    }

    protected TransportHandler createTransportHandler() {
        return new TransportHandler();
    }

    @Override
    public void setFurtherOptions(final AdminParser parser) {
        this.sendonly = setShortLongOpt(parser, OPT_SEND_ONLY_SHORT, OPT_SEND_ONLY_LONG, "Send report without displaying it (Disables default)", false, NeededQuadState.notneeded);

        this.displayonly = setShortLongOpt(parser, OPT_DISPLAY_ONLY_SHORT, OPT_DISPLAY_ONLY_LONG, "Display report without sending it (Disables default)", false, NeededQuadState.notneeded);

        this.csv = setShortLongOpt(parser, OPT_CSV_SHORT, OPT_CSV_LONG, "Show output as CSV", false, NeededQuadState.notneeded);

        this.advancedreport = setShortLongOpt(parser, OPT_ADVANCEDREPORT_SHORT, OPT_ADVANCEDREPORT_LONG, "Run an advanced report (could take some time with a lot of contexts)", false, NeededQuadState.notneeded);

        this.savereport = setShortLongOpt(parser, OPT_SAVEREPORT_SHORT, OPT_SAVEREPORT_LONG, "Save the report as JSON String instead of sending it", false, NeededQuadState.notneeded);

        this.showcombi = setShortLongOpt(parser, OPT_SHOWCOMBINATION_SHORT, OPT_SHOWCOMBINATION_LONG, "Show access combination for bitmask", true, NeededQuadState.notneeded);

        this.runAsReport = setShortLongOpt(parser, OPT_APPSUITE_RUN_REPORT_SHORT, OPT_APPSUITE_RUN_REPORT_LONG, "Schedule an appsuite style report. Will print out the reports UUID or, if a report is being generated, the UUID of the pending report", false, NeededQuadState.notneeded);

        this.asReportType = setShortLongOpt(parser, OPT_APPSUITE_REPORT_TYPE_SHORT, OPT_APPSUITE_REPORT_TYPE_LONG, "The type of the report to run. Leave this off for the 'default' report. 'Known reports next to 'default': 'extended', 'oscs-extended' Enables additional options, as listed below (provisioning-bundels needed)", true, NeededQuadState.notneeded);

        this.inspectAsReports = setLongOpt(parser, OPT_APPSUITE_INSPECT_REPORTS_LONG, "Prints information about currently running reports", false, false);

        this.cancelAsReports = setLongOpt(parser, OPT_APPSUITE_CANCEL_REPORTS_LONG, "Cancels pending reports", false, false);

        this.getAsReport = setShortLongOpt(parser, OPT_APPSUITE_GET_REPORT_SHORT, OPT_APPSUITE_GET_REPORT_LONG, "Retrieve the report that was generated, can (and should) be combined with the options for sending, displaying or saving the report", false, NeededQuadState.notneeded);

        this.runAndDeliverAsReport = setShortLongOpt(parser, OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT, OPT_APPSUITE_RUN_AND_DELIVER_REPORT_LONG, "Create a new report and send it immediately. Note: This command will run until the report is finished, and that could take a while. Can (and should) be combined with the options for sending, displaying or saving the report ", false, NeededQuadState.notneeded);

        this.runAndDeliverOldReport = setShortLongOpt(parser, OPT_RUN_AND_DELIVER_OLD_REPORT_SHORT, OPT_RUN_AND_DELIVER_OLD_REPORT_LONG, "Run old report type. Used to have a backward compatibility.", false, NeededQuadState.notneeded);

        this.timeframeStart = setShortLongOpt(parser, OPT_APPSUITE_SET_TIMEFRAME_START_SHORT, OPT_APPSUITE_SET_TIMEFRAME_START_LONG, "Set the starting date of the timeframe in format: dd.mm.yyyy", true, NeededQuadState.notneeded);

        this.timeframeEnd = setShortLongOpt(parser, OPT_APPSUITE_SET_TIMEFRAME_END_SHORT, OPT_APPSUITE_SET_TIMEFRAME_END_LONG, "Set the ending date of the timeframe in format: dd.mm.yyyy. If start date is set and this parameter not, the current Date is taken as timeframe end.", true, NeededQuadState.notneeded);

        this.singleTenant = setShortLongOpt(parser, OPT_OXAAS_SET_SINGLE_BRAND_SHORT, OPT_OXAAS_SET_SINGLE_BRAND_LONG, "OXAAS only: Run the report for a single brand, identified by the sid of the brands admin. oxaas-extended report-type only", true, NeededQuadState.notneeded);

        this.ignoreAdmins = setShortLongOpt(parser, OPT_OXAAS_SET_IGNORE_ADMINS_SHORT, OPT_OXAAS_SET_IGNORE_ADMINS_LONG, "OXAAS only: Ignore admins and dont show users of that category. oxaas-extended report-type only", false, NeededQuadState.notneeded);

        this.driveMetrics = setShortLongOpt(parser, OPT_OXAAS_SET_DRIVE_METRICS_SHORT, OPT_OXAAS_SET_DRIVE_METRICS_LONG, "OXAAS only: Get drive metrics for each user. oxaas-extended report-type only", false, NeededQuadState.notneeded);

        this.mailMetrics = setShortLongOpt(parser, OPT_OXAAS_SET_MAIL_METRICS_SHORT, OPT_OXAAS_SET_MAIL_METRICS_LONG, "OXAAS only: Get mail metrics for each user. oxaas-extended report-type only", false, NeededQuadState.notneeded);
    }

    protected void print(final List<Total> totals, final List<ContextDetail> contextDetails, final List<MacDetail> macDetails, Map<String, String> serverConfiguration, final String[] versions, final AdminParser parser, final ClientLoginCount clc, final ClientLoginCount clcYear) {
        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createVersionList(versions)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createVersionList(versions)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createTotalList(totals)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createTotalList(totals)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createMacList(macDetails)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createMacList(macDetails)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createConfigurationList(serverConfiguration)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createConfigurationList(serverConfiguration)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createLogincountList(clc)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createLogincountList(clc)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createLogincountListYear(clcYear)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createLogincountListYear(clcYear)).write();
        }

        System.out.println("");

        if (null != contextDetails) {
            System.out.println("");

            if (null != parser.getOptionValue(this.csv)) {
                new CSVWriter(System.out, ObjectHandler.createDetailList(contextDetails)).write();
            } else {
                new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createDetailList(contextDetails)).write();
            }
        }
    }

    /*
     * FIXME: This should be read from the admin api somewhere later
     */
    private static final Map<String, String> ADMIN_COMBI_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 8762945373815023607L;

        {
            put("CALENDAR", "access-calendar");
            put("CONTACTS", "access-contacts");
            put("DELEGATE_TASKS", "access-delegate-tasks");
            put("EDIT_PUBLIC_FOLDERS", "access-edit-public-folder");
            put("ICAL", "access-ical");
            put("INFOSTORE", "access-infostore");
            put("READ_CREATE_SHARED_FOLDERS", "access-read-create-shared-Folders");
            put("SYNCML", "access-syncml");
            put("MOBILITY", "access-syncml");
            put("TASKS", "access-tasks");
            put("VCARD", "access-vcard");
            put("WEBDAV", "access-webdav");
            put("WEBDAV_XML", "access-webdav-xml");
            put("WEBMAIL", "access-webmail");
            put("EDIT_GROUP", "access-edit-group");
            put("EDIT_RESOURCE", "access-edit-resource");
            put("EDIT_PASSWORD", "access-edit-password");
            put("COLLECT_EMAIL_ADDRESSES", "access-collect-email-addresses");
            put("MULTIPLE_MAIL_ACCOUNTS", "access-multiple-mail-accounts");
            put("SUBSCRIPTION", "access-subscription");
            put("PUBLICATION", "access-publication");
            put("ACTIVE_SYNC", "access-active-sync");
            put("USM", "access-usm");
            put("OLOX20", "access-olox20");
            put("DENIED_PORTAL", "access-denied-portal");
        }
    });

    private void displayCombinationAndExit(final String combi) {
        int accCombi;
        try {
            accCombi = Integer.parseInt(combi);
            final Class<UserConfiguration> clazz = UserConfiguration.class;
            for (final Field f : clazz.getFields()) {
                if (f.getType().getName().equals("int")) {
                    String shortName = ADMIN_COMBI_MAP.get(f.getName());
                    if (null != shortName) {
                        final int bit = f.getInt(clazz);
                        if ((bit & accCombi) == bit) {
                            System.out.printf("%c[32m%35s: on%c[0m\n", 27, shortName, 27);
                        } else {
                            System.out.printf("%c[31m%35s: off%c[0m\n", 27, shortName, 27);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println(combi + " is not a number");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return;
    }

    protected ObjectName getAppSuiteReportingName() {
        try {
            return new ObjectName("com.openexchange.reporting.appsuite", "name", "AppSuiteReporting");
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * Retrieve a last report of the given type and determine further actions depending on the given {@link ReportMode}
     * what should be done with the report.
     * 
     * @param reportType, the report type to retrieve
     * @param mode, the report mode
     * @param savereport, save this report
     * @param server, server connection used to invoke report getting methods
     */
    protected void getASReport(Object reportType, ReportMode mode, boolean savereport, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData report = (CompositeData) server.invoke(getAppSuiteReportingName(), "retrieveLastReport", new Object[] { reportType }, new String[] { String.class.getCanonicalName() });

            if (report == null) {
                CompositeData errorReport = (CompositeData) server.invoke(getAppSuiteReportingName(), "retrieveLastErrorReport", new Object[] { reportType }, new String[] { String.class.getCanonicalName() });
                System.out.println();
                if (errorReport == null) { // no successful and no error report available
                    System.out.println(NO_REPORT_FOUND_MSG);
                } else {
                    String uuid = (String) errorReport.get("uuid");
                    String type = (String) errorReport.get("type");
                    System.out.println("The following errors have been found for the report with uuid " + uuid + " for type " + type);

                    String error = (new JSONObject((String) errorReport.get("data")).get("error")).toString();
                    JSONObject errors = new JSONObject(error.trim());

                    Iterator<?> keys = errors.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        Object object = (String) errors.get(key);
                        if (object instanceof String) {
                            System.out.println(errors.get(key));
                        }
                    }
                }
                return;
            }
            System.out.println("");
            switch (mode) {
                case SENDONLY:
                case SAVEONLY:
                    createTransportHandler().sendASReport(report, savereport);
                    break;

                case DISPLAYONLY:
                    printASReport(report);
                    break;

                case NONE:
                    System.out.println(NO_OPTION_SELECTED_USING_THE_DEFAULT_DISPLAY_AND_SEND);
                    //$FALL-THROUGH$
                case MULTIPLE:
                    if (ReportMode.NONE != mode) {
                        System.out.println(TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
                    }
                    //$FALL-THROUGH$
                case DISPLAYANDSEND:
                default:
                    savereport = false;
                    // send via TransportHandler 
                    createTransportHandler().sendASReport(report, savereport);
                    // print to console
                    printASReport(report);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Generate a report with the given parameters and wait until its finished.
     * 
     * @param reportType, the report type
     * @param mode, the mode of report generation
     * @param silent, should the current status of the report be displayed on the console
     * @param savereport, save the report to disc or not
     * @param server, the server connection
     * @param isCustomTimerange, does this report have a custom timerange
     * @param start, starting date of the report
     * @param end, end date of the report
     * @param isShowSingleTenant, should a report be generated for only one tenant
     * @param singleTenantId, the id of the tenants admin
     * @param isIgnoreAdmin, should admin users be ignored
     * @param isShowDriveMetrics, calculate drive metrics
     * @param isShowMailMetrics, calculate mail metrics
     */
    private void runAndDeliverASReport(ReportMode mode, boolean silent, boolean savereport, MBeanServerConnection server, ReportConfigs reportConfig) {

        try {
            String uuid = "";
            //            if (reportType.equals("oxaas-extended")) {
            //                uuid = (String) server.invoke(getAppSuiteReportingName(), "run", new Object[] { reportType, start, end, isCustomTimerange, isShowSingleTenant, singleTenantId, isIgnoreAdmin, isShowDriveMetrics, isShowMailMetrics }, new String[] { String.class.getCanonicalName(), Date.class.getCanonicalName(), Date.class.getCanonicalName(), Boolean.class.getCanonicalName(), Boolean.class.getCanonicalName(), Long.class.getCanonicalName(), Boolean.class.getCanonicalName(), Boolean.class.getCanonicalName(), Boolean.class.getCanonicalName() });
            //                if (uuid == null && isShowSingleTenant) {
            //                    System.out.println("No contexts for this brand or the sid is invalid. Report generation aborted.");
            //                    return;
            //                }
            //            } else {
            //                if (isCustomTimerange) {
            //                    uuid = (String) server.invoke(getAppSuiteReportingName(), "run", new Object[] { reportType, start, end }, new String[] { String.class.getCanonicalName(), Date.class.getCanonicalName(), Date.class.getCanonicalName() });
            //                } else {
            //                    
            //                }
            //            }
            uuid = (String) server.invoke(getAppSuiteReportingName(), "run", new Object[] { reportConfig }, new String[] { CompositeData.class.getCanonicalName() });

            // Start polling
            boolean done = false;
            int charNum = 0;

            while (!done) {
                CompositeData[] reports = (CompositeData[]) server.invoke(getAppSuiteReportingName(), "retrievePendingReports", new Object[] { reportConfig.getType() }, new String[] { String.class.getCanonicalName() });
                //                if ((reports == null) || (reports.length == 0)) {
                //                    Object o = server.invoke(getAppSuiteReportingName(), "retrieveLastErrorReport", new Object[] { reportType }, new String[] { String.class.getCanonicalName() });
                //                }

                boolean found = false;
                for (CompositeData report : reports) {
                    if (report.get("uuid").equals(uuid)) {
                        found = true;
                        if (!silent) {
                            eraseStatusLine(charNum);
                            charNum = printStatusLine(report);
                        }
                    }
                }

                done = !found;

                // wait until the report is done
                if (!done) {
                    Thread.sleep(silent ? 60000 : 10000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // retrieve the latest report of this type and do what the mode tells to do
        getASReport(reportConfig.getType(), mode, savereport, server);
    }

    /**
     * Print the status of the current report:
     * 
     * Example:
     * Time | uuid | context/totalContexts | percent | how long the report has been running
     * 13:58:43, 058b5dafb1ec4911917c24290b04b97b: 1/1 (100,00 %) ETA: 0 milliseconds
     * 
     * @param report
     * @return
     */
    private int printStatusLine(CompositeData report) {
        Long start = (Long) report.get("startTime");
        Long now = System.currentTimeMillis();

        long elapsedTime = now - start;

        Integer totalContexts = (Integer) report.get("tasks");
        Integer pendingContexts = (Integer) report.get("pendingTasks");

        int finishedContexts = totalContexts - pendingContexts;

        long timePerContext = -1;
        if (finishedContexts > 0) {
            timePerContext = elapsedTime / finishedContexts;
        }

        StringBuilder b = new StringBuilder();
        b.append(getCurrentTimeStamp() + ", ");
        b.append(report.get("uuid")).append(": ");
        b.append(String.format("%d/%d (%.2f %%) ", finishedContexts, totalContexts, ((float) finishedContexts / totalContexts) * 100f));
        if (timePerContext > 0) {
            b.append("ETA: " + prettyPrintTimeInterval(timePerContext * pendingContexts));
        }

        System.out.print(b);

        return b.length();
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private void eraseStatusLine(int charNum) {
        StringBuilder b = new StringBuilder(charNum + 1);
        b.append("\n\b");
        for (int i = 0; i < charNum; i++) {
            b.append('\b');
        }
        System.out.print(b);
    }

    /**
     * Cancel a report of the given type.
     * 
     * @param reportType, the type of the report
     * @param server, the server connection
     */
    private void cancelASReports(Object reportType, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData[] reports = (CompositeData[]) server.invoke(getAppSuiteReportingName(), "retrievePendingReports", new Object[] { reportType }, new String[] { String.class.getCanonicalName() });
            System.out.println("");
            if (reports.length == 0) {
                System.out.println("Nothing to cancel, there are no reports currently pending.");
            }
            for (CompositeData report : reports) {
                Object uuid = report.get("uuid");
                System.out.println("Cancelling " + uuid);

                server.invoke(getAppSuiteReportingName(), "flushPending", new Object[] { uuid, reportType }, new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Get the status of the current running report of the given type.
     * 
     * @param reportType, the type of the report
     * @param server, the server connection
     */
    private void inspectASReports(Object reportType, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData[] reports = (CompositeData[]) server.invoke(getAppSuiteReportingName(), "retrievePendingReports", new Object[] { reportType }, new String[] { String.class.getCanonicalName() });
            System.out.println("");
            if (reports.length == 0) {
                System.out.println("There are no reports currently pending.");
            }
            for (CompositeData report : reports) {
                printASDiagnostics(report);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Run a report with the given report type and print out the uuid only. Additionally a custom timerange can be set
     * 
     * @param reportType, the type of the report
     * @param server, the server connection
     * @param isCustomTimerange, should this report have a custom timerange
     * @param start, starting date of the timerange
     * @param end, ending time of the timerange
     */
    private void runASReport(Object reportType, MBeanServerConnection server, ReportConfigs reportConfig) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            String uuid = "";
            uuid = (String) server.invoke(getAppSuiteReportingName(), "run", new Object[] { reportConfig }, new String[] { CompositeData.class.getCanonicalName() });
            System.out.println("\nRunning report with uuid: " + uuid);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Print a given reports header with probable time left.
     * 
     * @param compositeData
     */
    private void printASDiagnostics(CompositeData compositeData) {
        Long start = (Long) compositeData.get("startTime");
        Long now = System.currentTimeMillis();

        long elapsedTime = now - start;

        Integer totalContexts = (Integer) compositeData.get("tasks");
        Integer pendingContexts = (Integer) compositeData.get("pendingTasks");

        int finishedContexts = totalContexts - pendingContexts;

        long timePerContext = -1;
        if (finishedContexts > 0) {
            timePerContext = elapsedTime / finishedContexts;

        }

        System.out.println("UUID: " + compositeData.get("uuid"));
        System.out.println("Type: " + compositeData.get("type"));
        System.out.println("Current elapsed time: " + prettyPrintTimeInterval(elapsedTime));
        System.out.println("Finished contexts: " + String.format("%d/%d (%.2f %%)", finishedContexts, totalContexts, ((float) finishedContexts / totalContexts) * 100f));
        if (timePerContext > 0) {
            System.out.println("Avg. time per context: " + prettyPrintTimeInterval(timePerContext));
            System.out.println("Projected time left: " + prettyPrintTimeInterval(timePerContext * pendingContexts));
        }
    }

    /**
     * Print the given report to console.
     * 
     * @param report
     */
    private void printASReport(CompositeData report) {
        try {
            Long start = (Long) report.get("startTime");
            Long end = (Long) report.get("stopTime");

            long elapsedTime = end.longValue() - start.longValue();
            if (elapsedTime < 0) {
                elapsedTime = -elapsedTime;
            }

            Integer totalContexts = (Integer) report.get("tasks");

            long timePerContext = elapsedTime / totalContexts.longValue();

            System.out.println("UUID: " + report.get("uuid"));
            System.out.println("Type: " + report.get("type"));
            System.out.println("Total time: " + prettyPrintTimeInterval(elapsedTime));
            System.out.println("Avg. time per context: " + prettyPrintTimeInterval(timePerContext));
            System.out.println("Report was finished: " + new Date(end));
            System.out.println("\n------ report -------");
            JSONObject data = new JSONObject((String) report.get("data"));
            //TODO QS-VS: Korrektur der Methode zum Output der Daten, Einrückungen werden komplett ignoriert
            if (data.getBoolean("needsComposition")) {
                System.out.println("{");
                for (String string : data.keySet()) {
                    if (data.get(string) instanceof String) {
                        System.out.println("  \"" + string + "\" : \"" + data.get(string) + "\",");
                    } else if(data.get(string) instanceof Boolean) {
                        System.out.println("  \"" + string + "\" : " + data.get(string) + ",");
                        // TODO QS-VS: Besser aus dem Report selbst herauslesen welcher Parameter das Lesen von Platte triggert
                    } else if (string.equals("macdetail") || string.equals("oxaas")){
                       try {
                        Report.printStoredReportContentToConsole((String) report.get("storageFolderPath"), (String) report.get("uuid"));
                       } catch (IOException e) {
                        e.printStackTrace();
                       } 
                    } else {
                        JSONObject obj = (JSONObject) data.get(string);
                        System.out.println("  " + string + " : " + obj.toString(2, 1) + ",");
                    }
                }
                System.out.println("}");
            } else {
                System.out.println(data.toString(20));
            }
            System.out.println("------ end -------\n");
        } catch (JSONException e) {
            System.out.println("Illegal data sent from server!");
            e.printStackTrace();
        }
    }
    
    /**
     * Fetches the stored report in the storage folder defined by the given path and print the whole
     * report to console. Very memory friendly because of utilizing {@link Scanner} operations.
     * 
     * The whole report-file is identified by the ".report" ending.
     * 
     * @param reportFolderPath, the path to the folder where the report-file is stored
     * @throws IOException
     */
    

    /**
     * Convert milliseconds to a pretty time output String.
     * 
     * Example:
     * 0 hours, 28 minutes, 3 seconds
     * 
     * @param interval, the time in milliseconds
     *            @return, a pretty time String
     */
    private String prettyPrintTimeInterval(long interval) {
        // FROM: http://stackoverflow.com/questions/635935/how-can-i-calculate-a-time-span-in-java-and-format-the-output

        if (interval < 1000) {
            return interval + " milliseconds";
        }
        long diffInSeconds = (interval / 1000) * (110 / 100);

        long diff[] = new long[] { 0, 0, 0 };
        /* sec */diff[2] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        /* min */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[0] = (diffInSeconds = diffInSeconds / 60);

        return String.format("%d hour%s, %d minute%s, %d second%s", diff[0], diff[0] > 1 || diff[0] == 0 ? "s" : "", diff[1], diff[1] > 1 || diff[1] == 0 ? "s" : "", diff[2], diff[2] > 1 || diff[2] == 0 ? "s" : "");
    }
}
