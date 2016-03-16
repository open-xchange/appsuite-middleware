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

package com.openexchange.ajax.session.actions;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AbstractRedirectParser;


/**
 * {@link TokenLoginV2Parser}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class TokenLoginV2Parser extends AbstractRedirectParser<TokenLoginV2Response> {

    private int status;
    private boolean loginSuccessful;

    public TokenLoginV2Parser() {
        super();
        loginSuccessful = false;
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        this.status = resp.getStatusLine().getStatusCode();
        Header[] headers = resp.getHeaders("Set-Cookie");
        for (Header header : headers) {
            if (header.getValue().contains(LoginServlet.SECRET_PREFIX)) {
                loginSuccessful = true;
                break;
            }
        }
        return super.checkResponse(resp, request);
    }

    @Override
    protected TokenLoginV2Response createResponse(String myLocation) {
        int fragIndex = myLocation.indexOf('#');
        if (-1 == fragIndex) {
            return new TokenLoginV2Response(status, myLocation, loginSuccessful);
        }
        String redirectUrl = myLocation.substring(0, fragIndex);
        return new TokenLoginV2Response(status, redirectUrl, loginSuccessful);
    }

}
