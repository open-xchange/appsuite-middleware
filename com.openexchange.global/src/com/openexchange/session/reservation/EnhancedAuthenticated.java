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

package com.openexchange.session.reservation;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.session.Session;


/**
 * Convenience class for {@link Enhancer} implementations. Just pass the original {@link Authenticated} instance
 * as constructor parameter and this class takes care of preserving already set cookies/headers and calls parent
 * session enhancements. If you want to enhance the created session, subclass this class and override {@link #doEnhanceSession(Session)}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class EnhancedAuthenticated implements Authenticated, ResponseEnhancement, SessionEnhancement {

    private final List<Header> headers = new LinkedList<Header>();

    private final List<Cookie> cookies = new LinkedList<Cookie>();

    private final Authenticated delegate;

    private ResultCode code;

    private String redirect;

    /**
     * Initializes a new {@link EnhancedAuthenticated}.
     * @param delegate
     */
    public EnhancedAuthenticated(Authenticated delegate) {
        super();
        this.delegate = delegate;
        if (delegate instanceof ResponseEnhancement) {
            ResponseEnhancement re = (ResponseEnhancement)delegate;
            Cookie[] delegateCookies = re.getCookies();
            if (delegateCookies != null && delegateCookies.length > 0) {
                for (Cookie cookie : delegateCookies) {
                    addCookie(cookie);
                }
            }

            Header[] delegateHeaders = re.getHeaders();
            if (delegateHeaders != null && delegateHeaders.length > 0) {
                for (Header header : delegateHeaders) {
                    addHeader(header);
                }
            }
        }
    }

    public void setResultCode(ResultCode code) {
        this.code = code;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    /**
     * Override this to apply your session enhancement after potential other enhancements took place.
     * <p>
     * Most likely specified session is an instance of <code>SessionDescription</code> providing more possibilities to modify spawned session.
     *
     * @param session The session
     */
    protected void doEnhanceSession(Session session) {
        // empty per default
    }

    /**
     * Don't override this method but override {@link #doEnhanceSession(Session)} instead!
     */
    @Override
    public void enhanceSession(Session session) {
        if (delegate instanceof SessionEnhancement) {
            ((SessionEnhancement)delegate).enhanceSession(session);
        }

        doEnhanceSession(session);
    }

    @Override
    public ResultCode getCode() {
        if (code != null) {
            return code;
        }

        if (delegate instanceof ResponseEnhancement) {
            ResultCode delegateCode = ((ResponseEnhancement)delegate).getCode();
            if (delegateCode != null && delegateCode != ResultCode.SUCCEEDED) {
                return delegateCode;
            }
        }

        return ResultCode.SUCCEEDED;
    }

    @Override
    public Header[] getHeaders() {
        return headers.toArray(new Header[headers.size()]);
    }

    @Override
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    @Override
    public String getRedirect() {
        if (redirect != null) {
            return redirect;
        }

        if (delegate instanceof ResponseEnhancement) {
            return ((ResponseEnhancement)delegate).getRedirect();
        }

        return null;
    }

    @Override
    public String getContextInfo() {
        return delegate.getContextInfo();
    }

    @Override
    public String getUserInfo() {
        return delegate.getUserInfo();
    }

}
