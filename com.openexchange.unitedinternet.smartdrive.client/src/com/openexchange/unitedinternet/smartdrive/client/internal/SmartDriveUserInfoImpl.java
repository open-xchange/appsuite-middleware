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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import com.openexchange.unitedinternet.smartdrive.client.SmartDriveUserInfo;

/**
 * {@link SmartDriveUserInfoImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SmartDriveUserInfoImpl implements SmartDriveUserInfo {

    private String attachmentDir;

    private String trashDir;

    private String videoDir;

    private String musicDir;

    private String docDir;

    private String pictureDir;

    private String rootDir;

    private int maxFilesPerDirectory;

    private int trafficOwnerQuota;

    private int storageFotoalbum;

    private int storageQuota;

    private int storageSmartDrive;

    private int maxFileCount;

    private int storageFileCount;

    private int trafficGuestQuota;

    private int trafficGuestUsed;

    private int maxFileSize;

    private int storageFreemail;

    private int trafficOwnerUsed;

    private int trafficUpload;

    private int trafficUploadQuota;

    private int maxFileNameLength;

    private String mountDir;

    /**
     * Initializes a new {@link SmartDriveUserInfoImpl}.
     */
    public SmartDriveUserInfoImpl() {
        super();
    }

    public int getMaxFileNameLength() {
        return maxFileNameLength;
    }

    public int getStorageFreemail() {
        return storageFreemail;
    }

    public int getTrafficOwnerUsed() {
        return trafficOwnerUsed;
    }

    public int getTrafficUpload() {
        return trafficUpload;
    }

    public int getTrafficUploadQuota() {
        return trafficUploadQuota;
    }

    public int getTrafficGuestQuota() {
        return trafficGuestQuota;
    }

    public int getTrafficGuestUsed() {
        return trafficGuestUsed;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public int getStorageSmartDrive() {
        return storageSmartDrive;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public int getStorageFileCount() {
        return storageFileCount;
    }

    public int getStorageQuota() {
        return storageQuota;
    }

    public int getStorageFotoalbum() {
        return storageFotoalbum;
    }

    public int getTrafficOwnerQuota() {
        return trafficOwnerQuota;
    }

    public int getMaxFilesPerDirectory() {
        return maxFilesPerDirectory;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getPictureDir() {
        return pictureDir;
    }

    public String getMountDir() {
        return mountDir;
    }

    public String getDocDir() {
        return docDir;
    }

    public String getMusicDir() {
        return musicDir;
    }

    public String getVideoDir() {
        return videoDir;
    }

    public String getTrashDir() {
        return trashDir;
    }

    public String getAttachmentDir() {
        return attachmentDir;
    }

    /**
     * Sets the attachmentDir
     * 
     * @param attachmentDir The attachmentDir to set
     */
    public void setAttachmentDir(final String attachmentDir) {
        this.attachmentDir = attachmentDir;
    }

    /**
     * Sets the trashDir
     * 
     * @param trashDir The trashDir to set
     */
    public void setTrashDir(final String trashDir) {
        this.trashDir = trashDir;
    }

    /**
     * Sets the mountDir
     * 
     * @param mountDir The mountDir to set
     */
    public void setMountDir(final String mountDir) {
        this.mountDir = mountDir;
    }

    /**
     * Sets the videoDir
     * 
     * @param videoDir The videoDir to set
     */
    public void setVideoDir(final String videoDir) {
        this.videoDir = videoDir;
    }

    /**
     * Sets the musicDir
     * 
     * @param musicDir The musicDir to set
     */
    public void setMusicDir(final String musicDir) {
        this.musicDir = musicDir;
    }

    /**
     * Sets the docDir
     * 
     * @param docDir The docDir to set
     */
    public void setDocDir(final String docDir) {
        this.docDir = docDir;
    }

    /**
     * Sets the pictureDirc
     * 
     * @param pictureDir The pictureDirc to set
     */
    public void setPictureDir(final String pictureDir) {
        this.pictureDir = pictureDir;
    }

    /**
     * Sets the rootDir
     * 
     * @param rootDir The rootDir to set
     */
    public void setRootDir(final String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * Sets the maxFilesPerDirectory
     * 
     * @param maxFilesPerDirectory The maxFilesPerDirectory to set
     */
    public void setMaxFilesPerDirectory(final int maxFilesPerDirectory) {
        this.maxFilesPerDirectory = maxFilesPerDirectory;
    }

    /**
     * Sets the trafficOwnerQuota
     * 
     * @param trafficOwnerQuota The trafficOwnerQuota to set
     */
    public void setTrafficOwnerQuota(final int trafficOwnerQuota) {
        this.trafficOwnerQuota = trafficOwnerQuota;
    }

    /**
     * Sets the storageFotoalbum
     * 
     * @param storageFotoalbum The storageFotoalbum to set
     */
    public void setStorageFotoalbum(final int storageFotoalbum) {
        this.storageFotoalbum = storageFotoalbum;
    }

    /**
     * Sets the storageQuota
     * 
     * @param storageQuota The storageQuota to set
     */
    public void setStorageQuota(final int storageQuota) {
        this.storageQuota = storageQuota;
    }

    /**
     * Sets the storageSmartDrive
     * 
     * @param storageSmartDrive The storageSmartDrive to set
     */
    public void setStorageSmartDrive(final int storageSmartDrive) {
        this.storageSmartDrive = storageSmartDrive;
    }

    /**
     * Sets the maxFileCount
     * 
     * @param maxFileCount The maxFileCount to set
     */
    public void setMaxFileCount(final int maxFileCount) {
        this.maxFileCount = maxFileCount;
    }

    /**
     * Sets the storageFileCount
     * 
     * @param storageFileCount The storageFileCount to set
     */
    public void setStorageFileCount(final int storageFileCount) {
        this.storageFileCount = storageFileCount;
    }

    /**
     * Sets the trafficGuestQuota
     * 
     * @param trafficGuestQuota The trafficGuestQuota to set
     */
    public void setTrafficGuestQuota(final int trafficGuestQuota) {
        this.trafficGuestQuota = trafficGuestQuota;
    }

    /**
     * Sets the trafficGuestUsed
     * 
     * @param trafficGuestUsed The trafficGuestUsed to set
     */
    public void setTrafficGuestUsed(final int trafficGuestUsed) {
        this.trafficGuestUsed = trafficGuestUsed;
    }

    /**
     * Sets the maxFileSize
     * 
     * @param maxFileSize The maxFileSize to set
     */
    public void setMaxFileSize(final int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the storageFreemail
     * 
     * @param storageFreemail The storageFreemail to set
     */
    public void setStorageFreemail(final int storageFreemail) {
        this.storageFreemail = storageFreemail;
    }

    /**
     * Sets the trafficOwnerUsed
     * 
     * @param trafficOwnerUsed The trafficOwnerUsed to set
     */
    public void setTrafficOwnerUsed(final int trafficOwnerUsed) {
        this.trafficOwnerUsed = trafficOwnerUsed;
    }

    /**
     * Sets the trafficUpload
     * 
     * @param trafficUpload The trafficUpload to set
     */
    public void setTrafficUpload(final int trafficUpload) {
        this.trafficUpload = trafficUpload;
    }

    /**
     * Sets the trafficUploadQuota
     * 
     * @param trafficUploadQuota The trafficUploadQuota to set
     */
    public void setTrafficUploadQuota(final int trafficUploadQuota) {
        this.trafficUploadQuota = trafficUploadQuota;
    }

    /**
     * Sets the maxFileNameLength
     * 
     * @param maxFileNameLength The maxFileNameLength to set
     */
    public void setMaxFileNameLength(final int maxFileNameLength) {
        this.maxFileNameLength = maxFileNameLength;
    }

}
