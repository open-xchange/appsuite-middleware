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

import static com.openexchange.java.Autoboxing.b;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;

/**
 * {@link TestMailAuthenticityHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityHandler extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityHandler}.
     */
    public TestMailAuthenticityHandler() {
        super();
    }

    /**
     * Tests the trivial case where the <code>Authentication-Results</code>
     * header field is completely absent.
     */
    @Test
    public void testNoHeaderPresent() {
        perform();

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", null, result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertTrue("The mail authenticity mechansism results should be null", result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class) == null);
    }

    /**
     * Tests the nearly trivial case where the <code>Authentication-Results</code> header field is present,
     * but no actual authenticity was performed on the MTA's side.
     */
    @Test
    public void testWithHeaderPresentButNoAuthenticationDone() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.org>");
        perform("ox.io 1; none");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertTrue("The mail authenticity mechansism results should be empty", result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).isEmpty());
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via the SPF method.
     */
    @Test
    public void testSPFAuthentication() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(1);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", SPFResult.PASS);
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by the MTA via unknown methods and the SPF method.
     */
    @Test
    public void testSeveralAuthenticationsWithUnknownMethodsAndSPF() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; sender-id=pass header.from=example.net", "ox.io; auth=pass (cram-md5) smtp.auth=sender@example.net; spf=pass smtp.mailfrom=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(1);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", SPFResult.PASS);

        Map<String, String> unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(0);
        Map<String, String> expectedUnknownMech = new HashMap<>();
        expectedUnknownMech.put("result", "pass");
        expectedUnknownMech.put("reason", "cram-md5");
        expectedUnknownMech.put("mechanism", "auth");
        expectedUnknownMech.put("smtp.auth", "sender@example.net");
        assertEquals(expectedUnknownMech, unknownMech);

        unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(1);
        expectedUnknownMech.clear();
        expectedUnknownMech.put("result", "pass");
        expectedUnknownMech.put("mechanism", "sender-id");
        expectedUnknownMech.put("header.from", "example.net");
        assertEquals(expectedUnknownMech, unknownMech);

    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs via unknown methods and the DKIM and SPF methods.
     */
    @Test
    public void testSeveralAuthenticationsDifferentMTAs() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; auth=pass (cram-md5) smtp.auth=sender@example.com; spf=fail smtp.mailfrom=example.com", "ox.io; sender-id=fail header.from=example.com; dkim=pass (good signature) header.d=example.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("example.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.com", "Pass with domain example.com", DKIMResult.PASS);

        Map<String, String> unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(0);
        Map<String, String> expectedUnknownMech = new HashMap<>();
        expectedUnknownMech.put("result", "fail");
        expectedUnknownMech.put("mechanism", "sender-id");
        expectedUnknownMech.put("header.from", "example.com");
        assertEquals(expectedUnknownMech, unknownMech);

        unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(1);
        expectedUnknownMech.clear();
        expectedUnknownMech.put("result", "pass");
        expectedUnknownMech.put("mechanism", "auth");
        expectedUnknownMech.put("reason", "cram-md5");
        expectedUnknownMech.put("smtp.auth", "sender@example.com");
        assertEquals(expectedUnknownMech, unknownMech);
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * and the message was authenticated by different MTAs with a multi-tiered authenticity.
     */
    @Test
    public void testMultiTieredAuthenticationDifferentMTAs() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@newyork.example.com>");
        perform("ox.io; dkim=pass (good signature) header.i=@newyork.example.com header.d=@example.com", "ox.io; dkim=pass reason=\"good signature\" header.i=@mail-router.example.net; dkim=fail reason=\"bad signature\" header.i=@newyork.example.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", "newyork.example.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(1);
        assertUnconsideredAmount(0);

        // Assert considered
        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "newyork.example.com", "Pass with domain newyork.example.com", DKIMResult.PASS);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs failed to authenticate with SPF and DKIM
     */
    @Test
    public void testFailingSPFAndTempErrorDKIM() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <bob@aliceland.com>");
        perform("ox.io; dkim=temperror (no key for signature) header.i=@aliceland.com header.s=dkim header.b=sl5RAv9n; spf=fail (ox.io: domain of bob@aliceland.com does not designate 1.2.3.4 as permitted sender) smtp.mailfrom=bob@aliceland.com");

        assertStatus(MailAuthenticityStatus.SUSPICIOUS, result.getStatus());
        //assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "aliceland.com", "Temporary Error with domain aliceland.com", DKIMResult.TEMPERROR);
        assertAuthenticityMechanismResult(results.get(1), "aliceland.com", "Fail with domain aliceland.com", SPFResult.FAIL);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs passed to authenticate with SPF and DMARC
     */
    @Test
    public void testPassDMARCAndSPF() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <alice@aliceland.com>");
        perform("ox.io; spf=pass (ox.io: domain of alice@aliceland.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=Alice@aliceland.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=aliceland.com");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("aliceland.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "aliceland.com", "Pass", DMARCResult.PASS);
        assertAuthenticityMechanismResult(results.get(1), "aliceland.com", "Pass with domain aliceland.com", SPFResult.PASS);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTAs failed to authenticate with DKIM due to a temporary error and passed the SPF validation
     */
    @Test
    public void testDKIMTempErrorAndSPFPass() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <alice@ice.bobland.com>");
        perform("ox.io; dkim=temperror (no key for signature) header.i=@ice.bobland.com header.s=e header.b=Sw4o2uM4; spf=pass (ox.io: domain of alice@ice.bobland.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=alice@ice.bobland.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("ice.bobland.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "ice.bobland.com", "Temporary Error with domain ice.bobland.com", DKIMResult.TEMPERROR);
        assertAuthenticityMechanismResult(results.get(1), "ice.bobland.com", "Pass with domain ice.bobland.com", SPFResult.PASS);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTA passes the validation of DKIM and ignores all other unknown mechanisms
     */
    @Test
    public void testDKIMPassUnknownMechanisms() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@ox.io>");
        perform("ox.io; dkim=pass reason=\"1024-bit key; unprotected key\" header.d=ox.io header.i=@ox.io header.b=lolhN/LS; dkim-adsp=pass; dkim-atps=neutral");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("ox.io", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(1);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "ox.io", "Pass with domain ox.io", DKIMResult.PASS);

        Map<String, String> unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(0);
        Map<String, String> expectedUnknownMech = new HashMap<>();
        expectedUnknownMech.put("result", "pass");
        expectedUnknownMech.put("mechanism", "dkim-adsp");
        assertEquals(expectedUnknownMech, unknownMech);

        unknownMech = (Map) result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).get(1);
        expectedUnknownMech.clear();
        expectedUnknownMech.put("result", "neutral");
        expectedUnknownMech.put("mechanism", "dkim-atps");
        assertEquals(expectedUnknownMech, unknownMech);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the MTA passes the validation of all known mechanisms. Also tests the correct ordering
     * of the different mechanism results, i.e. DMARC > DKIM > SPF
     */
    @Test
    public void testPassAllKnownMechanisms() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        perform("ox.io; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; dkim=pass header.i=@foobar.com header.s=201705 header.b=0WC5u+VZ; dkim=pass header.i=@foobar.com header.s=201705 header.b=doOaQjgp; spf=pass (ox.io: domain of jane.doe@foobar.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@foobar.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(3);
        assertUnconsideredAmount(0);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.PASS);
        assertAuthenticityMechanismResult(results.get(1), "foobar.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult(results.get(2), "foobar.com", SPFResult.PASS);
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * has valid mechanisms but the <code>authserv-id</code> is not in the configured allowed list.
     */
    @Test
    public void testNotValidAuthServId() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        perform("some-auth-servId; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());

        assertTrue("The from domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN) == null);
        assertTrue("The mail authentication mechanism results do not match", result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).isEmpty());
        assertTrue("The unknown mail authentication mechanism results do not match", result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).isEmpty());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * has valid mechanisms but the <code>authserv-id</code> is absent.
     */
    @Test
    public void testAbsentAuthServId() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        perform("; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());

        assertTrue("The from domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN) == null);
        assertTrue("The mail authentication mechanism results do not match", result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).isEmpty());
        assertTrue("The unknown mail authentication mechanism results do not match", result.getAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, List.class).isEmpty());
    }

    /**
     * Tests the real world case where the <code>Authentication-Results</code> header field is present
     * and the DMARC and DKIM delivered a 'none' status and the SPF passed.
     */
    @Test
    public void testDMARCNoneDKIMNoneSPFPass() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        perform("ox.io; dkim=none header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; spf=pass (ox.io: domain of jane.doe@foobar.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@foobar.com; dmarc=none (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertDomain("foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.NONE);
        assertAuthenticityMechanismResult(results.get(1), "foobar.com", DKIMResult.NONE);
        assertAuthenticityMechanismResult(results.get(2), "foobar.com", "Pass with domain foobar.com", SPFResult.PASS);
    }

    /**
     * Tests the edge case where the <code>Authentication-Results</code> header field is present
     * and the DMARC passed but the <code>From</code> header has a different domain as in DMARC.
     */
    @Test
    public void testDMARCWithMismatchingFromHeader() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@some.foobar.com>");
        perform("ox.io; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(1);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.PASS);
    }

    /**
     * Tests the edge case where the <code>Authentication-Results</code> header field is present
     * and one DMARC passed one failed but the <code>From</code> header has a different domain as in
     * the passing DMARC.
     */
    @Test
    public void testMultipleDMARCWithOneMismatchingFromHeader() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@some.foobar.com>");
        perform("ox.io; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=some.foobar.com");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertEquals("The domain does not match", "foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(1);
        assertUnconsideredAmount(0);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.PASS);
    }

    /**
     * Tests the case where the <code>Authentication-Results</code> header field is present
     * the DMARC and DKIM passes but the SPF fails.
     */
    @Test
    public void testDMARCPassDKIMPassSPFFail() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        perform("ox.io; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=foobar.com; spf=fail smtp.mailfrom=foobar.com; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.PASS);
        assertAuthenticityMechanismResult(results.get(1), "foobar.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult(results.get(2), "foobar.com", SPFResult.FAIL);
    }

    /**
     * Tests an extreme edge case where the <code>Authentication-Results</code> header field is present
     * and every mechanism is present twice.
     */
    @Test
    public void testDuplicateAllMechanisms() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@foobar.com>");
        //@formatter:off
        perform("ox.io; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=foobar.com; spf=fail smtp.mailfrom=foobar.com; " + 
            "dmarc=pass (p=NONE sp=NONE dis=REJECT) header.from=foobar.com; dkim=pass header.i=@foobar.com header.s=201705 header.b=VvWVD9kg; " + 
            "dkim=fail header.i=@foobar.com header.s=201705 header.b=0WC5u+VZ; spf=pass (ox.io: domain of jane.doe@foobar.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@foobar.com; ");
        //@formatter:on

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);
        assertUnconsideredAmount(0);

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "foobar.com", DMARCResult.PASS);
        assertAuthenticityMechanismResult(results.get(1), "foobar.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult(results.get(2), "foobar.com", SPFResult.PASS);
    }

    /**
     * Tests case sensitive domain checks
     */
    @Test
    public void testCaseSensitiveDomain() throws AddressException {
        fromAddresses[0] = new InternetAddress("John Doe <John.Doe@fooBAR.com>");
        //@formatter:off
        perform("ox.io; dkim=fail reason=\"key not found in DNS\" (0-bit key; unprotected) header.d=some.thirdparty.com header.i=@some.thirdparty.com header.b=\"yYNhMf8S\"; dkim-atps=neutral",
            "ox.io; spf=pass (mailfrom) smtp.mailfrom=FoObAr.cOM (client-ip=1.2.3.4; helo=another.third.party.domain.com; envelope-from=john.doe@foobar.com; receiver=<UNKNOWN>",
            "ox.io; dmarc=none (p=none dis=none) header.from=fooBAR.com");
        //@formatter:on

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertEquals("The domain does not match", "foobar.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN).toString().toLowerCase());
        assertFalse("No domain mismatch was expected", b(result.getAttribute(MailAuthenticityResultKey.DOMAIN_MISMATCH, Boolean.class)));

        List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        assertAuthenticityMechanismResult(results.get(0), "fooBAR.com", DMARCResult.NONE);
        assertAuthenticityMechanismResult(results.get(1), "some.thirdparty.com", DKIMResult.FAIL);
        assertAuthenticityMechanismResult(results.get(2), "FoObAr.cOM", SPFResult.PASS);
    }
    
    /**
     * Tests the DMARC 'reject' policy
     */
    @Test
    public void testDMARCRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("John Doe <jOhN.d0e@foo.com>");
        perform("ox.io; dmarc=fail (p=reJecT sp=NONE dis=NONE) header.from=foo.com; spf=fail smtp.mailfrom=foo.com; dkim=pass header.i=@foo.com header.s=201705 header.b=VvWVD9kg");
        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
    }
    
    /**
     * Tests the DMARC 'quarantine' policy
     */
    @Test
    public void testDMARCQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("John Doe <jOhN.d0e@foo.com>");
        perform("ox.io; dmarc=fail (p=QuaRANTine sp=NONE dis=NONE) header.from=foo.com; spf=fail smtp.mailfrom=foo.com; dkim=pass header.i=@foo.com header.s=201705 header.b=VvWVD9kg");
        assertStatus(MailAuthenticityStatus.SUSPICIOUS, result.getStatus());
    }
    
    /**
     * Tests the DMARC 'none' policy
     */
    @Test
    public void testDMARCNonePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("John Doe <jOhN.d0e@foo.com>");
        perform("ox.io; dmarc=fail (p=NOne sp=NONE dis=NONE) header.from=foo.com; spf=fail smtp.mailfrom=foo.com; dkim=pass header.i=@foo.com header.s=201705 header.b=VvWVD9kg");
        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
    }
}
