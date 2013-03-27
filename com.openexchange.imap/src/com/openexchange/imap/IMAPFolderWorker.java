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

package com.openexchange.imap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.ListLsubRuntimeException;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.notify.internal.IMAPNotifierMessageRecentListener;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.api.enhanced.MailMessageStorageLong;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights.Right;

/**
 * {@link IMAPFolderWorker} - An abstract class that extends {@link MailMessageStorage} by convenience methods for working on a certain IMAP
 * folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class IMAPFolderWorker extends MailMessageStorageLong {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPFolderWorker.class));

    protected static final String STR_INBOX = "INBOX";

    protected static final String STR_FALSE = "false";

    protected static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

    private static final class FailFastError {

        final MessagingException error;
        final long stamp;

        FailFastError(final MessagingException e) {
            super();
            this.error = e;
            this.stamp = System.currentTimeMillis();
        }
    } // End of class FailFastError

    private static final ConcurrentMap<String, FailFastError> FAIL_FAST = new NonBlockingHashMap<String, FailFastError>();

    private static volatile Integer failFastTimeout;
    private static int failFastTimeout() {
        Integer tmp = failFastTimeout;
        if (null == tmp) {
            synchronized (IMAPFolderWorker.class) {
                tmp = failFastTimeout;
                if (null == tmp) {
                    final ConfigurationService service = IMAPServiceRegistry.getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 10000 : service.getIntProperty("com.openexchange.imap.failFastTimeout", 10000));
                    failFastTimeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Checks for possible fail-fast error.
     * 
     * @param imapStore The IMAP store to check
     * @param fullName The mailbox full name
     * @throws MessagingException If such a fail-fast error exists and has not elapsed, yet
     */
    public static void checkFailFast(final IMAPStore imapStore, final String fullName) throws MessagingException {
        if (null == imapStore || null == fullName) {
            return;
        }
        final String key = fullName + '@' + imapStore.toString();
        final FailFastError failFastError = FAIL_FAST.get(key);
        if (null == failFastError) {
            return;
        }
        if ((System.currentTimeMillis() - failFastError.stamp) <= failFastTimeout()) {
            throw failFastError.error;
        }
        FAIL_FAST.remove(key, failFastError);
    }

    /*
     * Fields
     */

    protected final AccessedIMAPStore imapStore;
    protected final Session session;
    protected final int accountId;
    protected final Context ctx;
    protected final IMAPAccess imapAccess;
    protected final UserSettingMail usm;
    protected final IMAPConfig imapConfig;
    protected final ACLExtension aclExtension;
    protected IMAPFolder imapFolder;
    protected final Set<IMAPFolder> otherFolders;
    protected int holdsMessages = -1;

    /**
     * Initializes a new {@link IMAPFolderWorker}.
     *
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws OXException If context lading fails
     */
    public IMAPFolderWorker(final AccessedIMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws OXException {
        super();
        this.imapStore = imapStore;
        this.imapAccess = imapAccess;
        accountId = imapAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        imapConfig = imapAccess.getIMAPConfig();
        aclExtension = imapConfig.getACLExtension();
        otherFolders = new HashSet<IMAPFolder>(4);
    }

    private void openFolder(final int desiredMode, final IMAPFolder imapFolder) throws MessagingException {
        try {
            imapFolder.open(desiredMode);
        } catch (final MessagingException e) {
            if (toUpperCase(e.getMessage()).indexOf("[INUSE]") >= 0) {
                FAIL_FAST.put(imapFolder.getFullName() + '@' + imapStore.toString(), new FailFastError(e));
            }
            throw e;
        }
    }

    @Override
    public void releaseResources() throws OXException {
        closeIMAPFolder();
    }

    /**
     * Reports a modification of the IMAP folder denoted by specified full name. If stored IMAP folder's full name equals specified full
     * name, it is closed quietly.
     *
     * @param modifiedFullName The full name of the folder which has been modified
     */
    public void notifyIMAPFolderModification(final String modifiedFullName) {
        if ((null == imapFolder) || !modifiedFullName.equals(imapFolder.getFullName())) {
            /*
             * Modified folder did not affect remembered IMAP folder
             */
            return;
        }
        try {
            closeIMAPFolder();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * Reports a modification of the IMAP folders denoted by specified set of full names. If stored IMAP folder's full name is contained in
     * set of full names, it is closed quietly.
     *
     * @param modifiedFullNames The full names of the folders which have been modified
     */
    public void notifyIMAPFolderModification(final Set<String> modifiedFullNames) {
        if ((null == imapFolder) || !modifiedFullNames.contains(imapFolder.getFullName())) {
            /*
             * Modified folders did not affect remembered IMAP folder
             */
            return;
        }
        try {
            closeIMAPFolder();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Adds specified IMAP folder to set of maintained opened folders.
     *
     * @param folder The IMAP folder to add
     */
    protected void addOpenedFolder(final IMAPFolder folder) {
        if (null == folder) {
            return;
        }
        otherFolders.add(folder);
    }

    /**
     * Closes remembered IMAP folder (if non-<code>null</code>).
     *
     * @throws OXException If closing remembered IMAP folder fails
     */
    private void closeIMAPFolder() throws OXException {
        try {
            if (null != imapFolder) {
                imapFolder.close(false);
            }
        } catch (final IllegalStateException e) {
            LOG.debug("Invoked close() on a closed folder", e);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        } finally {
            resetIMAPFolder();
        }
    }

    private void closeOtherFolders() {
        for (final IMAPFolder f : otherFolders) {
            try {
                f.close(false);
            } catch (final Exception e) {
                // Ignore
            }
        }
        otherFolders.clear();
    }

    /**
     * Resets the IMAP folder by setting field {@link #imapFolder} to <code>null</code> and field {@link #holdsMessages} to <code>-1</code>.
     */
    protected void resetIMAPFolder() {
        holdsMessages = -1;
        imapFolder = null;
        closeOtherFolders();
    }

    /**
     * Determine if field {@link #imapFolder} indicates to hold messages.<br>
     * <b>NOTE</b>: This method assumes that field {@link #imapFolder} is <b>not</b> <code>null</code>.
     *
     * <pre>
     * return ((imapFolder.getType() &amp; IMAPFolder.HOLDS_MESSAGES) == 1)
     * </pre>
     *
     * @return <code>true</code> if field {@link #imapFolder} indicates to hold messages
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a messaging error occurs
     */
    protected boolean holdsMessages() throws OXException, MessagingException {
        if (holdsMessages == -1) {
            holdsMessages = ListLsubCache.getCachedLISTEntry(imapFolder.getFullName(), accountId, imapFolder, session).canOpen() ? 1 : 0;
        }
        return holdsMessages > 0;
    }

    /**
     * Sets and opens (only if exists) the folder in a safe manner, checks if selectable and for right {@link Right#READ}
     *
     * @param fullName The folder full name
     * @param desiredMode The desired opening mode (either {@link Folder#READ_ONLY} or {@link Folder#READ_WRITE})
     * @return The properly opened IMAP folder
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If user does not hold sufficient rights to open the IMAP folder in desired mode
     */
    protected final IMAPFolder setAndOpenFolder(final String fullName, final int desiredMode) throws MessagingException, OXException {
        return setAndOpenFolder(null, fullName, desiredMode);
    }

    private static final String DEFAULT_FOLDER_ID = MailFolder.DEFAULT_FOLDER_ID;

    /**
     * Sets and opens (only if exists) the folder in a safe manner, checks if selectable and for right {@link Right#READ}
     *
     * @param imapFolder The IMAP folder to check against
     * @param fullName The folder full name
     * @param desiredMode The desired opening mode (either {@link Folder#READ_ONLY} or {@link Folder#READ_WRITE})
     * @return The properly opened IMAP folder
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If user does not hold sufficient rights to open the IMAP folder in desired mode
     */
    protected final IMAPFolder setAndOpenFolder(final IMAPFolder imapFolder, final String fullName, final int desiredMode) throws MessagingException, OXException {
        if (null == fullName) {
            throw MailExceptionCode.MISSING_FULLNAME.create();
        }
        checkFailFast(imapStore, fullName);
        if (imapFolder == this.imapFolder) {
            closeOtherFolders();
        }
        final boolean isDefaultFolder = DEFAULT_FOLDER_ID.equals(fullName);
        if (imapFolder != null) {
            final String imapFolderFullname = imapFolder.getFullName();
            final boolean isIdenticalFolder = isDefaultFolder ? 0 == imapFolderFullname.length() : fullName.equals(imapFolderFullname);
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (imapFolder) {
                IMAPCommandsCollection.forceNoopCommand(imapFolder);
                try {
                    /*
                     * This call also checks if folder is opened
                     */
                    final int mode = imapFolder.getMode();
                    if (isIdenticalFolder && (mode >= desiredMode)) {
                        /*
                         * Identical folder is already opened in an appropriate mode.
                         */
                        // IMAPCommandsCollection.updateIMAPFolder(imapFolder,
                        // mode);
                        clearCache(imapFolder);
                        return imapFolder;
                    }
                    /*
                     * Folder is open, so close folder
                     */
                    try {
                        imapFolder.close(false/*Folder.READ_WRITE == mode*/);
                    } finally {
                        if (imapFolder == this.imapFolder) {
                            resetIMAPFolder();
                        }
                    }
                } catch (final IllegalStateException e) {
                    /*
                     * Folder not open
                     */
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("IMAP folder's mode could not be checked, because folder is closed. Going to open folder.", e);
                    }
                }
                /*
                 * Folder is closed here
                 */
                if (isIdenticalFolder) {
                    try {
                        if ((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0) { // NoSelect
                            throw IMAPException.create(
                                IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                                imapConfig,
                                session,
                                imapFolderFullname);
                        } else if (imapConfig.isSupportsACLs() && !aclExtension.canRead(RightsCache.getCachedRights(
                            imapFolder,
                            true,
                            session,
                            accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapFolderFullname);
                        }
                    } catch (final MessagingException e) { // No access
                        throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolderFullname);
                    }
                    if ((desiredMode == Folder.READ_WRITE) && ((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0) && STR_FALSE.equalsIgnoreCase(imapAccess.getMailProperties().getProperty(
                        MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT,
                        STR_FALSE)) && IMAPCommandsCollection.isReadOnly(imapFolder)) {
                        throw IMAPException.create(IMAPException.Code.READ_ONLY_FOLDER, imapConfig, session, imapFolderFullname);
                    }
                    if (imapStore.notifyRecent() && (desiredMode == Folder.READ_WRITE)) {
                        IMAPNotifierMessageRecentListener.addNotifierFor(imapFolder, fullName, accountId, session, true);
                    }
                    /*
                     * Open identical folder in right mode
                     */
                    openFolder(desiredMode, imapFolder);
                    return imapFolder;
                }
            } // End of synchronized
        }
        final IMAPFolder retval = (isDefaultFolder ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore.getFolder(fullName));
        if (imapStore.notifyRecent() && (desiredMode == Folder.READ_WRITE)) {
            IMAPNotifierMessageRecentListener.addNotifierFor(retval, fullName, accountId, session, true);
        }
        /*
         * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
         */
        synchronized (retval) {
            final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(fullName, accountId, retval, session);
            if (!isDefaultFolder && !STR_INBOX.equals(fullName) && (!listEntry.exists())) {
                throw IMAPException.create(
                    IMAPException.Code.FOLDER_NOT_FOUND,
                    imapConfig,
                    session,
                    isDefaultFolder ? MailFolder.DEFAULT_FOLDER_NAME : fullName);
            }
            if ((desiredMode != Folder.READ_ONLY) && (desiredMode != Folder.READ_WRITE)) {
                throw IMAPException.create(IMAPException.Code.UNKNOWN_FOLDER_MODE, imapConfig, session, Integer.valueOf(desiredMode));
            }
            final boolean selectable = listEntry.canOpen();
            if (!selectable) { // NoSelect
                throw IMAPException.create(
                    IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                    imapConfig,
                    session,
                    isDefaultFolder ? MailFolder.DEFAULT_FOLDER_NAME : fullName);
            }
            try {
                if (imapConfig.isSupportsACLs() && !aclExtension.canRead(RightsCache.getCachedRights(retval, true, session, accountId))) {
                    throw IMAPException.create(
                        IMAPException.Code.NO_FOLDER_OPEN,
                        imapConfig,
                        session,
                        isDefaultFolder ? MailFolder.DEFAULT_FOLDER_NAME : fullName);
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(
                    IMAPException.Code.NO_ACCESS,
                    imapConfig,
                    session,
                    e,
                    isDefaultFolder ? MailFolder.DEFAULT_FOLDER_NAME : fullName);
            }
            if ((Folder.READ_WRITE == desiredMode) && (!selectable) && STR_FALSE.equalsIgnoreCase(imapAccess.getMailProperties().getProperty(
                MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT,
                STR_FALSE)) && IMAPCommandsCollection.isReadOnly(retval)) {
                throw IMAPException.create(
                    IMAPException.Code.READ_ONLY_FOLDER,
                    imapConfig,
                    session,
                    isDefaultFolder ? MailFolder.DEFAULT_FOLDER_NAME : fullName);
            }
            openFolder(desiredMode, retval);
        }
        return retval;
    }

    /**
     * Handles specified {@link RuntimeException} instance.
     *
     * @param e The runtime exception to handle
     * @return An appropriate {@link OXException}
     */
    protected OXException handleRuntimeException(final RuntimeException e) {
        if (e instanceof ListLsubRuntimeException) {
            ListLsubCache.clearCache(accountId, session);
            return MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
        return MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    protected static volatile Field messagesField;
    protected static volatile Field messageCacheField;
    protected static volatile Field uidTableField;

    /** Clears the cache */
    protected static void clearCache(final IMAPFolder imapFolder) {
        if (null == imapFolder) {
            return;
        }
        final Field messageCacheField = IMAPFolderWorker.messageCacheField;
        if (null == messageCacheField) {
            return;
        }
        final Field messagesField = IMAPFolderWorker.messagesField;
        if (null == messagesField) {
            return;
        }
        final Field uidTableField = IMAPFolderWorker.uidTableField;
        if (null == uidTableField) {
            return;
        }

        try {
            final com.sun.mail.imap.MessageCache mc = (com.sun.mail.imap.MessageCache) messageCacheField.get(imapFolder);
            if (null != mc) {
                final IMAPMessage[] messages = (IMAPMessage[]) messagesField.get(mc);
                if (null != messages) {
                    Arrays.fill(messages, null);
                }
            }

            final Hashtable<?, ?> uidTable = (Hashtable<?, ?>) uidTableField.get(imapFolder);
            if (null != uidTable) {
                uidTable.clear();
            }
        } catch (final IllegalArgumentException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /** Safely clears the cache */
    protected static void clearCacheSafe(final IMAPFolder imapFolder) {
        try {
            clearCache(imapFolder);
        } catch (final Exception e) {
            // Ignore
        }
    }

    /** ASCII-wise upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

}
