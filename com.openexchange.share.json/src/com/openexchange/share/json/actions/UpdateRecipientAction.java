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

package com.openexchange.share.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link UpdateRecipientAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateRecipientAction extends AbstractShareAction {

    /**
     * Initializes a new {@link UpdateRecipientAction}.
     *
     * @param services The service lookup
     * @param translatorFactory
     */
    public UpdateRecipientAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * extract parameters
         */
        int guestID = requestData.getIntParameter("entity");
        if (0 >= guestID) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("entity");
        }
        /*
         * Parse recipient
         */
        AnonymousRecipient recipient = null;
        try {
            ShareRecipient tmp = ShareJSONParser.parseRecipient((JSONObject) requestData.requireData());
            if (false == AnonymousRecipient.class.isInstance(tmp)) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create();//TODO
            }

            recipient = (AnonymousRecipient) tmp;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }


        String updatedPassword = recipient.getPassword();
        if (false == Strings.isEmpty(updatedPassword)) {
            updatedPassword = services.getService(ShareCryptoService.class).encrypt(updatedPassword);
        }

        Context context = getContextService().getContext(session.getContextId());
        UserService userService = getUserService();
        User guestUser = userService.getUser(guestID, context);
        String previousPassword = guestUser.getUserPassword();
        if (null == updatedPassword && null != previousPassword || false == updatedPassword.equals(previousPassword)) {
            UserImpl updatedUser = new UserImpl(guestUser);
            if (Strings.isEmpty(updatedPassword)) {
                updatedUser.setPasswordMech("");
                updatedUser.setUserPassword(null);
            } else {
                updatedUser.setUserPassword(updatedPassword);
                updatedUser.setPasswordMech(ShareCryptoService.PASSWORD_MECH_ID);
            }
            userService.updateUser(updatedUser, context);
        }

        /*
         * return empty result in case of success
         */
        return new AJAXRequestResult(new JSONObject());
    }

}
