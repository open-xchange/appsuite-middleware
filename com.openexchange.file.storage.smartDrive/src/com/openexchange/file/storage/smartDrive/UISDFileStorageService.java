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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.file.storage.smartDrive;

import java.util.Collections;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.smartDrive.services.Services;
import com.openexchange.session.Session;


/**
 * {@link UISDFileStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UISDFileStorageService implements FileStorageService {

    public static UISDFileStorageService newInstance() throws FileStorageException {
        UISDFileStorageService service = new UISDFileStorageService();
        return service;
    }

    private FileStorageAccountManager accountManager;
    
    private void applyAccountManager() throws FileStorageException {
        this.accountManager = Services.accountManagerFor(this);
    }

    public FileStorageAccountManager getAccountManager() throws FileStorageException {
        if(accountManager == null) {
            applyAccountManager();
        }
        return accountManager;
    }

    public String getDisplayName() {
        return "Smart Drive";
    }

    public String getId() {
        return UISDConstants.ID;
    }

    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws FileStorageException {
        FileStorageAccount account = accountManager.getAccount(accountId, session);
        
        return new UISDAccountAccess(this, account, session);
    }

    public DynamicFormDescription getFormDescription() {
        return null;
    }

}
