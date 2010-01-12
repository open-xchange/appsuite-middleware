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

package com.openexchange.messaging;

/**
 * {@link MessagingFolderAccess} - Provides access to folder storage.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingFolderAccess {

    /**
     * Checks if a folder exists whose fullname matches given <code>fullname</code>
     * 
     * @param fullname The fullname
     * @return <code>true</code> if folder exists in account; otherwise <code>false</code>
     * @throws MessagingException If existence cannot be checked
     */
    public boolean exists(final String fullname) throws MessagingException;

    /**
     * Gets the folder identified through given fullname
     * 
     * @param fullname The fullname
     * @return The corresponding instance of {@link MessagingFolder}
     * @throws MessagingException If either folder does not exist or could not be fetched
     */
    public MessagingFolder getFolder(final String fullname) throws MessagingException;

    /**
     * Gets the first level subfolders located below the folder whose fullname matches given parameter <code>parentFullname</code>.
     * <p>
     * If no subfolders exist below identified folder the constant {@link #EMPTY_PATH} should be returned.
     * 
     * @param parentFullname The parent fullname
     * @param all Whether all or only subscribed subfolders shall be returned. If underlying messaging system does not support folder
     *            subscription, this argument should always be treated as <code>true</code>.
     * @return An array of {@link MessagingFolder} representing the subfolders
     * @throws MessagingException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public MessagingFolder[] getSubfolders(final String parentFullname, final boolean all) throws MessagingException;

    /**
     * Gets the account's root folder.
     * 
     * @return The account's root folder
     * @throws MessagingException If account's default folder cannot be delivered
     */
    public MessagingFolder getRootFolder() throws MessagingException;

    /**
     * Checks user's default folder as defined in user's messaging settings and creates them if any is missing.
     * <p>
     * See also {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedSpam() createConfirmedSpam()},
     * {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedHam() createConfirmedHam()}, and
     * {@link com.openexchange.spamhandler.SpamHandler#isUnsubscribeSpamFolders() unsubscribeSpamFolders()}.
     * 
     * @throws MessagingException If user's default folder could not be checked
     */
    // public void checkDefaultFolders() throws MessagingException;

    /**
     * Creates a new messaging folder with attributes taken from given messaging folder description
     * 
     * @param toCreate The messaging folder to create
     * @return The fullname of the created messaging folder
     * @throws MessagingException If creation fails
     */
    public String createFolder(MessagingFolder toCreate) throws MessagingException;

    /**
     * Updates an existing messaging folder identified through given fullname. All attributes set in given messaging folder instance are
     * applied.
     * <p>
     * The currently known attributes that make sense being updated are:
     * <ul>
     * <li>permissions</li>
     * <li>subscription</li>
     * </ul>
     * Of course more folder attributes may be checked by implementation to enhance update operations.
     * <p>
     * <b>Note</b>: If underlying messaging system does not support the corresponding capability, the update is treated as a no-op.
     * 
     * @param fullname The fullname of the messaging folder to update
     * @param toUpdate The messaging folder to update containing only the modified fields
     * @return The fullname of the updated messaging folder
     * @throws MessagingException If either folder does not exist or cannot be updated
     */
    public String updateFolder(String fullname, MessagingFolder toUpdate) throws MessagingException;

    /**
     * Moves the folder identified through given fullname to the path specified through argument <code>newFullname</code>. Thus a rename can
     * be implicitly performed.
     * <p>
     * E.g.:
     * 
     * <pre>
     * my.path.to.folder -&gt; my.newpath.to.folder
     * </pre>
     * 
     * @param fullname The folder fullname
     * @param newFullname The new fullname to move to
     * @return The new fullname where the folder has been moved
     * @throws MessagingException If either folder does not exist or cannot be moved
     */
    public String moveFolder(String fullname, String newFullname) throws MessagingException;

    /**
     * Renames the folder identified through given fullname to the specified new name.
     * <p>
     * E.g.:
     * 
     * <pre>
     * my.path.to.folder -&gt; my.path.to.newfolder
     * </pre>
     * 
     * @param fullname The folder fullname
     * @param newName The new name
     * @return The new fullname
     * @throws MessagingException If either folder does not exist or cannot be renamed
     */
    public String renameFolder(final String fullname, final String newName) throws MessagingException;

    /**
     * Deletes an existing messaging folder identified through given fullname.
     * <p>
     * This is a convenience method that invokes {@link #deleteFolder(String, boolean)} with <code>hardDelete</code> set to
     * <code>false</code>.
     * 
     * @param fullname The fullname of the messaging folder to delete
     * @return The fullname of the deleted messaging folder
     * @throws MessagingException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(final String fullname) throws MessagingException;

    /**
     * Deletes an existing messaging folder identified through given fullname.
     * <p>
     * If <code>hardDelete</code> is not set and folder is not located below default trash folder it is backed up (including subfolder tree)
     * in default trash folder; otherwise it is deleted permanently.
     * <p>
     * While another backup folder with the same name already exists below default trash folder, an increasing serial number is appended to
     * folder name until its name is unique inside default trash folder's subfolders. E.g.: If folder "DeleteMe" already exists below
     * default trash folder, the next name would be "DeleteMe2". If again a folder "DeleteMe2" already exists below default trash folder,
     * the next name would be "DeleteMe3", and so no.
     * <p>
     * If default trash folder cannot hold subfolders, the folder is either deleted permanently or an appropriate exception may be thrown.
     * 
     * @param fullname The fullname of the messaging folder to delete
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @return The fullname of the deleted messaging folder
     * @throws MessagingException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(String fullname, boolean hardDelete) throws MessagingException;

    /**
     * Deletes the content of the folder identified through given fullname.
     * 
     * @param fullname The fullname of the messaging folder whose content should be cleared
     * @throws MessagingException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(final String fullname) throws MessagingException;

    /**
     * Deletes the content of the folder identified through given fullname.
     * 
     * @param fullname The fullname of the messaging folder whose content should be cleared
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @throws MessagingException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(String fullname, boolean hardDelete) throws MessagingException;

    /**
     * Gets the reverse path from the folder identified through given fullname to parental default folder. All occurring folders on that
     * path are contained in reverse order in returned array of {@link MessagingFolder} instances.
     * 
     * @param fullname The folder fullname
     * @return All occurring folders in reverse order as an array of {@link MessagingFolder} instances.
     * @throws MessagingException If either folder does not exist or path cannot be determined
     */
    public MessagingFolder[] getPath2DefaultFolder(final String fullname) throws MessagingException;

    /**
     * Detects both quota limit and quota usage of STORAGE resource on given messaging folder's quota-root. If the folder denoted by passed
     * messaging folder's quota-root is the INBOX itself, the whole account's STORAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) storage size.
     * <p>
     * Note that the {@link Quota#getLimit()} and {@link Quota#getUsage()} is in 1024 octets.
     * 
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of STORAGE resource
     * @throws MessagingException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getStorageQuota(final String folder) throws MessagingException;

    /**
     * Detects both quota limit and quota usage of MESSAGE resource on given messaging folder's quota-root. If the folder denoted by passed
     * messaging folder's quota-root is the INBOX itself, the whole account's MESSAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) message amount.
     * 
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of MESSAGE resource
     * @throws MessagingException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getMessageQuota(final String folder) throws MessagingException;

    /**
     * Detects both quotas' limit and usage on given messaging folder's quota-root for specified resource types. If the folder denoted by
     * passed messaging folder's quota-root is the INBOX itself, the whole account's quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) resources.
     * <p>
     * If no quota restriction exists for a certain resource type, both quota usage and limit value carry constant {@link Quota#UNLIMITED}
     * to indicate no limitations on that resource type.
     * <p>
     * Note that the {@link Quota#getLimit()} and {@link Quota#getUsage()} returned for {@link Quota.Type#STORAGE} quota is in 1024 octets.
     * 
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @param types The desired quota resource types
     * @return The quotas for specified resource types
     * @throws MessagingException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota[] getQuotas(String folder, Quota.Type[] types) throws MessagingException;

}
