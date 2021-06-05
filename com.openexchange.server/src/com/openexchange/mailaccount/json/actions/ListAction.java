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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.writer.DefaultMailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.READ)
public final class ListAction extends AbstractMailAccountAction {

    public static final String ACTION = AJAXServlet.ACTION_LIST;

    /**
     * Initializes a new {@link ListAction}.
     */
    public ListAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jData) throws OXException, JSONException {
        if (null == jData) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        List<Attribute> attributes = getColumns(requestData.getParameter(AJAXServlet.PARAMETER_COLUMNS));

        JSONArray jIds = jData.toArray();
        int len = jIds.length();

        boolean multipleEnabled = session.getUserPermissionBits().isMultipleMailAccounts();
        List<MailAccount> accounts = new ArrayList<MailAccount>(len);
        for (int i = 0, k = len; k-- > 0; i++) {
            int id = jIds.getInt(i);
            MailAccount account = storageService.getMailAccount(id, session.getUserId(), session.getContextId());
            if (!isUnifiedINBOXAccount(account) && (multipleEnabled || isDefaultMailAccount(account))) {
                accounts.add(account);
            }
        }

        MailAccount[] mailAccounts = accounts.toArray(new MailAccount[accounts.size()]);
        mailAccounts = checkSpamInfo(mailAccounts, session);

        JSONArray jAccounts = DefaultMailAccountWriter.writeArray(mailAccounts, attributes, session);
        return new AJAXRequestResult(jAccounts);
    }

}
