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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share;

import java.io.IOException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareRequest;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.exception.OXException;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link GuestClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestClient extends AJAXClient {

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @throws Exception
     */
    public GuestClient() throws Exception {
        super(new AJAXSession(), true);
        getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    public ResolveShareResponse resolve(ParsedShare share) throws ClientProtocolException, IOException, OXException, JSONException {
        if (AuthenticationMode.ANONYMOUS == share.getAuthentication()) {
            setCredentials(null);
        } else {
            setCredentials(share.getGuestMailAddress(), share.getGuestPassword());
        }
        ResolveShareRequest request = new ResolveShareRequest(share);
        ResolveShareResponse response = Executor.execute(this, request);
        return response;
    }

    private DefaultHttpClient getHttpClient() {
        return getSession().getHttpClient();
    }

    private void setCredentials(org.apache.http.auth.Credentials credentials) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (null != credentials) {
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
        }
        getHttpClient().setCredentialsProvider(credentialsProvider);
    }

    private void setCredentials(String username, String password) {
        setCredentials(new UsernamePasswordCredentials(username, password));
    }

}
