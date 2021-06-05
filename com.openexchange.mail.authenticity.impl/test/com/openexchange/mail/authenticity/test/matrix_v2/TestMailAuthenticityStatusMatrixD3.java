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
 * {@link TestMailAuthenticityStatusMatrixD3}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityStatusMatrixD3 extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrixD3}.
     */
    public TestMailAuthenticityStatusMatrixD3() {
        super();
    }

    /**
     * (SPF: neutral, DKIM: fail, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=reject sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.FAIL, B(false), "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: fail, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=reject sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.FAIL, null, "example.net", DMARCResult.FAIL, DKIMResult.FAIL);
    }

    /**
     * (SPF: neutral, DKIM: fail, DMARC: fail (p=reject) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=quarantine sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.SUSPICIOUS, B(false), "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: fail, DMARC: fail (p=reject) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=quarantine sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.SUSPICIOUS, null, "example.net", DMARCResult.FAIL, DKIMResult.FAIL);
    }

    /**
     * (SPF: neutral, DKIM: fail, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCFailNonePolicyWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: fail, DMARC: fail (p=none), Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCFailNonePolicyWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: missing, DKIM: fail, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCFailNonePolicyWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, null, "example.net", DMARCResult.FAIL, DKIMResult.FAIL);
    }

    /**
     * (SPF: missing, DKIM: fail, DMARC: fail (p=none), Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCFailNonePolicyWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, null, "example.net", DMARCResult.FAIL, DKIMResult.FAIL);
    }

    /**
     * (SPF: neutral, DKIM: fail, DMARC: missing, Domain Match: true) -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCMissingWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(false), "example.net", DKIMResult.FAIL, SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: fail, DMARC: missing, Domain Match: false) -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFNeutralDKIMFailDMARCMissingWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, B(true), "example.net", DKIMResult.FAIL, SPFResult.NEUTRAL);
    }
    
    /**
     * (SPF: missing, DKIM: fail, DMARC: missing, Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCMissingWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, null, "example.net", DKIMResult.FAIL);
    }
    
    /**
     * (SPF: missing, DKIM: fail, DMARC: missing, Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFMissingDKIMFailDMARCMissingWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; dkim=fail (bad signature) header.d=example.net");
        assertResults(1, MailAuthenticityStatus.NEUTRAL, null, "example.net", DKIMResult.FAIL);
    }
}
