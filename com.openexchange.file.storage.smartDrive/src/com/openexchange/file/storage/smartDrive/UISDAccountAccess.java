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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.smartDrive.services.Services;
import com.openexchange.session.Session;
import com.openexchange.smartdrive.client.SmartDriveAccess;
import com.openexchange.smartdrive.client.SmartDriveException;
import com.openexchange.smartdrive.client.SmartDriveStatefulAccess;
import com.openexchange.smartdrive.client.SmartDriveStatelessAccess;


/**
 * {@link UISDAccountAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UISDAccountAccess implements FileStorageAccountAccess {

    private UISDFileStorageService fss;
    private FileStorageAccount account;
    private Session session;
    private UISDFileAccess fileAccess;
    private UISDFolderAccess folderAccess;
    private SmartDriveAccess smartDrive;

    public UISDAccountAccess(UISDFileStorageService fileStorageService, FileStorageAccount account, Session session) {
        this.fss = fileStorageService;
        this.account = account;
        this.session = session;
        this.fileAccess = new UISDFileAccess(this);
        this.folderAccess = new UISDFolderAccess(this);
    }

    public String getAccountId() {
        return account.getId();
    }

    public FileStorageFileAccess getFileAccess() throws FileStorageException {
        return fileAccess;
    }

    public FileStorageFolderAccess getFolderAccess() throws FileStorageException {
        return folderAccess;
    }

    public FileStorageFolder getRootFolder() throws FileStorageException {
        return folderAccess.getRootFolder();
    }

    public FileStorageService getService() {
        return fss;
    }

    public boolean cacheable() {
        return false;
    }

    public void close() {
        smartDrive = null;
    }

    public void connect() throws FileStorageException {
        if(smartDrive != null) {
            return;
        }
        if(session.getParameter(UISDConstants.SMART_DRIVE_SESSION_KEY) != null) {
            smartDrive = (SmartDriveAccess) session.getParameter(UISDConstants.SMART_DRIVE_SESSION_KEY);
            return;
        }
        Map<String, Object> configuration = account.getConfiguration();
        String userName = (String) configuration.get("login");
        if(userName == null) {
            userName = session.getUserlogin();
        }
        
        String url = (String) configuration.get("url");
        
        if(configuration.get("password") == null) {
            configuration = new HashMap<String, Object>(configuration);
            configuration.put("password", session.getPassword());
        }
        
        try {
            smartDrive = Services.getSmartDriveFactory().createSmartDriveAccess(userName, url, configuration);
            smartDrive.getStatefulAccess();
            session.setParameter(UISDConstants.SMART_DRIVE_SESSION_KEY, smartDrive);
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }
    }

    public boolean isConnected() {
        return smartDrive != null;
    }

    public boolean ping() throws FileStorageException {
        return isConnected();
    }
    
    public SmartDriveStatelessAccess getStatelessAccess() throws FileStorageException {
        try {
            return smartDrive.getStatelessAccess();
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }
    }
    
    public SmartDriveStatefulAccess getStatefulAccess() throws FileStorageException {
        try {
            return smartDrive.getStatefulAccess();
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }
    }

    public int getUserId() {
        return session.getUserId();
    }

    public FileStorageAccount getAccount() {
        return account;
    }

}
