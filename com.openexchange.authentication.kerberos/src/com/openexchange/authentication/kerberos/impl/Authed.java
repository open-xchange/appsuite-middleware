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

package com.openexchange.authentication.kerberos.impl;

import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.SESSION_SUBJECT;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link Authed}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Authed implements Authenticated, SessionEnhancement, ResponseEnhancement {

    private final String contextInfo;
    private final String userInfo;
    private final ClientPrincipal principal;

    public Authed(String contextInfo, String userInfo, ClientPrincipal principal) {
        super();
        this.contextInfo = contextInfo;
        this.userInfo = userInfo;
        this.principal = principal;
    }

    @Override
    public void enhanceSession(final Session session) {
        if (null != session) {
            session.setParameter(SESSION_SUBJECT, principal.getDelegateSubject());
            session.setParameter(SESSION_PRINCIPAL, principal);
        }
    }

    @Override
    public String getContextInfo() {
        return contextInfo;
    }

    @Override
    public String getUserInfo() {
        return userInfo;
    }

    @Override
    public ResultCode getCode() {
        return ResultCode.SUCCEEDED;
    }

    @Override
    public Header[] getHeaders() {
        if (null != principal.getResponseTicket()) {
            return new Header[] {
                new Header() {
                    @Override
                    public String getValue() {
                            return "Negotiate " + Base64.encode(principal.getResponseTicket());
                    }
                    @Override
                    public String getName() {
                            return "WWW-Authenticate";
                    }
                }
            };
        }
        return new Header[0];
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public String getRedirect() {
        return null;
    }
}
