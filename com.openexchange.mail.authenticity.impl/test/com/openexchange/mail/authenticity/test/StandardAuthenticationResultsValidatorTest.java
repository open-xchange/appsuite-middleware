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

import java.util.Arrays;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.AuthenticationResultsValidator;
import com.openexchange.mail.authenticity.impl.core.StandardAuthenticationResultsValidator;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link StandardAuthenticationResultsValidatorTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StandardAuthenticationResultsValidatorTest extends AbstractTestMailAuthenticity {

    /**
     * Initialises a new {@link StandardAuthenticationResultsValidatorTest}.
     */
    public StandardAuthenticationResultsValidatorTest() {
        super();
    }

    @Test
    public void testCorrectOverallResult() throws Exception {
        AuthenticationResultsValidator validator = StandardAuthenticationResultsValidator.getInstance();

        List<String> authHeaders = Arrays.asList(
            "open-xchange.com; dkim=pass (1024-bit key; unprotected) header.d=foreigner.com header.i=rootserver@hosting.foreigner.com",
            "open-xchange.com; spf=none (mailfrom) smtp.mailfrom=bounce.superforeigner.com (client-ip=212.227.126.222; helo=mbulk.foreigner.com; envelope-from=99562902061843288.2.2030025036@bounce.superforeigner.com; receiver=<UNKNOWN>)",
            "open-xchange.com; dmarc=none (p=none dis=none) header.from=hosting.foreigner.com"
        );
        List<AllowedAuthServId> allowedAuthServIds = AllowedAuthServId.allowedAuthServIdsFor("open-xchange.com");
        InternetAddress from = new QuotedInternetAddress("Super Hosting <rootserver@hosting.foreigner.com>");


        MailAuthenticityResult result = validator.parseHeaders(authHeaders, from, allowedAuthServIds, null);
        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
    }

    @Test
    public void testCorrectOverallResult2() throws Exception {
        AuthenticationResultsValidator validator = StandardAuthenticationResultsValidator.getInstance();

        List<String> authHeaders = Arrays.asList(
            "open-xchange.com; spf=none (mailfrom) smtp.mailfrom=barfoo.org (client-ip=168.135.221.145; helo=hydra.barfoo.org; envelope-from=opensuse-buildservice+bounces-25693-marcus.klein=open-xchange.com@barfoo.org; receiver=<UNKNOWN>)",
            "open-xchange.com; dmarc=none (p=none dis=none) header.from=foo.de"
        );
        List<AllowedAuthServId> allowedAuthServIds = AllowedAuthServId.allowedAuthServIdsFor("open-xchange.com");
        InternetAddress from = new QuotedInternetAddress("Alice <alice@foo.de>");


        MailAuthenticityResult result = validator.parseHeaders(authHeaders, from, allowedAuthServIds, null);
        assertStatus(MailAuthenticityStatus.NEUTRAL, result.getStatus());
    }

}
