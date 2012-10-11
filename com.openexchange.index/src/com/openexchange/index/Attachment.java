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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index;

import java.io.InputStream;

/**
 * {@link Attachment}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Attachment {
    
    private String uuid;

    private int module;

    private String service;

    private String account;

    private String folder;

    private String id;

    private String fileName;

    private long fileSize;

    private String mimeType;

    private String md5Sum;

    private InputStream file;
    

    public Attachment() {
        super();
    }
    
    /**
     * Gets the uuid
     *
     * @return The uuid
     */
    public String getUuid() {
        return uuid;
    }
    
    /**
     * Sets the uuid
     *
     * @param uuid The uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the module
     * 
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Sets the module
     * 
     * @param module The module to set
     */
    public void setModule(int module) {
        this.module = module;
    }

    /**
     * Gets the service
     * 
     * @return The service
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the service
     * 
     * @param service The service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Gets the account
     * 
     * @return The account
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the account
     * 
     * @param account The account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Gets the folder
     * 
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the folder
     * 
     * @param folder The folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Gets the id
     * 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     * 
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the fileName
     * 
     * @return The fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the fileName
     * 
     * @param fileName The fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the fileSize
     * 
     * @return The fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the fileSize
     * 
     * @param fileSize The fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets the mimeType
     * 
     * @return The mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mimeType
     * 
     * @param mimeType The mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Gets the md5Sum
     * 
     * @return The md5Sum
     */
    public String getMd5Sum() {
        return md5Sum;
    }

    /**
     * Sets the md5Sum
     * 
     * @param md5Sum The md5Sum to set
     */
    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    /**
     * Gets the file
     * 
     * @return The file
     */
    public InputStream getFile() {
        return file;
    }

    /**
     * Sets the file
     * 
     * @param file The file to set
     */
    public void setFile(InputStream file) {
        this.file = file;
    }

}
