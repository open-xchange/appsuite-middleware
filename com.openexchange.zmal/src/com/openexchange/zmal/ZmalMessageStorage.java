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

import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
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

    /**
     * Initializes a new {@link ZmalMessageStorage}.
     * 
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws OXException If initialization fails
     */
    public ZmalMessageStorage(final ZmalSoapPerformer performer, final ZmalAccess zmalAccess, final Session session) throws OXException {
        super();
        this.performer = performer;
        this.zmalAccess = zmalAccess;
        zmalFolderStorage = zmalAccess.getFolderStorage();
        accountId = zmalAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        zmalConfig = zmalAccess.getZmalConfig();
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

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(String folder, boolean includeSent, boolean cache, IndexRange indexRange, long max, MailSortField sortField, OrderDirection order, MailField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateMessageColorLabel(String fullName, int colorLabel) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateMessageFlags(String fullName, int flags, boolean set) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MailMessage[] getMessages(String fullName, String[] mailIds, MailField[] fields, String[] headerNames) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MailMessage[] getMessagesByMessageID(String... messageIDs) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] appendMessages(String destFolder, MailMessage[] msgs) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] copyMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailMessageStorage#deleteMessages(java.lang.String, java.lang.String[], boolean)
     */
    @Override
    public void deleteMessages(String folder, String[] mailIds, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailMessageStorage#getMessages(java.lang.String, java.lang.String[], com.openexchange.mail.MailField[])
     */
    @Override
    public MailMessage[] getMessages(String folder, String[] mailIds, MailField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailMessageStorage#releaseResources()
     */
    @Override
    public void releaseResources() throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailMessageStorage#searchMessages(java.lang.String, com.openexchange.mail.IndexRange, com.openexchange.mail.MailSortField, com.openexchange.mail.OrderDirection, com.openexchange.mail.search.SearchTerm, com.openexchange.mail.MailField[])
     */
    @Override
    public MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailMessageStorage#updateMessageFlags(java.lang.String, java.lang.String[], int, boolean)
     */
    @Override
    public void updateMessageFlags(String folder, String[] mailIds, int flags, boolean set) throws OXException {
        // TODO Auto-generated method stub
        
    }

    

}
