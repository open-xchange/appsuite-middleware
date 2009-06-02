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

package com.openexchange.imap;

import java.io.IOException;
import java.io.Writer;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.RootSubfolderCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPDefaultFolderChecker} - The IMAP default folder checker.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPDefaultFolderChecker {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPDefaultFolderChecker.class);

    private static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

    private final Session session;

    private final int accountId;

    private final IMAPStore imapStore;

    private final Context ctx;

    private final IMAPConfig imapConfig;

    /**
     * Initializes a new {@link IMAPDefaultFolderChecker}.
     * 
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapConfig The IMAP config
     */
    public IMAPDefaultFolderChecker(final int accountId, final Session session, final Context ctx, final IMAPStore imapStore, final IMAPConfig imapConfig) {
        super();
        this.accountId = accountId;
        this.session = session;
        this.imapStore = imapStore;
        this.ctx = ctx;
        this.imapConfig = imapConfig;
    }

    /**
     * Checks if given fullname denotes a default folder.
     * 
     * @param folderFullName The fullname to check
     * @return <code>true</code> if given fullname denotes a default folder; otherwise <code>false</code>
     * @throws MailException If check for default folder fails
     */
    public boolean isDefaultFolder(final String folderFullName) throws MailException {
        boolean isDefaultFolder = false;
        isDefaultFolder = (folderFullName.equalsIgnoreCase("INBOX"));
        for (int index = 0; (index < 6) && !isDefaultFolder; index++) {
            if (folderFullName.equalsIgnoreCase(getDefaultFolder(index))) {
                return true;
            }
        }
        return isDefaultFolder;
    }

    /**
     * Gets the default folder for specified index.
     * 
     * @param index The default folder index taken from class <code>StorageUtility</code>
     * @return The default folder for specified index
     * @throws MailException If default folder retrieval fails
     */
    public String getDefaultFolder(final int index) throws MailException {
        if (!isDefaultFoldersChecked()) {
            checkDefaultFolders();
        }
        if (StorageUtility.INDEX_INBOX == index) {
            return "INBOX";
        }
        final String retval = getDefaultMailFolder(index);
        if (retval != null) {
            return retval;
        }
        setDefaultFoldersChecked(false);
        checkDefaultFolders();
        return getDefaultMailFolder(index);
    }

    private String getDefaultMailFolder(final int index) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.getParamDefaultFolderArray(accountId));
        return arr == null ? null : arr[index];
    }

    /**
     * Checks default folders.
     * 
     * @throws MailException If default folder check fails
     */
    public void checkDefaultFolders() throws MailException {
        if (!isDefaultFoldersChecked()) {
            synchronized (session) {
                try {
                    if (isDefaultFoldersChecked()) {
                        return;
                    }
                    if (LOG.isDebugEnabled()) {
                        final StringBuilder sb = new StringBuilder(2048);
                        sb.append("\n\nDefault folder check for account ").append(accountId).append(" (");
                        sb.append(imapConfig.getServer()).append(")\n");
                        new Throwable().printStackTrace(new java.io.PrintWriter(new StringWriter(sb)));
                        sb.append('\n');
                        LOG.debug(sb.toString());
                    }
                    final long start = System.currentTimeMillis();
                    /*
                     * Get INBOX folder
                     */
                    final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder("INBOX");
                    if (!inboxFolder.exists()) {
                        throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, "INBOX");
                    }
                    if (!inboxFolder.isSubscribed()) {
                        /*
                         * Subscribe INBOX folder
                         */
                        inboxFolder.setSubscribed(true);
                    }
                    final StringBuilder tmp = new StringBuilder(128);
                    /*
                     * Get prefix for default folder names, NOT fullnames!
                     */
                    final String prefix;
                    final char sep;
                    {
                        final String[] sa = getDefaultFolderPrefix(inboxFolder, tmp);
                        prefix = sa[0];
                        sep = sa[1].charAt(0);
                    }
                    /*
                     * Check for mbox
                     */
                    final int type;
                    final boolean mboxEnabled;
                    {
                        final String param = MailSessionParameterNames.getParamMBox(accountId);
                        Boolean mbox = (Boolean) session.getParameter(param);
                        if (null == mbox) {
                            mbox = Boolean.valueOf(!IMAPCommandsCollection.supportsFolderType(inboxFolder, FOLDER_TYPE, prefix));
                            session.setParameter(param, mbox);
                        }
                        mboxEnabled = mbox.booleanValue();
                    }
                    if (mboxEnabled) {
                        type = IMAPFolder.HOLDS_MESSAGES;
                    } else {
                        type = FOLDER_TYPE;
                    }
                    /*
                     * Load mail account
                     */
                    final boolean isSpamOptionEnabled;
                    final MailAccount mailAccount;
                    try {
                        mailAccount = IMAPServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true).getMailAccount(
                            accountId,
                            session.getUserId(),
                            session.getContextId());
                        final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
                        isSpamOptionEnabled = usm.isSpamOptionEnabled();
                    } catch (final ServiceException e) {
                        throw new MailException(e);
                    } catch (final MailAccountException e) {
                        throw new MailException(e);
                    }
                    /*
                     * Check default folders
                     */
                    final DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(
                        accountId,
                        session.getUserId(),
                        session.getContextId());
                    final String[] defaultFolderFullnames = defaultFolderNamesProvider.getDefaultFolderFullnames(
                        mailAccount,
                        isSpamOptionEnabled);
                    final String[] defaultFolderNames = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, isSpamOptionEnabled);
                    final SpamHandler spamHandler;
                    {
                        spamHandler = isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();
                    }
                    for (int i = 0; i < defaultFolderNames.length; i++) {
                        if (StorageUtility.INDEX_CONFIRMED_HAM == i) {
                            if (spamHandler.isCreateConfirmedHam()) {
                                final String[] prefixAndName = choosePrefixAndName(prefix, defaultFolderNames[i], defaultFolderFullnames[i]);
                                setDefaultMailFolder(i, checkDefaultFolder(
                                    prefixAndName[0],
                                    prefixAndName[1],
                                    sep,
                                    type,
                                    spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                    tmp));
                            } else if (LOG.isDebugEnabled()) {
                                LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedHam()=false");
                            }
                        } else if (StorageUtility.INDEX_CONFIRMED_SPAM == i) {
                            if (spamHandler.isCreateConfirmedSpam()) {
                                final String[] prefixAndName = choosePrefixAndName(prefix, defaultFolderNames[i], defaultFolderFullnames[i]);
                                setDefaultMailFolder(i, checkDefaultFolder(
                                    prefixAndName[0],
                                    prefixAndName[1],
                                    sep,
                                    type,
                                    spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                    tmp));
                            } else if (LOG.isDebugEnabled()) {
                                LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedSpam()=false");
                            }
                        } else {
                            final String[] prefixAndName = choosePrefixAndName(prefix, defaultFolderNames[i], defaultFolderFullnames[i]);
                            setDefaultMailFolder(i, checkDefaultFolder(prefixAndName[0], prefixAndName[1], sep, type, 1, tmp));
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new StringBuilder(64).append("Default folders check for account ").append(accountId).append(" took ").append(
                            System.currentTimeMillis() - start).append("msec").toString());
                    }
                    setDefaultFoldersChecked(true);
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e, imapConfig);
                }
            }
        }
    }

    private String[] getDefaultFolderPrefix(final IMAPFolder inboxFolder, final StringBuilder tmp) throws MessagingException, IMAPException {
        /*
         * Check for NAMESPACE capability
         */
        final char sep;
        if (imapConfig.getImapCapabilities().hasNamespace()) {
            /*
             * Perform the NAMESPACE command to detect the subfolder prefix. From rfc2342: Clients often attempt to create mailboxes for
             * such purposes as maintaining a record of sent messages (e.g. "Sent Mail") or temporarily saving messages being composed (e.g.
             * "Drafts"). For these clients to inter-operate correctly with the variety of IMAP4 servers available, the user must enter the
             * prefix of the Personal Namespace used by the server. Using the NAMESPACE command, a client is able to automatically discover
             * this prefix without manual user configuration.
             */
            final Folder[] personalNamespaces = imapStore.getPersonalNamespaces();
            if (personalNamespaces == null || personalNamespaces[0] == null) {
                throw new IMAPException(IMAPException.Code.MISSING_PERSONAL_NAMESPACE);
            }
            sep = personalNamespaces[0].getSeparator();
            setSeparator(sep);
            final String persPrefix = personalNamespaces[0].getFullName();
            if ((persPrefix.length() == 0)) {
                if (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace() && IMAPCommandsCollection.canCreateSubfolder(
                    persPrefix,
                    inboxFolder)) {
                    /*
                     * Personal namespace folder allows subfolders and nested default folder are demanded, thus use INBOX as prefix although
                     * NAMESPACE signals to use no prefix.
                     */
                    tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
                }
            } else {
                tmp.append(persPrefix).append(personalNamespaces[0].getSeparator());
            }
        } else {
            /*
             * Examine INBOX folder since NAMESPACE capability is not supported
             */
            sep = inboxFolder.getSeparator();
            setSeparator(sep);
            final boolean inboxInferiors = inferiors(inboxFolder);
            /*
             * Examine root folder if subfolders allowed
             */
            final boolean rootInferiors = RootSubfolderCache.canCreateSubfolders(
                (DefaultFolder) imapStore.getDefaultFolder(),
                true,
                session,
                accountId).booleanValue();
            /*
             * Determine where to create default folders and store as a prefix for folder fullname
             */
            if (!inboxInferiors && rootInferiors) {
                // Create folder beside INBOX folder
                tmp.append("");
            } else if (inboxInferiors && !rootInferiors) {
                // Create folder under INBOX folder
                tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
            } else if (inboxInferiors && rootInferiors) {
                if (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace()) {
                    /*
                     * Only allow default folder below INBOX if inferiors are permitted nested default folder are explicitly allowed
                     */
                    tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
                } else {
                    tmp.append("");
                }
            } else {
                // Cannot occur: No folders are allowed to be created, neither below INBOX nor below root folder
                throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, "INBOX");
            }
        }
        final String prefix = tmp.toString();
        tmp.setLength(0);
        return new String[] { prefix, String.valueOf(sep) };
    }

    private void setDefaultMailFolder(final int index, final String fullname) {
        final String key = MailSessionParameterNames.getParamDefaultFolderArray(accountId);
        String[] arr = (String[]) session.getParameter(key);
        if (null == arr) {
            arr = new String[6];
            session.setParameter(key, arr);
        }
        arr[index] = fullname;
    }

    private String checkDefaultFolder(final String prefix, final String name, final char sep, final int type, final int subscribe, final StringBuilder tmp) throws MessagingException {
        /*
         * Check default folder
         */
        final boolean checkSubscribed = true;
        final Folder f = imapStore.getFolder(tmp.append(prefix).append(name).toString());
        tmp.setLength(0);
        if (!f.exists() /*&& !f.create(type)*/) {
            // Try with own routine to get a valid error message on failure
            IMAPCommandsCollection.createFolder((IMAPFolder) f, sep, type);
            /*-
             * 
             * 
            final IMAPException oxme = new IMAPException(
                IMAPException.Code.NO_DEFAULT_FOLDER_CREATION,
                tmp.append(prefix).append(name).toString());
            tmp.setLength(0);
            LOG.error(oxme.getMessage(), oxme);
            checkSubscribed = false;
             */
        }
        if (checkSubscribed) {
            if (1 == subscribe) {
                if (!f.isSubscribed()) {
                    try {
                        f.setSubscribed(true);
                    } catch (final MethodNotSupportedException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (final MessagingException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } else if (0 == subscribe) {
                if (f.isSubscribed()) {
                    try {
                        f.setSubscribed(false);
                    } catch (final MethodNotSupportedException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (final MessagingException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.append("Default folder \"").append(f.getFullName()).append("\" successfully checked for IMAP account ").append(
                accountId).append(" (").append(imapConfig.getServer()).append(')').toString());
            tmp.setLength(0);
        }
        return f.getFullName();
    }

    private static String[] choosePrefixAndName(final String prefix, final String name, final String fullname) {
        if (null == fullname || 0 == fullname.length()) {
            return new String[] { prefix, name };
        }
        return new String[] { "", fullname };
    }

    private boolean isDefaultFoldersChecked() {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId));
        return (b != null) && b.booleanValue();
    }

    private void setDefaultFoldersChecked(final boolean checked) {
        session.setParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId), Boolean.valueOf(checked));
    }

    /**
     * Stores specified separator character in session parameters for future look-ups
     * 
     * @param separator The separator character
     */
    private void setSeparator(final char separator) {
        session.setParameter(MailSessionParameterNames.getParamSeparator(accountId), Character.valueOf(separator));
    }

    /**
     * Checks if inferiors (subfolders) are allowed by specified folder; meaning to check if it is capable to hold folders.
     * 
     * @param folder The folder to check
     * @return <code>true</code> if inferiors (subfolders) are allowed by specified folder; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    private static boolean inferiors(final IMAPFolder folder) throws MessagingException {
        return ((folder.getType() & IMAPFolder.HOLDS_FOLDERS) == IMAPFolder.HOLDS_FOLDERS);
    }

    private static class StringWriter extends Writer {

        private final StringBuilder buf;

        /**
         * Create a new string writer, using the default initial string-buffer size.
         */
        public StringWriter() {
            buf = new StringBuilder();
            lock = buf;
        }

        /**
         * Create a new string writer, using the specified initial string-buffer size.
         * 
         * @param initialSize an int specifying the initial size of the buffer.
         */
        public StringWriter(final int initialSize) {
            if (initialSize < 0) {
                throw new IllegalArgumentException("Negative buffer size");
            }
            buf = new StringBuilder(initialSize);
            lock = buf;
        }

        /**
         * Create a new string writer, using the specified string builder.
         * 
         * @param buf The string builder to use
         */
        public StringWriter(final StringBuilder buf) {
            if (null == buf) {
                throw new IllegalArgumentException("Buffer is null");
            }
            this.buf = buf;
            lock = this.buf;
        }

        /**
         * Write a single character.
         */
        @Override
        public void write(final int c) {
            buf.append((char) c);
        }

        /**
         * Write a portion of an array of characters.
         * 
         * @param cbuf Array of characters
         * @param off Offset from which to start writing characters
         * @param len Number of characters to write
         */
        @Override
        public void write(final char cbuf[], final int off, final int len) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            buf.append(cbuf, off, len);
        }

        /**
         * Write a string.
         */
        @Override
        public void write(final String str) {
            buf.append(str);
        }

        /**
         * Write a portion of a string.
         * 
         * @param str String to be written
         * @param off Offset from which to start writing characters
         * @param len Number of characters to write
         */
        @Override
        public void write(final String str, final int off, final int len) {
            buf.append(str.substring(off, off + len));
        }

        /**
         * Appends the specified character sequence to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(csq)</tt> behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(csq.toString())
         * </pre>
         * <p>
         * Depending on the specification of <tt>toString</tt> for the character sequence <tt>csq</tt>, the entire sequence may not be
         * appended. For instance, invoking the <tt>toString</tt> method of a character buffer will return a subsequence whose content
         * depends upon the buffer's position and limit.
         * 
         * @param csq The character sequence to append. If <tt>csq</tt> is <tt>null</tt>, then the four characters <tt>"null"</tt> are
         *            appended to this writer.
         * @return This writer
         * @since 1.5
         */
        @Override
        public StringWriter append(final CharSequence csq) {
            if (csq == null) {
                write("null");
            } else {
                write(csq.toString());
            }
            return this;
        }

        /**
         * Appends a subsequence of the specified character sequence to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(csq, start,
         * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(csq.subSequence(start, end).toString())
         * </pre>
         * 
         * @param csq The character sequence from which a subsequence will be appended. If <tt>csq</tt> is <tt>null</tt>, then characters
         *            will be appended as if <tt>csq</tt> contained the four characters <tt>"null"</tt>.
         * @param start The index of the first character in the subsequence
         * @param end The index of the character following the last character in the subsequence
         * @return This writer
         * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt> is greater than <tt>end</tt>, or
         *             <tt>end</tt> is greater than <tt>csq.length()</tt>
         * @since 1.5
         */
        @Override
        public StringWriter append(final CharSequence csq, final int start, final int end) {
            final CharSequence cs = (csq == null ? "null" : csq);
            write(cs.subSequence(start, end).toString());
            return this;
        }

        /**
         * Appends the specified character to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(c)</tt> behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(c)
         * </pre>
         * 
         * @param c The 16-bit character to append
         * @return This writer
         * @since 1.5
         */
        @Override
        public StringWriter append(final char c) {
            write(c);
            return this;
        }

        /**
         * Return the buffer's current value as a string.
         */
        @Override
        public String toString() {
            return buf.toString();
        }

        /**
         * Return the string buffer itself.
         * 
         * @return StringBuffer holding the current buffer value.
         */
        public StringBuilder getBuffer() {
            return buf;
        }

        /**
         * Flush the stream.
         */
        @Override
        public void flush() {
            // Nothing to do
        }

        /**
         * Closing a <tt>StringWriter</tt> has no effect. The methods in this class can be called after the stream has been closed without
         * generating an <tt>IOException</tt>.
         */
        @Override
        public void close() throws IOException {
            // Nothing to do
        }
    } // End of StringWriter

}
