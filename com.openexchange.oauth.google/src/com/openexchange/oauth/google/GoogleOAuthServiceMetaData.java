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

package com.openexchange.oauth.google;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.Google2Api;
import org.scribe.utils.StreamUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link GoogleOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class GoogleOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    /**
     * Initializes a new {@link GoogleOAuthServiceMetaData}.
     *
     * @param services the service lookup instance
     */
    public GoogleOAuthServiceMetaData(final ServiceLookup services) {
        super(services, KnownApi.GOOGLE, GoogleOAuthScope.values());
    }

    @Override
    protected String getPropertyId() {
        return "google";
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.singletonList(OAuthPropertyID.redirectUrl);
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return Google2Api.class;
    }

    @Override
    public String processAuthorizationURL(String authUrl, Session session) throws OXException {
        StringBuilder authUrlBuilder = new StringBuilder(super.processAuthorizationURL(authUrl, session));
        // Request a refresh token, too
        return authUrlBuilder.append("&approval_prompt=force").append("&access_type=offline").toString();
    }

    private Pattern identityPattern = Pattern.compile("\"id\":\\s*\"(\\S*?)\"");

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.impl.AbstractOAuthServiceMetaData#getUserIdentity(com.openexchange.oauth.OAuthToken)
     */
    @Override
    public String getUserIdentity(String accessToken) throws OXException {
        // Reference implementation (WIP)
        if (Strings.isEmpty(accessToken)) {
            return null;  //TODO: or throw exception token not found/invalid token/whatever?
        }
        //TODO: contact google and fetch the user identity
        // GET https://www.googleapis.com/oauth2/v1/userinfo
        //  Authorization: bearer oauthToken.getAccessToken();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.googleapis.com/oauth2/v1/userinfo").openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.connect();

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
            String body = StreamUtils.getStreamContents(stream); //TODO: replace with in-house stream reader
            Matcher matcher = identityPattern.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new OXException(31145, "No user identity can be retrieved");
            }
        } catch (MalformedURLException e) {
            throw new OXException(31145, "Malformed URL", e);
        } catch (IOException e) {
            throw new OXException(31145, "I/O error", e);
        }
    }
}
