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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageThreadReferences;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailThread;
import com.openexchange.mail.dataobjects.MailThreads;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ThreadReferencesAction}
 *
 * @author <a href="mailto:joshua.wirtz@open-xchange.com">Joshua Wirtz</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public class ThreadReferencesAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ThreadReferencesAction}.
     *
     * @param services The service look-up
     */
    public ThreadReferencesAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        // Read parameters
        String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
        ColumnCollection columnCollection = req.checkColumnsAndHeaders(true);
        int[] columns = columnCollection.getFields();
        String[] headers = columnCollection.getHeaders();
        String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
        String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
        if (sort != null && order == null) {
            throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ORDER);
        }
        int size;
        {
            String s = req.getParameter("size");
            if (null == s) {
                size = -1;
            } else {
                try {
                    size = Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    throw MailExceptionCode.INVALID_INT_VALUE.create(e, s);
                }
            }
        }
        columns = prepareColumns(columns, MailListField.RECEIVED_DATE.getField());

        int orderDir = OrderDirection.ASC.getOrder();
        if (order != null) {
            if (order.equalsIgnoreCase("asc")) {
                orderDir = OrderDirection.ASC.getOrder();
            } else if (order.equalsIgnoreCase("desc")) {
                orderDir = OrderDirection.DESC.getOrder();
            } else {
                throw MailExceptionCode.INVALID_INT_VALUE.create(AJAXServlet.PARAMETER_ORDER);
            }
        }

        int sortCol = req.getSortFieldFor(sort);
        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folderId);
        MailSortField sortField = MailSortField.getField(sortCol);
        OrderDirection orderDirection = OrderDirection.getOrderDirection(orderDir);

        MailServletInterface mailInterface = getMailInterface(req);

        SearchTerm<?> searchTerm = null;
        {
            JSONValue searchValue = req.getRequest().getData(JSONValue.class);
            if (null != searchValue) {
                if (searchValue.isArray()) {
                    JSONArray ja = searchValue.toArray();
                    int length = ja.length();
                    if (length <= 0) {
                        return new AJAXRequestResult(new JSONArray(0), "json");
                    }

                    int[] searchCols = new int[length];
                    String[] searchPats = new String[length];
                    for (int i = 0; i < length; i++) {
                        final JSONObject tmp = ja.getJSONObject(i);
                        searchCols[i] = tmp.getInt(Mail.PARAMETER_COL);
                        searchPats[i] = tmp.getString(AJAXServlet.PARAMETER_SEARCHPATTERN);
                    }
                    searchTerm = mailInterface.createSearchTermFrom(searchCols, searchPats, true);
                } else {
                    JSONArray searchArray = searchValue.toObject().getJSONArray(Mail.PARAMETER_FILTER);
                    searchTerm = mailInterface.createSearchTermFrom(SearchTermParser.parse(searchArray));
                }
            } else {
                // Default is to only query undeleted messages
                searchTerm = new FlagTerm(MailMessage.FLAG_DELETED, false);
            }
        }

        mailInterface.openFor(folderId);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();
        {
            MailConfig mailConfig = mailAccess.getMailConfig();
            MailCapabilities capabilities = mailConfig.getCapabilities();

            if (!capabilities.hasThreadReferences()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            IMailMessageStorageThreadReferences threadReferencesMessageStorage = messageStorage.supports(IMailMessageStorageThreadReferences.class);
            if (null == threadReferencesMessageStorage) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            if (!threadReferencesMessageStorage.isThreadReferencesSupported()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            List<MailThread> threadReferences = threadReferencesMessageStorage.getThreadReferences(fullnameArgument.getFullName(), size, sortField, orderDirection, searchTerm, MailField.getFields(columns), headers);

            return new AJAXRequestResult(new MailThreads(threadReferences), "mail");
        }
    }

}
