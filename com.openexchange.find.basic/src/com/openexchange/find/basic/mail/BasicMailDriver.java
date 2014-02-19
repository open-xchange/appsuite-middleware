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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.basic.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.session.ServerSession;


/**
 * A basic implementation to search within the mail module. Based on {@link IMailMessageStorage}
 * and {@link SearchTerm}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailDriver extends MockMailDriver {

    private static final Logger LOG = LoggerFactory.getLogger(BasicMailDriver.class);

    public BasicMailDriver() {
        super();
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return true;
    }

    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        final SearchParameters parameters = SearchParameters.newInstance(searchRequest, session);
        MailService mailService = Services.getMailService();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(parameters.getMailFolder());
            final int accountId = fullnameArgument.getAccountId();
            String fullname = fullnameArgument.getFullname();
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            List<MailMessage> messages = searchMessages(mailAccess, session, accountId, fullname, parameters);
            List<Document> documents = new ArrayList<Document>(messages.size());
            for (MailMessage message : messages) {
                documents.add(new MailDocument(message));
            }

            return new SearchResult(-1, parameters.getStart(), documents);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private List<MailMessage> searchMessages(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final ServerSession session, final int accountId, final String fullname, final SearchParameters parameters) throws OXException {
        int start = parameters.getStart();
        int size = parameters.getSize();
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        MailMessage[] messages = messageStorage.searchMessages(
            fullname,
            parameters.getIndexRange(),
            parameters.getSortField(),
            parameters.getOrderDirection(),
            parameters.getSearchTerm(),
            parameters.getMailFields());

        if (start > messages.length) {
            return Collections.emptyList();
        }

        List<MailMessage> resultMessages = new ArrayList<MailMessage>(messages.length);
        Collections.addAll(resultMessages, messages);
        int toIndex = (start + size) <= resultMessages.size() ? (start + size) : resultMessages.size();
        return resultMessages.subList(start, toIndex);
    }

    private List<MailMessage> searchMessagesRecursive(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final ServerSession session, final int accountId, final String fullname, final SearchParameters parameters) throws OXException {
        /*
         * TODO: Wait for PM / UI decision and delete or finish me.
         */
        Future<List<MailFolder>> foldersFuture = determineSubfolders(mailAccess, accountId, fullname);
        int start = parameters.getStart();
        int size = parameters.getSize();
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        MailMessage[] messages = messageStorage.searchMessages(
            fullname,
            parameters.getIndexRange(),
            parameters.getSortField(),
            parameters.getOrderDirection(),
            parameters.getSearchTerm(),
            parameters.getMailFields());

        List<MailMessage> resultMessages = new ArrayList<MailMessage>(2 * messages.length);
        Collections.addAll(resultMessages, messages);
        try {
            List<MailFolder> subfolders = foldersFuture.get();
            for (MailFolder folder : subfolders) {
                Collections.addAll(resultMessages, messageStorage.searchMessages(
                    folder.getFullname(),
                    parameters.getIndexRange(),
                    parameters.getSortField(),
                    parameters.getOrderDirection(),
                    parameters.getSearchTerm(),
                    parameters.getMailFields()));
            }
        } catch (InterruptedException e) {
            throw new OXException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OXException) {
                throw (OXException) e.getCause();
            }

            throw new OXException(e.getCause());
        }

        if (start > resultMessages.size()) {
            return Collections.emptyList();
        }

        Collections.sort(resultMessages, new MailMessageComparator(
            MailSortField.RECEIVED_DATE,
            true,
            session.getUser().getLocale()));

        int toIndex = (start + size) <= resultMessages.size() ? (start + size) : resultMessages.size();
        return resultMessages.subList(start, toIndex);
    }

    private Future<List<MailFolder>> determineSubfolders(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final int accountId, final String fullname) throws OXException {
        ThreadPoolService threadPoolService = Services.getThreadPoolService();
        return threadPoolService.submit(new Task<List<MailFolder>>() {
            @Override
            public void setThreadName(ThreadRenamer threadRenamer) {}
            @Override
            public void beforeExecute(Thread t) {}
            @Override
            public void afterExecute(Throwable t) {
                if (t != null) {
                    LOG.error("Could not load mail subfolders to search in for parent {} in account {}.", fullname, accountId, t);
                }
            }

            @Override
            public List<MailFolder> call() throws Exception {
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                List<MailFolder> folders = new LinkedList<MailFolder>();
                collectMailFolders(fullname, folderStorage, accountId, folders);
                return folders;
            }
        });
    }

    private void collectMailFolders(final String parentFullName, final IMailFolderStorage folderStorage, final int accountId, final List<MailFolder> folders) throws OXException {
        MailFolder[] subfolders = folderStorage.getSubfolders(parentFullName, false);
        Collections.addAll(folders, subfolders);
        for (final MailFolder child : subfolders) {
            if (child.hasSubscribedSubfolders()) {
                collectMailFolders(child.getFullname(), folderStorage, accountId, folders);
            }
        }
    }
}
