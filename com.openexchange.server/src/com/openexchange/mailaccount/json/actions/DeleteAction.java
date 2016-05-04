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

package com.openexchange.mailaccount.json.actions;

import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "delete", description = "Delete mail accounts", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, requestBody = "An array with the IDs of the mail accounts to delete.")
public final class DeleteAction extends AbstractMailAccountAction {

    public static final String ACTION = AJAXServlet.ACTION_DELETE;

    /**
     * Initializes a new {@link DeleteAction}.
     */
    public DeleteAction() {
        super();
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jBody) throws OXException {
        /*
         * Compose JSON array with id
         */
        final JSONArray jsonArray = jBody.toArray();
        final int len = jsonArray.length();
        /*
         * Delete
         */
        try {
            final JSONArray responseArray = new JSONArray();
            if (!session.getUserPermissionBits().isMultipleMailAccounts()) {
                for (int i = 0; i < len; i++) {
                    final int id = jsonArray.getInt(i);
                    if (MailAccount.DEFAULT_ID != id) {
                        throw MailAccountExceptionCodes.NOT_ENABLED.create(
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()));
                    }
                }
            }
            final MailAccountFacade mailAccountFacade = getMailAccountFacade();
            for (int i = 0; i < len; i++) {
                final int id = jsonArray.getInt(i);
                final MailAccount mailAccount = mailAccountFacade.getMailAccount(id, session.getUserId(), session.getContextId());

                if (!isUnifiedINBOXAccount(mailAccount)) {
                    mailAccountFacade.deleteMailAccount(
                        id,
                        Collections.<String, Object> emptyMap(),
                        session.getUserId(),
                        session.getContextId());
                }

                responseArray.put(id);
            }
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(responseArray);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
