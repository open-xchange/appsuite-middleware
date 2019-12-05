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

package com.openexchange.file.storage.webdav;

import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link CapabilityAwareAccountManager} - a {@link FileStorageAccountManager} which checks if the user has the appropriate capability to add or update an account.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class CapabilityAwareAccountManager implements FileStorageAccountManager {

    private final String cap;
    private final String id;
    private final CapabilityService capService;
    private final FileStorageAccountManager delegate;
    
    /**
     * Initializes a new {@link CapabilityAwareAccountManager}.
     * 
     * @param cap The capability to check
     * @param id The id of the file storage
     * @param capService The {@link CapabilityService}
     * @param delegate The {@link FileStorageAccountManager} which performs the operations if allowed.
     */
    public CapabilityAwareAccountManager(@NonNull String cap, @NonNull String id, @NonNull CapabilityService capService, @NonNull FileStorageAccountManager delegate) {
        super();
        this.cap = cap;
        this.capService = capService;
        this.id = id;
        this.delegate = delegate;
    }
    
    private void checkCapability(Session session) throws OXException {
        if(false == capService.getCapabilities(session).contains(cap)) {
            throw WebdavExceptionCodes.MISSING_CAPABILITY.create(id);
        }
    }
    
    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        checkCapability(session);
        return delegate.addAccount(account, session);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        checkCapability(session);
        delegate.updateAccount(account, session);
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        delegate.deleteAccount(account, session);
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return delegate.getAccounts(session);
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        return delegate.getAccount(id, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        delegate.cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        delegate.removeUnrecoverableItems(secret, session);
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        delegate.migrateToNewSecret(oldSecret, newSecret, session);
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return delegate.hasEncryptedItems(session);
    }
}
