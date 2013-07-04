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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.oauth.facebook;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.FacebookApi;
import com.openexchange.exception.OXException;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.session.Session;

/**
 * {@link OAuthServiceMetaDataFacebookImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthServiceMetaDataFacebookImpl extends AbstractOAuthServiceMetaData implements com.openexchange.oauth.ScribeAware {

    private final DeferringURLService deferrer;

    /**
     * Initializes a new {@link OAuthServiceMetaDataFacebookImpl}.
     * @param configurationService
     */
    public OAuthServiceMetaDataFacebookImpl(final DeferringURLService deferrer) {
        super();
        this.deferrer = deferrer;
        setAPIKeyName("com.openexchange.facebook.apiKey");
        setAPISecretName("com.openexchange.facebook.secretKey");
    }

    @Override
    public String getDisplayName() {
        return "Facebook";
    }

    @Override
    public String getId() {
        return "com.openexchange.oauth.facebook";
    }

    @Override
    public boolean needsRequestToken() {
        return false;
    }

    @Override
    public String getScope() {
        return "offline_access,publish_stream,read_stream,status_update,user_about_me,friends_about_me," +
        		"user_activities,friends_activities,user_birthday,friends_birthday,user_education_history," +
        		"friends_education_history,user_events,friends_events,user_hometown,friends_hometown," +
        		"user_interests,friends_interests,user_likes,friends_likes,user_location,friends_location," +
        		"user_photos,friends_photos,user_relationships,friends_relationships,user_relationship_details," +
        		"friends_relationship_details,user_status,friends_status,user_videos,friends_videos," +
        		"user_website,friends_website,user_work_history,friends_work_history,email";
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, Session session) {
        if (deferrer == null) {
            return callbackUrl;
        }
        return deferrer.getDeferredURL(callbackUrl);
    }

    @Override
    public String processAuthorizationURL(final String authUrl) {
        final String removeMe = "response_type=token&";
        final int pos = authUrl.indexOf(removeMe);
        return pos < 0 ? authUrl : new StringBuilder(authUrl.length()).append(authUrl.substring(0, pos)).append(
            authUrl.substring(pos + removeMe.length())).toString();
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) {
        final String code = parameter.get("code");
        arguments.put(OAuthConstants.ARGUMENT_PIN, code);
        arguments.put(OAuthConstants.ARGUMENT_CALLBACK, modifyCallbackURL((String)state.get(OAuthConstants.ARGUMENT_CALLBACK), (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION)));
    }

    private static final int BUFSIZE = 8192;

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments) throws OXException {
        final String code = (String) arguments.get(OAuthConstants.ARGUMENT_PIN);
        final String callback = (String) arguments.get(OAuthConstants.ARGUMENT_CALLBACK);
        final Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);

        Reader reader = null;
        try {
            final StringBuilder builder = new StringBuilder(BUFSIZE << 1);
            /*
             * Compose URL
             */
            builder.append("https://graph.facebook.com/oauth/access_token?client_id=").append(getAPIKey(session));
            builder.append("&redirect_uri=").append(URLEncoder.encode(callback, "UTF-8"));
            builder.append("&client_secret=").append(getAPISecret(session));
            builder.append("&code=").append(code);
            final URL url = new URL(builder.toString());
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            /*
             * Initialize a reader on URL connection...
             */
            reader = new InputStreamReader(connection.getInputStream(), com.openexchange.java.Charsets.UTF_8);
            /*
             * ... and read response using direct buffering
             */
            builder.setLength(0);
            final char[] buf = new char[BUFSIZE];
            int read;
            while ((read = reader.read(buf, 0, BUFSIZE)) > 0) {
                builder.append(buf, 0, read);
            }
            return parseResponse(builder.toString());
        } catch (final MalformedURLException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // IGNORE
                }
            }
        }
    }

    private static final Pattern EXTRACTOR = Pattern.compile("access_token=(.*?)&?");

    private OAuthToken parseResponse(final String string) {
        final Matcher matcher = EXTRACTOR.matcher(string);
        String token = null;
        if(matcher.matches()) {
            token = matcher.group(1);
            token = checkToken(token);
        }

        return new DefaultOAuthToken(token, "");
    }

    private static final Pattern P_EXPIRES = Pattern.compile("&expires(=[0-9]+)?$");

    private static String checkToken(final String accessToken) {
        if (accessToken.indexOf("&expires") < 0) {
            return accessToken;
        }
        final Matcher m = P_EXPIRES.matcher(accessToken);
        final StringBuffer sb = new StringBuffer(accessToken.length());
        if (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

	@Override
	public API getAPI() {
		return API.FACEBOOK;
	}

    @Override
    public Class<? extends Api> getScribeService() {
        return FacebookApi.class;
    }

}
