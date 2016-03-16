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

package com.openexchange.file.storage.fs;

import java.io.File;
import java.util.HashMap;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.FileStorageServiceFactory;
import com.openexchange.file.storage.SingleAccountManager;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link FSFileStorageServiceFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FSFileStorageServiceFactory implements FileStorageServiceFactory {
    
    private final File file;
    private final ServiceLookup services;

    public FSFileStorageServiceFactory(ServiceLookup serviceLookup, File file) {
        super();
        this.services = serviceLookup;
        this.file = file;
        
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) {
        return new FSAccountAccess(accountId, file, session, this);
    }

    @Override
    public FileStorageAccountManager getAccountManager() {
        
        return new SingleAccountManager(this) {
            
            @Override
            public FileStorageAccount getAccount(Session session) {
                DefaultFileStorageAccount account = new DefaultFileStorageAccount();
                account.setConfiguration(new HashMap<String, Object>());
                account.setDisplayName("fs");
                account.setFileStorageService(getFileStorageService());
                account.setId("1");
                account.setServiceId(getFileStorageService().getId());
                return account;
            }
        };
    }

    @Override
    public FileStorageFileAccess getFileAccess(Session session, String accountId) {
        return new FSFileAccess(file, session, new FSAccountAccess(accountId, file, session, this));
    }

    @Override
    public FileStorageFolderAccess getFolderAccess(Session session, String accountId) {
        return new FSFolderAccess(file, session);
    }

    @Override
    public FileStorageService getFileStorageService() {
        return new FileSystemFileStorageService(services, file);
    }

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }

}
