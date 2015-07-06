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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import javax.management.MBeanServerConnection;
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
@PrepareForTest({ ObjectHandler.class, VersionHandler.class })
public class ReportClientBaseTest {

    private static final String VERSIONS_BUILD_DATE = "build date develop";

    private static final String CLIENT_COUNT_VALUES= "1111   0     0         22      11";

    private static final String SERVER_CONFIG_HEADER = "key                                         value";

    private static final String MAC_COUNT = "268422943 15    4   0";

    private static final String USER_COUNT = "4        16    22";

    private static final String USER_HEADER = "contexts users guests";

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

    private ReportClientBase reportClientBase = null;

    @Mock
    private MBeanServerConnection serverConnection;

    @Mock
    private TransportHandler transportHandler;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ObjectHandler.class);

        List<Total> totals = new ArrayList<>();
        totals.add(new Total("4", "16", "22"));
        PowerMockito.when(ObjectHandler.getTotalObjects(serverConnection)).thenReturn(totals);
        PowerMockito.when(ObjectHandler.createTotalList(totals)).thenReturn(ObjectHandlerForTest.createTotalList(totals));

        List<MacDetail> macdetails = new ArrayList<>();
        MacDetail macDetail = new MacDetail("268422943", "15");
        macDetail.setNrAdm("4");
        macDetail.setNrDisabled("0");
        macdetails.add(macDetail);
        PowerMockito.when(ObjectHandler.getMacObjects(serverConnection)).thenReturn(macdetails);
        PowerMockito.when(ObjectHandler.createMacList(macdetails)).thenReturn(ObjectHandlerForTest.createMacList(macdetails));

        Map<String, String> serverConfig = new HashMap<>();
        serverConfig.put("com.openexchange.mail.adminMailLoginEnabled", "true");
        PowerMockito.when(ObjectHandler.getServerConfiguration(serverConnection)).thenReturn(serverConfig);
        PowerMockito.when(ObjectHandler.createConfigurationList(serverConfig)).thenReturn(ObjectHandlerForTest.createConfigurationList(serverConfig));

        ClientLoginCount clientLoginCount = new ClientLoginCount();
        clientLoginCount.setCaldav("11");
        clientLoginCount.setCarddav("22");
        clientLoginCount.setOlox2("0");
        clientLoginCount.setMobileapp("0");
        clientLoginCount.setUsmeas("1111");
        PowerMockito.when(ObjectHandler.getClientLoginCount(serverConnection)).thenReturn(clientLoginCount);
        PowerMockito.when(ObjectHandler.createLogincountList(clientLoginCount)).thenReturn(ObjectHandlerForTest.createLogincountList(clientLoginCount));

        ClientLoginCount clientLoginCountYear = new ClientLoginCount();
        clientLoginCountYear.setCaldav("33");
        clientLoginCountYear.setCarddav("22");
        clientLoginCountYear.setOlox2("0");
        clientLoginCountYear.setUsmeasYear("111111");
        PowerMockito.when(ObjectHandler.getClientLoginCount(serverConnection, true)).thenReturn(clientLoginCountYear);
        PowerMockito.when(ObjectHandler.createLogincountListYear(clientLoginCount)).thenReturn(ObjectHandlerForTest.createLogincountListYear(clientLoginCount));

        PowerMockito.mockStatic(VersionHandler.class);
        PowerMockito.when(VersionHandler.getServerVersion()).thenReturn(VERSIONS);
        PowerMockito.when(ObjectHandler.createVersionList(VERSIONS)).thenReturn(ObjectHandlerForTest.createVersionList(VERSIONS));

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
    }

    @Test
    public void testGetASReport_noReportFound_outputHint() {
        reportClientBase.getASReport(null, ReportMode.NONE, false, serverConnection);
        assertEquals(ReportClientBase.NO_REPORT_FOUND_MSG.trim(), systemOutRule.getLog().trim());
    }

    @Test
    public void testStart_noOptionSet_reportSent() throws IOException, JSONException {
        reportClientBase.start(new Builder().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_noOptionSet_reportPrinted() {
        reportClientBase.start(new Builder().build(), REPORT);

        validatePrint();
    }

    @Test
    public void testStart_displayOnly_displayed() {
        reportClientBase.start(new Builder().addDisplay().build(), REPORT);

        validatePrint();
    }

    @Test
    public void testStart_displayOnly_displayedNotSend() throws IOException, JSONException {
        reportClientBase.start(new Builder().addDisplay().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.never()).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_sendOnly_sent() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSend().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
    }

    @Test
    public void testStart_sendOnly_sentNotDisplayed() {
        reportClientBase.start(new Builder().addSend().build(), REPORT);

        //TODO check not displayed
    }

    @Test
    public void testStart_sendAndDisplay_displayedAndSent() throws IOException, JSONException {
        reportClientBase.start(new Builder().addSend().addDisplay().build(), REPORT);

        Mockito.verify(transportHandler, Mockito.times(1)).sendReport(Matchers.anyList(), Matchers.anyList(), Matchers.anyList(), Matchers.anyMap(), (String[]) Matchers.any(), (ClientLoginCount) Matchers.any(), (ClientLoginCount) Matchers.any(), Matchers.anyBoolean());
        validatePrint();
    }

    //CSV only included in print method
    @Test
    public void testStart_csvOnly_printOutputSentReportDisplayedCSV() {
        reportClientBase.start(new Builder().addCSV().build(), REPORT);

        validatePrintCSV();
    }

    //--------------------------------------------------------------------------------------------------------

    private void validatePrintCSV() {
        assertTrue(systemOutRule.getLog().trim().contains(VERSION_CSV.trim()));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(VERSIONS_BUILD_DATE_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(USER_HEADER_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(USER_COUNT_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(MAC_HEADER_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(MAC_COUNT_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(SERVER_CONFIG_HEADER_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(SERVER_CONFIG_VALUE_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(CLIENT_COUNT_HEADER_CSV.trim().replaceAll("\\s","")));
        assertTrue(systemOutRule.getLog().trim().replaceAll("\\s","").contains(CLIENT_COUNT_VALUES_CSV.trim().replaceAll("\\s","")));
    }
    private void validatePrint() {
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(VERSION.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(VERSIONS_BUILD_DATE.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(USER_HEADER.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(USER_COUNT.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(MAC_HEADER.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(MAC_COUNT.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(SERVER_CONFIG_HEADER.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(SERVER_CONFIG_VALUE.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(CLIENT_COUNT_HEADER.trim().replaceAll(" +", " ")));
        assertTrue(systemOutRule.getLog().trim().replaceAll(" +", " ").contains(CLIENT_COUNT_VALUES.trim().replaceAll(" +", " ")));
    }

    //--------------------------------------------------------------------------------------------------------
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
        private static final String OPT_APPSUITE_REPORT_TYPE_SHORT = "-t";
        private static final String OPT_APPSUITE_RUN_AND_DELIVER_REPORT_SHORT = "-x";
        private static final String OPT_RUN_AND_DELIVER_OLD_REPORT_SHORT = "-o";

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
            params.add(OPT_SHOWCOMBINATION_SHORT + " " + param);
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

        public Builder addReportAppsuiteReport(String param) {
            params.add(OPT_APPSUITE_REPORT_TYPE_SHORT + " " + param);
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

        public String[] build() {
            return params.toArray(new String[params.size()]);
        }
    }

    //HACK: circumvent static mocking of ObjectHandler
    static class ObjectHandlerForTest {

        protected static List<List<Object>> createTotalList(final List<Total> totals) {
            final List<List<Object>> retval = new ArrayList<List<Object>>();
            retval.add(Arrays.asList((Object) "contexts", "users", "guests"));

            for (final Total tmp : totals) {
                retval.add(Arrays.asList((Object) tmp.getContexts(), tmp.getUsers(), tmp.getGuests()));
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
