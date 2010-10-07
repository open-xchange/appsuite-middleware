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

package com.openexchange.unitedinternet.smartdrive.client;

/**
 * {@link SmartDriveUserInfo}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SmartDriveUserInfo {

    /**
     * Gets the max. file name length
     * 
     * @return The max. file name length
     */
    int getMaxFileNameLength();

    /**
     * Gets the storage freemail
     * 
     * @return The storage freemail
     */
    int getStorageFreemail();

    /**
     * Gets the traffic owner used
     * 
     * @return The traffic owner used
     */
    int getTrafficOwnerUsed();

    /**
     * Gets the traffic upload
     * 
     * @return The traffic upload
     */
    int getTrafficUpload();

    /**
     * Gets the traffic upload quota
     * 
     * @return The traffic upload quota
     */
    int getTrafficUploadQuota();

    /**
     * Gets the traffic guest quota
     * 
     * @return The traffic guest quota
     */
    int getTrafficGuestQuota();

    /**
     * Gets the traffic guest used
     * 
     * @return The traffic guest used
     */
    int getTrafficGuestUsed();

    /**
     * Gets the max. file size
     * 
     * @return The max. file size
     */
    int getMaxFileSize();

    /**
     * Gets the storage SmartDrive
     * 
     * @return The storage SmartDrive
     */
    int getStorageSmartDrive();

    /**
     * Gets the max. file count.
     * 
     * @return The max. file count
     */
    int getMaxFileCount();

    /**
     * Gets the actual number of resources
     * 
     * @return The storage file count
     */
    int getStorageFileCount();

    /**
     * Gets the storage quota
     * 
     * @return The storage quota
     */
    int getStorageQuota();

    /**
     * Gets the storage photo album
     * 
     * @return The storage photo album
     */
    int getStorageFotoalbum();

    /**
     * Gets the traffic
     * 
     * @return The traffic
     */
    int getTrafficOwnerQuota();

    /**
     * Gets the max. files per directory
     * 
     * @return The max. files per directory
     */
    int getMaxFilesPerDirectory();

    /**
     * Gets the root directory. Mostly <code>"/"</code>.
     * 
     * @return The root directory
     */
    String getRootDir();

    /**
     * Gets the picture directory.
     * 
     * @return The picture directory
     */
    String getPictureDir();

    /**
     * Gets the mount directory.
     * 
     * @return The mount directory
     */
    String getMountDir();

    /**
     * Gets the documents directory.
     * 
     * @return The documents directory
     */
    String getDocDir();

    /**
     * Gets the music directory.
     * 
     * @return The music directory
     */
    String getMusicDir();

    /**
     * Gets the video directory.
     * 
     * @return The video directory
     */
    String getVideoDir();

    /**
     * Gets the trash directory.
     * 
     * @return The trash directory
     */
    String getTrashDir();

    /**
     * Gets the attachment directory.
     * 
     * @return The attachment directory
     */
    String getAttachmentDir();

}
