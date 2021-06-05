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

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.MailAccountFields;
import com.openexchange.mailaccount.json.writer.DefaultMailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetAction extends AbstractMailAccountAction implements MailAccountFields {

    public static final String ACTION = AJAXServlet.ACTION_GET;

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException {
        final int id = requireIntParameter(AJAXServlet.PARAMETER_ID, requestData);

        try {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

            MailAccount mailAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId());

            if (isUnifiedINBOXAccount(mailAccount)) {
                // Treat as no hit
                throw MailAccountExceptionCodes.NOT_FOUND.create(
                    Integer.valueOf(id),
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }

            if (!session.getUserPermissionBits().isMultipleMailAccounts() && !isDefaultMailAccount(mailAccount)) {
                throw MailAccountExceptionCodes.NOT_ENABLED.create(
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }

            mailAccount = checkSpamInfo(mailAccount, session);

            final JSONObject jsonAccount = DefaultMailAccountWriter.write(mailAccount);
            // final JSONObject jsonAccount = MailAccountWriter.write(checkFullNames(mailAccount, storageService, session));

            {
                final JSlobId jSlobId = new JSlobId(JSLOB_SERVICE_ID, Integer.toString(id), session.getUserId(), session.getContextId());
                final JSlob jSlob = getStorage().opt(jSlobId);
                Map<String, Object> map = null == jSlob ? null : (Map<String, Object>) JSONCoercion.coerceToNative(jSlob.getJsonObject());
                if (map != null && !map.isEmpty()) {
                    jsonAccount.put(META, JSONCoercion.coerceToJSON(map));
                }
            }

            return new AJAXRequestResult(jsonAccount);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
