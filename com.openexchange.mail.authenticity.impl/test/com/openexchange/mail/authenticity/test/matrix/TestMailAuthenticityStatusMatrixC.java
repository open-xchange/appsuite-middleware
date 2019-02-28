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

package com.openexchange.mail.authenticity.test.matrix;

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
 * {@link TestMailAuthenticityStatusMatrixC}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityStatusMatrixC extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrixC}.
     */
    public TestMailAuthenticityStatusMatrixC() {
        super();
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: pass (p=none) -> (Overall Result: PASS)
     */
    @Test
    public void testSPFPassDKIMFailDMARCPass() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.PASS, false, "example.net", DMARCResult.PASS, DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: fail (p=reject) -> (Overall Result: FAIL)
     */
    @Test
    public void testSPFPassDKIMFailDMARCFailRejectPolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=reject sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.FAIL, false, "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: fail (p=quarantine) -> (Overall Result: SUSPICIOUS)
     */
    @Test
    public void testSPFPassDKIMFailDMARCFailQuarantinePolicy() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=quarantine sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.SUSPICIOUS, false, "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: fail (p=none), Domain Match: true -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMFailDMARCFailNonePolicyWithDomainMatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, false, "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: fail (p=none), Domain Match: false -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMFailDMARCFailNonePolicyWithDomainMismatch() throws Exception {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.org>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net; dmarc=fail (p=none sp=NONE dis=NONE) header.from=example.net");
        assertResults(3, MailAuthenticityStatus.NEUTRAL, true, "example.net", DMARCResult.FAIL, DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: missing, Domain Match: true) -> (Overall Result: NEUTRAL, Domain Match: true)
     */
    @Test
    public void testSPFPassDKIMFailDMARCMissingWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, false, "example.net", DKIMResult.FAIL, SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, DMARC: missing, Domain Match: false) -> (Overall Result: NEUTRAL, Domain Match: false)
     */
    @Test
    public void testSPFPassDKIMFailDMARCMissingWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");
        assertResults(2, MailAuthenticityStatus.NEUTRAL, true, "example.net", DKIMResult.FAIL, SPFResult.PASS);
    }
}
