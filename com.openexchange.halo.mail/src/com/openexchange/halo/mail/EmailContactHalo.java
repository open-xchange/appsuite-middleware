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

package com.openexchange.halo.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.halo.AbstractContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.utils.Column;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.ToTerm;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class EmailContactHalo extends AbstractContactHalo implements HaloContactDataSource {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EmailContactHalo}.
     *
     * @param services The service look-up
     */
    public EmailContactHalo(final ServiceLookup services) {
        super();
        this.services = ExceptionOnAbsenceServiceLookup.valueOf(services);
    }

    /**
     * Checks for columns.
     *
     * @return The column collection
     * @throws OXException If parameter is missing
     */
    private ColumnCollection requireColumns(AJAXRequestData requestData) throws OXException {
        String parameter = requestData.getParameter(AJAXServlet.PARAMETER_COLUMNS);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_COLUMNS);
        }

        List<Column> l;
        {
            String[] sa = Strings.splitByComma(parameter);
            l = new ArrayList<Column>(sa.length);
            for (String s : sa) {
                int field = Tools.getUnsignedInteger(s);
                l.add(field > 0 ? Column.field(field) : Column.header(s));
            }
        }

        return new ColumnCollection(l);
    }

    private MailField[] checkFields(MailField[] fields) {
        MailField idField = MailField.ID;
        for (int i = fields.length; i-- > 0;) {
            if (idField == fields[i]) {
                return fields;
            }
        }

        MailField[] newFields = new MailField[fields.length + 1];
        newFields[0] = idField;
        System.arraycopy(fields, 0, newFields, 1, fields.length);
        return newFields;
    }

    private RetrievalResult retrieveMessages(int limit, SearchTerm<?> senderTerm, SearchTerm<?> recipientTerm, MailField[] fields, String[] headers, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        String sentFullName = mailAccess.getFolderStorage().getSentFolder();
        IndexRange indexRange = new IndexRange(0, limit);

        if (null == headers || 0 >= headers.length) {
            // No headers requested
            List<MailMessage> inboxMessages = Arrays.asList(mailAccess.getMessageStorage().searchMessages("INBOX", indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, senderTerm, fields));
            List<MailMessage> sentMessages = Arrays.asList(mailAccess.getMessageStorage().searchMessages(sentFullName, indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, recipientTerm, fields));
            return new RetrievalResult(inboxMessages, sentMessages);
        }

        // Check for extended message storage
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageExt) {
            IMailMessageStorageExt extMsgStorage = (IMailMessageStorageExt) messageStorage;
            List<MailMessage> inboxMessages = Arrays.asList(extMsgStorage.searchMessages("INBOX", indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, senderTerm, fields, headers));
            List<MailMessage> sentMessages = Arrays.asList(extMsgStorage.searchMessages(sentFullName, indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, recipientTerm, fields, headers));
            return new RetrievalResult(inboxMessages, sentMessages);
        }

        // Headers are required to be fetched dedicatedly; therefore we need the mail identifier to be contained in requested fields
        MailField[] cols = checkFields(fields);
        MailMessage[] mails = mailAccess.getMessageStorage().searchMessages("INBOX", indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, senderTerm, cols);
        MessageUtility.enrichWithHeaders("INBOX", mails, headers, messageStorage);
        List<MailMessage> inboxMessages = Arrays.asList(mails);

        mails = mailAccess.getMessageStorage().searchMessages(sentFullName, indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, recipientTerm, cols);
        MessageUtility.enrichWithHeaders(sentFullName, mails, headers, messageStorage);
        List<MailMessage> sentMessages = Arrays.asList(mails);
        return new RetrievalResult(inboxMessages, sentMessages);
    }

    @Override
    public String getId() {
        return "com.openexchange.halo.mail";
    }

    @Override
    public AJAXRequestResult investigate(final HaloContactQuery query, final AJAXRequestData req, final ServerSession session) throws OXException {
        MailService mailService = services.getService(MailService.class);

        String[] headers;
        MailField[] requestedFields;
        {
            ColumnCollection columnCollection = requireColumns(req);
            requestedFields = MailField.getFields(columnCollection.getFields());
            headers = columnCollection.getHeaders();
        }
        int limit = req.getIntParameter(AJAXServlet.PARAMETER_LIMIT);
        limit = limit < 0 ? 10 : limit;

        final List<String> addresses = getEMailAddresses(query.getContact());
        if (isUserThemselves(session.getUser(), addresses)) {
            return new AJAXRequestResult(Collections.<MailMessage> emptyList(), "mail");
        }

        MailAccount[] userMailAccounts;
        {
            final MailAccountStorageService mailAccountService = services.getService(MailAccountStorageService.class);
            if (searchingExternalMailboxesIsFast()) {
                userMailAccounts = mailAccountService.getUserMailAccounts(session.getUserId(), session.getContextId());
            } else {
                userMailAccounts = new MailAccount[] { mailAccountService.getDefaultMailAccount(session.getUserId(), session.getContextId()) };
            }
        }

        SearchTerm<?> senderTerm = generateSenderSearch(addresses);
        SearchTerm<?> recipientTerm = generateRecipientSearch(addresses);
        List<MailMessage> messages = new LinkedList<MailMessage>();
        for (MailAccount mailAccount : userMailAccounts) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = mailService.getMailAccess(session, mailAccount.getId());
                mailAccess.connect();
                RetrievalResult retrievees = retrieveMessages(limit, senderTerm, recipientTerm, requestedFields, headers, mailAccess);
                messages.addAll(retrievees.inboxMessages);
                messages.addAll(retrievees.sentMessage);
            } finally {
                if (mailAccess != null) {
                    mailAccess.close(true);
                }
            }
        }

        Collections.sort(messages, new Comparator<MailMessage>() {

            @Override
            public int compare(final MailMessage arg0, final MailMessage arg1) {
                final Date sentDate1 = arg1.getSentDate();
                final Date sentDate0 = arg0.getSentDate();
                if (sentDate1 == null) {
                    return null == sentDate0 ? 0 : -1;
                }
                return null == sentDate0 ? 1 : sentDate1.compareTo(sentDate0);
            }
        });

        messages = messages.subList(0, Math.min(limit, messages.size()));
        return new AJAXRequestResult(messages, "mail");
    }

    protected SearchTerm<?> generateSenderSearch(final List<String> addresses) {
        final List<SearchTerm<?>> queries = new LinkedList<SearchTerm<?>>();
        for (final String addr : addresses) {
            queries.add(new FromTerm(addr));
        }
        return generateSearch(queries);
    }

    protected SearchTerm<?> generateRecipientSearch(final List<String> addresses) {
        final List<SearchTerm<?>> queries = new LinkedList<SearchTerm<?>>();
        for (final String addr : addresses) {
            queries.add(new ORTerm(new CcTerm(addr), new ToTerm(addr)));
        }
        return generateSearch(queries);
    }

    protected SearchTerm<?> generateSearch(final List<SearchTerm<?>> terms) {
        if (terms == null || terms.isEmpty()) {
            return null;
        }
        final int size = terms.size();
        if (1 == size) {
            return terms.get(0);
        }
        ORTerm orTerm = new ORTerm(terms.get(0), terms.get(1));
        for (int i = 2; i < size; i++) {
            orTerm = new ORTerm(orTerm, terms.get(i));
        }
        return orTerm;
    }

    protected boolean searchingExternalMailboxesIsFast() {
        return false; // TODO: once indexing is implemented, this should check whether it is turned on.
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private static final class RetrievalResult {

        /** The messages queried from INBOX folder */
        final List<MailMessage> inboxMessages;

        /** The messages queried from standard sent folder */
        final List<MailMessage> sentMessage;

        RetrievalResult(List<MailMessage> inboxMessages, List<MailMessage> sentMessage) {
            super();
            this.inboxMessages = inboxMessages;
            this.sentMessage = sentMessage;
        }
    }
}
