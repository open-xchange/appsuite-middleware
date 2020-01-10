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

package com.openexchange.gdpr.dataexport.provider.mail.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderInfo;

/**
 * {@link DefaultFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultFolder implements Folder {

    static AtomicReference<Set<String>> FULL_NAMES_TO_IGNORE = new AtomicReference<>(Collections.emptySet());

    /**
     * Sets the full names to ignore.
     *
     * @param fullNamesToIgnore The set containing the full names to ignore
     */
    public static void setFullNamesToIgnore(Set<String> fullNamesToIgnore) {
        if (fullNamesToIgnore == null) {
            return;
        }

        FULL_NAMES_TO_IGNORE.set(ImmutableSet.copyOf(fullNamesToIgnore));
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final FolderAccess folderAccess;
    private final String fullname;
    private final boolean trash;
    private final String name;
    private final boolean subscribed;
    private final boolean holdsMessages;
    private final boolean holdsFolders;
    private final boolean rootFolder;
    private final boolean shared;
    private final boolean publik;

    /**
     * Initializes a new {@link DefaultFolder}.
     *
     * @param folderInfo The folder info
     * @param infoSupport The folder info access
     */
    public DefaultFolder(MailFolderInfo folderInfo, IMailFolderStorageInfoSupport infoSupport) {
        super();
        String fullname = folderInfo.getFullname();
        folderAccess = new MailFolderInfoAccess(fullname, infoSupport);
        this.fullname = fullname;
        trash = folderInfo.isTrash();
        name = folderInfo.getName();
        subscribed = folderInfo.isSubscribed();
        holdsFolders = folderInfo.isHoldsFolders();
        holdsMessages = folderInfo.isHoldsMessages();
        rootFolder = folderInfo.isRootFolder();
        shared = folderInfo.isShared();
        publik = folderInfo.isPublic();
    }

    /**
     * Initializes a new {@link DefaultFolder}.
     *
     * @param folder The folder
     * @param folderStorage The folder access
     */
    public DefaultFolder(MailFolder folder, IMailFolderStorage folderStorage) {
        super();
        String fullname = folder.getFullname();
        folderAccess = new MailFolderAccess(fullname, folderStorage);
        this.fullname = fullname;
        trash = folder.isTrash();
        name = folder.getName();
        subscribed = folder.isSubscribed();
        holdsFolders = folder.isHoldsFolders();
        holdsMessages = folder.isHoldsMessages();
        rootFolder = folder.isRootFolder();
        shared = folder.isShared();
        publik = folder.isPublic();
    }

    @Override
    public String getFullname() {
        return fullname;
    }

    @Override
    public boolean isTrash() {
        return trash;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public boolean isHoldsMessages() {
        return holdsMessages;
    }

    @Override
    public boolean isHoldsFolders() {
        return holdsFolders;
    }

    @Override
    public boolean isRootFolder() {
        return rootFolder;
    }

    @Override
    public boolean isShared() {
        return shared;
    }

    @Override
    public boolean isPublic() {
        return publik;
    }

    @Override
    public List<Folder> getChildren(boolean subscribedOnly) throws OXException {
        return folderAccess.getChildren(subscribedOnly);
    }

    // ------------------------------------------------------ Helpers ----------------------------------------------------------------------

    private static class MailFolderInfoAccess implements FolderAccess {

        private final IMailFolderStorageInfoSupport infoSupport;
        private final String fullname;

        /**
         * Initializes a new {@link MailFolderInfoAccess}.
         *
         * @param fullname The folder full name
         * @param infoSupport The folder info access
         */
        MailFolderInfoAccess(String fullname, IMailFolderStorageInfoSupport infoSupport) {
            super();
            this.infoSupport = infoSupport;
            this.fullname = fullname;
        }

        @Override
        public List<Folder> getChildren(boolean subscribedOnly) throws OXException {
            List<MailFolderInfo> children = infoSupport.getFolderInfos(fullname, subscribedOnly);
            if (children == null) {
                return Collections.emptyList();
            }

            int size = children.size();
            if (size <= 0) {
                return Collections.emptyList();
            }

            Set<String> fullNamesToIgnore = FULL_NAMES_TO_IGNORE.get();
            boolean ignoreFullNamesToIgnore = fullNamesToIgnore.isEmpty();

            List<Folder> retval = new ArrayList<>(size);
            for (MailFolderInfo mailFolderInfo : children) {
                if (ignoreFullNamesToIgnore || !fullNamesToIgnore.contains(mailFolderInfo.getFullname())) {
                    retval.add(new DefaultFolder(mailFolderInfo, infoSupport));
                }
            }
            return retval;
        }
    }

    private static class MailFolderAccess implements FolderAccess {

        private final IMailFolderStorage folderStorage;
        private final String fullname;

        /**
         * Initializes a new {@link MailFolderAccess}.
         *
         * @param fullname The folder full name
         * @param folderStorage The folder access
         */
        MailFolderAccess(String fullname, IMailFolderStorage folderStorage) {
            super();
            this.folderStorage = folderStorage;
            this.fullname = fullname;
        }

        @Override
        public List<Folder> getChildren(boolean subscribedOnly) throws OXException {
            MailFolder[] children = folderStorage.getSubfolders(fullname, !subscribedOnly);
            if (children == null || children.length <= 0) {
                return Collections.emptyList();
            }

            Set<String> fullNamesToIgnore = FULL_NAMES_TO_IGNORE.get();
            boolean ignoreFullNamesToIgnore = fullNamesToIgnore.isEmpty();

            List<Folder> retval = new ArrayList<>(children.length);
            for (MailFolder mailFolder : children) {
                if (ignoreFullNamesToIgnore || !fullNamesToIgnore.contains(mailFolder.getFullname())) {
                    retval.add(new DefaultFolder(mailFolder, folderStorage));
                }
            }
            return retval;
        }

    }

}
