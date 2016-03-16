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

package com.openexchange.api2;

import java.sql.Timestamp;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * FolderSQLInterface for OX Folders
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface FolderSQLInterface {

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible root folders
     */
    SearchIterator<FolderObject> getRootFolderForUser() throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible subfolders of parent folder whose ID matches given
     *         <tt>parentId</tt>
     */
    SearchIterator<FolderObject> getSubfolders(int parentId, Timestamp since) throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible ancestor folders of folder whose ID matches
     *         <tt>folderId</tt> and which are located on path to root folder.
     */
    SearchIterator<FolderObject> getPathToRoot(int folderId) throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible shared folders
     */
    SearchIterator<FolderObject> getSharedFoldersFrom(int owner, Timestamp since) throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible public <b>calendar</b> folders which cannot be
     *         shown in a hierarchical tree-view cause any ancestore is not
     *         visible to user
     */
    SearchIterator<FolderObject> getNonTreeVisiblePublicCalendarFolders() throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible public <b>task</b> folders which cannot be shown in
     *         a hierarchical tree-view cause any ancestore is not visible to
     *         user
     */
    SearchIterator<FolderObject> getNonTreeVisiblePublicTaskFolders() throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible public <b>contact</b> folders which cannot be shown
     *         in a hierarchical tree-view cause any ancestore is not visible to
     *         user
     */
    SearchIterator<FolderObject> getNonTreeVisiblePublicContactFolders() throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible public <b>infostore</b> folders which cannot be
     *         shown in a hierarchical tree-view cause any ancestore is not
     *         visible to user
     */
    SearchIterator<FolderObject> getNonTreeVisiblePublicInfostoreFolders() throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible folders that have been modified (created or edited)
     *         since given timestamp
     */
    SearchIterator<FolderObject> getModifiedUserFolders(Date since) throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all folders
     *         that have been modified (created or edited) since given timestamp
     *         regardless of user's permissions
     */
    SearchIterator<FolderObject> getAllModifiedFolders(Date since) throws OXException;

    /**
     * @return an instance of <tt>SearchIterator</tt> containing all
     *         user-visible folders that have been deleted since given timestamp
     */
    SearchIterator<FolderObject> getDeletedFolders(Date since) throws OXException;

    /**
     * @return an instance of <tt>FolderObject</tt> which matches to given
     *         <tt>id</tt>
     */
    FolderObject getFolderById(int id) throws OXException;

    /**
     * @return an instance of <tt>FolderObject</tt> which is of module
     *         infostore and is marked as user's default folder
     */
    FolderObject getUsersInfostoreFolder() throws OXException;

    /**
     * Either creates (if folder does not exist, yet) or changes a folder
     *
     * @return an instance of <tt>FolderObject</tt> that represents the newly
     *         created or modified <tt>FolderObject</tt> object
     */
    FolderObject saveFolderObject(FolderObject folderobject, Date clientLastModified) throws OXException;

    /**
     * Deletes a folder
     *
     * @return the id of the deleted <tt>FolderObject</tt> object
     */
    int deleteFolderObject(FolderObject folderobject, Date clientLastModified) throws OXException;

    /**
     * Deletes all items located in given folder
     *
     * @param delFolderObj -
     *            the unique ID of the folder whose content should be deleted
     * @param clientLastModified -
     *            the client's last modified timestamp
     * @return the ID of cleared folder as an <code>int</code>
     * @throws OXException
     */
    int clearFolder(FolderObject delFolderObj, Date clientLastModified) throws OXException;
}
