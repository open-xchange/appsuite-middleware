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
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetTreeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetTreeAction extends AbstractMailAccountTreeAction {

    public static final String ACTION = "get_tree";

    /**
     * Initializes a new {@link GetTreeAction}.
     */
    public GetTreeAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException, JSONException {
        final int id = requireIntParameter(AJAXServlet.PARAMETER_ID, requestData);

        final MailAccountStorageService storageService =
            ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

        final MailAccount mailAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId());

        if (isUnifiedINBOXAccount(mailAccount)) {
            // Treat as no hit
            throw MailAccountExceptionCodes.NOT_FOUND.create(
                Integer.valueOf(id),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }

        if (!session.getUserPermissionBits().isMultipleMailAccounts() && !isDefaultMailAccount(mailAccount)) {
            throw
                MailAccountExceptionCodes.NOT_ENABLED.create(
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }

        // Create a mail access instance
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
        return new AJAXRequestResult(actionValidateTree0(mailAccess, session));
    }

}
