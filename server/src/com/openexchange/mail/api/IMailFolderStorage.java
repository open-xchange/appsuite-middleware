
package com.openexchange.mail.api;

import com.openexchange.mail.MailException;
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
     * Checks if a folder exists whose fullname matches given <code>fullname</code>
     * 
     * @param fullname The fullname
     * @return <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     * @throws MailException If existence cannot be checked
     */
    public boolean exists(final String fullname) throws MailException;

    /**
     * Gets the folder identified through given fullname
     * 
     * @param fullname The fullname
     * @return The corresponding instance of {@link MailFolder}
     * @throws MailException If either folder does not exist or could not be fetched
     */
    public MailFolder getFolder(final String fullname) throws MailException;

    /**
     * Gets the first level subfolders located below the folder whose fullname matches given parameter <code>parentFullname</code>.
     * <p>
     * If no subfolders exist below identified folder the constant {@link #EMPTY_PATH} should be returned.
     * 
     * @param parentFullname The parent fullname
     * @param all Whether all or only subscribed subfolders shall be returned. If underlying mailing system does not support folder
     *            subscription, this argument should always be treated as <code>true</code>.
     * @return An array of {@link MailFolder} representing the subfolders
     * @throws MailException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException;

    /**
     * Gets the mailbox's root folder.
     * 
     * @return The mailbox's root folder
     * @throws MailException If mailbox's default folder cannot be delivered
     */
    public MailFolder getRootFolder() throws MailException;

    /**
     * Checks user's default folder as defined in user's mail settings and creates them if any is missing.
     * <p>
     * See also {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedSpam() createConfirmedSpam()},
     * {@link com.openexchange.spamhandler.SpamHandler#isCreateConfirmedHam() createConfirmedHam()}, and
     * {@link com.openexchange.spamhandler.SpamHandler#isUnsubscribeSpamFolders() unsubscribeSpamFolders()}.
     * 
     * @throws MailException If user's default folder could not be checked
     */
    public void checkDefaultFolders() throws MailException;

    /**
     * Creates a new mail folder with attributes taken from given mail folder description
     * 
     * @param toCreate The mail folder to create
     * @return The fullname of the created mail folder
     * @throws MailException If creation fails
     */
    public String createFolder(MailFolderDescription toCreate) throws MailException;

    /**
     * Updates an existing mail folder identified through given fullname. All attributes set in given mail folder description are applied.
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
     * @param fullname The fullname of the mail folder to update
     * @param toUpdate The mail folder to update containing only the modified fields
     * @return The fullname of the updated mail folder
     * @throws MailException If either folder does not exist or cannot be updated
     */
    public String updateFolder(String fullname, MailFolderDescription toUpdate) throws MailException;

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
     * @throws MailException If either folder does not exist or cannot be moved
     */
    public String moveFolder(String fullname, String newFullname) throws MailException;

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
     * @throws MailException If either folder does not exist or cannot be renamed
     */
    public String renameFolder(final String fullname, final String newName) throws MailException;

    /**
     * Deletes an existing mail folder identified through given fullname.
     * <p>
     * This is a convenience method that invokes {@link #deleteFolder(String, boolean)} with <code>hardDelete</code> set to
     * <code>false</code>.
     * 
     * @param fullname The fullname of the mail folder to delete
     * @return The fullname of the deleted mail folder
     * @throws MailException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(final String fullname) throws MailException;

    /**
     * Deletes an existing mail folder identified through given fullname.
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
     * @param fullname The fullname of the mail folder to delete
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @return The fullname of the deleted mail folder
     * @throws MailException If either folder does not exist or cannot be deleted
     */
    public String deleteFolder(String fullname, boolean hardDelete) throws MailException;

    /**
     * Deletes the content of the folder identified through given fullname.
     * 
     * @param fullname The fullname of the mail folder whose content should be cleared
     * @throws MailException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(final String fullname) throws MailException;

    /**
     * Deletes the content of the folder identified through given fullname.
     * 
     * @param fullname The fullname of the mail folder whose content should be cleared
     * @param hardDelete Whether to delete permanently or to backup into trash folder
     * @throws MailException If either folder does not exist or its content cannot be cleared
     */
    public void clearFolder(String fullname, boolean hardDelete) throws MailException;

    /**
     * Gets the reverse path from the folder identified through given fullname to parental default folder. All occurring folders on that
     * path are contained in reverse order in returned array of {@link MailFolder} instances.
     * 
     * @param fullname The folder fullname
     * @return All occurring folders in reverse order as an array of {@link MailFolder} instances.
     * @throws MailException If either folder does not exist or path cannot be determined
     */
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException;

    /**
     * Detects both quota limit and quota usage of STORAGE resource on given mailbox folder's quota-root. If the folder denoted by passed
     * mailbox folder's quota-root is the INBOX itself, the whole mailbox's STORAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) storage size.
     * <p>
     * Note that the {@link Quota#getLimit()} and {@link Quota#getUsage()} is in 1024 octets.
     * 
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of STORAGE resource
     * @throws MailException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getStorageQuota(final String folder) throws MailException;

    /**
     * Detects both quota limit and quota usage of MESSAGE resource on given mailbox folder's quota-root. If the folder denoted by passed
     * mailbox folder's quota-root is the INBOX itself, the whole mailbox's MESSAGE quota is going to be returned; meaning the sum of all
     * available (limit) and allocated (usage) message amount.
     * 
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @return The quota of MESSAGE resource
     * @throws MailException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota getMessageQuota(final String folder) throws MailException;

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
     * @param folder The folder fullname (if <code>null</code> <i>"INBOX"</i> is used)
     * @param types The desired quota resource types
     * @return The quotas for specified resource types
     * @throws MailException If either folder does not exist or quota limit and/or quote usage cannot be determined
     */
    public Quota[] getQuotas(String folder, Quota.Type[] types) throws MailException;

    /**
     * Gets the fullname of default confirmed ham folder
     * 
     * @return The fullname of default confirmed ham folder
     * @throws MailException If confirmed ham folder's fullname cannot be returned
     */
    public String getConfirmedHamFolder() throws MailException;

    /**
     * Gets the fullname of default confirmed spam folder
     * 
     * @return The fullname of default confirmed spam folder
     * @throws MailException If confirmed spam folder's fullname cannot be returned
     */
    public String getConfirmedSpamFolder() throws MailException;

    /**
     * Gets the fullname of default drafts folder
     * 
     * @return The fullname of default drafts folder
     * @throws MailException If draft folder's fullname cannot be returned
     */
    public String getDraftsFolder() throws MailException;

    /**
     * Gets the fullname of default spam folder
     * 
     * @return The fullname of default spam folder
     * @throws MailException If spam folder's fullname cannot be returned
     */
    public String getSpamFolder() throws MailException;

    /**
     * Gets the fullname of default sent folder
     * 
     * @return The fullname of default sent folder
     * @throws MailException If sent folder's fullname cannot be returned
     */
    public String getSentFolder() throws MailException;

    /**
     * Gets the fullname of default trash folder
     * 
     * @return The fullname of default trash folder
     * @throws MailException If trash folder's fullname cannot be returned
     */
    public String getTrashFolder() throws MailException;

    /**
     * Releases all used resources when closing parental {@link MailAccess}
     * 
     * @throws MailException If resources cannot be released
     */
    public void releaseResources() throws MailException;

}
