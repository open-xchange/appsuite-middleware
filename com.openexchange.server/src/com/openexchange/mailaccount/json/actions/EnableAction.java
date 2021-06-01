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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mailaccount.KnownStatus;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.MailAccountFields;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnableAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class EnableAction extends AbstractValidateMailAccountAction implements MailAccountFields {

    public static final String ACTION = "enable";

    /**
     * Initializes a new {@link EnableAction}.
     */
    public EnableAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException {
        try {
            int id = requireIntParameter(AJAXServlet.PARAMETER_ID, requestData);

            if (MailAccount.DEFAULT_ID != id && !session.getUserPermissionBits().isMultipleMailAccounts()) {
                UnifiedInboxManagement unifiedInboxManagement = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                if ((null == unifiedInboxManagement) || (id != unifiedInboxManagement.getUnifiedINBOXAccountID(session))) {
                    throw MailAccountExceptionCodes.NOT_ENABLED.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                }
            }

            MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            storageService.enableMailAccount(id, session.getUserId(), session.getContextId());

            KnownStatus status = KnownStatus.OK;
            String message = status.getMessage(session.getUser().getLocale());

            JSONObject jStatus = new JSONObject(4).put("status", status.getId());
            if (Strings.isNotEmpty(message)) {
                jStatus.put("message", message);
            }

            return new AJAXRequestResult(new JSONObject(2).put(Integer.toString(id), jStatus), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
