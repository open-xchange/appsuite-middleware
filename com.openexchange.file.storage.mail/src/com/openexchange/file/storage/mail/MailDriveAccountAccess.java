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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.file.storage.mail;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.mail.FullName.Type;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link MailDriveAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final FullNameCollection fullNameCollection;
    private final Session session;
    private final FileStorageService service;
    private volatile boolean connected;

    /**
     * Initializes a new {@link MailDriveAccountAccess}.
     *
     * @throws OXException If initialization fails
     */
    public MailDriveAccountAccess(FullNameCollection fullNameCollection, FileStorageService service, Session session) {
        super();
        this.fullNameCollection = fullNameCollection;
        this.service = service;
        this.session = session;
    }

    /**
     * Gets the collection of full names for virtual attachment folders.
     *
     * @return The collection of full names for virtual attachment folders
     */
    public FullNameCollection getFullNameCollection() {
        return fullNameCollection;
    }

    /**
     * Checks given folder identifier.
     *
     * @param folderId The folder identifier to check
     * @param fullNameCollection The collection of full names for virtual attachment folders
     * @return The associated full name or <code>null</code> if folder identifier is invalid
     */
    public static FullName optFolderId(String folderId, FullNameCollection fullNameCollection) {
        Type type = Type.typeByFolderId(folderId);
        return null == type ? null : fullNameCollection.getFullNameFor(type);
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        return FileStorageCapabilityTools.supportsByClass(MailDriveFileAccess.class, capability);
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public boolean ping() throws OXException {
        return MailAccess.getInstance(session).ping();
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public String getAccountId() {
        return MailDriveConstants.ACCOUNT_ID;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (!connected) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new MailDriveFileAccess(fullNameCollection, session, this);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (!connected) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new MailDriveFolderAccess(fullNameCollection, session);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        connect();
        return getFolderAccess().getRootFolder();
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

}
