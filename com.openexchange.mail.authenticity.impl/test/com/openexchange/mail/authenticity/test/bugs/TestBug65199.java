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
 * {@link TestBug65199}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class TestBug65199 extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link TestBug65199}.
     */
    public TestBug65199() {
        super();
    }

    /**
     * Teeeeeest
     */
    @Test
    public void test() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@aliceland.com>");
        perform("ox.io; spf=pass (ox.io: domain of alice@aliceland.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=@aliceland.com; dkim=pass header.i=@aliceland.com header.s=sh1244 header.b=nhkmxmg1; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=aliceland.com");

        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("aliceland.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);

        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "aliceland.com", SPFResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "aliceland.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(2), "aliceland.com", DMARCResult.PASS);

    }

}
