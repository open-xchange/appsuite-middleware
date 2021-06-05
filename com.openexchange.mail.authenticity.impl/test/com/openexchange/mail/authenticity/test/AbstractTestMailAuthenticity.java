/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.authenticity.test;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.CustomRuleChecker;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
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
    protected MailAuthenticityResult result;
    protected InternetAddress[] fromAddresses;

    private MailMessage mailMessage;
    private HeaderCollection headerCollection;
    private ArgumentCaptor<MailAuthenticityResult> argumentCaptor;
    private Session session;
    protected LeanConfigurationService leanConfig;
    private MailAuthenticityMetricLogger metricsLogger;
    private TrustedMailService trustedMailService;
    private LeanConfigurationService leanConfigService;

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
    public void setUpTest() {
        argumentCaptor = ArgumentCaptor.forClass(MailAuthenticityResult.class);
        headerCollection = new HeaderCollection();

        session = mock(Session.class);

        when(I(session.getUserId())).thenReturn(I(1));
        when(I(session.getContextId())).thenReturn(I(1));

        trustedMailService = mock(TrustedMailService.class);
        leanConfigService = mock(LeanConfigurationService.class);
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

        handler = new MailAuthenticityHandlerImpl(trustedMailService, services, new CustomRuleChecker(leanConfigService));
    }

    /**
     * Asserts the results
     * 
     * @param expectedAmountOfMechanisms The expected amount of mechanisms in the result
     * @param expectedOverallResult The expected overall {@link MailAuthenticityStatus}
     * @param expectedDomaimMismatch Whether a domain mismatch is expected
     * @param expectedDomain The expected domain name
     * @param expectedAuthenticityMechanismResults The (optional) expected authenticity mechanism results
     */
    protected void assertResults(int expectedAmountOfMechanisms, MailAuthenticityStatus expectedOverallResult, Boolean expectedDomaimMismatch, String expectedDomain, AuthenticityMechanismResult... expectedAuthenticityMechanismResults) {
        assertNotNull("The authenticity result is 'null'.", result);
        assertStatus(expectedOverallResult, result.getStatus());
        assertDomainMismatch(expectedDomaimMismatch, expectedDomain, result);
        assertAmount(expectedAmountOfMechanisms);
        if (expectedAmountOfMechanisms > 0) {
            assertNotNull("The expected authenticity mechanism results is 'null'.", expectedAuthenticityMechanismResults);
        }
        assertEquals("The expected amoount of authenticity mechanism results differs.", expectedAmountOfMechanisms, expectedAuthenticityMechanismResults.length);
        int index = 0;
        for (AuthenticityMechanismResult expectedResult : expectedAuthenticityMechanismResults) {
            assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(index++), expectedDomain, expectedResult);
        }
    }

    /**
     * Asserts whether there is a(n) (optional) domain mismatch expected.
     * 
     * @param expectedDomaimMismatch Whether a domain match is expected. <code>null</code> if it is not part of the result
     * @param expectedDomain The expected domain
     * @param result The mail authenticity result
     */
    private void assertDomainMismatch(Boolean expectedDomaimMismatch, String expectedDomain, MailAuthenticityResult result) {
        if (expectedDomaimMismatch == null) {
            return;
        }
        assertNotNull("The domain mismatch attribute is missing from the result.", result.getAttribute(MailAuthenticityResultKey.DOMAIN_MISMATCH));
        assertEquals("The domain mismatch attribute differs.", expectedDomaimMismatch, result.getAttribute(MailAuthenticityResultKey.DOMAIN_MISMATCH));
        if (b(expectedDomaimMismatch)) {
            assertEquals("The domain does not match.", expectedDomain, result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        }
    }

    /**
     * Asserts that the {@link MailAuthenticityResult} contains the specified amount of results
     *
     * @param amount The amount of results
     */
    protected void assertAmount(int amount) {
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
    protected void assertAuthenticityMechanismResult(MailAuthenticityMechanismResult actualMechanismResult, String expectedDomain, AuthenticityMechanismResult expectedResult) {
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
    protected void assertStatus(MailAuthenticityStatus expected, MailAuthenticityStatus actual) {
        assertEquals("The overall status does not match", expected, actual);
    }

    /**
     * Asserts that the domains are euqla
     *
     * @param expected The expected domain
     * @param actual The actual domain
     */
    protected void assertDomain(String expected, String actual) {
        assertEquals("The domain does not match", expected, actual);
    }

    /**
     * Performs the mail authenticity handling with no header
     */
    protected void perform() {
        perform(new String[] {});
    }

    /**
     * Performs the mail authenticity handling with the specified headers and
     * captures the result via the {@link ArgumentCaptor} to the 'result' object
     *
     * @param headers The 'Authentication-Results' headers to add
     */
    protected void perform(String... headers) {
        for (String header : headers) {
            headerCollection.addHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS, header);
        }
        handler.handle(session, mailMessage);
        verify(mailMessage).setAuthenticityResult(argumentCaptor.capture());
        result = argumentCaptor.getValue();
    }
}
