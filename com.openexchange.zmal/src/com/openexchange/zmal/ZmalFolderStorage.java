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

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.MailFolderUtility.isEmpty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.session.Session;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.converters.ZmalFolderParser;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

/**
 * {@link ZmalFolderStorage} - The Zimbra mail folder storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalFolderStorage extends MailFolderStorage implements IMailFolderStorageEnhanced2 {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ZmalFolderStorage.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * The max. length for a mailbox name
     */
    private static final int MAX_MAILBOX_NAME = 60;

    private static final String STR_INBOX = "INBOX";

    private static final String STR_MSEC = "msec";

    private final ZmalAccess zmalAccess;

    private final ZmalSoapPerformer performer;

    private final int accountId;

    private final Session session;

    private final Context ctx;

    private final ZmalConfig zmalConfig;

    private Character separator;

    private final ZmalFolderParser parser;

    private final String authToken;


    /**
     * Initializes a new {@link ZmalFolderStorage}
     *
     * @param performer The SOAP performer
     * @param zmalAccess The Zimbra mail access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public ZmalFolderStorage(final String authToken, final ZmalSoapPerformer performer, final ZmalAccess zmalAccess, final Session session) throws OXException {
        super();
        this.authToken = authToken;
        this.performer = performer;
        this.zmalAccess = zmalAccess;
        accountId = zmalAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        zmalConfig = zmalAccess.getZmalConfig();
        parser = new ZmalFolderParser(performer);
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the associated context.
     *
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * Gets the associated Zimbra mail configuration.
     *
     * @return The Zimbra mail configuration
     */
    public ZmalConfig getZmalConfig() {
        return zmalConfig;
    }

    /**
     * Gets the Zimbra mail access.
     *
     * @return The Zimbra mail access
     */
    public ZmalAccess getZmalAccess() {
        return zmalAccess;
    }

    /**
     * Gets the associated account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    @Override
    public int[] getTotalAndUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return new int[] { 0, 0 };
        }
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            final MailFolder folder = folders.get(0);
            return new int[] { folder.getMessageCount(), folder.getUnreadMessageCount() };
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            final MailFolder folder = folders.get(0);
            return folder.getUnreadMessageCount();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getNewCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            final MailFolder folder = folders.get(0);
            return folder.getUnreadMessageCount();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getTotalCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            final MailFolder folder = folders.get(0);
            return folder.getMessageCount();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean exists(final String fullName) throws OXException {
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            return !folders.isEmpty();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        try {
            // Perform SOAP request
            final Element request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+fullName);
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> folders = parser.parseFolders(response);
            return folders.get(0);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        try {
            final Element request;
            if (DEFAULT_FOLDER_ID.equals(parentFullName)) {
                request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder");
            } else {
                request = performer.parse(ZmalType.MAIL, "GetFolderRequest/folder @l="+parentFullName);
            }
            request.addAttribute("visible", 1);
            final ZmalSoapResponse response = performer.perform(ZmalType.MAIL, request);
            final List<MailFolder> subFolders = parser.parseSubFolders(response);
            if (!all) {
                for (final Iterator<MailFolder> it = subFolders.iterator(); it.hasNext();) {
                    if (!it.next().isSubscribed()) {
                        it.remove();
                    }
                }
            }
            return subFolders.toArray(new MailFolder[subFolders.size()]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        // Nothing
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        final String name = toCreate.getName();
        if (isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (name.length() > MAX_MAILBOX_NAME) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(MAX_MAILBOX_NAME));
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String moveFolder(final String fullName, final String newFullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName) || DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw ZmalException.create(ZmalException.Code.NO_ROOT_MOVE, zmalConfig, session, new Object[0]);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        if (fullName.equals(DEFAULT_FOLDER_ID)) {
            return EMPTY_PATH;
        }
        final List<MailFolder> list = new ArrayList<MailFolder>();
        MailFolder folder = getFolder(fullName);
        list.add(folder);
        final String defaultFolderId = MailFolder.DEFAULT_FOLDER_ID;
        String pfn;
        while (!defaultFolderId.equals((pfn = folder.getFullname()))) {
            folder = getFolder(pfn);
            list.add(folder);
        }
        return list.toArray(new MailFolder[list.size()]);
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return "";
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDraftsFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSentFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSpamFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTrashFolder() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseResources() throws ZmalException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws OXException {
        throw new UnsupportedOperationException();
    }

    /*
     * ++++++++++++++++++ Helper methods ++++++++++++++++++
     */

}
