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

package com.openexchange.file.storage;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.secret.SecretExceptionCodes;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;


/**
 * {@link SecretAwareFileStorageAccountManager} - An account manager that ensures a non-empty secret string when serving a
 * {@link #getAccounts(Session)} call.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SecretAwareFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * Gets a new {@code SecretAwareFileStorageAccountManager} instance.
     *
     * @param manager The backing account manager
     * @return The secret-aware account manager or <code>null</code>
     */
    public static SecretAwareFileStorageAccountManager newInstanceFor(FileStorageAccountManager manager) {
        return null == manager ? null : new SecretAwareFileStorageAccountManager(manager);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    /** The delegate */
    private final FileStorageAccountManager manager;

    /**
     * Initializes a new {@link SecretAwareFileStorageAccountManager}.
     */
    private SecretAwareFileStorageAccountManager(FileStorageAccountManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        return manager.addAccount(account, session);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        manager.updateAccount(account, session);
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        manager.deleteAccount(account, session);
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        SecretService secretService = Services.getOptionalService(SecretService.class);
        if (null != secretService && Strings.isEmpty(secretService.getSecret(session))) {
            // The OAuth-based file storage needs a valid secret string for operation
            return Collections.emptyList();
        }
        try {
            return manager.getAccounts(session);
        } catch (OXException e) {
            if (!SecretExceptionCodes.EMPTY_SECRET.equals(e)) {
                throw e;
            }
            // The OAuth-based file storage needs a valid secret string for operation
            return Collections.emptyList();
        }
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        return manager.getAccount(id, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        manager.cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        manager.removeUnrecoverableItems(secret, session);
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        manager.migrateToNewSecret(oldSecret, newSecret, session);
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return manager.hasEncryptedItems(session);
    }

}
