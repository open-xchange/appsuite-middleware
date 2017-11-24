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
import com.openexchange.mail.authenticity.MailAuthenticationHandler;
import com.openexchange.mail.authenticity.common.MailAuthenticationStatus;
import com.openexchange.mail.authenticity.common.mechanism.AuthenticationMechanismResult;
import com.openexchange.mail.authenticity.common.mechanism.MailAuthenticationMechanism;
import com.openexchange.mail.authenticity.common.mechanism.MailAuthenticationMechanismResult;
import com.openexchange.mail.authenticity.common.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.common.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.internal.MailAuthenticationHandlerImpl;
import com.openexchange.mail.dataobjects.MailAuthenticationResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link TestMailAuthenticationHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticationHandler {

    public static void main(String[] args) {
        //String[] authHeaders = { "mx.xyz.com; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=VvWVD9kg; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=0WC5u+VZ; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=doOaQjgp; spf=pass (xyz.com: domain of jane.doe@open-xchange.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@open-xchange.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=open-xchange.com" };
        String[] authHeaders = { "mx1.open-xchange.com; dkim=pass reason=\"1024-bit key; unprotected key\" header.d=ox.io header.i=@ox.io header.b=lolhN/LS; dkim-adsp=pass; dkim-atps=neutral" };
        MailAuthenticationHandlerImpl m = new MailAuthenticationHandlerImpl();
        //MailAuthenticationResult r = m.parseHeaders(authHeaders);
        //System.err.println(r);

        String b = "spf=pass (xyz.com: domain of jane.doe@open-xchange.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@open-xchange.com";
        String a = "dkim=temperror (no key for signature) header.i=@foo.com header.s=e header.b=Sw4o2uM4";
        String c = "blah";
        String d = "key=value key2=value2 key3=value3";

        //System.out.println(m.parseLine(a));
        //System.out.println(m.parseLine(b));
        //System.out.println(m.parseLine(c));
        //System.out.println(m.parseLine(d));
    }

    private MailMessage mailMessage;
    private HeaderCollection headerCollection;
    private ArgumentCaptor<MailAuthenticationResult> argumentCaptor;
    private MailAuthenticationHandler handler;
    private MailAuthenticationResult result;

    @Before
    public void setUpTest() {
        argumentCaptor = ArgumentCaptor.forClass(MailAuthenticationResult.class);
        headerCollection = new HeaderCollection();

        mailMessage = mock(MailMessage.class);
        when(mailMessage.getHeaders()).thenReturn(headerCollection);

        handler = new MailAuthenticationHandlerImpl();
    }

    /**
     * Tests the trivial case where the <code>Authentication-Results</code>
     * header field is completely absent.
     */
    @Test
    public void testNoHeaderPresent() {
        perform();

        assertEquals("The overall status does not match", MailAuthenticationStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", null, result.getDomain());
        assertTrue("The mail authentication mechansisms should be empty", result.getAuthenticationMechanisms().isEmpty());
        assertTrue("The mail authentication mechansism results should be empty", result.getMailAuthenticationMechanismResults().isEmpty());
    }

    /**
     * Tests the nearly trivial case where the <code>Authentication-Results</code> header field is present,
     * but no actual authentication was performed on the MTA's side.
     */
    @Test
    public void testWithHeaderPresentButNoAuthenticationDone() {
        perform("example.org 1; none");

        assertEquals("The overall status does not match", MailAuthenticationStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", "example.org", result.getDomain());
        assertTrue("The mail authentication mechansisms should be empty", result.getAuthenticationMechanisms().isEmpty());
        assertTrue("The mail authentication mechansism results should be empty", result.getMailAuthenticationMechanismResults().isEmpty());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via the SPF method.
     */
    @Test
    public void testSPFAuthentication() {
        perform("example.com; spf=pass smtp.mailfrom=example.net");

        assertEquals("The overall status does not match", MailAuthenticationStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authentication mechanisms amount does not match", 1, result.getAuthenticationMechanisms().size());
        assertTrue("The mail authentication mechansism does not match", result.getAuthenticationMechanisms().contains(MailAuthenticationMechanism.SPF));
        assertEquals("The mail authentication mechanism results amount does not match", 1, result.getMailAuthenticationMechanismResults().size());

        MailAuthenticationMechanismResult mechanismResult = result.getMailAuthenticationMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticationMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via unknown methods and the SPF method.
     */
    @Test
    public void testSeveralAuthenticationsWithUnknownMethodsAndSPF() {
        perform("example.com; sender-id=pass header.from=example.net", "example.com; auth=pass (cram-md5) smtp.auth=sender@example.net; spf=pass smtp.mailfrom=example.net");

        assertEquals("The overall status does not match", MailAuthenticationStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authentication mechanisms amount does not match", 1, result.getAuthenticationMechanisms().size());
        assertTrue("The mail authentication mechansism does not match", result.getAuthenticationMechanisms().contains(MailAuthenticationMechanism.SPF));
        assertEquals("The mail authentication mechanism results amount does not match", 1, result.getMailAuthenticationMechanismResults().size());

        MailAuthenticationMechanismResult mechanismResult = result.getMailAuthenticationMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticationMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", SPFResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs via unknown methods and the DKIM and SPF methods.
     */
    @Test
    public void testSeveralAuthenticationsDifferentMTAs() {
        perform("example.com; auth=pass (cram-md5) smtp.auth=sender@example.com; spf=fail smtp.mailfrom=example.com", "example.com; sender-id=fail header.from=example.com; dkim=pass (good signature) header.d=example.com");

        assertEquals("The overall status does not match", MailAuthenticationStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authentication mechanisms amount does not match", 1, result.getAuthenticationMechanisms().size());
        assertTrue("The mail authentication mechansism does not match", result.getAuthenticationMechanisms().contains(MailAuthenticationMechanism.DKIM));
        assertEquals("The mail authentication mechanism results amount does not match", 1, result.getMailAuthenticationMechanismResults().size());

        MailAuthenticationMechanismResult mechanismResult = result.getMailAuthenticationMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "example.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticationMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs with a multi-tiered authentication.
     */
    @Test
    public void testMultiTieredAuthenticationDifferentMTAs() {
        perform("example.net; dkim=pass (good signature) header.i=@newyork.example.com", "example.com; dkim=pass reason=\"good signature\" header.i=@mail-router.example.net; dkim=fail reason=\"bad signature\" header.i=@newyork.example.com");

        assertEquals("The overall status does not match", MailAuthenticationStatus.FAIL, result.getStatus());
        assertEquals("The domain does not match", "example.com", result.getDomain());
        assertEquals("The mail authentication mechanisms amount does not match", 1, result.getAuthenticationMechanisms().size());
        assertTrue("The mail authentication mechansism does not match", result.getAuthenticationMechanisms().contains(MailAuthenticationMechanism.DKIM));
        assertEquals("The mail authentication mechanism results amount does not match", 2, result.getMailAuthenticationMechanismResults().size());

        MailAuthenticationMechanismResult mechanismResult = result.getMailAuthenticationMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", "mail-router.example.net", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticationMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.PASS.getTechnicalName(), s.getTechnicalName());

        mechanismResult = result.getMailAuthenticationMechanismResults().get(1);
        assertEquals("The mechanism's domain does not match", "newyork.example.com", mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.FAIL.getTechnicalName(), s.getTechnicalName());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the attributes are heavily loaded with comments
     */
    @Test
    public void testCommentHeavy() {
        perform("foo.example.net (foobar) 1 (baz); dkim (Because I like it) / 1 (One yay) = (wait for it) fail policy (A dot can go here) . (like that) expired (this surprised me) = (as I wasn't expecting it) 1362471462");

        assertEquals("The overall status does not match", MailAuthenticationStatus.FAIL, result.getStatus());
        assertEquals("The domain does not match", "foo.example.net", result.getDomain());
        assertEquals("The mail authentication mechanisms amount does not match", 1, result.getAuthenticationMechanisms().size());
        assertTrue("The mail authentication mechansism does not match", result.getAuthenticationMechanisms().contains(MailAuthenticationMechanism.DKIM));
        assertEquals("The mail authentication mechanism results amount does not match", 1, result.getMailAuthenticationMechanismResults().size());

        MailAuthenticationMechanismResult mechanismResult = result.getMailAuthenticationMechanismResults().get(0);
        assertEquals("The mechanism's domain does not match", null, mechanismResult.getDomain());
        assertNotNull("The mechanism's result is null", mechanismResult.getResult());

        AuthenticationMechanismResult s = mechanismResult.getResult();
        assertEquals("The mechanism's result does not match", DKIMResult.FAIL.getTechnicalName(), s.getTechnicalName());
    }

    ///////////////////////////// HELPERS //////////////////////////////

    /**
     * Performs the mail authentication handling with no header
     */
    private void perform() {
        perform(new String[] {});
    }

    /**
     * Performs the mail authentication handling with the specified headers and
     * captures the result via the {@link ArgumentCaptor} to the 'result' object
     *
     * @param headers The 'Authentication-Results' headers to add
     */
    private void perform(String... headers) {
        for (String header : headers) {
            headerCollection.addHeader(MailAuthenticationHandler.AUTH_RESULTS_HEADER, header);
        }
        // TODO use proper session
        handler.handle(null, mailMessage);
        verify(mailMessage).setAuthenticationResult(argumentCaptor.capture());
        result = argumentCaptor.getValue();
    }
}
