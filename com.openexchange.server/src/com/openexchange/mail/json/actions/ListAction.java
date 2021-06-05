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

package com.openexchange.mail.json.actions;

import static com.openexchange.tools.Collections.newHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public final class ListAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services
     */
    public ListAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            // Read in parameters
            ColumnCollection columnCollection = req.checkColumnsAndHeaders(true);
            int[] columns = columnCollection.getFields();
            String[] headers = columnCollection.getHeaders();
            /*
             * Get map
             */
            final Map<String, List<String>> idMap = fillMapByArray((JSONArray) req.getRequest().getData());
            if (idMap.isEmpty()) {
                /*
                 * Request body is an empty JSON array
                 */
                return new AJAXRequestResult(Collections.<MailMessage> emptyList(), "mail");
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Start response
             */
            final List<MailMessage> list = new LinkedList<MailMessage>();
            columns = prepareColumns(columns);
            for (Map.Entry<String, List<String>> entry : idMap.entrySet()) {
                MailMessage[] mails = mailInterface.getMessageList(entry.getKey(), toArray(entry.getValue()), columns, headers);
                int accountID = mailInterface.getAccountID();
                for (int i = 0; i < mails.length; i++) {
                    MailMessage mail = mails[i];
                    if (mail != null) {
                        if (!mail.containsAccountId()) {
                            mail.setAccountId(accountID);
                        }
                        list.add(mail);
                    }
                }
            }
            AJAXRequestResult result = new AJAXRequestResult(list, "mail");
            result.addWarnings(mailInterface.getWarnings());
            return result;
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final Map<String, List<String>> fillMapByArray(JSONArray idArray) throws JSONException, OXException {
        if (null == idArray) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        final int length = idArray.length();
        if (length <= 0) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> idMap = newHashMap(4);
        final String parameterFolderId = AJAXServlet.PARAMETER_FOLDERID;
        final String parameterId = AJAXServlet.PARAMETER_ID;
        String folder;
        List<String> list;
        {
            final JSONObject idObject = idArray.getJSONObject(0);
            folder = ensureString(parameterFolderId, idObject);
            list = new ArrayList<String>(length);
            idMap.put(folder, list);
            list.add(ensureString(parameterId, idObject));
        }
        for (int i = 1; i < length; i++) {
            final JSONObject idObject = idArray.getJSONObject(i);
            final String fld = ensureString(parameterFolderId, idObject);
            if (!folder.equals(fld)) {
                folder = fld;
                final List<String> tmp = idMap.get(folder);
                if (tmp == null) {
                    list = new ArrayList<String>(length);
                    idMap.put(folder, list);
                } else {
                    list = tmp;
                }
            }
            list.add(ensureString(parameterId, idObject));
        }
        return idMap;
    }

    private static String ensureString(String key, JSONObject jo) throws OXException {
        if (!jo.hasAndNotNull(key)) {
            throw MailExceptionCode.MISSING_PARAMETER.create(key);
        }
        return jo.optString(key);
    }

    private static String[] toArray(Collection<String> c) {
        return c.toArray(new String[c.size()]);
    }

}
