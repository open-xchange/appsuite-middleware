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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link TestMailAuthenticityHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityHandler {

    private MailMessage mailMessage;
    private HeaderCollection headerCollection;
    private ArgumentCaptor<MailAuthenticityResult> argumentCaptor;
    private MailAuthenticityHandler handler;
    private MailAuthenticityResult result;

    /**
     * Initialises a new {@link TestMailAuthenticityHandler}.
     */
    public TestMailAuthenticityHandler() {
        super();
    }

    /**
     * Sets up the test case
     */
    @Before
    public void setUpTest() {
        argumentCaptor = ArgumentCaptor.forClass(MailAuthenticityResult.class);
        headerCollection = new HeaderCollection();

        mailMessage = mock(MailMessage.class);
        when(mailMessage.getHeaders()).thenReturn(headerCollection);

        handler = new MailAuthenticityHandlerImpl();
    }

    /**
     * Tests the trivial case where the <code>Authentication-Results</code>
     * header field is completely absent.
     */
    @Test
    public void testNoHeaderPresent() {
        perform();

        assertEquals("The overall status does not match", MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", null, result.getDomain());
        assertTrue("The mail authenticity mechansisms should be empty", result.getAuthenticityMechanisms().isEmpty());
        assertTrue("The mail authenticity mechansism results should be empty", result.getMailAuthenticityMechanismResults().isEmpty());
    }

    /**
     * Tests the nearly trivial case where the <code>Authentication-Results</code> header field is present,
     * but no actual authenticity was performed on the MTA's side.
     */
    @Test
    public void testWithHeaderPresentButNoAuthenticationDone() {
        perform("example.org 1; none");

        assertEquals("The overall status does not match", MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", "example.org", result.getDomain());
        assertTrue("The mail authenticity mechansisms should be empty", result.getAuthenticityMechanisms().isEmpty());
        assertTrue("The mail authenticity mechansism results should be empty", result.getMailAuthenticityMechanismResults().isEmpty());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via the SPF method.
     */
    @Test
    public void testSPFAuthentication() {
        perform("example.com; spf=pass smtp.mailfrom=example.net");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 1, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 1, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via unknown methods and the SPF method.
     */
    @Test
    public void testSeveralAuthenticationsWithUnknownMethodsAndSPF() {
        perform("example.com; sender-id=pass header.from=example.net", "example.com; auth=pass (cram-md5) smtp.auth=sender@example.net; spf=pass smtp.mailfrom=example.net");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 1, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 1, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs via unknown methods and the DKIM and SPF methods.
     */
    @Test
    public void testSeveralAuthenticationsDifferentMTAs() {
        perform("example.com; auth=pass (cram-md5) smtp.auth=sender@example.com; spf=fail smtp.mailfrom=example.com", "example.com; sender-id=fail header.from=example.com; dkim=pass (good signature) header.d=example.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 1, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertEquals("The mail authenticity mechanism results amount does not match", 1, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "good signature", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs with a multi-tiered authenticity.
     */
    @Test
    public void testMultiTieredAuthenticationDifferentMTAs() {
        perform("example.net; dkim=pass (good signature) header.i=@newyork.example.com", "example.com; dkim=pass reason=\"good signature\" header.i=@mail-router.example.net; dkim=fail reason=\"bad signature\" header.i=@newyork.example.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.FAIL, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 1, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertEquals("The mail authenticity mechanism results amount does not match", 2, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "mail-router.example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "\"good signature\"", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "newyork.example.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "\"bad signature\"", mechanismResult.getReason());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.FAIL.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs failed to authenticate with SPF and DKIM
     */
    @Test
    public void testFailingSPFAndTempErrorDKIM() {
        perform("ox.io; dkim=temperror (no key for signature) header.i=@some.mta.hop header.s=dkim header.b=sl5RAv9n; spf=fail (ox.io: domain of bob@aliceland.com does not designate 1.2.3.4 as permitted sender) smtp.mailfrom=bob@aliceland.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.FAIL, result.getStatus());
        assertEquals("The domain does not match", "ox.io", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 2, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 2, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "some.mta.hop", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "no key for signature", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.TEMPERROR.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "aliceland.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "ox.io: domain of bob@aliceland.com does not designate 1.2.3.4 as permitted sender", mechanismResult.getReason());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.FAIL.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs passed to authenticate with SPF and DMARC
     */
    @Test
    public void testPassDMARCAndSPF() {
        perform("ox.io; spf=pass (ox.io: domain of alice@aliceland.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=Alice@aliceland.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=aliceland.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "ox.io", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 2, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DMARC));
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 2, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "aliceland.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        //assertEquals("The mechanism's reason does not match", "p=NONE sp=NONE dis=NONE", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DMARCResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "aliceland.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "ox.io: domain of alice@aliceland.com designates 1.2.3.4 as permitted sender", mechanismResult.getReason());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs failed to authenticate with DKIM due to a temporary error and passed the SPF validation
     */
    @Test
    public void testDKIMTempErrorAndSPFPass() {
        perform("mx.ox.io; dkim=temperror (no key for signature) header.i=@bobland.com header.s=e header.b=Sw4o2uM4; spf=pass (ox.io: domain of alice@ice.bobland.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=alice@ice.bobland.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "mx.ox.io", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 2, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 2, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "bobland.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "no key for signature", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.TEMPERROR.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "ice.bobland.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "ox.io: domain of alice@ice.bobland.com designates 1.2.3.4 as permitted sender", mechanismResult.getReason());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTA passes the validation of DKIM and ignores all other unknown mechanisms
     */
    @Test
    public void testDKIMPassUnknownMechanisms() {
        perform("mx1.open-xchange.com; dkim=pass reason=\"1024-bit key; unprotected key\" header.d=ox.io header.i=@ox.io header.b=lolhN/LS; dkim-adsp=pass; dkim-atps=neutral");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "mx1.open-xchange.com", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 1, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertEquals("The mail authenticity mechanism results amount does not match", 1, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "ox.io", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "\"1024-bit key; unprotected key\"", mechanismResult.getReason());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTA passes the validation of all known mechanisms. Also tests the correct ordering
     * of the different mechanism results, i.e. DMARC > DKIM > SPF
     */
    @Test
    public void testPassAllKnownMechanisms() {
        perform("mx.ox.io; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; dkim=pass header.i=@foobar.com header.s=201705 header.b=0WC5u+VZ; dkim=pass header.i=@foobar.com header.s=201705 header.b=doOaQjgp; spf=pass (ox.io: domain of jane.doe@foobar.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@foobar.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertEquals("The overall status does not match", MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "mx.ox.io", result.getDomain());
        assertEquals("The mail authenticity mechanisms amount does not match", 3, result.getAuthenticityMechanisms().size());
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DMARC));
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.DKIM));
        assertTrue("The mail authenticity mechansism does not match", result.getAuthenticityMechanisms().contains(MailAuthenticityMechanism.SPF));
        assertEquals("The mail authenticity mechanism results amount does not match", 5, result.getMailAuthenticityMechanismResults().size());

        MailAuthenticityMechanismResult mechanismResult = result.getMailAuthenticityMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "foobar.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticityMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DMARCResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "foobar.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(2);
        assertEquals("The mechanism's domain does not match", "foobar.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(3);
        assertEquals("The mechanism's domain does not match", "foobar.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticityMechanismResults().get(4);
        assertEquals("The mechanism's domain does not match", "foobar.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());
        assertEquals("The mechanism's reason does not match", "ox.io: domain of jane.doe@foobar.com designates 1.2.3.4 as permitted sender", mechanismResult.getReason());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    ///////////////////////////// HELPERS //////////////////////////////
    
    /**
     * Performs the mail authenticity handling with no header
     */
    private void perform() {
        perform(new String[] {});
    }

    /**
     * Performs the mail authenticity handling with the specified headers and
     * captures the result via the {@link ArgumentCaptor} to the 'result' object
     *
     * @param headers The 'Authentication-Results' headers to add
     */
    private void perform(String... headers) {
        for (String header : headers) {
            headerCollection.addHeader(MailAuthenticityHandler.AUTH_RESULTS_HEADER, header);
        }
        handler.handle(mailMessage);
        verify(mailMessage).setAuthenticityResult(argumentCaptor.capture());
        result = argumentCaptor.getValue();
    }
}
