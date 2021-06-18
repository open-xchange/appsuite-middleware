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
package com.openexchange.mail.authenticity.test.bugs;

import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.test.AbstractTestMailAuthenticity;

/**
 * {@link BugsTestSuite}
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.6
 */
public class TestMWB900 extends AbstractTestMailAuthenticity{
    
    /**
     * Initialises a new {@link TestMWB900}.
     */
    public TestMWB900() {
        super();
    }

    /**
     * MWB900 Test
     */
    @Test
    public void test() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@aliceland.com>");
        perform("ox.io; dkim=pass header.i=@aliceland.com header.s=201705; spf=pass smtp.mailfrom=aliceland.com; dmarc=pass(p=REJECT) header.from=aliceland.com;");
        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("aliceland.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "aliceland.com", SPFResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "aliceland.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(2), "aliceland.com", DMARCResult.PASS);
    }

}
