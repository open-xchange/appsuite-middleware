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

package com.openexchange.file.storage.infostore.internal;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link InfostoreAccountAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final ServerSession session;
    private final InfostoreFileStorageService service;
    private InfostoreFolderAccess folders;
    private InfostoreAdapterFileAccess files;

    public InfostoreAccountAccess(final Session session, final InfostoreFileStorageService service) throws OXException {
        this.session = ServerSessionAdapter.valueOf(session);
        this.service = service;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        return FileStorageCapabilityTools.supportsByClass(InfostoreAdapterFileAccess.class, capability);
    }

    @Override
    public String getAccountId() {
        return InfostoreDefaultAccountManager.DEFAULT_ID;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if(files != null) {
            return files;
        }
        return files = new InfostoreAdapterFileAccess(session, service.getInfostore(), service.getSearch(), this);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if(folders != null) {
            return folders;
        }
        return folders = new InfostoreFolderAccess(session, service.getInfostore());
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolderAccess().getRootFolder();
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public void close() {
        // Nope
    }

    @Override
    public void connect() throws OXException {
        // Bypassed...
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

}
