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

package com.openexchange.file.storage.appsuite;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.Credentials;
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
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link AppsuiteAccountAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class AppsuiteAccountAccess implements CapabilityAware {

    private final FileStorageAccount account;
    private final FileStorageService service;
    private final Session session;
    private final ApiClientService clientFactory;

    private ShareClient appsuiteClient;

    /**
     * Initializes a new {@link AppsuiteAccountAccess}.
     *
     * @param service The {@link FileStorageService}
     * @param clientFactory The {@link ApiClientService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    public AppsuiteAccountAccess(FileStorageService service, ApiClientService clientFactory, FileStorageAccount account, Session session) {
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory must not be null");
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    /**
     * Gets the {@link Session}
     *
     * @return The session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Gets the associated {@link FileStorageAccount}
     *
     * @return The associated account
     */
    public FileStorageAccount getAccount() {
        return this.account;
    }

    @Override
    public String getAccountId() {
        return account.getId();
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        return new AppsuiteFileAccess(this, appsuiteClient);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        connect();
        return new AppsuiteFolderAccess(this, appsuiteClient);
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

    @Override
    public void connect() throws OXException {
        if (isConnected()) {
            return;
        }

        Map<String, Object> configuration = account.getConfiguration();
        String shareUrl = (String) configuration.get(AppsuiteFileStorageConstants.SHARE_URL);
        if (Strings.isEmpty(shareUrl)) {
            throw FileStorageExceptionCodes.INVALID_URL.create("not provided", "empty");
        }

        String password = (String) configuration.get(AppsuiteFileStorageConstants.PASSWORD);
        Optional<Credentials> credentials = password != null ? Optional.of(new Credentials("", password)) : Optional.empty();

        this.appsuiteClient = new ShareClient(session, clientFactory.getApiClient(session, shareUrl, credentials));
    }

    @Override
    public boolean isConnected() {
        return appsuiteClient != null;
    }

    @Override
    public void close() {
        appsuiteClient = null;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            connect();
            appsuiteClient.ping();
            return true;
        } finally {
            close();
        }
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        return FileStorageCapabilityTools.supportsByClass(AppsuiteFileAccess.class, capability);
    }
}
