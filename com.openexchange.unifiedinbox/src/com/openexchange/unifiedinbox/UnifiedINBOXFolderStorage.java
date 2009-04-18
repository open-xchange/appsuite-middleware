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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.converters.UnifiedINBOXFolderConverter;

/**
 * {@link UnifiedINBOXFolderStorage} - The Unified INBOX folder storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXFolderStorage extends MailFolderStorage {

    private final UnifiedINBOXAccess access;

    private final Session session;

    private final Context ctx;

    /**
     * Initializes a new {@link UnifiedINBOXFolderStorage}
     * 
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws UnifiedINBOXException If context loading fails
     */
    public UnifiedINBOXFolderStorage(final UnifiedINBOXAccess access, final Session session) throws UnifiedINBOXException {
        super();
        this.access = access;
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new UnifiedINBOXException(e);
        }
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        return UnifiedINBOXAccess.INBOX.equals(fullname);
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return UnifiedINBOXFolderConverter.getRootFolder();
        }
        if (UnifiedINBOXAccess.INBOX.equals(fullname)) {
            return UnifiedINBOXFolderConverter.getUnifiedINBOXFolder();
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
    }

    private static final String PATTERN_ALL = "%";

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            return new MailFolder[] { UnifiedINBOXFolderConverter.getUnifiedINBOXFolder() };
        }
        if (UnifiedINBOXAccess.INBOX.equals(parentFullname)) {
            return EMPTY_PATH;
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, parentFullname);
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        return UnifiedINBOXFolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        // Unified INBOX mailbox only contains ONE folder: The INBOX folder
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_CREATION_FAILED);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.MOVE_DENIED);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UPDATE_DENIED);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.DELETE_DENIED);
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.CLEAR_NOT_SUPPORTED);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return EMPTY_PATH;
        }
        if (!UnifiedINBOXAccess.INBOX.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
        }
        return new MailFolder[] { UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(), UnifiedINBOXFolderConverter.getRootFolder() };
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return null;
    }

    @Override
    public String getSentFolder() throws MailException {
        return null;
    }

    @Override
    public String getSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getTrashFolder() throws MailException {
        return null;
    }

    @Override
    public void releaseResources() throws UnifiedINBOXException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

}
