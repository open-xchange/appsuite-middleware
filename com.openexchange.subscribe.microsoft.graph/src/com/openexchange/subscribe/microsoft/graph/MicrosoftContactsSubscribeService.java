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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.subscribe.microsoft.graph;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.microsoft.graph.parser.ContactParser;
import com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService;

/**
 * {@link MicrosoftContactsSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftContactsSubscribeService extends AbstractOAuthSubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftContactsSubscribeService.class);

    private static final String LIST_CONTACTS_END_POINT = "https://graph.microsoft.com/v1.0/me/contacts";
    private static final String SOURCE_ID = KnownApi.MICROSOFT_GRAPH.getServiceId() + ".contact";
    private static final String USER_AGENT = "Open-Xchange Microsoft Graph REST Client";

    private final CloseableHttpClient httpClient;
    private final ContactParser parser;

    /**
     * Initialises a new {@link MicrosoftContactsSubscribeService}.
     */
    public MicrosoftContactsSubscribeService(OAuthServiceMetaData metadata, ServiceLookup services) {
        super(metadata, SOURCE_ID, FolderObject.CONTACT, "Microsoft Graph", services);
        httpClient = initializeHttpClient();
        parser = new ContactParser(httpClient);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService#getKnownApi()
     */
    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.MICROSOFT_GRAPH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        OAuthAccount oauthAccount = getOAuthAccount(session, subscription);
        return parser.parseFeed(fetchData(oauthAccount.getToken()).toObject());
    }

    private JSONValue fetchData(String accessToken) throws OXException {
        /////////////////// ! \\\\\\\\\\\\\\ FIXME TO BE REMOVED!!!! ONLY FOR TESTING !!!!
        parser.setAccessToken(accessToken);
        /////////////////// ! \\\\\\\\\\\\\\ TO BE REMOVED!!!! ONLY FOR TESTING !!!!
        CloseableHttpResponse httpResponse = null;
        HttpRequestBase httpRequest = new HttpGet();
        try {
            httpRequest.setURI(new URI(LIST_CONTACTS_END_POINT));
            httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            httpRequest.addHeader(HttpHeaders.ACCEPT, "application/json");

            LOG.debug("Executing request: '{}'", httpRequest.getURI());
            httpResponse = httpClient.execute(httpRequest);
            LOG.debug("Request '{}' completed with status code '{}'", httpRequest.getURI(), httpResponse.getStatusLine().getStatusCode());
            // Get the response code and assert
            HttpUtil.assertStatusCode(httpResponse);
            //            if (wholeResponse.hasAndNotNull("error")) {
            //                JSONObject error = wholeResponse.getJSONObject("error");
            //                String code = error.getString("code");
            //                if (code.equals("request_token_unauthorized")) {
            //                    throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(KnownApi.MICROSOFT_GRAPH.getShortName(), OXScope.contacts_ro.getDisplayName());
            //                }
            //                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(error.getString("message"));
            //            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create("The response entity is 'null'");
            }

            return HttpUtil.parseStream(entity.getContent());
        } catch (final IOException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (URISyntaxException e) {
            throw new OXException(666, "Invalid URI", e);
        } finally {
            HttpUtil.consume(httpResponse);
            HttpUtil.reset(httpRequest);
        }
    }

    /**
     * Initialises the HTTP client
     *
     * @return The initialised {@link CloseableHttpClient}
     */
    private CloseableHttpClient initializeHttpClient() {
        ClientConfig clientConfig = ClientConfig.newInstance();
        clientConfig.setUserAgent(USER_AGENT);

        return HttpClients.getHttpClient(clientConfig);
    }
}
