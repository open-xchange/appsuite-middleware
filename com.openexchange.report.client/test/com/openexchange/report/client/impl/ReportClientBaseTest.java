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

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.report.appsuite.serialization.ReportConfigs;
import com.openexchange.report.client.configuration.ReportConfiguration;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.ContextModuleAccessCombination;
import com.openexchange.report.client.container.MacDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.report.client.impl.ReportClientBase.ReportMode;
import com.openexchange.report.client.transport.TransportHandler;

/**
 * {@link ReportClientTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ObjectHandler.class, VersionHandler.class, ReportClientBase.class })
public class ReportClientBaseTest {

    private static final String UUID_CONST = UUID.randomUUID().toString();

    private static final String APPSUITE_REPORT = "{\"macdetail\":{\"capabilitySets\":[{\"total\":2,\"capabilities\":[\"auto_publish_attachments\",\"autologin\",\"caldav\",\"carddav\",\"drive\",\"infostore\",\"mailfilter\",\"oauth\",\"oauth-grants\",\"pop3\",\"printing\",\"publish_mail_attachments\",\"rss\",\"search\",\"twitter\",\"xing\",\"xingjson\"],\"quota\":1073741824,\"admin\":0,\"disabled\":0},{\"total\":11,\"capabilities\":[\"active_sync\",\"auto_publish_attachments\",\"autologin\",\"caldav\",\"calendar\",\"carddav\",\"collect_email_addresses\",\"conflict_handling\",\"contacts\",\"delegate_tasks\",\"drive\",\"edit_password\",\"edit_public_folders\",\"edit_resource\",\"filestore\",\"freebusy\",\"gab\",\"groupware\",\"ical\",\"infostore\",\"mailfilter\",\"mobility\",\"multiple_mail_accounts\",\"oauth\",\"oauth-grants\",\"olox20\",\"participants_dialog\",\"pim\",\"pop3\",\"portal\",\"printing\",\"publication\",\"publish_mail_attachments\",\"read_create_shared_folders\",\"rss\",\"search\",\"subscription\",\"tasks\",\"twitter\",\"usm\",\"vcard\",\"webdav\",\"webdav_xml\",\"webmail\",\"xing\",\"xingjson\"],\"quota\":1073741824,\"admin\":3,\"disabled\":0},{\"total\":6,\"capabilities\":[\"active_sync\",\"auto_publish_attachments\",\"autologin\",\"caldav\",\"calendar\",\"carddav\",\"collect_email_addresses\",\"conflict_handling\",\"contacts\",\"delegate_tasks\",\"drive\",\"edit_password\",\"edit_public_folders\",\"edit_resource\",\"freebusy\",\"gab\",\"groupware\",\"ical\",\"infostore\",\"mailfilter\",\"mobility\",\"multiple_mail_accounts\",\"oauth\",\"oauth-grants\",\"olox20\",\"participants_dialog\",\"pim\",\"pop3\",\"portal\",\"printing\",\"publication\",\"publish_mail_attachments\",\"read_create_shared_folders\",\"rss\",\"search\",\"subscription\",\"tasks\",\"twitter\",\"usm\",\"vcard\",\"webdav\",\"webdav_xml\",\"webmail\",\"xing\",\"xingjson\"],\"quota\":1073741824,\"admin\":2,\"disabled\":0}]},\"total\":{\"guests\":22,\"contexts\":5,\"users\":19,\"report-format\":\"appsuite-short\"},\"clientlogincountyear\":{\"appsuite\":\"7\",\"olox2\":\"0\",\"caldav\":\"0\",\"usm-eas\":\"0\",\"mobileapp\":\"0\",\"ox6\":\"9\",\"carddav\":\"1\"},\"clientlogincount\":{\"appsuite\":\"4\",\"olox2\":\"0\",\"caldav\":\"0\",\"usm-eas\":\"0\",\"mobileapp\":\"0\",\"ox6\":\"2\",\"carddav\":\"1\"},\"uuid\":\"af15880a836a4d66870f469a9daa2bee\",\"reportType\":\"default\",\"timestamps\":{\"start\":1436186159042,\"stop\":1436186159858},\"version\":{\"version\":\"7.8.0-Rev0\",\"buildDate\":\"develop\"},\"configs\":{\"com.openexchange.mail.adminMailLoginEnabled\":\"true\"},\"needsComposition\":false}";
    private static final String APPSUITE_REPORT_DIAGNOSTICS_UUID = "UUID: ";
    private static final String APPSUITE_REPORT_DIAGNOSTIC_TYPE = "    Type: appsuite";
    private static final String APPSUITE_REPORT_DIAGNOSTICS_TIME = "    Current elapsed time: 0 hours, 0 minutes";
    private static final String APPSUITE_REPORT_DIAGNOSTICS_CONTEXTS = "    Finished contexts:";
    private static final String APPSUITE_REPORT_DIAGNOSTICS_AVG = "    Avg. time per context:";
    private static final String APPSUITE_REPORT_DIAGNOSTICS_TIME_LEFT = "    Projected time left:";

    private static final String VERSIONS_BUILD_DATE = "build date develop";

    private static final String CLIENT_COUNT_VALUES = "1111   0     0         22      11";

    private static final String SERVER_CONFIG_HEADER = "key                                         value";

    private static final String MAC_COUNT = "268422943 15    4   0";

    private static final String USER_COUNT = "4        16    22       10";

    private static final String USER_HEADER = "contexts users guests links";

    private static final String MAC_HEADER = "mac       count adm disabled";

    private static final String SERVER_CONFIG_VALUE = "com.openexchange.mail.adminMailLoginEnabled true";

    private static final String CLIENT_COUNT_HEADER = "usmeas olox2 mobileapp carddav caldav";

    private static final String VERSION = "version    7.8.0 Rev0";

    private static final String VERSIONS_BUILD_DATE_CSV = "\"build date\",\"develop\"";

    private static final String CLIENT_COUNT_VALUES_CSV = "\"1111\",\"0\",\"0\",\"22\",\"11\"";

    private static final String SERVER_CONFIG_HEADER_CSV = "\"key\",\"value\"";

    private static final String MAC_COUNT_CSV = "\"268422943\",\"15\",\"4\",\"0\"";

    private static final String USER_COUNT_CSV = "\"4\",\"16\",\"22\"";

    private static final String USER_HEADER_CSV = "\"contexts\",\"users\",\"guests\"";

    private static final String MAC_HEADER_CSV = "\"mac\",\"count\",\"adm\",\"disabled\"";

    private static final String SERVER_CONFIG_VALUE_CSV = "\"com.openexchange.mail.adminMailLoginEnabled\",\"true\"";

    private static final String CLIENT_COUNT_HEADER_CSV = "\"usmeas\",\"olox2\",\"mobileapp\",\"carddav\",\"caldav\"";

    private static final String VERSION_CSV = "\"version\",\"7.8.0 Rev0\"";

    private static final String[] VERSIONS = new String[] { "7.8.0 Rev0", "develop" };

    private static final String REPORT = "report";

    private static final String ADVANCED_HEADER = "id age created                       admin permission module access combination";
    private static final String ADVANCED_VALUE = "22 108 Thu Mar 19 14:01:51 CET 2015  268422943        268422943";

    private static final String ADVANCED_HEADER_CSV = "\"id\",\"age\",\"created\",\"admin permission\",\"module access combination\",\"users\",\"inactive\"";
    private static final String ADVANCED_VALUE_CSV = "\"22\",\"108\",\"Thu Mar 19 14:01:51 CET 2015\",\"268422943\",\"268422943\",\"4\",\"0\"";

    private ReportClientBase reportClientBase = null;

    private List<ContextDetail> contextDetails = null;

    private List<Total> totals = null;

    private List<MacDetail> macdetails = null;

    private Map<String, String> serverConfig = null;

    private ClientLoginCount clientLoginCount = null;

    private ClientLoginCount clientLoginCountYear = null;

    @Mock
    private MBeanServerConnection serverConnection;

    @Mock
    private TransportHandler transportHandler;

    @Mock
    private CompositeData report;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @Mock
    private ReportConfiguration reportConfiguration;

    //------------------------------------ SETUP ---------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        reportClientBase = new ReportClientBase() {

            @Override
            protected TransportHandler createTransportHandler() {
                return transportHandler;
            }

            @Override
            protected MBeanServerConnection initConnection(Map<String, String[]> env) {
                return serverConnection;
            }
        };

        setUpOldReportStyle();
        setUpAppsuiteReport();
    }

    private void setUpAppsuiteReport() throws Exception {
        Mockito.doReturn(new CompositeData[] { report }).when(serverConnection).invoke(reportClientBase.getAppSuiteReportingName(), "retrievePendingReports", new Object[] { "default" }, new String[] { String.class.getCanonicalName() });
        Mockito.when(serverConnection.invoke(reportClientBase.getAppSuiteReportingName(), "retrieveLastReport", new Object[] { "default" }, new String[] { String.class.getCanonicalName() })).thenReturn(report);
        Mockito.when(serverConnection.invoke(reportClientBase.getAppSuiteReportingName(), "run", new Object[] { "default" }, new String[] { String.class.getCanonicalName() })).thenReturn(UUID_CONST);

        Mockito.when(report.get("startTime")).thenReturn(new Date().getTime() - 10000);
        Mockito.when(report.get("stopTime")).thenReturn(new Date().getTime());
        Mockito.when(report.get("tasks")).thenReturn(new Integer(1111));
        Mockito.when(report.get("pendingTasks")).thenReturn(new Integer(2));
        Mockito.when(report.get("uuid")).thenReturn(UUID_CONST);
        Mockito.when(report.get("type")).thenReturn("appsuite");
        Mockito.when(report.get("data")).thenReturn(APPSUITE_REPORT);
        PowerMockito.whenNew(ReportConfiguration.class).withAnyArguments().thenReturn(reportConfiguration);
        Mockito.when(reportConfiguration.getMaxChunkSize()).thenReturn("100");
        Mockito.when(reportConfiguration.getReportStorage()).thenReturn("/test/");
        
    }

    private void setUpOldReportStyle() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException, InvalidAttributeValueException {
        PowerMockito.mockStatic(ObjectHandler.class);

        contextDetails = new ArrayList<>();
        List<ContextModuleAccessCombination> mad = new ArrayList<>();
        mad.add(new ContextModuleAccessCombination("268422943", "4", "0"));
        contextDetails.add(new ContextDetail("22", "108", "108", "Thu Mar 19 14:01:51 CET 2015", "268422943", "268422943", mad));
        PowerMockito.when(ObjectHandler.getDetailObjects(serverConnection)).thenReturn(contextDetails);
        PowerMockito.when(ObjectHandler.createDetailList(contextDetails)).thenReturn(ObjectHandlerForTest.createDetailList(contextDetails));

        totals = new ArrayList<>();
        totals.add(new Total("4", "16", "22", "10"));
        PowerMockito.when(ObjectHandler.getTotalObjects(serverConnection)).thenReturn(totals);
        PowerMockito.when(ObjectHandler.createTotalList(totals)).thenReturn(ObjectHandlerForTest.createTotalList(totals));

        macdetails = new ArrayList<>();
        MacDetail macDetail = new MacDetail("268422943", "15");
        macDetail.setNrAdm("4");
        macDetail.setNrDisabled("0");
        macdetails.add(macDetail);
        PowerMockito.when(ObjectHandler.getMacObjects(serverConnection)).thenReturn(macdetails);
        PowerMockito.when(ObjectHandler.createMacList(macdetails)).thenReturn(ObjectHandlerForTest.createMacList(macdetails));

        serverConfig = new HashMap<>();
        serverConfig.put("com.openexchange.mail.adminMailLoginEnabled", "true");
        PowerMockito.when(ObjectHandler.getServerConfiguration(serverConnection)).thenReturn(serverConfig);
        PowerMockito.when(ObjectHandler.createConfigurationList(serverConfig)).thenReturn(ObjectHandlerForTest.createConfigurationList(serverConfig));

        clientLoginCount = new ClientLoginCount();
        clientLoginCount.setCaldav("11");
        clientLoginCount.setCarddav("22");
        clientLoginCount.setOlox2("0");
        clientLoginCount.setMobileapp("0");
        clientLoginCount.setUsmeas("1111");
        PowerMockito.when(ObjectHandler.getClientLoginCount(serverConnection)).thenReturn(clientLoginCount);
        PowerMockito.when(ObjectHandler.createLogincountList(clientLoginCount)).thenReturn(ObjectHandlerForTest.createLogincountList(clientLoginCount));

        clientLoginCountYear = new ClientLoginCount();
        clientLoginCountYear.setCaldav("33");
        clientLoginCountYear.setCarddav("22");
        clientLoginCountYear.setOlox2("0");
        clientLoginCountYear.setUsmeasYear("111111");
        PowerMockito.when(ObjectHandler.getClientLoginCount(serverConnection, true)).thenReturn(clientLoginCountYear);
        PowerMockito.when(ObjectHandler.createLogincountListYear(clientLoginCount)).thenReturn(ObjectHandlerForTest.createLogincountListYear(clientLoginCount));

        PowerMockito.mockStatic(VersionHandler.class);
        PowerMockito.when(VersionHandler.getServerVersion()).thenReturn(VERSIONS);
        PowerMockito.when(ObjectHandler.createVersionList(VERSIONS)).thenReturn(ObjectHandlerForTest.createVersionList(VERSIONS));
    }

    //------------------------------------ TESTS ---------------------------------------------------------
    @Test
    public void testGetASReport_noReportFound_outputHint() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        Mockito.when(serverConnection.invoke(reportClientBase.getAppSuiteReportingName(), "retrieveLastReport", new Object[] { "default" }, new String[] { String.class.getCanonicalName() })).thenReturn(null);

        reportClientBase.getASReport(null, ReportMode.NONE, false, serverConnection);

        assertTrue(systemOutRule.getLog().contains(ReportClientBase.NO_REPORT_FOUND_MSG));
    }

    @Test
    public void testStart_noOptionSet_reportSentAndPrinted_inAppsuiteStyle() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_runOldStyle_reportSentAndPrinted() throws IOException, JSONException {
        reportClientBase.start(new Builder().addRunAndDeliverOldReport().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
        validatePrint();
    }

    @Test
    public void testStart_displayOnly_displayedNotSend() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addDisplay().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.never()).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_displayOnly_displayedNotSend_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addDisplay().addRunAndDeliverOldReport().build(), REPORT);

        validatePrint();
        Mockito.verify(transportHandler, Mockito.never()).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_sendAndSave_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSend().addSaveReport().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_sendAndSave_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSend().addSaveReport().addRunAndDeliverOldReport().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        validatePrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
    }

    @Test
    public void testStart_sendOnly_sentNotDisplayed() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSend().build(), REPORT);

        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
    }

    @Test
    public void testStart_sendOnly_sentNotDisplayed_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSend().addRunAndDeliverOldReport().build(), REPORT);

        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
    }

    @Test
    public void testStart_sendAndDisplay_displayedAndSent() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSend().addDisplay().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_sendAndDisplay_displayedAndSent_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSend().addDisplay().addRunAndDeliverOldReport().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
        validatePrint();
    }

    //CSV only included in print method
    @Test
    public void testStart_csvOnly_noCsvForAppsuiteReportAvailable() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addCSV().build(), REPORT);

        validatePrint(ReportClientBase.CSV_NOT_SUPPORTED_MSG);
        validatePrint(APPSUITE_REPORT);
        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
    }

    @Test
    public void testStart_csvOnly_printOutputSentReportDisplayedCSV_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addCSV().addRunAndDeliverOldReport().build(), REPORT);

        validatePrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
    }

    @Test
    public void testStart_saveReport_onlySaved() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSaveReport().build(), REPORT);

        validateNotPrint(APPSUITE_REPORT);
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, true);
    }

    @Test
    public void testStart_saveReport_onlySaved_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSaveReport().addRunAndDeliverOldReport().build(), REPORT);

        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, true);
    }

    @Test
    public void testStart_saveAndDisplayReport_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSaveReport().addDisplay().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        validatePrint(APPSUITE_REPORT);
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
    }

    @Test
    public void testStart_saveAndDisplayReport_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSaveReport().addDisplay().addRunAndDeliverOldReport().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        validatePrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, null, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
    }

    @Test
    public void testStart_saveAndDisplayAndAdvancedReport_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addSaveReport().addDisplay().addAdvancedReport().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        validatePrint(APPSUITE_REPORT);
        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
    }

    @Test
    public void testStart_saveAndDisplayAndAdvancedReport_tooManyArgumentsFallBackToDisplayAndSendWithoutSaving_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSaveReport().addDisplay().addAdvancedReport().addRunAndDeliverOldReport().build(), REPORT);

        validatePrint(ReportClientBase.TOO_MANY_ARGUMENTS_USING_THE_DEFAULT_DISPLAY_AND_SEND);
        validatePrint();
        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(totals, macdetails, contextDetails, serverConfig, VERSIONS, clientLoginCount, clientLoginCountYear, false);
    }

    @Test
    public void testStart_displayAndAdvancedReport_displayAdvanced() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addDisplay().addAdvancedReport().build(), REPORT);

        validatePrint(APPSUITE_REPORT);
        Mockito.verify(transportHandler, Mockito.never()).sendASReport(report, false);
    }

    @Test
    public void testStart_displayAndAdvancedReport_displayAdvanced_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addDisplay().addAdvancedReport().addRunAndDeliverOldReport().build(), REPORT);

        validatePrint(true);
        Mockito.verify(transportHandler, Mockito.never()).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_displayAndAdvancedReportAndCSV_displayAdvancedCSV() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addDisplay().addAdvancedReport().addCSV().build(), REPORT);

        validatePrint(ReportClientBase.ADVANCED_NOT_SUPPORTED_MSG);
        validatePrint(APPSUITE_REPORT);
        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.never()).sendASReport(report, false);
    }

    @Test
    public void testStart_displayAndAdvancedReportAndCSV_displayAdvancedCSV_inOldStyle() throws IOException, JSONException {
        reportClientBase.start(new Builder().addDisplay().addAdvancedReport().addCSV().addRunAndDeliverOldReport().build(), REPORT);

        validatePrintCSV(true);
        Mockito.verify(transportHandler, Mockito.never()).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_showCombi_showsCombi() {
        reportClientBase.start(new Builder().addShowAccessCombination("268422943").build(), REPORT);

        validatePrint("access-denied-portal: off");
        validatePrint("access-edit-resource: on");
    }

    @Test
    public void testStart_showCombi_showsCombiAndDoesNothingElse_reportStyleUnrelated() throws IOException, JSONException {
        reportClientBase.start(new Builder().addShowAccessCombination("268422943").addAdvancedReport().addCSV().addDisplay().addGetAppsuiteReport().addRunAndDeliverAppsuiteReport().build(), REPORT);

        validateNotPrint();
        validateNotPrintCSV();
        Mockito.verify(transportHandler, Mockito.never()).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    //------------------------------------ APPSUITE REPORT ---------------------------------------------------------

    @Test
    public void testStart_getAppsuiteReport_noReportGeneratedOutputNotFound() throws IOException, InstanceNotFoundException, MBeanException, ReflectionException {
        Mockito.when(serverConnection.invoke((ObjectName) Matchers.any(), Matchers.anyString(), (Object[]) Matchers.any(), (String[]) Matchers.any())).thenReturn(null);

        reportClientBase.start(new Builder().addGetAppsuiteReport().build(), REPORT);

        validatePrint(ReportClientBase.NO_REPORT_FOUND_MSG);
    }

    @Test
    public void testStart_getAppsuiteReportNoAdditionalOptionSelected_fallThroughDisplayAndSend() throws IOException, JSONException {
        reportClientBase.start(new Builder().addGetAppsuiteReport().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_getAppsuiteReportAndCsv_csvNotValidForAppsuiteReportFallThroughDisplayAndSend() throws IOException, JSONException {
        reportClientBase.start(new Builder().addGetAppsuiteReport().addCSV().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_getAppsuiteReportAndAdvanced_advancedOptionNotValidForAppsuiteReportFallThroughDisplayAndSend() throws IOException, JSONException {
        reportClientBase.start(new Builder().addGetAppsuiteReport().addAdvancedReport().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_getAppsuiteReportAndDisplay_displayReport() throws IOException, JSONException {
        reportClientBase.start(new Builder().addGetAppsuiteReport().addDisplay().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.never()).sendASReport((CompositeData) Matchers.any(), Matchers.anyBoolean());
        validatePrint(APPSUITE_REPORT);
    }
    
    @Test
    public void testStart_getAppsuiteReportWithTimeframeAndDisplay_displayReport() throws IOException, JSONException {
        Builder builder = new Builder();
        builder.addGetAppsuiteReport().addDisplay();
        builder.addGetAppsuiteReport().addTimeframeStart("01.01.2016");
        builder.addGetAppsuiteReport().addTimeframeEnd("01.03.2016");
        
        reportClientBase.start(builder.build(), REPORT);

        Mockito.verify(transportHandler, Mockito.never()).sendASReport((CompositeData) Matchers.any(), Matchers.anyBoolean());
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_runAndDeliverOption_getPrintAndSentReportBecauseOfFallThroughToDefault() throws IOException, JSONException {
        Mockito.when(report.get("uuid")).thenReturn(UUID.randomUUID().toString());

        reportClientBase.start(new Builder().addRunAndDeliverAppsuiteReport().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendASReport(report, false);
        validatePrint(APPSUITE_REPORT);
    }

    @Test
    public void testStart_runOption_runAndRetrieveResultsAndPrintDiagnostics() throws IOException, JSONException, InstanceNotFoundException, MBeanException, ReflectionException {
        reportClientBase.start(new Builder().addRunAppsuiteReport().build(), REPORT);

        //No longer valid test since the method expects a distinct instance of ReportConfigs.
        //Mockito.verify(serverConnection, Mockito.times(1)).invoke(reportClientBase.getAppSuiteReportingName(), "run", new Object[] { any(ReportConfigs.class) }, new String[] { CompositeData.class.getCanonicalName() });
        Mockito.verify(serverConnection, Mockito.times(1)).invoke(reportClientBase.getAppSuiteReportingName(), "retrievePendingReports", new Object[] { "default" }, new String[] { String.class.getCanonicalName() });
        Mockito.verify(transportHandler, Mockito.never()).sendASReport((CompositeData) Matchers.any(), Matchers.anyBoolean());
        validateAppsuiteDiagnosticsPrint();
    }

    @Test
    public void testStart_cancelReports_reportCanceled() throws IOException, InstanceNotFoundException, MBeanException, ReflectionException {
        reportClientBase.start(new Builder().addCancelReport().build(), REPORT);

        Mockito.verify(serverConnection, Mockito.times(1)).invoke(reportClientBase.getAppSuiteReportingName(), "retrievePendingReports", new Object[] { "default" }, new String[] { String.class.getCanonicalName() });
        Mockito.verify(serverConnection, Mockito.times(1)).invoke(reportClientBase.getAppSuiteReportingName(), "flushPending", new Object[] { report.get("uuid"), "default" }, new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });
    }

    @Test
    public void testStart_inspectReports_outputDiagnostics() {
        reportClientBase.start(new Builder().addInspectReports().build(), REPORT);

        validateAppsuiteDiagnosticsPrint();
    }

    //------------------------------------ VALIDATION ---------------------------------------------------------

    private void validateAppsuiteDiagnosticsPrint() {
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTIC_TYPE.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTICS_AVG.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTICS_CONTEXTS.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTICS_TIME.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTICS_TIME_LEFT.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(APPSUITE_REPORT_DIAGNOSTICS_UUID.replaceAll("\\s", "").trim()));

    }

    private void validatePrintCSV(boolean advanced) {
        if (advanced) {
            assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(ADVANCED_HEADER_CSV.replaceAll("\\s", "").trim()));
            assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(ADVANCED_VALUE_CSV.replaceAll("\\s", "").trim()));
        }
        validatePrintCSV();
    }

    private void validatePrint(boolean advanced) {
        if (advanced) {
            assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(ADVANCED_HEADER.replaceAll("\\s", "").trim()));
            assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(ADVANCED_VALUE.replaceAll("\\s", "").trim()));
        }
        validatePrint();
    }

    private void validatePrintCSV() {
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(VERSION_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(VERSIONS_BUILD_DATE_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(USER_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(USER_COUNT_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(MAC_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(MAC_COUNT_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(SERVER_CONFIG_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(SERVER_CONFIG_VALUE_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(CLIENT_COUNT_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertTrue(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(CLIENT_COUNT_VALUES_CSV.replaceAll("\\s", "").trim()));
    }

    private void validateNotPrintCSV() {
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(VERSION_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(VERSIONS_BUILD_DATE_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(USER_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(USER_COUNT_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(MAC_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(MAC_COUNT_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(SERVER_CONFIG_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(SERVER_CONFIG_VALUE_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(CLIENT_COUNT_HEADER_CSV.replaceAll("\\s", "").trim()));
        assertFalse(systemOutRule.getLog().replaceAll("\\s", "").trim().contains(CLIENT_COUNT_VALUES_CSV.replaceAll("\\s", "").trim()));
    }

    private void validatePrint() {
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(VERSION.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(VERSIONS_BUILD_DATE.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(USER_HEADER.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(USER_COUNT.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(MAC_HEADER.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(MAC_COUNT.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(SERVER_CONFIG_HEADER.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(SERVER_CONFIG_VALUE.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(CLIENT_COUNT_HEADER.replaceAll(" +", " ").trim()));
        assertTrue(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(CLIENT_COUNT_VALUES.replaceAll(" +", " ").trim()));
    }

    private void validateNotPrint() {
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(VERSION.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(VERSIONS_BUILD_DATE.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(USER_HEADER.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(USER_COUNT.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(MAC_HEADER.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(MAC_COUNT.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(SERVER_CONFIG_HEADER.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(SERVER_CONFIG_VALUE.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(CLIENT_COUNT_HEADER.replaceAll(" +", " ").trim()));
        assertFalse(systemOutRule.getLog().replaceAll(" +", " ").trim().contains(CLIENT_COUNT_VALUES.replaceAll(" +", " ").trim()));
    }

    private void validatePrint(String text) {
        assertTrue(systemOutRule.getLog().replace("\n", "").replace("\r", "").replaceAll(" +", "").trim().contains(text.replace("\n", "").replace("\r", "").replaceAll(" +", "").trim()));
    }

    private void validateNotPrint(String text) {
        assertFalse(systemOutRule.getLog().replace("\n", "").replace("\r", "").replaceAll(" +", "").trim().contains(text.replace("\n", "").replace("\r", "").replaceAll(" +", "").trim()));
    }

    //------------------------------------ HELPER ---------------------------------------------------------
    private static class Builder {

        private final List<String> params = new ArrayList<>();

        private static final String OPT_HELP_SHORT = "-h";
        private static final String OPT_HOST_SHORT = "-H";
        private static final String OPT_TIMEOUT_SHORT = "-T";
        private static final String OPT_JMX_AUTH_PASSWORD_SHORT = "-P";
        private static final String OPT_JMX_AUTH_USER_SHORT = "-J";
        private static final String OPT_SEND_ONLY_SHORT = "-s";
        private static final String OPT_DISPLAY_ONLY_SHORT = "-d";
        private static final String OPT_CSV_SHORT = "-c";
        private static final String OPT_ADVANCEDREPORT_SHORT = "-a";
        private static final String OPT_SAVEREPORT_SHORT = "-f";
        private static final String OPT_SHOWCOMBINATION_SHORT = "-b";
        private static final String OPT_APPSUITE_RUN_REPORT_SHORT = "-e";
        private static final String OPT_APPSUITE_GET_REPORT_SHORT = "-g";
        private static final String OPT_APPSUITE_CANCEL_REPORTS_LONG = "--cancel-appsuite-reports";
        private static final String OPT_APPSUITE_INSPECT_REPORTS_LONG = "--inspect-appsuite-reports";
        private static final String OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT = "-x";
        private static final String OPT_RUN_AND_DELIVER_OLD_REPORT_SHORT = "-o";
        private static final String OPT_TIMEFRAME_START = "-S";
        private static final String OPT_TIMEFRAME_END = "-E";

        public Builder addHelp() {
            params.add(OPT_HELP_SHORT);
            return this;
        }

        public Builder addHost() {
            params.add(OPT_HOST_SHORT);
            return this;
        }

        public Builder addTimeout() {
            params.add(OPT_TIMEOUT_SHORT);
            return this;
        }

        public Builder addJmxAuthPwd(String param) {
            params.add(OPT_JMX_AUTH_PASSWORD_SHORT + " " + param);
            return this;
        }

        public Builder addJmxAuthUser(String param) {
            params.add(OPT_JMX_AUTH_USER_SHORT + " " + param);
            return this;
        }

        public Builder addSend() {
            params.add(OPT_SEND_ONLY_SHORT);
            return this;
        }

        public Builder addDisplay() {
            params.add(OPT_DISPLAY_ONLY_SHORT);
            return this;
        }

        public Builder addCSV() {
            params.add(OPT_CSV_SHORT);
            return this;
        }

        public Builder addAdvancedReport() {
            params.add(OPT_ADVANCEDREPORT_SHORT);
            return this;
        }

        public Builder addSaveReport() {
            params.add(OPT_SAVEREPORT_SHORT);
            return this;
        }

        public Builder addShowAccessCombination(String param) {
            params.add(OPT_SHOWCOMBINATION_SHORT);
            params.add(param);
            return this;
        }

        public Builder addRunAppsuiteReport() {
            params.add(OPT_APPSUITE_RUN_REPORT_SHORT);
            return this;
        }

        public Builder addGetAppsuiteReport() {
            params.add(OPT_APPSUITE_GET_REPORT_SHORT);
            return this;
        }

        public Builder addCancelReport() {
            params.add(OPT_APPSUITE_CANCEL_REPORTS_LONG);
            return this;
        }

        public Builder addInspectReports() {
            params.add(OPT_APPSUITE_INSPECT_REPORTS_LONG);
            return this;
        }

        public Builder addRunAndDeliverAppsuiteReport() {
            params.add(OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT);
            return this;
        }

        public Builder addRunAndDeliverOldReport() {
            params.add(OPT_RUN_AND_DELIVER_OLD_REPORT_SHORT);
            return this;
        }
        
        public Builder addTimeframeStart(String timeString) {
            params.add(OPT_TIMEFRAME_START);
            params.add(timeString);
            return this;
        }
        
        public Builder addTimeframeEnd(String timeString) {
            params.add(OPT_TIMEFRAME_END);
            params.add(timeString);
            return this;
        }

        public String[] build() {
            return params.toArray(new String[params.size()]);
        }
    }

    //HACK: circumvent static mocking of ObjectHandler
    static class ObjectHandlerForTest {

        protected static List<List<Object>> createTotalList(final List<Total> totals) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "contexts", "users", "guests", "links"));

            for (final Total tmp : totals) {
                retval.add(Arrays.asList((Object) tmp.getContexts(), tmp.getUsers(), tmp.getGuests(), tmp.getLinks()));
            }

            return retval;
        }

        protected static List<List<Object>> createDetailList(final List<ContextDetail> contextDetails) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "id", "age", "created", "admin permission", "module access combination", "users", "inactive"));

            final TreeSet<Integer> sorted = new TreeSet<Integer>(new Comparator<Integer>() {

                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return o1.compareTo(o2);
                }
            });

            final HashMap<String, List<List<Object>>> sortDetails = new HashMap<String, List<List<Object>>>();

            for (final ContextDetail tmp : contextDetails) {
                for (final ContextModuleAccessCombination moduleAccessCombination : tmp.getModuleAccessCombinations()) {

                    sorted.add(new Integer(tmp.getId()));

                    List<List<Object>> tmpList;
                    if (!sortDetails.containsKey(tmp.getId())) {
                        tmpList = new ArrayList<List<Object>>();
                    } else {
                        tmpList = sortDetails.get(tmp.getId());
                    }
                    tmpList.add(Arrays.asList((Object) new Integer(tmp.getId()), tmp.getAge(), tmp.getCreated(), tmp.getAdminmac(), moduleAccessCombination.getUserAccessCombination(), moduleAccessCombination.getUserCount(), moduleAccessCombination.getInactiveCount()));
                    sortDetails.put(tmp.getId(), tmpList);
                }
            }

            for (final Integer tmp : sorted) {
                for (final List<Object> tmpList : sortDetails.get(String.valueOf(tmp))) {
                    retval.add(tmpList);
                }
            }

            return retval;
        }

        protected static List<List<Object>> createConfigurationList(final Map<String, String> serverConfiguration) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "key", "value"));

            for (final Entry<String, String> tmp : serverConfiguration.entrySet()) {
                retval.add(Arrays.asList((Object) tmp.getKey(), tmp.getValue()));
            }
            return retval;
        }

        protected static List<List<Object>> createMacList(final List<MacDetail> macDetails) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "mac", "count", "adm", "disabled"));

            for (final MacDetail tmp : macDetails) {
                retval.add(Arrays.asList((Object) tmp.getId(), tmp.getCount(), tmp.getNrAdm(), tmp.getNrDisabled()));
            }
            return retval;
        }

        protected static List<List<Object>> createVersionList(final String[] versions) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "version", versions[0]));
            retval.add(Arrays.asList((Object) "build date", versions[1]));
            return retval;
        }

        protected static List<List<Object>> createLogincountList(final ClientLoginCount lcount) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "usmeas", "olox2", "mobileapp", "carddav", "caldav"));
            retval.add(Arrays.asList((Object) lcount.getUsmeas(), lcount.getOlox2(), lcount.getMobileapp(), lcount.getCarddav(), lcount.getCaldav()));
            return retval;
        }

        protected static List<List<Object>> createLogincountListYear(final ClientLoginCount lcount) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "usmeasyear", "olox2year", "mobileappyear", "carddavyear", "caldavyear"));
            retval.add(Arrays.asList((Object) lcount.getUsmeas(), lcount.getOlox2(), lcount.getMobileapp(), lcount.getCarddav(), lcount.getCaldav()));
            return retval;
        }
    }
}
