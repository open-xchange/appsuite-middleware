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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.dropbox;

import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthToken;


/**
 * {@link DropboxOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxOAuthServiceMetaData extends AbstractOAuthServiceMetaData {

    /**
     * Initializes a new {@link DropboxOAuthServiceMetaData}.
     */
    public DropboxOAuthServiceMetaData(final ConfigurationService configService) {
        super();
        id = "com.openexchange.oauth.dropbox";
        displayName = "Dropbox";
        apiKey = configService.getProperty("com.openexchange.oauth.dropbox.apiKey");
        apiSecret = configService.getProperty("com.openexchange.oauth.dropbox.apiSecret");
    }

    @Override
    public API getAPI() {
        return API.DROPBOX;
    }

    @Override
    public String processAuthorizationURLCallbackAware(String authUrl, String callback) {
        if (isEmpty(callback)) {
            return authUrl;
        }
        return new StringAllocator(authUrl).append("&oauth_callback=").append(urlEncode(callback)).toString();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    @Override
    public void processArguments(Map<String, Object> arguments, Map<String, String> parameter, Map<String, Object> state) {
        // Remeber uid in arguments
        final String uid = parameter.get("uid");
        if (!isEmpty(uid)) {
            arguments.put("uid", uid);
        }
    }

    @Override
    public OAuthToken getOAuthToken(Map<String, Object> arguments) throws OXException {
        final String uid = (String) arguments.get("uid");
        
        
        
        return (OAuthToken) arguments.get(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
    }

}
