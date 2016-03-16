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

package com.openexchange.folderstorage.mail;

import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link MailFolderType} - The folder type for mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderType implements FolderType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailFolderType.class);

    private static final int LEN = MailFolder.DEFAULT_FOLDER_ID.length();

    private static final MailFolderType instance = new MailFolderType();

    /**
     * Gets the {@link MailFolderType} instance.
     *
     * @return The {@link MailFolderType} instance
     */
    public static MailFolderType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link MailFolderType}.
     */
    private MailFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    @Override
    public boolean servesFolderId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (!folderId.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return false;
        }
        final int len = folderId.length();
        final char separator = '/';
        int index = LEN;
        while (index < len && folderId.charAt(index) != separator) {
            index++;
        }
        // Parse account ID
        if (index != LEN) {
            try {
                Integer.parseInt(folderId.substring(LEN, index));
            } catch (final NumberFormatException e) {
                final IllegalArgumentException err = new IllegalArgumentException("Mail account is not a number: " + folderId);
                err.initCause(e);
                LOG.warn("Ignoring invalid folder identifier", err);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean servesParentId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (PRIVATE_FOLDER_ID.equals(folderId)) {
            return true;
        }
        if (!folderId.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return false;
        }
        final int len = folderId.length();
        final char separator = '/';
        int index = LEN;
        while (index < len && folderId.charAt(index) != separator) {
            index++;
        }
        // Parse account ID
        if (index != LEN) {
            try {
                Integer.parseInt(folderId.substring(LEN, index));
            } catch (final NumberFormatException e) {
                final IllegalArgumentException err = new IllegalArgumentException("Mail account is not a number: " + folderId);
                err.initCause(e);
                LOG.error("", err);
                return false;
            }
        }
        return true;
    }

}
