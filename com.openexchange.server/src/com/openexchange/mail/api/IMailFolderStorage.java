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


package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Quota;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;

/**
 * {@link IMailFolderStorage} - Offers basic access methods to mail folder(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailFolderStorage {

    /**
     * The constant to return or represent an empty path.
     */
    public static final MailFolder[] EMPTY_PATH = new MailFolder[0];

    /**
     * Checks if a folder exists whose full name matches given <code>fullName</code>
     *
     * @param fullName The full name
     * @return <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     * @throws OXException If existence cannot be checked
     */
    public boolean exists(final String fullName) throws OXException;

    /**
     * Gets the folder identified through given full name
     *
     * @param fullName The full name
     * @return The corresponding instance of {@link MailFolder}
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public MailFolder getFolder(final String fullName) throws OXException;

    /**
     * Gets the first level subfolders located below the folder whose fullName matches given parameter <code>parentFullName</code>.
     * <p>
     * If no subfolders exist below identified folder the constant {@link #EMPTY_PATH} should be returned.
     *
     * @param parentFullName The parent full name
     * @param all Whether all or only subscribed subfolders shall be returned. If underlying mailing system does not support folder
     *            subscription, this argument should always be treated as <code>true</code>.
     * @return An array of {@link MailFolder} representing the subfolders
     * @throws OXException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException;

    /**
     * Gets the mailbox's root folder.
     *
     * @return The mailbox's root folder
     * @throws OXException If mailbox's default folder cannot be delivered
     */
    public MailFolder getRootFolder() throws OXException;

    /**
     * Gets the prefix (incl. separator character) for default folders.
     *
     * @return The prefix
     * @throws OXException If a mail error occurs
     */
    public String getDefaultFolderPrefix() throws OXException;

    /**
     * Checks user's default folder as defined in user's mail settings and creates them if any is missing.
     * <p>
     * See also {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedSpam() createConfirmedSpam()},
     * {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedHam() createConfirmedHam()}, and
     * {@link com.openexchange.spamhandler.SpamHandler#isUnsubscribeSpamFolders() unsubscribeSpamFolders()}.
     *
     * @throws OXException If user's default folder could not be checked
     */
    public void checkDefaultFolders() throws OXException;

    /**
     * Creates a new mail folder with attributes taken from given mail folder description
     *
     * @param toCreate The mail folder to create
     * @return The fullName of the created mail folder
     * @throws OXException If creation fails
     */
    public String createFolder(MailFolderDescription toCreate) throws OXException;

    /**
     * Updates an existing mail folder identified through given full name. All attributes set in given mail folder description are applied.
     * <p>
     * The currently known attributes that make sense being updated are:
     * <ul>
     * <li>permissions; meaning folder's permissions are updated if {@link MailFolderDescription#containsPermissions()} returns
     * <code>true</code></li>
     * <li>subscription; meaning a subscribe/unsubscribe operation is performed if {@link MailFolderDescription#containsSubscribed()}
     * returns <code>true</code></li>
     * </ul>
     * Of course more folder attributes may be checked by implementation to enhance update operations. The programmer may extend the
     * {@link MailFolderDescription} class to do so.
     * <p>
     * <b>Note</b>: If underlying mailing system does not support the corresponding capability, the update is treated as a no-op. For
     * example if both {@link MailCapabilities#hasPermissions()} and {@link MailCapabilities#hasSubscription()} indicate <code>false</code>,
     * the associated update operations are not going to be performed.
     *
     * @param fullName The full name of the mail folder to update
     * @param toUpdate The mail folder to update containing only the modified fields
     * @return The fullName of the updated mail folder
     * @throws OXException If either folder does not exist or cannot be updated
     */
    public String updateFolder(String fullName, MailFolderDescription toUpdate) throws OXException;

    /**
     * Moves the folder identified through given full name to the path specified through argument <code>newFullName</code>. Thus a rename can
     * be implicitly performed.
     * <p>
     * E.g.:
     *
     * <pre>
     * my.path.to.folder -&gt; my.newpath.to.folder
     * </pre>
     *
     * @param fullName The folder full name
     * @param newFullName The new full name to move to
     * @return The new fullName where the folder has been moved
     * @throws OXException If either folder does not exist or cannot be moved
     */
    public String moveFolder(String fullName, String newFullName) throws OXException;

    /**
     * Renames the folder identified through given full name to the specified new name.
     * <p>
     * E.g.:
     *
     * <pre>
     * my.path.to.folder -&gt; my.path.to.newfolder
     * </pre>
     *
     * @param fullName The folder full name
     * @param newName The new name
     * @return The new fullName
     * @throws OXException If either folder does not exist or cannot be renamed
     */
    public String renameFolder(final String fullName, final String newName) throws OXException;

    /**
     * Deletes an existing mail folder identified through given full name.
     * <p>
     * This is a convenience method that invokes {@link #deleteFolder(String, boolean)} with <code>hardDelete</code> set to
     * <code>false</code>.
     *
     * @param fullName The full name of the mail folder to delete
     * @return The fullName of the deleted mail folder
     * @throws OXException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(final String fullName) throws OXException;

    /**
     * Deletes an existing mail folder identified through given full name.
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
     * @param fullName The full name of the mail folder to delete
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @return The fullName of the deleted mail folder
     * @throws OXException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(String fullName, boolean hardDelete) throws OXException;

    /**
     * Deletes the content of the folder identified through given full name.
     *
     * @param fullName The full name of the mail folder whose content should be cleared
     * @throws OXException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(final String fullName) throws OXException;

    /**
     * Deletes the content of the folder identified through given full name.
     *
     * @param fullName The full name of the mail folder whose content should be cleared
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @throws OXException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(String fullName, boolean hardDelete) throws OXException;

    /**
     * Gets the reverse path from the folder identified through given full name to parental default folder. All occurring folders on that
     * path are contained in reverse order in returned array of {@link MailFolder} instances.
     *
     * @param fullName The folder full name
     * @return All occurring folders in reverse order as an array of {@link MailFolder} instances.
     * @throws OXException If either folder does not exist or path cannot be determined
     */
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException;

    /**
     * Detects both quota limit and quota usage of STORAGE resource on given mailbox folder's quota-root. If the folder denoted by passed
     * mailbox folder's quota-root is the INBOX itself, the whole mailbox's STORAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) storage size.
     * <p>
     * Note that the {@link Quota#getLimit()} and {@link Quota#getUsage()} is in 1024 octets.
     *
     * @param fullName The folder full name (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of STORAGE resource
     * @throws OXException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getStorageQuota(final String fullName) throws OXException;

    /**
     * Detects both quota limit and quota usage of MESSAGE resource on given mailbox folder's quota-root. If the folder denoted by passed
     * mailbox folder's quota-root is the INBOX itself, the whole mailbox's MESSAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) message amount.
     *
     * @param fullName The folder full name (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of MESSAGE resource
     * @throws OXException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getMessageQuota(final String fullName) throws OXException;

    /**
     * Detects both quotas' limit and usage on given mailbox folder's quota-root for specified resource types. If the folder denoted by
     * passed mailbox folder's quota-root is the INBOX itself, the whole mailbox's quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) resources.
     * <p>
     * If no quota restriction exists for a certain resource type, both quota usage and limit value carry constant {@link Quota#UNLIMITED}
     * to indicate no limitations on that resource type.
     * <p>
     * Note that the {@link Quota#getLimit()} and {@link Quota#getUsage()} returned for {@link Quota.Type#STORAGE} quota is in 1024 octets.
     *
     * @param fullName The folder full name (if <code>null</code> <i>"INBOX"</i> is used)
     * @param types The desired quota resource types
     * @return The quotas for specified resource types
     * @throws OXException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota[] getQuotas(String fullName, Quota.Type[] types) throws OXException;

    /**
     * Gets the full name of default confirmed ham folder
     *
     * @return The full name of default confirmed ham folder
     * @throws OXException If confirmed ham folder's full name cannot be returned
     */
    public String getConfirmedHamFolder() throws OXException;

    /**
     * Gets the full name of default confirmed spam folder
     *
     * @return The full name of default confirmed spam folder
     * @throws OXException If confirmed spam folder's full name cannot be returned
     */
    public String getConfirmedSpamFolder() throws OXException;

    /**
     * Gets the full name of default drafts folder
     *
     * @return The full name of default drafts folder
     * @throws OXException If draft folder's full name cannot be returned
     */
    public String getDraftsFolder() throws OXException;

    /**
     * Gets the full name of default spam folder
     *
     * @return The full name of default spam folder
     * @throws OXException If spam folder's full name cannot be returned
     */
    public String getSpamFolder() throws OXException;

    /**
     * Gets the full name of default sent folder
     *
     * @return The full name of default sent folder
     * @throws OXException If sent folder's full name cannot be returned
     */
    public String getSentFolder() throws OXException;

    /**
     * Gets the full name of default trash folder
     *
     * @return The full name of default trash folder
     * @throws OXException If trash folder's full name cannot be returned
     */
    public String getTrashFolder() throws OXException;

    /**
     * Releases all used resources when closing parental {@link MailAccess}
     *
     * @throws OXException If resources cannot be released
     */
    public void releaseResources() throws OXException;

}
