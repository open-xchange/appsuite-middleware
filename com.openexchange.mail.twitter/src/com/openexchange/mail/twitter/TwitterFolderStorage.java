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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mail.twitter;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.twitter.converters.TwitterFolderConverter;
import com.openexchange.mail.twitter.services.TwitterServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link TwitterFolderStorage} - The twitter folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterFolderStorage extends MailFolderStorage {

    private final Session session;

    private final Context ctx;

    private User user;

    /**
     * Initializes a new {@link TwitterFolderStorage}.
     *
     * @param session The session
     * @throws OXException If initialization fails
     */
    public TwitterFolderStorage(final Session session) throws OXException {
        super();
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * Gets session user.
     *
     * @return The session user
     * @throws OXException If retrieving user fails
     */
    private User getUser() throws OXException {
        if (null == user) {
            try {
                final UserService userService = TwitterServiceRegistry.getServiceRegistry().getService(UserService.class, true);
                user = userService.getUser(session.getUserId(), ctx);
            } catch (final OXException e) {
                throw new OXException(e);
            }
        }
        return user;
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        // Nothing to do
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws OXException {
        if ("INBOX".equals(fullname) || MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            throw MailExceptionCode.NO_DELETE_ACCESS.create(fullname);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        final String parentFullname = toCreate.getParentFullname();
        if ("INBOX".equals(parentFullname) || MailFolder.DEFAULT_FOLDER_ID.equals(parentFullname)) {
            throw MailExceptionCode.NO_CREATE_ACCESS.create(parentFullname);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(parentFullname);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws OXException {
        if ("INBOX".equals(fullname) || MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            throw MailExceptionCode.FOLDER_DELETION_DENIED.create(fullname);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        if ("INBOX".equals(fullname)) {
            return true;
        }
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        return false;
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return null;
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        if ("INBOX".equals(fullname)) {
            return TwitterFolderConverter.getINBOXFolder(session);
        } else if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            return TwitterFolderConverter.getINBOXFolder(session);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < quotas.length; i++) {
            quotas[i] = Quota.getUnlimitedQuota(types[i]);
        }
        return quotas;
    }

    @Override
    public String getSentFolder() throws OXException {
        return null;
    }

    @Override
    public String getSpamFolder() throws OXException {
        return null;
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        if ("INBOX".equals(parentFullname)) {
            return EMPTY_PATH;
        }
        if (MailFolder.DEFAULT_FOLDER_ID.equals(parentFullname)) {
            return new MailFolder[] { TwitterFolderConverter.getINBOXFolder(session) };
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(parentFullname);
    }

    @Override
    public String getTrashFolder() throws OXException {
        return null;
        // throw new OXException(
        // OXException.Code.DEFAULT_FOLDER_CHECK_FAILED,
        // "twitter.com",
        // getUserInfo4Error(),
        // Integer.valueOf(session.getUserId()),
        // Integer.valueOf(session.getContextId()),
        // "");
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws OXException {
        if ("INBOX".equals(fullname) || MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            throw MailExceptionCode.FOLDER_MOVE_DENIED.create(fullname);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public void releaseResources() throws OXException {
        // Nothing to do
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws OXException {
        if ("INBOX".equals(fullname) || MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            throw MailExceptionCode.FOLDER_UPDATE_DENIED.create(fullname);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
    }

}
