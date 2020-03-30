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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.report.appsuite.internal;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;

/**
 * {@link ReportInformationTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ReportInformation.class, Services.class })
public class ReportInformationTest {

  //@formatter:off
    private static final List<String> OUTPUT_DEBIAN_PACKAGES = Arrays.asList( 
        "open-xchange/unknown,now 7.10.3-3 all [installed]",
        "open-xchange-admin/unknown,now 7.10.3-3 all [installed]");

    private static final List<String> OUTPUT_PACKAGES_EXPECTED = Arrays.asList(
        "open-xchange", "open-xchange-admin");
    
    private static final List<String> OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES = Arrays.asList(
        "open-xchange.noarch                     7.10.3-3_21.1      @ox-appsuite-backend", 
        "open-xchange-admin.noarch               7.10.3-3_21.1      @ox-appsuite-backend");

    private static final List<String> OUTPUT_SLES_PACKAGES = Arrays.asList( 
        "i | open-xchange       | 7.10.3-3                                                       | package",
        "i | open-xchange-admin | 7.10.3-3                                                       | package");
    //@formatter:on

    @Mock
    private ConfigurationService mockedConfigurationService;

    @Mock
    private Connection mockedConnection;

    @Mock
    private DatabaseMetaData mockedDatabaseMetaData;

    @Mock
    private DatabaseService mockedDatabaseService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        spy(ReportInformation.class);
        mockStatic(Services.class);
        when(Services.getService(ConfigurationService.class)).thenReturn(mockedConfigurationService);
        mockStatic(System.class);

    }

    @Test
    public void testFormatPackageList_AmazonLinux() {
        List<String> output = ReportInformation.formatPackageList(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES, Distribution.AMAZONLINUX);
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testFormatPackageList_Centos() {
        List<String> output = ReportInformation.formatPackageList(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES, Distribution.CENTOS);
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testFormatPackageList_Debian() {
        List<String> output = ReportInformation.formatPackageList(OUTPUT_DEBIAN_PACKAGES, Distribution.DEBIAN);
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testFormatPackageList_NoLinux() {
        List<String> input = Arrays.asList("Test");
        List<String> output = ReportInformation.formatPackageList(input, Distribution.NOLINUX);
        assertEquals(input, output);
    }

    @Test
    public void testFormatPackageList_OutputEmpty() {
        List<String> output = ReportInformation.formatPackageList(Arrays.asList(), null);
        assertTrue(output.isEmpty());
    }

    @Test
    public void testFormatPackageList_OutputNull() {
        assertNull(ReportInformation.formatPackageList(null, null));
    }

    @Test
    public void testFormatPackageList_RHEL() {
        List<String> output = ReportInformation.formatPackageList(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES, Distribution.RHEL);
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testFormatPackageList_SLES() {
        List<String> output = ReportInformation.formatPackageList(OUTPUT_SLES_PACKAGES, Distribution.SLES);
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testFormatPackageList_Unknown() {
        List<String> input = Arrays.asList("Test");
        List<String> output = ReportInformation.formatPackageList(input, Distribution.UNKNOWN);
        assertEquals(input, output);
    }

    @Test
    public void testGetConfiguredThirdPartyAPIsNotOAuth_Empty() {
        List<ThirdPartyAPI> configuredAPIs = ReportInformation.getConfiguredThirdPartyAPIsNotOAuth();
        assertEquals(0, configuredAPIs.size());
    }

    @Test
    public void testGetConfiguredThirdPartyAPIsNotOAuth_Filled() {
        List<ThirdPartyAPI> noOAuthAPIs = Arrays.asList(ThirdPartyAPI.SCHEDJOULES);
        for (ThirdPartyAPI api : noOAuthAPIs) {
            String apiKey = "TestKey";
            when(mockedConfigurationService.getProperty(api.getPropertyName() + ".apiKey")).thenReturn(apiKey);
        }
        List<ThirdPartyAPI> configuredAPIs = ReportInformation.getConfiguredThirdPartyAPIsNotOAuth();
        assertEquals(noOAuthAPIs.size(), configuredAPIs.size());
        for (ThirdPartyAPI api : noOAuthAPIs) {
            assertTrue(configuredAPIs.contains(api));
        }
    }

    @Test
    public void testGetConfiguredThirdPartyAPIsViaOAuth_Empty() {
        List<ThirdPartyAPI> configuredAPIs = ReportInformation.getConfiguredThirdPartyAPIsViaOAuth();
        assertEquals(0, configuredAPIs.size());
    }

    @Test
    public void testGetConfiguredThirdPartyAPIsViaOAuth_Filled() {
        List<ThirdPartyAPI> oAuthAPIs = Arrays.asList(ThirdPartyAPI.BOXCOM, ThirdPartyAPI.DROPBOX, ThirdPartyAPI.GOOGLE, ThirdPartyAPI.MICROSOFT, ThirdPartyAPI.TWITTER, ThirdPartyAPI.XING, ThirdPartyAPI.YAHOO);
        for (ThirdPartyAPI api : oAuthAPIs) {
            String enabledValue = B(true).toString();
            String apiKey = "TestKey";
            String apiSecret = "TestSecret";
            when(mockedConfigurationService.getProperty(api.getPropertyName())).thenReturn(enabledValue);
            when(mockedConfigurationService.getProperty(api.getPropertyName() + ".apiKey")).thenReturn(apiKey);
            when(mockedConfigurationService.getProperty(api.getPropertyName() + ".apiSecret")).thenReturn(apiSecret);
        }
        List<ThirdPartyAPI> configuredAPIs = ReportInformation.getConfiguredThirdPartyAPIsViaOAuth();
        assertEquals(oAuthAPIs.size(), configuredAPIs.size());
        for (ThirdPartyAPI api : oAuthAPIs) {
            assertTrue(configuredAPIs.contains(api));
        }
    }

    @Test
    public void testGetDatabaseVersion_OXException() throws OXException {
        when(Services.getService(DatabaseService.class)).thenReturn(mockedDatabaseService);
        when(mockedDatabaseService.getReadOnly()).thenThrow(new OXException());
        ReportInformation.getDatabaseVersion();
    }

    @Test
    public void testGetDatabaseVersion_SQLException() throws OXException, SQLException {
        when(Services.getService(DatabaseService.class)).thenReturn(mockedDatabaseService);
        when(mockedDatabaseService.getReadOnly()).thenReturn(mockedConnection);
        when(mockedConnection.getMetaData()).thenThrow(new SQLException());
        ReportInformation.getDatabaseVersion();
    }

    @Test
    public void testGetDatabaseVersion_Success() throws OXException, SQLException {
        String databaseName = "MySQL";
        String databaseVersion = "5.6.x";

        when(Services.getService(DatabaseService.class)).thenReturn(mockedDatabaseService);
        when(mockedDatabaseService.getReadOnly()).thenReturn(mockedConnection);
        when(mockedConnection.getMetaData()).thenReturn(mockedDatabaseMetaData);
        when(mockedDatabaseMetaData.getDatabaseProductName()).thenReturn(databaseName);
        when(mockedDatabaseMetaData.getDatabaseProductVersion()).thenReturn(databaseVersion);

        assertEquals(databaseName + " " + databaseVersion, ReportInformation.getDatabaseVersion());
    }

    @Test
    public void testGetDistribution_AmazonLinux() throws Exception {
        setUpAmazonLinux();
        assertEquals(Distribution.AMAZONLINUX, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_Centos() throws Exception {
        setUpCentOS();
        assertEquals(Distribution.CENTOS, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_Debian() throws Exception {
        setUpDebian();
        assertEquals(Distribution.DEBIAN, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_NoLinux() {
        when(System.getProperty("os.name")).thenReturn("MacOS");
        assertEquals(Distribution.NOLINUX, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_RHEL() throws Exception {
        setUpRHEL();
        assertEquals(Distribution.RHEL, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_SLES() throws Exception {
        setUpSLES();
        assertEquals(Distribution.SLES, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistribution_Unknown() throws Exception {
        setUpLinux();
        doReturn(Arrays.asList("Empty")).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");

        assertEquals(Distribution.UNKNOWN, ReportInformation.getDistribution());
    }

    @Test
    public void testGetDistributionName_LinuxAmazon() throws Exception {
        setUpAmazonLinux();
        assertEquals("Amazon Linux AMI 2015.03", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_LinuxCentOS() throws Exception {
        setUpCentOS();
        assertEquals("CentOS Linux 7 (Core)", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_LinuxDebian() throws Exception {
        setUpDebian();
        assertEquals("Debian GNU/Linux 10 (buster)", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_LinuxRHEL() throws Exception {
        setUpRHEL();
        assertEquals("Red Hat Enterprise Linux Server 7.3 (Maipo)", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_LinuxSLES() throws Exception {
        setUpSLES();
        assertEquals("SUSE Linux Enterprise Server 12", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_NoLinux() {
        when(System.getProperty("os.name")).thenReturn("MacOS");
        assertEquals("", ReportInformation.getDistributionName());
    }

    @Test
    public void testGetDistributionName_NoPrettyName() throws Exception {
        setUpLinux();
        doReturn(Arrays.asList("Debian")).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");

        assertEquals("", ReportInformation.getDistributionName());
    }

    @Test
    public void testInstalledPackages_AmazonLinux() throws Exception {
        setUpAmazonLinux();
        doReturn(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES).when(ReportInformation.class, "executeCommand", "/bin/sh", "-c", "yum list installed | egrep " + "'open-xchange|readerengine'");
        List<String> output = ReportInformation.getInstalledPackages();
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testInstalledPackages_Centos() throws Exception {
        setUpCentOS();
        doReturn(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES).when(ReportInformation.class, "executeCommand", "/bin/sh", "-c", "yum list installed | egrep " + "'open-xchange|readerengine'");
        List<String> output = ReportInformation.getInstalledPackages();
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testInstalledPackages_Debian() throws Exception {
        setUpDebian();
        doReturn(OUTPUT_DEBIAN_PACKAGES).when(ReportInformation.class, "executeCommand", "/bin/sh", "-c", "apt --installed list | egrep " + "'open-xchange|readerengine'");
        List<String> output = ReportInformation.getInstalledPackages();
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testInstalledPackages_NoLinux() {
        when(System.getProperty("os.name")).thenReturn("MacOS");
        List<String> output = ReportInformation.getInstalledPackages();
        assertTrue(output.isEmpty());
    }

    @Test
    public void testInstalledPackages_RHEL() throws Exception {
        setUpRHEL();
        doReturn(OUTPUT_RHEL_CENTOS_AMAZONLINUX_PACKAGES).when(ReportInformation.class, "executeCommand", "/bin/sh", "-c", "yum list installed | egrep " + "'open-xchange|readerengine'");
        List<String> output = ReportInformation.getInstalledPackages();
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testInstalledPackages_SLES() throws Exception {
        setUpSLES();
        doReturn(OUTPUT_SLES_PACKAGES).when(ReportInformation.class, "executeCommand", "/bin/sh", "-c", "zypper se -i | egrep " + "'open-xchange|readerengine'");
        List<String> output = ReportInformation.getInstalledPackages();
        assertEquals(OUTPUT_PACKAGES_EXPECTED.size(), output.size());
        for (int i = 0; i < OUTPUT_PACKAGES_EXPECTED.size(); i++) {
            assertEquals(OUTPUT_PACKAGES_EXPECTED.get(i), output.get(i));
        }
    }

    @Test
    public void testInstalledPackages_Unknown() {
        setUpLinux();
        List<String> output = ReportInformation.getInstalledPackages();
        assertTrue(output.isEmpty());
    }

    @Test
    public void testIsLinux_MacOS() {
        when(System.getProperty("os.name")).thenReturn("MacOS");
        assertFalse(ReportInformation.isLinux());
    }

    @Test
    public void testIsLinux_Null() {
        when(System.getProperty("os.name")).thenReturn(null);
        assertFalse(ReportInformation.isLinux());
    }

    @Test
    public void testIsLinux_True() {
        setUpLinux();
        assertTrue(ReportInformation.isLinux());
    }

    private void setUpLinux() {
        when(System.getProperty("os.name")).thenReturn("Linux");
    }

    private void setUpAmazonLinux() throws Exception {
        setUpLinux();
        //@formatter:off
        List<String> output = Arrays.asList(
            "NAME=\"Amazon Linux AMI\"", 
            "VERSION=\"2015.03\"", 
            "ID=\"amzn\"", 
            "ID_LIKE=\"rhel fedora\"", 
            "VERSION_ID=\"2015.03\"", 
            "PRETTY_NAME=\"Amazon Linux AMI 2015.03\"", 
            "ANSI_COLOR=\"0;33\"", 
            "CPE_NAME=\"cpe:/o:amazon:linux:2015.03:ga\"", 
            "HOME_URL=\"http://aws.amazon.com/amazon-linux-ami/\"");
        //@formatter:on
        doReturn(output).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");
    }

    private void setUpCentOS() throws Exception {
        setUpLinux();
        //@formatter:off
        List<String> output = Arrays.asList(
            "NAME=\"CentOS Linux\"" , 
            "VERSION=\"7 (Core)\"" , 
            "ID=\"centos\"" , 
            "ID_LIKE=\"rhel fedora\"" , 
            "VERSION_ID=\"7\"" , 
            "PRETTY_NAME=\"CentOS Linux 7 (Core)\"" , 
            "ANSI_COLOR=\"0;31\"" , 
            "CPE_NAME=\"cpe:/o:centos:centos:7\"" , 
            "HOME_URL=\"https://www.centos.org/\"" , 
            "BUG_REPORT_URL=\"https://bugs.centos.org/\"" , 
            "" , 
            "CENTOS_MANTISBT_PROJECT=\"CentOS-7\"" , 
            "CENTOS_MANTISBT_PROJECT_VERSION=\"7\"" , 
            "REDHAT_SUPPORT_PRODUCT=\"centos\"" , 
            "REDHAT_SUPPORT_PRODUCT_VERSION=\"7\"");
        //@formatter:on
        doReturn(output).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");
    }

    private void setUpDebian() throws Exception {
        setUpLinux();
      //@formatter:off
        List<String> output = Arrays.asList(
            "PRETTY_NAME=\"Debian GNU/Linux 10 (buster)\"",  
            "NAME=\"Debian GNU/Linux\"",  
            "VERSION_ID=\"10\"",  
            "VERSION=\"10 (buster)\"",  
            "VERSION_CODENAME=buster",  
            "ID=debian",  
            "HOME_URL=\"https://www.debian.org/\"",  
            "SUPPORT_URL=\"https://www.debian.org/support\"",  
            "BUG_REPORT_URL=\"https://bugs.debian.org/\"");
        //@formatter:on
        doReturn(output).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");
    }

    private void setUpRHEL() throws Exception {
        setUpLinux();
        //@formatter:off
        List<String> output = Arrays.asList(
            "NAME=\"Red Hat Enterprise Linux Server\"", 
            "VERSION=\"7.3 (Maipo)\"", 
            "ID=\"rhel\"", 
            "ID_LIKE=\"fedora\"", 
            "VERSION_ID=\"7.3\"", 
            "PRETTY_NAME=\"Red Hat Enterprise Linux Server 7.3 (Maipo)\"", 
            "ANSI_COLOR=\"0;31\"", 
            "CPE_NAME=\"cpe:/o:redhat:enterprise_linux:7.3:GA:server\"", 
            "HOME_URL=\"https://www.redhat.com/\"", 
            "BUG_REPORT_URL=\"https://bugzilla.redhat.com/\"", 
            "REDHAT_BUGZILLA_PRODUCT=\"Red Hat Enterprise Linux 7\"", 
            "REDHAT_BUGZILLA_PRODUCT_VERSION=7.3", 
            "REDHAT_SUPPORT_PRODUCT=\"Red Hat Enterprise Linux\"", 
            "REDHAT_SUPPORT_PRODUCT_VERSION=\"7.3\"");
        //@formatter:on
        doReturn(output).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");
    }

    private void setUpSLES() throws Exception {
        setUpLinux();
        //@formatter:off
        List<String> output = Arrays.asList(
            "NAME=\"SLES\"", 
            "VERSION=\"12\"", 
            "VERSION_ID=\"12\"", 
            "PRETTY_NAME=\"SUSE Linux Enterprise Server 12\"", 
            "ID=\"sles\"", 
            "ANSI_COLOR=\"0;32\"", 
            "CPE_NAME=\"cpe:/o:suse:sles:12\"");
        //@formatter:on
        doReturn(output).when(ReportInformation.class, "executeCommand", "cat", "/etc/os-release");
    }
}
