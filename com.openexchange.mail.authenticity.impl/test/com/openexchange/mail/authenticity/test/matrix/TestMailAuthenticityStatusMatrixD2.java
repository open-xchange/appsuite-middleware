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

package com.openexchange.mail.authenticity.test.matrix;

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
 * {@link TestMailAuthenticityStatusMatrixD2}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityStatusMatrixD2 extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrixD2}.
     */
    public TestMailAuthenticityStatusMatrixD2() {
        super();
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dmarc=fail (p=reject sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.FAIL, null, "example.net", DMARCResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: missing, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFMissingDKIMMissingDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dmarc=fail (p=reject sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(1, MailAuthenticityStatus.FAIL, null, "example.net", DMARCResult.FAIL);
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: fail (p=quarantine) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dmarc=fail (p=quarantine sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.SUSPICIOUS, null, "example.net", DMARCResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: missing, DMARC: fail (p=quarantine) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFMissingDKIMMissingDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dmarc=fail (p=quarantine sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(1, MailAuthenticityStatus.SUSPICIOUS, null, "example.net", DMARCResult.FAIL);
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCFailNonePolicyWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DMARCResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCFailNonePolicyWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DMARCResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: neutral, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMNeutralDMARCFailNonePolicyWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DMARCResult.FAIL, DKIMResult.NEUTRAL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: neutral, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMNeutralDMARCFailNonePolicyWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DMARCResult.FAIL, DKIMResult.NEUTRAL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: missing, DMARC: fail (p=none) -> (Overall Result: NEUTRAL)
     */
    @Test
    public void testSPFMissingDKIMMissingDMARCFailNonePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, null, "example.net", DMARCResult.FAIL);
    }
    
    /**
     * (SPF: missing, DKIM: neutral, DMARC: fail (p=none) -> (Overall Result: NEUTRAL)
     */
    @Test
    public void testSPFMissingDKIMNeutralDMARCFailNonePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=neutral header.d=example.net; dmarc=fail (p=none sp=NEUTRAL dis=NEUTRAL) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, null, "example.net", DMARCResult.FAIL, DKIMResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: missing, Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCMissingWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", SPFResult.NEUTRAL);
    }
    
    /**
     * (SPF: neutral, DKIM: neutral, DMARC: missing, Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMNeutralDMARCMissingWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DKIMResult.NEUTRAL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: missing, DMARC: missing, Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMMissingDMARCMissingWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", SPFResult.NEUTRAL);
    }
    
    /**
     * (SPF: neutral, DKIM: neutral, DMARC: missing, Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMNeutralDMARCMissingWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DKIMResult.NEUTRAL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: missing, DMARC: missing -> (Overall Result: NEUTRAL)
     */
    @Test
    public void testSPFMissingDKIMMissingDMARCMissing() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io");
        assertResults(0, MailAuthenticityStatus.NEUTRAL, null, "example.net");
    }
    
    /**
     * (SPF: missing, DKIM: neutral, DMARC: missing -> (Overall Result: NEUTRAL)
     */
    @Test
    public void testSPFMissingDKIMNeutralDMARCMissing() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=neutral header.d=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, null, "example.net", DKIMResult.NEUTRAL);
    }
}
