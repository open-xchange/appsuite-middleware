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

package com.openexchange.mail.twitter.converters;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.twitter.dataobjects.TwitterMailFolder;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;

/**
 * {@link TwitterFolderConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterFolderConverter {

    /**
     * Prevent instantiation
     */
    private TwitterFolderConverter() {
        super();
    }

    /**
     * Gets the twitter INBOX folder.
     *
     * @param session The user's session
     * @return The twitter INBOX folder
     * @throws OXException If twitter INBOX folder cannot be returned
     */
    public static TwitterMailFolder getINBOXFolder(final Session session) throws OXException {
        final TwitterMailFolder mailFolder = new TwitterMailFolder();
        mailFolder.setRootFolder(false);
        mailFolder.setExists(true);
        mailFolder.setSeparator('/');
        mailFolder.setSubfolders(false);
        mailFolder.setSubscribedSubfolders(false);
        /*
         * Set fullname, name, and parent fullname
         */
        mailFolder.setFullname("INBOX");
        mailFolder.setName("Inbox");
        mailFolder.setParentFullname(MailFolder.DEFAULT_FOLDER_ID);
        mailFolder.setDefaultFolder(true);
        mailFolder.setDefaultFolderType(DefaultFolderType.INBOX);
        /*
         * INBOX folder only holds messages but no folders
         */
        mailFolder.setHoldsFolders(false);
        mailFolder.setHoldsMessages(true);
        /*
         * Permission
         */
        final DefaultMailPermission ownPermission = new DefaultMailPermission();
        ownPermission.setEntity(session.getUserId());
        ownPermission.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        ownPermission.setFolderAdmin(false);
        mailFolder.setOwnPermission(ownPermission);
        mailFolder.addPermission(ownPermission);
        /*
         * TODO: Set message counts
         */
        mailFolder.setMessageCount(-1);
        mailFolder.setNewMessageCount(-1);
        mailFolder.setUnreadMessageCount(-1);
        mailFolder.setDeletedMessageCount(-1);
        /*
         * INBOX folder is always subscribed
         */
        mailFolder.setSubscribed(true);
        /*
         * No user flag support
         */
        mailFolder.setSupportsUserFlags(false);
        return mailFolder;
    }

    /**
     * Gets the twitter root folder.
     *
     * @param session The user's session
     * @return The twitter root folder
     */
    public static TwitterMailFolder getRootFolder(final Session session) {
        final TwitterMailFolder mailFolder = new TwitterMailFolder();
        mailFolder.setRootFolder(true);
        mailFolder.setExists(true);
        mailFolder.setSeparator('/');
        mailFolder.setSubfolders(true);
        mailFolder.setSubscribedSubfolders(true);
        /*
         * Set fullname, name, and parent fullname
         */
        mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
        mailFolder.setName(MailFolder.DEFAULT_FOLDER_NAME);
        mailFolder.setParentFullname(null);
        mailFolder.setDefaultFolder(false);
        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
        /*
         * Root folder only holds folders but no messages
         */
        mailFolder.setHoldsFolders(true);
        mailFolder.setHoldsMessages(false);
        /*
         * Permission
         */
        final DefaultMailPermission ownPermission = new DefaultMailPermission();
        ownPermission.setEntity(session.getUserId());
        ownPermission.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        ownPermission.setFolderAdmin(false);
        mailFolder.setOwnPermission(ownPermission);
        mailFolder.addPermission(ownPermission);
        /*
         * Set message counts
         */
        mailFolder.setMessageCount(-1);
        mailFolder.setNewMessageCount(-1);
        mailFolder.setUnreadMessageCount(-1);
        mailFolder.setDeletedMessageCount(-1);
        /*
         * Root folder is always subscribed
         */
        mailFolder.setSubscribed(true);
        /*
         * No user flag support
         */
        mailFolder.setSupportsUserFlags(false);
        return mailFolder;
    }

}
