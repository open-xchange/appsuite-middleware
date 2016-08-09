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

package com.openexchange.file.storage.boxcom;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.oauth.API;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.session.Session;

/**
 * {@link BoxAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxAccountAccess implements CapabilityAware {

    private final FileStorageAccount account;
    private final Session session;
    private final FileStorageService service;
    private OAuthAccess boxAccess;

    /**
     * Initializes a new {@link BoxAccountAccess}.
     */
    public BoxAccountAccess(FileStorageService service, FileStorageAccount account, Session session) {
        super();
        this.service = service;
        this.account = account;
        this.session = session;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        return FileStorageCapabilityTools.supportsByClass(BoxFileAccess.class, capability);
    }

    /**
     * Gets the associated account
     *
     * @return The account
     */
    public FileStorageAccount getAccount() {
        return account;
    }

    @Override
    public void connect() throws OXException {
        OAuthAccessRegistryService service = Services.getService(OAuthAccessRegistryService.class);
        OAuthAccessRegistry registry = service.get(API.BOX_COM.getFullName());
        boxAccess = registry.get(session.getContextId(), session.getUserId());
        if (boxAccess == null) {
            BoxOAuthAccess access = new BoxOAuthAccess(account, session);
            registry.add(session.getContextId(), session.getUserId(), access);
            if (boxAccess == null) {
                boxAccess = access;
            }
        } else {
            ((BoxOAuthAccess) boxAccess).ensureNotExpired(session);
        }
        //boxAccess = BoxAccess.accessFor(account, session);
    }

    @Override
    public boolean isConnected() {
        return null != boxAccess;
    }

    @Override
    public void close() {
        boxAccess = null;
    }

    @Override
    public boolean ping() throws OXException {
        return boxAccess.ping();
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public String getAccountId() {
        return account.getId();
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (null == boxAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new BoxFileAccess((BoxOAuthAccess) boxAccess, account, session, this);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (null == boxAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new BoxFolderAccess((BoxOAuthAccess) boxAccess, account, session, this);
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
