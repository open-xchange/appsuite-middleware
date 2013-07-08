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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.admin.console.AbstractJMXTools;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.MacDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.report.client.transport.TransportHandler;
import com.openexchange.tools.CSVWriter;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

public class ReportClient extends AbstractJMXTools {

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
    
    private static final String OPT_APPSUITE_SILENT_LONG = "silent";
    
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

    private CLIOption runAndDeliverAsReport = null;

    private CLIOption asReportType = null;

    private CLIOption asSilent = null;
    
    public enum ReportMode {
        SENDONLY, DISPLAYONLY, SAVEONLY, MULTIPLE, DISPLAYANDSEND, NONE
    };

    public static void main(final String args[]) {
        final AbstractJMXTools t = new ReportClient();
        t.start(args, "report");
    }

    @Override
    protected void furtherOptionsHandling(final AdminParser parser, final Map<String, String[]> env) {
        try {
            final String combi = (String) parser.getOptionValue(this.showcombi);
            if (null != combi) {
                displayCombinationAndExit(combi);
            }

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

            System.out.println("Starting the Open-Xchange report client. Note that the report generation may take a little while.");
            final MBeanServerConnection initConnection = initConnection(false, env);

            // Is one of the appsuite report options set? In that case do something completely different.

            if (null != parser.getOptionValue(this.runAsReport)) {
                runASReport(parser.getOptionValue(this.asReportType), initConnection);
                inspectASReports(parser.getOptionValue(this.asReportType), initConnection);
                return;
            } else if (null != parser.getOptionValue(this.inspectAsReports)) {
                inspectASReports(parser.getOptionValue(this.asReportType), initConnection);
                return;
            } else if (null != parser.getOptionValue(this.cancelAsReports)) {
                cancelASReports(parser.getOptionValue(this.asReportType), initConnection);
                return;
            } else if (null != parser.getOptionValue(this.getAsReport)) {
                getASReport(parser.getOptionValue(this.asReportType), mode, savereport, initConnection);
                return;
            } else if (null != parser.getOptionValue(this.runAndDeliverAsReport)) {
                runAndDeliverASReport(parser.getOptionValue(this.asReportType), mode, null != parser.getOptionValue(this.asReportType), savereport, initConnection);
                return;
            }

            final List<Total> totals = ObjectHandler.getTotalObjects(initConnection);
            List<ContextDetail> contextDetails = null;
            if (null != parser.getOptionValue(this.advancedreport)) {
                contextDetails = ObjectHandler.getDetailObjects(initConnection);
            }
            List<MacDetail> macDetails = ObjectHandler.getMacObjects(initConnection);
            final String[] versions = VersionHandler.getServerVersion();
            final ClientLoginCount clc = ObjectHandler.getClientLoginCount(initConnection);
            final ClientLoginCount clcYear = ObjectHandler.getClientLoginCount(initConnection, true);

            switch (mode) {
            case SENDONLY:
            case SAVEONLY:
                new TransportHandler().sendReport(totals, macDetails, contextDetails, versions, clc, clcYear, savereport);
                break;

            case DISPLAYONLY:
                print(totals, contextDetails, macDetails, versions, parser, clc, clcYear);
                break;

            case NONE:
                System.out.println("No option selected. Using the default (display and send)");
            case MULTIPLE:
                if (ReportMode.NONE != mode) {
                    System.out.println("Too many arguments. Using the default (display and send)");
                }
            case DISPLAYANDSEND:
            default:
                savereport = false;
                new TransportHandler().sendReport(totals, macDetails, contextDetails, versions, clc, clcYear, savereport);
                print(totals, contextDetails, macDetails, versions, parser, clc, clcYear);
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

    @Override
    public void setFurtherOptions(final AdminParser parser) {
        this.sendonly = setShortLongOpt(
            parser,
            OPT_SEND_ONLY_SHORT,
            OPT_SEND_ONLY_LONG,
            "Send report without displaying it (Disables default)",
            false,
            NeededQuadState.notneeded);
        this.displayonly = setShortLongOpt(
            parser,
            OPT_DISPLAY_ONLY_SHORT,
            OPT_DISPLAY_ONLY_LONG,
            "Display report without sending it (Disables default)",
            false,
            NeededQuadState.notneeded);
        this.csv = setShortLongOpt(parser, OPT_CSV_SHORT, OPT_CSV_LONG, "Show output as CSV", false, NeededQuadState.notneeded);
        this.advancedreport = setShortLongOpt(
            parser,
            OPT_ADVANCEDREPORT_SHORT,
            OPT_ADVANCEDREPORT_LONG,
            "Run an advanced report (could take some time with a lot of contexts)",
            false,
            NeededQuadState.notneeded);
        this.savereport = setShortLongOpt(
            parser,
            OPT_SAVEREPORT_SHORT,
            OPT_SAVEREPORT_LONG,
            "Save the report as JSON String instead of sending it",
            false,
            NeededQuadState.notneeded);
        this.showcombi = setShortLongOpt(
            parser,
            OPT_SHOWCOMBINATION_SHORT,
            OPT_SHOWCOMBINATION_LONG,
            "Show access combination for bitmask",
            true,
            NeededQuadState.notneeded);

        this.runAsReport = setShortLongOpt(
            parser,
            OPT_APPSUITE_RUN_REPORT_SHORT,
            OPT_APPSUITE_RUN_REPORT_LONG,
            "Schedule an appsuite style report. Will print out the reports UUID or, if a report is being generated, the UUID of the pending report",
            false,
            NeededQuadState.notneeded);

        this.asReportType = setShortLongOpt(
            parser,
            OPT_APPSUITE_REPORT_TYPE_SHORT,
            OPT_APPSUITE_REPORT_TYPE_LONG,
            "The type of the report to run. Leave this off for the 'default' report.",
            true,
            NeededQuadState.notneeded);

        this.inspectAsReports = setLongOpt(
            parser,
            OPT_APPSUITE_INSPECT_REPORTS_LONG,
            "Prints information about currently running reports",
            false,
            false);

        this.cancelAsReports = setLongOpt(parser, OPT_APPSUITE_CANCEL_REPORTS_LONG, "Cancels pending reports", false, false);

        this.getAsReport = setShortLongOpt(
            parser,
            OPT_APPSUITE_GET_REPORT_SHORT,
            OPT_APPSUITE_GET_REPORT_LONG,
            "Retrieve the report that was generated, can (and should) be combined with the options for sending, displaying or saving the report",
            false,
            NeededQuadState.notneeded);

        this.runAndDeliverAsReport = setShortLongOpt(
            parser,
            OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT,
            OPT_APPSUITE_RUN_AND_DELIVER_REPORT_LONG,
            "Create a new report and send it immediately. Note: This command will run until the report is finished, and that could take a while. Can (and should) be combined with the options for sending, displaying or saving the report ",
            false,
            NeededQuadState.notneeded);
        
        this.asSilent = setLongOpt(parser, OPT_APPSUITE_SILENT_LONG, "Do not print out the status line. To be used with " + OPT_APPSUITE_RUN_AND_DELIVER_REPORT_LONG + ".", false, false);
    }

    private void print(final List<Total> totals, final List<ContextDetail> contextDetails, final List<MacDetail> macDetails, final String[] versions, final AdminParser parser, final ClientLoginCount clc, final ClientLoginCount clcYear) {
        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createVersionList(versions)).write();
        } else {
            new TableWriter(
                System.out,
                new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) },
                ObjectHandler.createVersionList(versions)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createTotalList(totals)).write();
        } else {
            new TableWriter(
                System.out,
                new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) },
                ObjectHandler.createTotalList(totals)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createMacList(macDetails)).write();
        } else {
            new TableWriter(
                System.out,
                new ColumnFormat[] {
                    new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) },
                ObjectHandler.createMacList(macDetails)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createLogincountList(clc)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] {
                new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT),
                new ColumnFormat(Align.LEFT) }, ObjectHandler.createLogincountList(clc)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createLogincountListYear(clcYear)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] {
                new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT),
                new ColumnFormat(Align.LEFT) }, ObjectHandler.createLogincountListYear(clcYear)).write();
        }

        if (null != contextDetails) {
            System.out.println("");

            if (null != parser.getOptionValue(this.csv)) {
                new CSVWriter(System.out, ObjectHandler.createDetailList(contextDetails)).write();
            } else {
                new TableWriter(System.out, new ColumnFormat[] {
                    new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT),
                    new ColumnFormat(Align.LEFT) }, ObjectHandler.createDetailList(contextDetails)).write();
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
            put("FORUM", "access-forum");
            put("ICAL", "access-ical");
            put("INFOSTORE", "access-infostore");
            put("PINBOARD_WRITE_ACCESS", "access-pinboard-write");
            put("PROJECTS", "access-projects");
            put("READ_CREATE_SHARED_FOLDERS", "access-read-create-shared-Folders");
            put("RSS_BOOKMARKS", "access-rss-bookmarks");
            put("RSS_PORTAL", "access-rss-portal");
            put("SYNCML", "access-syncml");
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
        System.exit(0);
    }

    private ObjectName getAppSuiteReportingName() {
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

    private void getASReport(Object reportType, ReportMode mode, boolean savereport, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData report = (CompositeData) server.invoke(
                getAppSuiteReportingName(),
                "retrieveLastReport",
                new Object[] { reportType },
                new String[] { String.class.getCanonicalName() });
            System.out.println("");
            switch (mode) {
            case SENDONLY:
            case SAVEONLY:
                new TransportHandler().sendASReport(report, savereport);
                break;

            case DISPLAYONLY:
                printASReport(report);
                break;

            case NONE:
                System.out.println("No option selected. Using the default (display and send)");
            case MULTIPLE:
                if (ReportMode.NONE != mode) {
                    System.out.println("Too many arguments. Using the default (display and send)");
                }
            case DISPLAYANDSEND:
            default:
                savereport = false;
                new TransportHandler().sendASReport(report, savereport);
                printASReport(report);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void runAndDeliverASReport(Object reportType, ReportMode mode, boolean silent, boolean savereport, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
           String uuid = (String) server.invoke(
                getAppSuiteReportingName(),
                "run",
                new Object[] { reportType },
                new String[] { String.class.getCanonicalName() });
           
           
           // Start polling
           boolean done = false;
           int charNum = 0;
           
           while (!done) {
               CompositeData[] reports = (CompositeData[]) server.invoke(
                   getAppSuiteReportingName(),
                   "retrievePendingReports",
                   new Object[] { reportType },
                   new String[] { String.class.getCanonicalName() });
               
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
               
               if (!done) {
                   Thread.sleep(silent ? 60000 : 1000);
               }
           }
           
           
           
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        getASReport(reportType, mode, savereport, server);
    }

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
        
        b.append(report.get("uuid")).append(": ");
        b.append(String.format(
            "%d/%d (%.2f %%) ",
            finishedContexts,
            totalContexts,
            ((float) finishedContexts / totalContexts) * 100f));
        if (timePerContext > 0) {
            b.append("ETA: " + prettyPrintTimeInterval(timePerContext * pendingContexts));
        }
        
        System.out.print(b);
        
        return b.length();
    }

    private void eraseStatusLine(int charNum) {
        StringBuilder b = new StringBuilder(charNum + 1);
        b.append("\n\b");
        for(int i = 0; i < charNum; i++) {
            b.append('\b');
        }
        System.out.print(b);
    }

    private void cancelASReports(Object reportType, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData[] reports = (CompositeData[]) server.invoke(
                getAppSuiteReportingName(),
                "retrievePendingReports",
                new Object[] { reportType },
                new String[] { String.class.getCanonicalName() });
            System.out.println("");
            if (reports.length == 0) {
                System.out.println("Nothing to cancel, there are no reports currently pending.");
            }
            for (int i = 0; i < reports.length; i++) {
                CompositeData report = reports[i];
                Object uuid = report.get("uuid");
                System.out.println("Cancelling " + uuid);

                server.invoke(
                    getAppSuiteReportingName(),
                    "flushPending",
                    new Object[] { uuid, reportType },
                    new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void inspectASReports(Object reportType, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            CompositeData[] reports = (CompositeData[]) server.invoke(
                getAppSuiteReportingName(),
                "retrievePendingReports",
                new Object[] { reportType },
                new String[] { String.class.getCanonicalName() });
            System.out.println("");
            if (reports.length == 0) {
                System.out.println("There are no reports currently pending.");
            }
            for (int i = 0; i < reports.length; i++) {
                printASDiagnostics(reports[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void runASReport(Object reportType, MBeanServerConnection server) {
        if (reportType == null) {
            reportType = "default";
        }
        try {
            System.out.println("\nRunning report with uuid: " + server.invoke(
                getAppSuiteReportingName(),
                "run",
                new Object[] { reportType },
                new String[] { String.class.getCanonicalName() }));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

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
        System.out.println("Finished contexts: " + String.format(
            "%d/%d (%.2f %%)",
            finishedContexts,
            totalContexts,
            ((float) finishedContexts / totalContexts) * 100f));
        if (timePerContext > 0) {
            System.out.println("Avg. time per context: " + prettyPrintTimeInterval(timePerContext));
            System.out.println("Projected time left: " + prettyPrintTimeInterval(timePerContext * pendingContexts));
        }
    }

    private void printASReport(CompositeData report) {
        try {
            Long start = (Long) report.get("startTime");
            Long end = (Long) report.get("stopTime");

            long elapsedTime = end - start;

            Integer totalContexts = (Integer) report.get("tasks");

            long timePerContext = elapsedTime / totalContexts;

            System.out.println("UUID: " + report.get("uuid"));
            System.out.println("Type: " + report.get("type"));
            System.out.println("Total time: " + prettyPrintTimeInterval(elapsedTime));
            System.out.println("Avg. time per context: " + prettyPrintTimeInterval(timePerContext));
            System.out.println("Report was finished: " + new Date(end));
            System.out.println("\n------ report -------");
            System.out.println(new JSONObject((String) report.get("data")).toString(4));
            System.out.println("------ end -------\n");
        } catch (JSONException e) {
            System.out.println("Illegal data sent from server!");
            e.printStackTrace();
        }
    }

    private String prettyPrintTimeInterval(long interval) {
        // FROM: http://stackoverflow.com/questions/635935/how-can-i-calculate-a-time-span-in-java-and-format-the-output

        if (interval < 1000) {
            return interval + " milliseconds";
        }
        long diffInSeconds = interval / 1000;

        long diff[] = new long[] { 0, 0, 0 };
        /* sec */diff[2] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        /* min */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[0] = (diffInSeconds = diffInSeconds / 60);

        return String.format(
            "%d hour%s, %d minute%s, %d second%s",
            diff[0],
            diff[0] > 1 || diff[0] == 0 ? "s" : "",
            diff[1],
            diff[1] > 1 || diff[1] == 0 ? "s" : "",
            diff[2],
            diff[2] > 1 || diff[2] == 0 ? "s" : "");
    }
}
