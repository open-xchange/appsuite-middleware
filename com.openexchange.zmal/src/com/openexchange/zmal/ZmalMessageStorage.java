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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.dom4j.QName;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.zmal.config.IZmalProperties;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.converters.ZMessageConverter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.zclient.ZGetMessageParams;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMailbox.Options;
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
public final class ZmalMessageStorage extends MailMessageStorage implements IMailMessageStorageExt, IMailMessageStorageBatch, ISimplifiedThreadStructure {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ZmalMessageStorage.class);

    /*-
     * Members
     */

    private final ZmalAccess zmalAccess;
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
    private ZMessageConverter parser;

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
        this.zmalAccess = zmalAccess;
        zmalFolderStorage = zmalAccess.getFolderStorage();
        accountId = zmalAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        zmalConfig = zmalAccess.getZmalConfig();
    }

    private ZMessageConverter getParser() {
        ZMessageConverter p = parser;
        if (null == p) {
            p = new ZMessageConverter(url, performer.getConfig());
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

    private IZmalProperties getZmalProperties() {
        if (null == zmalProperties) {
            zmalProperties = zmalConfig.getZmalProperties();
        }
        return zmalProperties;
    }

    private Element newRequestElement(QName name) {
        return performer.mUseJson ? new JSONElement(name) : new XMLElement(name);
    }

    private Element messageAction(String op, String id) {
        Element req = newRequestElement(MailConstants.MSG_ACTION_REQUEST);
        Element actionEl = req.addUniqueElement(MailConstants.E_ACTION);
        actionEl.addAttribute(MailConstants.A_ID, id);
        actionEl.addAttribute(MailConstants.A_OPERATION, op);
        return actionEl;
    }

    private Options newOptions() {
        final Options options = new Options(authToken, url);
        options.setRequestProtocol(performer.mUseJson ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setResponseProtocol(performer.mUseJson ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setUserAgent("Open-Xchange Http Client", "v6.22");
        return options;
    }

    private ZMessage getZMessage(String mailId, boolean markSeen) throws ServiceException {
        final ZMailbox mailbox = new ZMailbox(newOptions());
        final ZGetMessageParams params = new ZGetMessageParams();
        params.setId(mailId);
        params.setMarkRead(markSeen);
        //params.setRawContent(true);
        ZMessage message = mailbox.getMessage(params);
        return message;
    }

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(String folder, boolean includeSent, boolean cache, IndexRange indexRange, long max, MailSortField sortField, OrderDirection order, MailField[] fields) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // TODO: 
            
            return null;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateMessageColorLabel(String fullName, int colorLabel) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // List for identifiers
            final List<String> ids = new LinkedList<String>();
            // Search for all
            final ZSearchParams mSearchParams = new ZSearchParams("in:"+fullName);
            mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            int mSearchPage = 0;
            boolean keegoing = true;
            while (keegoing) {
                ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                ZSearchResult result = pager.getResult();
                keegoing = result.hasMore();
                for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                    final ZSearchHit hit = iterator.next();
                    ids.add(hit.getId());
                }
            }
            updateMessageColorLabel(fullName, ids.toArray(new String[ids.size()]), colorLabel);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateMessageColorLabel(String folder, String[] mailIds, int colorLabel) throws OXException {
        // TODO:
    }

    @Override
    public void updateMessageFlags(String fullName, int flags, boolean set) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // List for identifiers
            final List<String> ids = new LinkedList<String>();
            // Search for all
            final ZSearchParams mSearchParams = new ZSearchParams("in:"+fullName);
            mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            int mSearchPage = 0;
            boolean keegoing = true;
            while (keegoing) {
                ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
                ZSearchResult result = pager.getResult();
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
    public MailMessage getMessage(String folder, String mailId, boolean markSeen) throws OXException {
        try {
            ZMessage message = getZMessage(mailId, markSeen);
            return getParser().convert(message);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailMessage[] getMessages(String fullName, String[] mailIds, MailField[] fields, String[] headerNames) throws OXException {
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
    public MailMessage[] getMessagesByMessageID(String... messageIDs) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // TODO: 
            
            return null;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] appendMessages(String destFolder, MailMessage[] msgs) throws OXException {
        throw new UnsupportedOperationException("ZmalMessageStorage.appendMessages()");
    }

    @Override
    public String[] copyMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // TODO: 
            
            return null;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteMessages(String folder, String[] mailIds, boolean hardDelete) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            if (hardDelete) {
                for (String mailId : mailIds) {
                    mailbox.deleteMessage(mailId);
                }
            } else {
                for (String mailId : mailIds) {
                    mailbox.trashMessage(mailId);
                }
            }
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        
    }

    @Override
    public MailMessage[] getMessages(String folder, String[] mailIds, MailField[] fields) throws OXException {
        return getMessages(folder, mailIds, fields, null);
    }

    @Override
    public void releaseResources() throws OXException {
        // Release resources
    }

    @Override
    public MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            // TODO: 
            
            return null;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateMessageFlags(String folder, String[] mailIds, int flagsArg, boolean set) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
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

}
