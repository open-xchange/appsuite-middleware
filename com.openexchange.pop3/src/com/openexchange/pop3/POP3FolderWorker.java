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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.pop3;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.util.Set;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.session.Session;
import com.sun.mail.pop3.DefaultFolder;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3FolderWorker} - An abstract class that extends {@link MailMessageStorage} by convenience methods for working on a certain POP3
 * folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class POP3FolderWorker extends MailMessageStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3FolderWorker.class);

    protected static final String STR_INBOX = "INBOX";

    protected static final String STR_FALSE = "false";

    protected static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

    /*
     * Fields
     */
    protected final POP3Store pop3Store;

    protected final Session session;

    protected final int accountId;

    protected final Context ctx;

    protected final POP3Access pop3Access;

    protected final UserSettingMail usm;

    protected final POP3Config pop3Config;

    protected POP3Folder pop3Folder;

    protected int holdsMessages = -1;

    /**
     * Initializes a new {@link POP3FolderWorker}.
     * 
     * @param pop3Store The POP3 store
     * @param popAccess The POP3 access
     * @param session The session providing needed user data
     * @throws POP3Exception If context lading fails
     */
    public POP3FolderWorker(final POP3Store pop3Store, final POP3Access popAccess, final Session session) throws POP3Exception {
        super();
        this.pop3Store = pop3Store;
        this.pop3Access = popAccess;
        this.accountId = popAccess.getAccountId();
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new POP3Exception(e);
        }
        usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        pop3Config = popAccess.getPOP3Config();
    }

    @Override
    public void releaseResources() throws MailException {
        if (null != pop3Folder) {
            closePOP3Folder();
        }
    }

    /**
     * Reports a modification of the POP3 folder denoted by specified fullname. If stored POP3 folder's fullname equals specified fullname,
     * it is closed quietly.
     * 
     * @param modifiedFullname The fullname of the folder which has been modified
     */
    public void notifyPOP3FolderModification(final String modifiedFullname) {
        if ((null == pop3Folder) || !modifiedFullname.equals(pop3Folder.getFullName())) {
            /*
             * Modified folder did not affect remembered POP3 folder
             */
            return;
        }
        try {
            closePOP3Folder();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * Reports a modification of the POP3 folders denoted by specified set of fullnames. If stored POP3 folder's fullname is contained in
     * set of fullnames, it is closed quietly.
     * 
     * @param modifiedFullnames The fullnames of the folders which have been modified
     */
    public void notifyPOP3FolderModification(final Set<String> modifiedFullnames) {
        if ((null == pop3Folder) || !modifiedFullnames.contains(pop3Folder.getFullName())) {
            /*
             * Modified folders did not affect remembered POP3 folder
             */
            return;
        }
        try {
            closePOP3Folder();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void closePOP3Folder() throws MailException {
        try {
            pop3Folder.close(false);
        } catch (final IllegalStateException e) {
            LOG.warn("Invoked close() on a closed folder", e);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        } finally {
            resetPOP3Folder();
        }
    }

    /**
     * Resets the POP3 folder by setting field {@link #pop3Folder} to <code>null</code> and field {@link #holdsMessages} to <code>-1</code>.
     */
    protected void resetPOP3Folder() {
        holdsMessages = -1;
        pop3Folder = null;
    }

    /**
     * Determine if field {@link #pop3Folder} indicates to hold messages.<br>
     * <b>NOTE</b>: This method assumes that field {@link #pop3Folder} is <b>not</b> <code>null</code>.
     * 
     * <pre>
     * return ((popFolder.getType() &amp; POP3Folder.HOLDS_MESSAGES) == 1)
     * </pre>
     * 
     * @return <code>true</code> if field {@link #pop3Folder} indicates to hold messages
     * @throws MessagingException If a messaging error occurs
     */
    protected boolean holdsMessages() throws MessagingException {
        if (holdsMessages == -1) {
            holdsMessages = ((pop3Folder.getType() & Folder.HOLDS_MESSAGES) == 0) ? 0 : 1;
        }
        return holdsMessages > 0;
    }

    /**
     * Sets and opens (only if exists) the folder in a safe manner.
     * 
     * @param fullname The folder fullname
     * @param desiredMode The desired opening mode (either {@link Folder#READ_ONLY} or {@link Folder#READ_WRITE})
     * @return The properly opened POP3 folder
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If user does not hold sufficient rights to open the POP3 folder in desired mode
     */
    protected final Folder setAndOpenFolder(final String fullname, final int desiredMode) throws MessagingException, MailException {
        return setAndOpenFolder(null, fullname, desiredMode);
    }

    /**
     * Sets and opens (only if exists) the folder in a safe manner.
     * 
     * @param popFolder The POP3 folder to check against
     * @param fullname The folder fullname
     * @param desiredMode The desired opening mode (either {@link Folder#READ_ONLY} or {@link Folder#READ_WRITE})
     * @return The properly opened POP3 folder
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If user does not hold sufficient rights to open the POP3 folder in desired mode
     */
    protected final Folder setAndOpenFolder(final Folder popFolder, final String fullname, final int desiredMode) throws MessagingException, MailException {
        if (null == fullname) {
            throw new MailException(MailException.Code.MISSING_FULLNAME);
        }
        final boolean isDefaultFolder = DEFAULT_FOLDER_ID.equals(fullname);
        final boolean isIdenticalFolder;
        if (isDefaultFolder) {
            isIdenticalFolder = (popFolder == null ? false : popFolder instanceof DefaultFolder);
        } else {
            isIdenticalFolder = (popFolder == null ? false : popFolder.getFullName().equals(fullname));
        }
        if (popFolder != null) {
            POP3CommandsCollection.forceNoopCommand(popFolder);
            try {
                /*
                 * This call also checks if folder is opened
                 */
                final int mode = popFolder.getMode();
                if (isIdenticalFolder && (mode >= desiredMode)) {
                    /*
                     * Identical folder is already opened in an appropriate mode.
                     */
                    // POP3CommandsCollection.updatePOP3Folder(popFolder,
                    // mode);
                    return popFolder;
                }
                /*
                 * Folder is open, so close folder
                 */
                try {
                    popFolder.close(false);
                } finally {
                    if (popFolder == this.pop3Folder) {
                        resetPOP3Folder();
                    }
                }
            } catch (final IllegalStateException e) {
                /*
                 * Folder not open
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug("POP3 folder's mode could not be checked, because folder is closed. Going to open folder.", e);
                }
            }
            /*
             * Folder is closed here
             */
            if (isIdenticalFolder) {
                try {
                    if ((popFolder.getType() & Folder.HOLDS_MESSAGES) == 0) { // NoSelect
                        throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, popFolder.getFullName());
                    }
                } catch (final MessagingException e) { // No access
                    throw new POP3Exception(POP3Exception.Code.NO_ACCESS, e, popFolder.getFullName());
                }
                if ((desiredMode == Folder.READ_WRITE) && ((popFolder.getType() & Folder.HOLDS_MESSAGES) == 0) && STR_FALSE.equalsIgnoreCase(pop3Access.getMailProperties().getProperty(
                    MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT,
                    STR_FALSE))) {
                    throw new POP3Exception(POP3Exception.Code.READ_ONLY_FOLDER, popFolder.getFullName());
                }
                /*
                 * Open identical folder in right mode
                 */
                popFolder.open(desiredMode);
                return popFolder;
            }
        }
        final POP3Folder retval = (isDefaultFolder ? (POP3Folder) pop3Store.getDefaultFolder() : (POP3Folder) pop3Store.getFolder(fullname));
        if (!isDefaultFolder && !retval.exists()) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, retval.getFullName());
        }
        if ((desiredMode != Folder.READ_ONLY) && (desiredMode != Folder.READ_WRITE)) {
            throw new POP3Exception(POP3Exception.Code.UNKNOWN_FOLDER_MODE, Integer.valueOf(desiredMode));
        }
        final boolean selectable = isSelectable(retval);
        if (!selectable) { // NoSelect
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, retval.getFullName());
        }
        if ((Folder.READ_WRITE == desiredMode) && (!selectable) && STR_FALSE.equalsIgnoreCase(pop3Access.getMailProperties().getProperty(
            MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT,
            STR_FALSE))) {
            throw new POP3Exception(POP3Exception.Code.READ_ONLY_FOLDER, retval.getFullName());
        }
        retval.open(desiredMode);
        return retval;
    }

    /**
     * Checks if specified POP3 folder is allowed for being selected through SELECT command.
     * 
     * @param popFolder The POP3 folder to check
     * @return <code>true</code> if specified POP3 folder is allowed for being selected through SELECT command; otherwise <code>false</code>
     * @throws MessagingException If POP3 folder's type cannot be determined
     */
    private static final boolean isSelectable(final POP3Folder popFolder) throws MessagingException {
        return (popFolder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES;
    }
}
