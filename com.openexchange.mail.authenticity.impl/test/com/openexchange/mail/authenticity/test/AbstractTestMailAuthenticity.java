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

package com.openexchange.mail.authenticity.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.CustomRuleChecker;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
import com.openexchange.mail.authenticity.impl.trusted.Icon;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractTestMailAuthenticity}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractTestMailAuthenticity {

    MailAuthenticityHandlerImpl handler;
    MailAuthenticityResult result;
    InternetAddress[] fromAddresses;

    private MailMessage mailMessage;
    private HeaderCollection headerCollection;
    private ArgumentCaptor<MailAuthenticityResult> argumentCaptor;
    private Session session;
    protected LeanConfigurationService leanConfig;
    private MailAuthenticityMetricLogger metricsLogger;

    /**
     * Initialises a new {@link AbstractTestMailAuthenticity}.
     */
    public AbstractTestMailAuthenticity() {
        super();
    }

    /**
     * Sets up the test case
     */
    @Before
    public void setUpTest() throws Exception {
        argumentCaptor = ArgumentCaptor.forClass(MailAuthenticityResult.class);
        headerCollection = new HeaderCollection();

        session = mock(Session.class);

        when(session.getUserId()).thenReturn(1);
        when(session.getContextId()).thenReturn(1);

        metricsLogger = mock(MailAuthenticityMetricLogger.class);
        leanConfig = mock(LeanConfigurationService.class);
        ServiceLookup services = mock(ServiceLookup.class);
        when(services.getService(MailAuthenticityMetricLogger.class)).thenReturn(metricsLogger);
        when(services.getService(LeanConfigurationService.class)).thenReturn(leanConfig);
        when(leanConfig.getProperty(1, 1, MailAuthenticityProperty.AUTHSERV_ID)).thenReturn("ox.io");

        mailMessage = mock(MailMessage.class);
        when(mailMessage.getHeaders()).thenReturn(headerCollection);

        fromAddresses = new InternetAddress[1];
        when(mailMessage.getFrom()).thenReturn(fromAddresses);

        TrustedMailService doNothingService = new TrustedMailService() {

            @Override
            public void handle(Session session, MailMessage mailMessage) {
                // Nothing
            }

            @Override
            public Icon getIcon(Session session, String uid) throws OXException {
                return null;
            }
        };

        LeanConfigurationService doNothingLeanService = new LeanConfigurationService() {

            @Override
            public String getProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
                return null;
            }

            @Override
            public String getProperty(Property property, Map<String, String> optionals) {
                return null;
            }

            @Override
            public String getProperty(int userId, int contextId, Property property) {
                return null;
            }

            @Override
            public String getProperty(Property property) {
                return null;
            }

            @Override
            public long getLongProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public long getLongProperty(Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public long getLongProperty(int userId, int contextId, Property property) {
                return 0;
            }

            @Override
            public long getLongProperty(Property property) {
                return 0;
            }

            @Override
            public int getIntProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public int getIntProperty(Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public int getIntProperty(int userId, int contextId, Property property) {
                return 0;
            }

            @Override
            public int getIntProperty(Property property) {
                return 0;
            }

            @Override
            public float getFloatProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public float getFloatProperty(Property property, Map<String, String> optionals) {
                return 0;
            }

            @Override
            public float getFloatProperty(int userId, int contextId, Property property) {
                return 0;
            }

            @Override
            public float getFloatProperty(Property property) {
                return 0;
            }

            @Override
            public boolean getBooleanProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
                return false;
            }

            @Override
            public boolean getBooleanProperty(Property property, Map<String, String> optionals) {
                return false;
            }

            @Override
            public boolean getBooleanProperty(int userId, int contextId, Property property) {
                return false;
            }

            @Override
            public boolean getBooleanProperty(Property property) {
                return false;
            }
        };

        handler = new MailAuthenticityHandlerImpl(doNothingService, services, new CustomRuleChecker(doNothingLeanService));
    }

    /**
     * Asserts that the {@link MailAuthenticityResult} contains the specified amount of results
     *
     * @param amount The amount of results
     */
    void assertAmount(int amount) {
        assertEquals("The mail authenticity mechanism results amount does not match", amount, result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).size());
    }

    /**
     * Asserts that the unconsidered {@link MailAuthenticityResult} contains the specified amount of results
     *
     * @param amount The amount of results
     */
    void assertUnconsideredAmount(int amount) {
        assertEquals("The mail authenticity unconsidered results amount does not match", amount, result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).size());
    }

    /**
     * Asserts that the specified {@link MailAuthenticityMechanismResult} contains the expected domain and status result
     *
     * @param actualMechanismResult The {@link MailAuthenticityMechanismResult}
     * @param expectedDomain The expected domain
     * @param expectedResult The expected result
     */
    void assertAuthenticityMechanismResult(MailAuthenticityMechanismResult actualMechanismResult, String expectedDomain, AuthenticityMechanismResult expectedResult) {
        assertEquals("The mechanism's domain does not match", expectedDomain, actualMechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", actualMechanismResult.getResult());
        AuthenticityMechanismResult s = actualMechanismResult.getResult();
        assertEquals("The mechanism's result does not match", expectedResult.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Asserts that the specified {@link MailAuthenticityMechanismResult} contains the expected domain, reason and status result
     *
     * @param actualMechanismResult The {@link MailAuthenticityMechanismResult}
     * @param expectedDomain The expected domain
     * @param expectedReason The expected reason
     * @param expectedResult The expected result
     */
    void assertAuthenticityMechanismResult(MailAuthenticityMechanismResult actualMechanismResult, String expectedDomain, String expectedReason, AuthenticityMechanismResult expectedResult) {
        assertAuthenticityMechanismResult(actualMechanismResult, expectedDomain, expectedResult);
        assertEquals("The mechanism's reason does not match", expectedReason, actualMechanismResult.getReason());
    }

    /**
     * Asserts that the objets are equal
     *
     * @param expected The expected {@link MailAuthenticityStatus}
     * @param actual The actual {@link MailAuthenticityStatus}
     */
    void assertStatus(MailAuthenticityStatus expected, MailAuthenticityStatus actual) {
        assertEquals("The overall status does not match", expected, actual);
    }

    /**
     * Asserts that the domains are euqla
     *
     * @param expected The expected domain
     * @param actual The actual domain
     */
    void assertDomain(String expected, String actual) {
        assertEquals("The domain does not match", expected, actual);
    }

    /**
     * Performs the mail authenticity handling with no header
     */
    void perform() {
        perform(new String[] {});
    }

    /**
     * Performs the mail authenticity handling with the specified headers and
     * captures the result via the {@link ArgumentCaptor} to the 'result' object
     *
     * @param headers The 'Authentication-Results' headers to add
     */
    void perform(String... headers) {
        for (String header : headers) {
            headerCollection.addHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS, header);
        }
        handler.handle(session, mailMessage);
        verify(mailMessage).setAuthenticityResult(argumentCaptor.capture());
        result = argumentCaptor.getValue();
    }
}
