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

package com.openexchange.subscribe.mslive;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.mslive.internal.ContactParser;

/**
 * {@link ContactsMSLiveSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContactsMSLiveSubscribeService extends AbstractMSLiveSubscribeService implements SubscribeService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactsMSLiveSubscribeService.class);

    private final SubscriptionSource source;

    /**
     * Initializes a new {@link ContactsMSLiveSubscribeService}.
     * 
     * @param oAuthServiceMetaData
     * @param services
     */
    public ContactsMSLiveSubscribeService(OAuthServiceMetaData oAuthServiceMetaData, ServiceLookup services) {
        super(oAuthServiceMetaData, services);
        source = initSS(FolderObject.CONTACT, "contact");
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    @Override
    public boolean handles(int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        final Session session = subscription.getSession();
        final OAuthAccount oauthAccount = MSLiveApiClient.getDefaultOAuthAccount(session);
        final String accessToken = MSLiveApiClient.getAccessToken(oauthAccount, session);
        final JSONObject contacts = fetchData(accessToken);
        final ContactParser parser = new ContactParser();
        return parser.parse(contacts);
    }

    /**
     * @param accessToken
     * @return
     * @throws OXException
     */
    private JSONObject fetchData(final String accessToken) throws OXException {
        JSONObject wholeResponse = new JSONObject();
        try {
            final String protectedUrl = "https://apis.live.net/v5.0/me/contacts?access_token=" + URLEncoder.encode(accessToken, "UTF-8");
            final GetMethod getMethod = new GetMethod(protectedUrl);

            final HttpClient client = new HttpClient();
            client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            client.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();
            wholeResponse = new JSONObject(response);

            if (wholeResponse.hasAndNotNull("error")) {
                JSONObject error = wholeResponse.getJSONObject("error");
                String code = error.getString("code");
                if (code.equals("request_token_unauthorized")) {
                    throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(API.MS_LIVE_CONNECT.getShortName(), Module.contacts_ro);
                }
                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(error.getString("message"));
            }

        } catch (final HttpException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e, e.getMessage());
        } catch (final IOException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        }
        return wholeResponse;
    }
}
