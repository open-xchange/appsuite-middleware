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

package com.openexchange.pop3.converters;

import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.pop3.dataobjects.POP3MailFolder;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.sun.mail.pop3.DefaultFolder;

/**
 * {@link POP3FolderConverter} - Converts a POP3 folder to an instance of {@link MailFolder}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3FolderConverter {

    /**
     * Prevent instantiation
     */
    private POP3FolderConverter() {
        super();
    }

    /**
     * Creates a folder data object from given POP3 folder.
     * 
     * @param pop3Folder The POP3 folder
     * @param session The session
     * @param ctx The context
     * @return an instance of <code>{@link POP3MailFolder}</code> containing the attributes from given POP3 folder
     * @throws MailException If conversion fails
     */
    public static POP3MailFolder convertFolder(final Folder pop3Folder, final Session session, final Context ctx) throws MailException {
        try {
            final POP3MailFolder mailFolder = new POP3MailFolder();
            // Subscription not supported by POP3, so every folder is "subscribed"
            mailFolder.setSubscribed(true);
            mailFolder.setSupportsUserFlags(false);
            final boolean isRoot = (pop3Folder instanceof DefaultFolder);
            mailFolder.setRootFolder(isRoot);
            final boolean exists = pop3Folder.exists();
            mailFolder.setExists(exists);
            mailFolder.setSeparator(pop3Folder.getSeparator());
            final String pop3Fullname = pop3Folder.getFullName();
            if (isRoot) {
                // Only the default folder contains subfolders, to be more precise it only contains the INBOX folder.
                mailFolder.setSubfolders(true);
                mailFolder.setSubscribedSubfolders(true);
                mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
                mailFolder.setParentFullname(null);
                mailFolder.setName(MailFolder.DEFAULT_FOLDER_NAME);
                mailFolder.setHoldsFolders(true);
                mailFolder.setHoldsMessages(false);
                {
                    final MailPermission ownPermission = new DefaultMailPermission();
                    ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
                    ownPermission.setAllObjectPermission(
                        OCLPermission.NO_PERMISSIONS,
                        OCLPermission.NO_PERMISSIONS,
                        OCLPermission.NO_PERMISSIONS);
                    ownPermission.setFolderAdmin(false);
                    mailFolder.setOwnPermission(ownPermission);
                    mailFolder.addPermission(ownPermission);
                }
                mailFolder.setDefaultFolder(false);
                mailFolder.setMessageCount(-1);
                mailFolder.setNewMessageCount(-1);
                mailFolder.setUnreadMessageCount(-1);
                mailFolder.setDeletedMessageCount(-1);
            } else {
                // A POP3 folder does not contain subfolders
                mailFolder.setSubfolders(false);
                mailFolder.setSubscribedSubfolders(false);
                mailFolder.setFullname(pop3Fullname);
                mailFolder.setParentFullname(MailFolder.DEFAULT_FOLDER_ID);
                mailFolder.setName(pop3Folder.getName());
                mailFolder.setHoldsFolders(false);
                mailFolder.setHoldsMessages(true);
                {
                    final MailPermission ownPermission = new DefaultMailPermission();
                    ownPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
                    mailFolder.setOwnPermission(ownPermission);
                    mailFolder.addPermission(ownPermission);
                }
                // Can only be INBOX folder, no other folder permitted/supported
                mailFolder.setDefaultFolder(true);
                // TODO: NEW and DELETED message must be calculated!
                mailFolder.setMessageCount(pop3Folder.getMessageCount());
                mailFolder.setNewMessageCount(pop3Folder.getMessageCount());
                mailFolder.setUnreadMessageCount(pop3Folder.getUnreadMessageCount());
                mailFolder.setDeletedMessageCount(pop3Folder.getDeletedMessageCount());
            }
            return mailFolder;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

}
