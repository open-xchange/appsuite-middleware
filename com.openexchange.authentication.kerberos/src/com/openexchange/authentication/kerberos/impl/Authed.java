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
        final ClientPrincipal principal = this.principal;
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
