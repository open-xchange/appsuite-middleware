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

import static org.junit.Assert.assertNull;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;

/**
 * {@link TestMailAuthenticityStatusMatrix}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
public class TestMailAuthenticityStatusMatrix extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrix}.
     */
    public TestMailAuthenticityStatusMatrix() {
        super();
    }

    /**
     * (SPF: pass, DKIM: pass, Domain Match: true) -> (Overall Result: pass)
     */
    @Test
    public void testSPFPassDKIMPassWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: pass, Domain Match: false) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFPassDKIMPassWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, Domain Match: true) -> (Overall Result: pass)
     */
    @Test
    public void testSPFPassDKIMNeutralWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: neutral, Domain Match: false) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFPassDKIMNeutralWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, Domain Match: true) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFPassDKIMFailWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: pass, DKIM: fail, Domain Match: false) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFPassDKIMFailWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=pass smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.PASS);
    }

    /**
     * (SPF: neutral, DKIM: pass, Domain Match: true) -> (Overall Result: pass)
     */
    @Test
    public void testSPFNeutralDKIMPassWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: pass, Domain Match: false) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFNeutralDKIMPassWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: neutral, Domain Match: true) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFNeutralDKIMNeutralWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: neutral, Domain Match: false) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFNeutralDKIMNeutralWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: fail, Domain Match: true) -> (Overall Result: neutral)
     */
    @Test
    public void testSPFNeutralDKIMFailWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: neutral, DKIM: fail, Domain Match: false) -> (Overall Result: fail)
     */
    @Test
    public void testSPFNeutralDKIMFailWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=neutral smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.NEUTRAL);
    }

    /**
     * (SPF: fail, DKIM: pass, Domain Match: true) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMPassWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertDomain("example.net", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }

    /**
     * (SPF: fail, DKIM: pass, Domain Match: false) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMPassWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=pass (good signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }

    /**
     * (SPF: fail, DKIM: neutral, Domain Match: true) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMNeutralWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }

    /**
     * (SPF: fail, DKIM: neutral, Domain Match: false) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMNeutralWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=neutral header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.NEUTRAL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }

    /**
     * (SPF: fail, DKIM: fail, Domain Match: true) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMFailWithDomainMatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.net>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }

    /**
     * (SPF: fail, DKIM: fail, Domain Match: false) -> (Overall Result: fail)
     */
    @Test
    public void testSPFFailDKIMFailWithDomainMismatch() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@example.com>");
        perform("ox.io; spf=fail smtp.mailfrom=example.net; dkim=fail (bad signature) header.d=example.net");

        assertStatus(MailAuthenticityStatus.FAIL, result.getStatus());
        assertNull("The domain does not match", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN));
        assertAmount(2);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "example.net", DKIMResult.FAIL);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "example.net", SPFResult.FAIL);
    }
}
