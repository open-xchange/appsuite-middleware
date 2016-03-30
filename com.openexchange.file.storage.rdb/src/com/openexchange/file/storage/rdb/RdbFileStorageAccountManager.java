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

package com.openexchange.file.storage.rdb;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.rdb.internal.CachingFileStorageAccountStorage;
import com.openexchange.session.Session;

/**
 * {@link RdbFileStorageAccountManager} - The default file storage account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class RdbFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * The file storage account storage cache.
     */
    private static final CachingFileStorageAccountStorage CACHE = CachingFileStorageAccountStorage.getInstance();

    /**
     * The identifier of associated file storage service.
     */
    private final String serviceId;

    /**
     * The file storage service.
     */
    private final FileStorageService service;

    /**
     * Initializes a new {@link RdbFileStorageAccountManager}.
     *
     * @param service The file storage service
     */
    public RdbFileStorageAccountManager(final FileStorageService service) {
        super();
        serviceId = service.getId();
        this.service = service;
    }

    /**
     * Gets the appropriate file storage account manager for specified account identifier and session.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The file storage account manager or <code>null</code>
     * @throws OXException If retrieval fails
     */
    public static RdbFileStorageAccountManager getAccountById(final String accountId, final Session session) throws OXException {
        try {
            final int id = Integer.parseInt(accountId);
            if (id < 0) {
                // Unsupported account identifier
                return null;
            }
            final FileStorageAccount account = CACHE.getAccount(id, session);
            return null == account ? null : new RdbFileStorageAccountManager(account.getFileStorageService());
        } catch (final NumberFormatException e) {
            // Unsupported account identifier
            return null;
        }
    }

    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        return CACHE.getAccount(serviceId, Integer.parseInt(id), session);
    }

    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        return CACHE.getAccounts(serviceId, session);
    }

    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        return String.valueOf(CACHE.addAccount(serviceId, account, session));
    }

    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        CACHE.deleteAccount(serviceId, account, session);
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        CACHE.updateAccount(serviceId, account, session);
    }

    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        return CACHE.hasEncryptedItems(service, session);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        CACHE.migrateToNewSecret(service, oldSecret, newSecret, session);
    }

    @Override
    public void cleanUp(final String secret, final Session session) throws OXException {
        CACHE.cleanUp(service, secret, session);
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        CACHE.removeUnrecoverableItems(service, secret, session);        
    }

}
