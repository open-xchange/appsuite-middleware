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

package com.openexchange.mail.authenticity.test.matrix_v2;

import static com.openexchange.java.Autoboxing.B;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.test.AbstractTestMailAuthenticity;

/**
 * {@link TestMailAuthenticityStatusMatrixB}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityStatusMatrixB extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrixB}.
     */
    public TestMailAuthenticityStatusMatrixB() {
        super();
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: pass (p=none) -> (Overall Result: PASS)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCPass() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dmarc=pass (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.PASS, B(false), "example.net", DMARCResult.PASS, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dmarc=fail (p=reject sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.FAIL, B(false), "example.net", DMARCResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: fail (p=quarantine) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dmarc=fail (p=quarantine sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.SUSPICIOUS, B(false), "example.net", DMARCResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCFailNonePolicyWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DMARCResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: fail (p=none), Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCFailNonePolicyWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DMARCResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, DMARC: fail (p=none), Domain Match: true) -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMNeutralDMARCFailNonePolicyWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DMARCResult.FAIL, DKIMResult.NEUTRAL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, DMARC: fail (p=none), Domain Match: false) -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMNeutralDMARCFailNonePolicyWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net; dmarc=fail (p=NONE sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DMARCResult.FAIL, DKIMResult.NEUTRAL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: missing, Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCMissingDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: missing, DMARC: missing, Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMMissingDMARCMissingDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, DMARC: missing, Domain Match: true) -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMNeutralDMARCMissingWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DKIMResult.NEUTRAL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, DMARC: missing, Domain Match: false) -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMNeutralDMARCMissingWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DKIMResult.NEUTRAL, SPFResult.PASS);
    }
}
