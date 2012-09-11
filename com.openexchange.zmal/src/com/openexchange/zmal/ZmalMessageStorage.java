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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.zmal;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.dom4j.QName;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Charsets;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.zmal.config.IZmalProperties;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.converters.ZMessageConverter;
import com.openexchange.zmal.search.ZmalSearchTermVisitor;
import com.openexchange.zmal.utils.UrlSink;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.zclient.ZConversation;
import com.zimbra.cs.zclient.ZConversation.ZMessageSummary;
import com.zimbra.cs.zclient.ZGetMessageParams;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMailbox.Fetch;
import com.zimbra.cs.zclient.ZMailbox.SearchSortBy;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZSearchHit;
import com.zimbra.cs.zclient.ZSearchPagerResult;
import com.zimbra.cs.zclient.ZSearchParams;
import com.zimbra.cs.zclient.ZSearchResult;

/**
 * {@link ZmalMessageStorage} - The Zimbra mail implementation of message storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalMessageStorage extends MailMessageStorage implements IMailMessageStorageExt, IMailMessageStorageBatch {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ZmalMessageStorage.class);

    /*-
     * Members
     */

    private final ZmalSoapPerformer performer;
    private final int accountId;
    private final Session session;
    private final Context ctx;
    private final ZmalConfig zmalConfig;
    private Locale locale;
    private final ZmalFolderStorage zmalFolderStorage;
    private IZmalProperties zmalProperties;
    private final String authToken;
    private final String url;
    private ZMailbox mailbox;
    private ZMessageConverter parser;
    private MailAccount mailAccount;

    /**
     * Initializes a new {@link ZmalMessageStorage}.
     * 
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws OXException If initialization fails
     */
    public ZmalMessageStorage(final String authToken, final ZmalSoapPerformer performer, final ZmalAccess zmalAccess, final Session session) throws OXException {
        super();
        this.authToken = authToken;
        this.url = performer.getUrl();
        this.performer = performer;
        zmalFolderStorage = zmalAccess.getFolderStorage();
        accountId = zmalAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        zmalConfig = zmalAccess.getZmalConfig();
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final RuntimeException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return mailAccount;
    }

    private boolean isTrash(final String id, final ZMailbox mailbox) throws ServiceException {
        return mailbox.getTrash().getId().equals(id);
    }

    private List<String> getAllIds(final String folderId, final MailSortField sortField, final OrderDirection order, final ZMailbox mailbox) throws ServiceException {
        // Search for all
        final ZSearchParams mSearchParams = new ZSearchParams("in:"+folderId);
        mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
        if (null != sortField) {
            final SearchSortBy searchSortBy;
            if (MailSortField.SENT_DATE.equals(sortField) || MailSortField.RECEIVED_DATE.equals(sortField)) {
                searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.dateDesc : ZMailbox.SearchSortBy.dateAsc;
            } else if (MailSortField.SUBJECT.equals(sortField)) {
                searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.subjDesc : ZMailbox.SearchSortBy.subjAsc;
            } else {
                searchSortBy = null;
            }
            if (null != searchSortBy) {
                mSearchParams.setSortBy(searchSortBy);
            }
        }
        int mSearchPage = 0;
        final List<String> ids = new LinkedList<String>();
        boolean keegoing = true;
        while (keegoing) {
            final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
            final ZSearchResult result = pager.getResult();
            keegoing = result.hasMore();
            for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                final ZSearchHit hit = iterator.next();
                ids.add(hit.getId());
            }
        }
        return ids;
    }

    private static String toCSV(final List<String> ids) {
        if (null == ids) {
            return null;
        }
        final int size = ids.size();
        if (0 == size) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(size << 1);
        sb.append(ids.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(',').append(ids.get(i));
        }
        return sb.toString();
    }

    private ZMailbox getMailbox() throws ServiceException {
        ZMailbox m = mailbox;
        if (null == m) {
            m = new ZMailbox(newOptions());
            mailbox = m;
        }
        return m;
    }

    private ZMessageConverter getConverter() throws ServiceException {
        ZMessageConverter p = parser;
        if (null == p) {
            p = new ZMessageConverter(url, performer.getConfig(), getMailbox());
            parser = p;
        }
        return p;
    }

    private Locale getLocale() throws OXException {
        if (locale == null) {
            try {
                if (session instanceof ServerSession) {
                    locale = ((ServerSession) session).getUser().getLocale();
                } else {
                    final UserService userService = Services.getService(UserService.class);
                    locale = userService.getUser(session.getUserId(), ctx).getLocale();
                }
            } catch (final RuntimeException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return locale;
    }

    private Element newRequestElement(final QName name) {
        return performer.isUseJson() ? new JSONElement(name) : new XMLElement(name);
    }

    private ZMailbox.Options newOptions() {
        final ZMailbox.Options options = new ZMailbox.Options(authToken, url);
        options.setRequestProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setResponseProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        final int timeout = zmalConfig.getZmalProperties().getZmalTimeout();
        if (timeout > 0) {
            options.setTimeout(timeout);
        }
        options.setUserAgent("Open-Xchange Http Client", "v6.22");
        return options;
    }

    protected ZMessage getZMessage(final String mailId, final boolean markSeen) throws ServiceException {
        final ZMailbox mailbox = getMailbox();
        final ZGetMessageParams params = new ZGetMessageParams();
        params.setId(mailId);
        params.setMarkRead(markSeen);
        //params.setRawContent(true);
        final ZMessage message = mailbox.getMessage(params);
        return message;
    }

    private byte[] getRawZMessage(final String mailId, final boolean markSeen, final ZMailbox optMmailbox) throws ServiceException, OXException {
        final ZMailbox mailbox = null == optMmailbox ? getMailbox() : optMmailbox;
        final ZGetMessageParams params = new ZGetMessageParams();
        params.setId(mailId);
        params.setMarkRead(markSeen);
        params.setRawContent(true);
        final ZMessage message = mailbox.getMessage(params);
        final String content = message.getContent();
        if (null == content || content.startsWith("http")) {
            String contentURL = null == content ? message.getContentURL() : content;
            return UrlSink.getContent(contentURL, zmalConfig, mailbox);
        }
        // Content available as String
        final ContentType ct = new ContentType(message.getMimeStructure().getContentType());
        if (!ct.startsWith("text/")) {
            return content.getBytes(Charsets.US_ASCII);
        }
        final String cs = ct.getCharsetParameter();
        return content.getBytes(null == cs ? Charsets.US_ASCII : Charsets.forName(cs));
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            final ZSearchParams mSearchParams = new ZSearchParams("in:"+folder);
            mSearchParams.setTypes(ZSearchParams.TYPE_CONVERSATION);
            if (null != sortField) {
                final SearchSortBy searchSortBy;
                if (MailSortField.SENT_DATE.equals(sortField) || MailSortField.RECEIVED_DATE.equals(sortField)) {
                    searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.dateDesc : ZMailbox.SearchSortBy.dateAsc;
                } else if (MailSortField.SUBJECT.equals(sortField)) {
                    searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.subjDesc : ZMailbox.SearchSortBy.subjAsc;
                } else {
                    searchSortBy = ZMailbox.SearchSortBy.none;
                }
                if (null != searchSortBy) {
                    mSearchParams.setSortBy(searchSortBy);
                }
            }
            int mSearchPage = 0;
            final List<String> ids = new LinkedList<String>();
            boolean keegoing = true;
            while (keegoing) {
                final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                final ZSearchResult result = pager.getResult();
                keegoing = result.hasMore();
                for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                    final ZSearchHit hit = iterator.next();
                    ids.add(hit.getId());
                }
            }
            // Get each conversation
            final TObjectIntMap<String> map = new TObjectIntHashMap<String>(ids.size() << 1);
            for (final String id : ids) {
                final ZConversation conversation = mailbox.getConversation(id, Fetch.all);
                int threadLevel = 0;
                for (final ZMessageSummary messageSummary : conversation.getMessageSummaries()) {
                    map.put(messageSummary.getId(), threadLevel++);
                }
            }
            // Get associated messages
            final ZMessageConverter converter = getConverter();
            final List<MailMessage> msgs = new ArrayList<MailMessage>(map.size());
            final AtomicReference<OXException> err1 = new AtomicReference<OXException>();
            final AtomicReference<ServiceException> err2 = new AtomicReference<ServiceException>();
            final AtomicReference<RuntimeException> err3 = new AtomicReference<RuntimeException>();
            map.forEachEntry(new TObjectIntProcedure<String>() {

                @Override
                public boolean execute(String mailId, int threadLevel) {
                    try {
                        final MailMessage msg = converter.convert(getZMessage(mailId, false));
                        msg.setThreadLevel(threadLevel);
                        msgs.add(msg);
                        return true;
                    } catch (OXException e) {
                        err1.set(e);
                    } catch (ServiceException e) {
                        err2.set(e);
                    } catch (final RuntimeException e) {
                        err3.set(e);
                    }
                    return false;
                }
            });
            OXException oe = err1.get();
            if (null != oe) {
                throw oe;
            }
            ServiceException se = err2.get();
            if (null != se) {
                throw se;
            }
            RuntimeException rte = err3.get();
            if (null != rte) {
                throw rte;
            }
            // Sort them
            final MailMessageComparator comparator = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale());
            Collections.sort(msgs, comparator);
            MailMessage[] mails = msgs.toArray(new MailMessage[0]);
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((fromIndex) > mails.length) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= mails.length) {
                    toIndex = mails.length;
                }
                final MailMessage[] tmp = mails;
                final int retvalLength = toIndex - fromIndex;
                mails = new MailMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, mails, 0, retvalLength);
            }
            return mails;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullName, final int colorLabel) throws OXException {
        throw new UnsupportedOperationException("ZmalMessageStorage.updateMessageColorLabel()");
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        throw new UnsupportedOperationException("ZmalMessageStorage.updateMessageColorLabel()");
    }

    @Override
    public void updateMessageFlags(final String fullName, final int flags, final boolean set) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            // List for identifiers
            final List<String> ids = new LinkedList<String>();
            // Search for all
            final ZSearchParams mSearchParams = new ZSearchParams("in:"+fullName);
            mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            int mSearchPage = 0;
            boolean keegoing = true;
            while (keegoing) {
                final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                final ZSearchResult result = pager.getResult();
                keegoing = result.hasMore();
                for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                    final ZSearchHit hit = iterator.next();
                    ids.add(hit.getId());
                }
            }
            updateMessageFlags(fullName, ids.toArray(new String[ids.size()]), flags, set);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        try {
            final ZMessage message = getZMessage(mailId, markSeen);
            return setAccountInfo(getConverter().convert(message)); 
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage[] getMessages(final String fullName, final String[] mailIds, final MailField[] fields, final String[] headerNames) throws OXException {
        final int length = mailIds.length;
        final MailMessage[] ret = new MailMessage[length];
        for (int i = 0; i < length; i++) {
            final String mailId = mailIds[i];
            if (null != mailId) {
                ret[i] = getMessage(fullName, mailId, false);
            }
        }
        return ret;
    }

    @Override
    public MailMessage[] getMessagesByMessageID(final String... messageIDs) throws OXException {
        if (null == messageIDs || 0 == messageIDs.length) {
            return EMPTY_RETVAL;
        }
        try {
            final ZMailbox mailbox = getMailbox();
            final StringBuilder query = new StringBuilder(32);
            query.append("msgid:").append(messageIDs[0]);
            for (int i = 1; i < messageIDs.length; i++) {
                query.append(" OR msgid:").append(messageIDs[0]);
            }
            final ZSearchParams mSearchParams = new ZSearchParams(query.toString());
            mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            int mSearchPage = 0;
            List<String> ids = new LinkedList<String>();
            boolean keegoing = true;
            while (keegoing) {
                final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                final ZSearchResult result = pager.getResult();
                keegoing = result.hasMore();
                for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                    final ZSearchHit hit = iterator.next();
                    ids.add(hit.getId());
                }
            }
            // Get them
            final ZMessageConverter converter = getConverter();
            final List<MailMessage> msgs = new ArrayList<MailMessage>(ids.size());
            for (final String mailId : ids) {
                msgs.add(converter.convert(getZMessage(mailId, false)));
            }
            return msgs.toArray(new MailMessage[0]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            final List<String> ids = new ArrayList<String>(msgs.length);
            for (final MailMessage msg : msgs) {
                ids.add(mailbox.addMessage(destFolder, null, null, 0, msg.getSourceBytes(), false));
            }
            return ids.toArray(new String[0]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            final List<String> ids = new ArrayList<String>(mailIds.length);
            for (final String mailId : mailIds) {
                ids.add(mailbox.addMessage(destFolder, null, null, 0, getRawZMessage(mailId, false, mailbox), true));
            }
            mailbox.deleteMessage(toCSV(Arrays.asList(mailIds)));
            return ids.toArray(new String[0]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            if (hardDelete || isTrash(folder, mailbox)) {
                mailbox.deleteMessage(toCSV(Arrays.asList(mailIds)));
            } else {
                mailbox.trashMessage(toCSV(Arrays.asList(mailIds)));
            }
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        return getMessages(folder, mailIds, fields, null);
    }

    @Override
    public void releaseResources() throws OXException {
        // Release resources
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            // Check: http://wiki.zimbra.com/index.php?title=Search_Tips
            final List<String> ids;
            if (null == searchTerm) {
                ids = getAllIds(folder, sortField, order, mailbox);
            } else {
                // Search
                final ZmalSearchTermVisitor visitor = new ZmalSearchTermVisitor();
                searchTerm.accept(visitor);
                final ZSearchParams mSearchParams = new ZSearchParams("in:"+folder + " " + visitor.getQuery());
                mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
                if (null != sortField) {
                    final SearchSortBy searchSortBy;
                    if (MailSortField.SENT_DATE.equals(sortField) || MailSortField.RECEIVED_DATE.equals(sortField)) {
                        searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.dateDesc : ZMailbox.SearchSortBy.dateAsc;
                    } else if (MailSortField.SUBJECT.equals(sortField)) {
                        searchSortBy = OrderDirection.DESC.equals(order) ? ZMailbox.SearchSortBy.subjDesc : ZMailbox.SearchSortBy.subjAsc;
                    } else {
                        searchSortBy = ZMailbox.SearchSortBy.none;
                    }
                    if (null != searchSortBy) {
                        mSearchParams.setSortBy(searchSortBy);
                    }
                }
                int mSearchPage = 0;
                ids = new LinkedList<String>();
                boolean keegoing = true;
                while (keegoing) {
                    final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                    final ZSearchResult result = pager.getResult();
                    keegoing = result.hasMore();
                    for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                        final ZSearchHit hit = iterator.next();
                        ids.add(hit.getId());
                    }
                }
            }
            // Get them
            final ZMessageConverter converter = getConverter();
            final List<MailMessage> msgs = new ArrayList<MailMessage>(ids.size());
            for (final String mailId : ids) {
                msgs.add(converter.convert(getZMessage(mailId, false)));
            }
            MailMessage[] mails = msgs.toArray(new MailMessage[0]);
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((fromIndex) > mails.length) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= mails.length) {
                    toIndex = mails.length;
                }
                final MailMessage[] tmp = mails;
                final int retvalLength = toIndex - fromIndex;
                mails = new MailMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, mails, 0, retvalLength);
            }
            return setAccountInfo(mails);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flagsArg, final boolean set) throws OXException {
        try {
            final ZMailbox mailbox = getMailbox();
            final StringBuilder sFlags = new StringBuilder(8);
            for (final String mailId : mailIds) {
                sFlags.setLength(0);
                sFlags.append(getZMessage(mailId, false).getFlags());
                boolean applyFlags = false;
                int flags = flagsArg;
                flags &= ~MailMessage.FLAG_RECENT;
                flags &= ~MailMessage.FLAG_USER;
                if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                    if (set) {
                        if (sFlags.indexOf("r") < 0) {
                            sFlags.append('r');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("r");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                if (((flags & MailMessage.FLAG_DELETED) > 0) && sFlags.indexOf("x") < 0) {
                    if (set) {
                        if (sFlags.indexOf("x") < 0) {
                            sFlags.append('x');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("x");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                if (((flags & MailMessage.FLAG_DRAFT) > 0) && sFlags.indexOf("d") < 0) {
                    if (set) {
                        if (sFlags.indexOf("d") < 0) {
                            sFlags.append('d');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("d");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                if (((flags & MailMessage.FLAG_FLAGGED) > 0) && sFlags.indexOf("f") < 0) {
                    if (set) {
                        if (sFlags.indexOf("f") < 0) {
                            sFlags.append('f');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("f");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                if (((flags & MailMessage.FLAG_SEEN) <= 0)) { //  NOT CONTAINED
                    if (set) {
                        if (sFlags.indexOf("u") < 0) {
                            sFlags.append('u');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("u");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                /*
                 * Check for forwarded flag
                 */
                if (((flags & MailMessage.FLAG_FORWARDED) > 0)) {
                    if (set) {
                        if (sFlags.indexOf("w") < 0) {
                            sFlags.append('w');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("w");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                /*
                 * Check for read acknowledgment flag
                 */
                if (((flags & MailMessage.FLAG_READ_ACK) > 0)) {
                    if (set) {
                        if (sFlags.indexOf("n") < 0) {
                            sFlags.append('n');
                            applyFlags = true;
                        }
                    } else {
                        final int pos = sFlags.indexOf("n");
                        if (pos >= 0) {
                            sFlags.deleteCharAt(pos);
                            applyFlags = true;
                        }
                    }
                }
                if (applyFlags) {
                    final String theFlags = sFlags.toString();
                    mailbox.updateMessage(mailId, null, null, theFlags);
                }
            }
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets account ID and name in given instance of {@link MailMessage}.
     * 
     * @param mailMessages The {@link MailMessage} instance
     * @return The given instance of {@link MailMessage} with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage setAccountInfo(final MailMessage mailMessage) throws OXException {
        if (null == mailMessage) {
            return null;
        }
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        mailMessage.setAccountId(id);
        mailMessage.setAccountName(name);
        return mailMessage;
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     * 
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage[] setAccountInfo(final MailMessage[] mailMessages) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            if (null != mailMessage) {
                mailMessage.setAccountId(id);
                mailMessage.setAccountName(name);
            }
        }
        return mailMessages;
    }

}
