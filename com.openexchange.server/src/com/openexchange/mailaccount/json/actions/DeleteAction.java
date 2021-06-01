/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class DeleteAction extends AbstractMailAccountAction {

    public static final String ACTION = AJAXServlet.ACTION_DELETE;

    /**
     * Initializes a new {@link DeleteAction}.
     */
    public DeleteAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jBody) throws OXException {
        /*
         * Compose JSON array with id
         */
        if (null == jBody) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONArray jsonArray = jBody.toArray();
        int len = jsonArray.length();
        /*
         * Delete
         */
        try {
            if (!session.getUserPermissionBits().isMultipleMailAccounts()) {
                for (int i = 0; i < len; i++) {
                    int accountId = jsonArray.getInt(i);
                    if (MailAccount.DEFAULT_ID != accountId) {
                        throw MailAccountExceptionCodes.NOT_ENABLED.create(
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()));
                    }
                }
            }
            MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            JSONArray responseArray = new JSONArray(len);
            for (int i = 0; i < len; i++) {
                int accountId = jsonArray.getInt(i);
                MailAccount mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());

                if (!isUnifiedINBOXAccount(mailAccount)) {
                    storageService.deleteMailAccount(
                        accountId,
                        Collections.singletonMap("com.openexchange.mailaccount.session", session),
                        session.getUserId(),
                        session.getContextId());
                }

                responseArray.put(accountId);
            }
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(responseArray);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
