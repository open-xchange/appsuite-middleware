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

package com.openexchange.oauth.json.oauthaccount;

import java.util.Collection;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.json.AbstractOAuthWriter;
import com.openexchange.session.Session;

/**
 * The OAuth account writer
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccountWriter extends AbstractOAuthWriter {

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
     * @param session The session
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     * @throws OXException
     */
    public static JSONObject write(final OAuthAccount account, Session session) throws JSONException, OXException {
        return write(account, Collections.emptyList(), session);
    }

    /**
     * Writes specified account as a JSON object.
     *
     * @param account The account
     * @param associations The OAuth account associations
     * @param session The session
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     * @throws OXException
     */
    public static JSONObject write(final OAuthAccount account, Collection<OAuthAccountAssociation> associations, Session session) throws JSONException, OXException {
        final JSONObject jAccount = new JSONObject(8);
        jAccount.put(AccountField.ID.getName(), account.getId());
        jAccount.put(AccountField.DISPLAY_NAME.getName(), account.getDisplayName());
        jAccount.put(AccountField.SERVICE_ID.getName(), account.getMetaData().getId());
        jAccount.put(AccountField.ENABLED_SCOPES.getName(), write(account.getEnabledScopes()));
        jAccount.put(AccountField.AVAILABLE_SCOPES.getName(), write(account.getMetaData().getAvailableScopes(session.getUserId(), session.getContextId())));
        writeAssociationsFor(jAccount, associations);
        return jAccount;
    }

    /**
     * Writes the specified associations to the specified {@link JSONObject}
     *
     * @param jAccount The oauth account as json object
     * @param associations The {@link OAuthAccountAssociation}s to write
     * @throws JSONException if a JSON error occurs
     */
    private static void writeAssociationsFor(JSONObject jAccount, Collection<OAuthAccountAssociation> associations) throws JSONException {
        if (associations == null || associations.isEmpty()) {
            jAccount.put(AccountField.ASSOCIATIONS.getName(), JSONArray.EMPTY_ARRAY);
            return;
        }
        JSONArray jAssociations = new JSONArray(associations.size());
        for (OAuthAccountAssociation association : associations) {
            jAssociations.put(writeAssociation(association));
        }
        jAccount.put(AccountField.ASSOCIATIONS.getName(), jAssociations);
    }

    /**
     * Writes the specified {@link OAuthAccountAssociation} as a {@link JSONObject}
     *
     * @param association The {@link OAuthAccountAssociation}
     * @return The {@link JSONObject} with the association
     * @throws JSONException if a JSON error occurs
     */
    private static JSONObject writeAssociation(OAuthAccountAssociation association) throws JSONException {
        JSONObject jAssociation = new JSONObject(8);
        jAssociation.put(AssociationField.ID.getName(), association.getId());
        jAssociation.put(AssociationField.NAME.getName(), association.getDisplayName());
        jAssociation.put(AssociationField.SCOPES.getName(), association.getScopes());
        jAssociation.put(AssociationField.MODULE.getName(), association.getModule());
        if (Strings.isNotEmpty(association.getFolder())) {
            jAssociation.put(AssociationField.FOLDER.getName(), association.getFolder());
        }
        return jAssociation;
    }

    /**
     * Writes specified interaction as a JSON object.
     *
     * @param interaction The interaction
     * @param uuid The UUID associated with request token secret
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     */
    public static JSONObject write(final OAuthInteraction interaction, final String uuid) throws JSONException {
        final JSONObject jInteraction = new JSONObject(6);
        jInteraction.put(AccountField.AUTH_URL.getName(), interaction.getAuthorizationURL());
        jInteraction.put(AccountField.INTERACTION_TYPE.getName(), interaction.getInteractionType().getName());
        jInteraction.put(AccountField.TOKEN.getName(), interaction.getRequestToken().getToken());
        jInteraction.put(OAuthConstants.SESSION_PARAM_UUID, uuid);
        return jInteraction;
    }
}
