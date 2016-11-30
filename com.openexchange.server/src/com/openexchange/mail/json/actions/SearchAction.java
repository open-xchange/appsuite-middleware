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

package com.openexchange.mail.json.actions;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchAction extends AbstractMailAction {

    /**
     * Initializes a new {@link SearchAction}.
     *
     * @param services
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            /*
             * Read parameters
             */
            String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            ColumnCollection columnCollection = req.checkColumnsAndHeaders();
            int[] columns = columnCollection.getFields();
            String[] headers = columnCollection.getHeaders();
            String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
            String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ORDER);
            }
            int[] fromToIndices;
            {
                final String s = req.getParameter("limit");
                if (null == s) {
                    final int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
                    final int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);
                    if (leftHandLimit == MailRequest.NOT_FOUND || rightHandLimit == MailRequest.NOT_FOUND) {
                        fromToIndices = null;
                    } else {
                        fromToIndices = new int[] { leftHandLimit < 0 ? 0 : leftHandLimit, rightHandLimit < 0 ? 0 : rightHandLimit};
                        if (fromToIndices[0] >= fromToIndices[1]) {
                            return new AJAXRequestResult(Collections.<MailMessage>emptyList(), "mail");
                        }
                    }
                } else {
                    int start;
                    int end;
                    try {
                        final int pos = s.indexOf(',');
                        if (pos < 0) {
                            start = 0;
                            final int i = Integer.parseInt(s.trim());
                            end = i < 0 ? 0 : i;
                        } else {
                            int i = Integer.parseInt(s.substring(0, pos).trim());
                            start = i < 0 ? 0 : i;
                            i = Integer.parseInt(s.substring(pos+1).trim());
                            end = i < 0 ? 0 : i;
                        }
                    } catch (final NumberFormatException e) {
                        throw MailExceptionCode.INVALID_INT_VALUE.create(e, s);
                    }
                    if (start >= end) {
                        return new AJAXRequestResult(Collections.<MailMessage>emptyList(), "mail");
                    }
                    fromToIndices = new int[] {start,end};
                }
            }
            boolean ignoreSeen = req.optBool("unseen");
            boolean ignoreDeleted = !req.optBool("deleted", true);
            final JSONValue searchValue = (JSONValue) req.getRequest().requireData();
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Perform search dependent on passed JSON value
             */
            columns = prepareColumns(columns);
            if (searchValue.isArray()) {
                /*
                 * Parse body into a JSON array
                 */
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
                /*
                 * Search mails
                 */
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
                /*
                 * Start response
                 */
                List<MailMessage> mails = new LinkedList<MailMessage>();
                SearchIterator<MailMessage> it = null;
                try {
                    if (("thread".equalsIgnoreCase(sort))) {
                        it = mailInterface.getThreadedMessages(folderId, null, MailSortField.RECEIVED_DATE.getField(), orderDir, searchCols, searchPats, true, columns);
                        for (int i = it.size(); i-- > 0;) {
                            MailMessage mail = it.next();
                            if (!discardMail(mail, ignoreSeen, ignoreDeleted)) {
                                if (!mail.containsAccountId()) {
                                    mail.setAccountId(mailInterface.getAccountID());
                                }
                                mails.add(mail);
                            }
                        }
                    } else {
                        int sortCol = req.getSortFieldFor(sort);

                        mailInterface.openFor(folderId);
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();

                        SearchTerm<?> searchTerm;
                        if (ignoreDeleted || ignoreSeen) {
                            SearchTerm<?> main = mailInterface.createSearchTermFrom(searchCols, searchPats, true);

                            SearchTerm<?> first = ignoreSeen ? new FlagTerm(MailMessage.FLAG_SEEN, false) : null;
                            SearchTerm<?> second = ignoreDeleted ? (ignoreSeen ? null /* Already filtered by unseen, thus OR term will always be true */: new ORTerm(new FlagTerm(MailMessage.FLAG_DELETED, false), new FlagTerm(MailMessage.FLAG_SEEN, false))) : null;

                            if (null == first) {
                                searchTerm = null == second ? main : new ANDTerm(main, second);
                            } else {
                                searchTerm = null == second ? new ANDTerm(main, first) : new ANDTerm(main, new ANDTerm(first, second));
                            }
                        } else {
                            searchTerm = mailInterface.createSearchTermFrom(searchCols, searchPats, true);
                        }

                        FullnameArgument fa = prepareMailFolderParam(folderId);
                        IndexRange indexRange = null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]);
                        MailSortField sortField = MailSortField.getField(sortCol);
                        OrderDirection orderDirection = OrderDirection.getOrderDirection(orderDir);

                        MailMessage[] result;
                        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                        if (null != headers && 0 < headers.length) {
                            if (messageStorage instanceof IMailMessageStorageExt) {
                                IMailMessageStorageExt ext = (IMailMessageStorageExt) messageStorage;
                                result = ext.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns), headers);
                            } else {
                                result = messageStorage.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns));
                                enrichWithHeaders(fa.getFullname(), result, headers, messageStorage);
                            }
                        } else {
                            result = messageStorage.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns));
                        }

                        for (MailMessage mm : result) {
                            if (null != mm) {
                                if (!mm.containsAccountId()) {
                                    mm.setAccountId(mailInterface.getAccountID());
                                }
                                mails.add(mm);
                            }
                        }
                    }
                } finally {
                    SearchIterators.close(it);
                }
                return new AJAXRequestResult(mails, "mail");
            }

            // Body is a JSON object
            JSONArray searchArray = searchValue.toObject().getJSONArray(Mail.PARAMETER_FILTER);
            /*
             * Pre-Select field writers
             */
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

            mailInterface.openFor(folderId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();

            SearchTerm<?> searchTerm;
            if (ignoreDeleted || ignoreSeen) {
                SearchTerm<?> main = mailInterface.createSearchTermFrom(SearchTermParser.parse(searchArray));

                SearchTerm<?> first = ignoreSeen ? new FlagTerm(MailMessage.FLAG_SEEN, false) : null;
                SearchTerm<?> second = ignoreDeleted ? (ignoreSeen ? null /* Already filtered by unseen, thus OR term will always be true */: new ORTerm(new FlagTerm(MailMessage.FLAG_DELETED, false), new FlagTerm(MailMessage.FLAG_SEEN, false))) : null;

                if (null == first) {
                    searchTerm = null == second ? main : new ANDTerm(main, second);
                } else {
                    searchTerm = null == second ? new ANDTerm(main, first) : new ANDTerm(main, new ANDTerm(first, second));
                }
            } else {
                searchTerm = mailInterface.createSearchTermFrom(SearchTermParser.parse(searchArray));
            }

            FullnameArgument fa = prepareMailFolderParam(folderId);
            IndexRange indexRange = null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]);
            MailSortField sortField = MailSortField.getField(sortCol);
            OrderDirection orderDirection = OrderDirection.getOrderDirection(orderDir);

            List<MailMessage> mails = new LinkedList<MailMessage>();

            MailMessage[] result;
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (null != headers && 0 < headers.length) {
                if (messageStorage instanceof IMailMessageStorageExt) {
                    IMailMessageStorageExt ext = (IMailMessageStorageExt) messageStorage;
                    result = ext.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns), headers);
                } else {
                    result = messageStorage.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns));
                    enrichWithHeaders(fa.getFullname(), result, headers, messageStorage);
                }
            } else {
                result = messageStorage.searchMessages(fa.getFullname(), indexRange, sortField, orderDirection, searchTerm, MailField.getFields(columns));
            }

            for (MailMessage mm : result) {
                if (null != mm) {
                    if (!mm.containsAccountId()) {
                        mm.setAccountId(mailInterface.getAccountID());
                    }
                    mails.add(mm);
                }
            }

            return new AJAXRequestResult(mails, "mail");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void enrichWithHeaders(String fullName, MailMessage[] mails, String[] headerNames, IMailMessageStorage messageStorage) throws OXException {
        int length = mails.length;
        MailMessage[] headers;
        {
            String[] ids = new String[length];
            for (int i = ids.length; i-- > 0;) {
                MailMessage m = mails[i];
                ids[i] = null == m ? null : m.getMailId();
            }
            headers = messageStorage.getMessages(fullName, ids, MailFields.toArray(MailField.HEADERS));
        }

        for (int i = length; i-- > 0;) {
            MailMessage mailMessage = mails[i];
            if (null != mailMessage) {
                MailMessage header = headers[i];
                if (null != header) {
                    for (String headerName : headerNames) {
                        String[] values = header.getHeader(headerName);
                        if (null != values) {
                            for (String value : values) {
                                mailMessage.addHeader(headerName, value);
                            }
                        }
                    }
                }
            }
        }
    }

}
