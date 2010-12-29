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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.oauth.json.oauthaccount;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthToken;

/**
 * The OAuth account writer
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccountWriter {

    /**
     * Initializes a new {@link AccountWriter}.
     */
    private AccountWriter() {
        super();
    }

    /**
     * Writes specified account as a JSON object.
     * 
     * @param account The account
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     */
    public static JSONObject write(final OAuthAccount account) throws JSONException {
        final JSONObject accountJSON = new JSONObject();
        accountJSON.put(AccountField.ID.getName(), account.getId());
        accountJSON.put(AccountField.DISPLAY_NAME.getName(), account.getDisplayName());
        accountJSON.put(AccountField.SERVICE_ID.getName(), account.getMetaData().getId());
        accountJSON.put(AccountField.TOKEN.getName(), account.getToken());
        accountJSON.put(AccountField.SECRET.getName(), account.getSecret());
        return accountJSON;
    }

    /**
     * Writes specified interaction as a JSON object.
     * 
     * @param interaction The interaction
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     */
    public static JSONObject write(final OAuthInteraction interaction) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(AccountField.AUTH_URL.getName(), interaction.getAuthorizationURL());
        json.put(AccountField.INTERACTION_TYPE.getName(), interaction.getInteractionType().getName());

        final OAuthToken requestToken = interaction.getRequestToken();
        if (null != requestToken) {
            JSONObject jsonToken = new JSONObject();
            jsonToken.put(AccountField.TOKEN.getName(), requestToken.getToken());
            jsonToken.put(AccountField.SECRET.getName(), requestToken.getSecret());
            json.put(AccountField.REQUEST_TOKEN.getName(), jsonToken);
        }
        return json;
    }

}
