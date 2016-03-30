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

package com.openexchange.mail;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.Serializable;
import java.util.Comparator;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link MailPath} - Represents a message's unique path inside a mailbox, that is the account ID followed by the folder full name followed
 * by the value of {@link #SEPERATOR} followed by mail's unique ID:<br>
 * Example: <i>default1/INBOX.Subfolder/1234</i>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPath implements Cloneable, Serializable {

    private static final long serialVersionUID = -7426425247685367823L;

    /**
     * Gets an appropriate instance of {@link Comparator} to sort instances of {@link MailPath}
     */
    public static final Comparator<MailPath> COMPARATOR = new Comparator<MailPath>() {

        @Override
        public int compare(final MailPath mailPath1, final MailPath mailPath2) {
            final int accountComp = (mailPath1.getAccountId() < mailPath2.getAccountId() ? -1 : (mailPath1.getAccountId() == mailPath2.getAccountId() ? 0 : 1));
            if (accountComp == 0) {
                final int folderComp = mailPath1.getFolder().compareTo(mailPath2.getFolder());
                return folderComp == 0 ? mailPath1.getMailID().compareTo(mailPath2.getMailID()) : folderComp;
            }
            return accountComp;
        }
    };

    /**
     * A <code>null</code> {@link MailPath}
     */
    public static final MailPath NULL = null;

    /**
     * The <code>'/'</code> character which separates folder's full name from mail's ID in a mail path
     */
    public static final char SEPERATOR = '/';

    /**
     * Gets the mail path corresponding to given folder full name and message UID
     *
     * @param accountId The account ID
     * @param folder The folder full name
     * @param mailId The mail ID
     * @return The mail path as {@link String}
     */
    public static String getMailPath(final int accountId, final String folder, final String mailId) {
        return new StringBuilder(32).append(prepareFullname(accountId, folder)).append(SEPERATOR).append(mailId).toString();
    }

    /**
     * Returns the mail paths for given comma-separated mail IDs each conform to pattern &lt;folder-path&gt;&lt;value-of-{@link #SEPERATOR}
     * &gt;&lt;mail-ID&gt;
     *
     * @param mailPaths The comma-separated mail IDs
     * @return The corresponding mail paths
     * @throws OXException If mail paths cannot be generated
     */
    public static MailPath[] getMailPaths(final String mailPaths) throws OXException {
        return getMailPaths(mailPaths.split(" *, *"));
    }

    /**
     * Returns the mail paths for given mail IDs each conform to pattern &lt;folder-path&gt;&lt;value-of-{@link #SEPERATOR}
     * &gt;&lt;mail-ID&gt;
     *
     * @param mailPaths The mail IDs
     * @return The corresponding mail paths
     * @throws OXException If mail paths cannot be generated
     */
    public static MailPath[] getMailPaths(final String[] mailPaths) throws OXException {
        final MailPath[] retval = new MailPath[mailPaths.length];
        for (int i = 0; i < mailPaths.length; i++) {
            retval[i] = new MailPath(mailPaths[i]);
        }
        return retval;
    }

    /**
     * Extracts the IDs from given mail paths
     *
     * @param mailPaths The mail IDs
     * @return The extracted IDs
     */
    public static String[] getUIDs(final MailPath[] mailPaths) {
        final String[] retval = new String[mailPaths.length];
        for (int i = 0; i < mailPaths.length; i++) {
            retval[i] = mailPaths[i].mailID;
        }
        return retval;
    }

    /*-
     * --------------------------------------------------------- Fields ---------------------------------------------------------
     */

    private int accountId;
    private String folder;
    private String str;
    private String mailID;

    /**
     * Default constructor
     */
    public MailPath() {
        super();
    }

    /**
     * Initializes a new {@link MailPath}
     *
     * @param mailPathStr The mail path's string representation
     * @throws OXException If mail path's string representation does not match expected pattern
     */
    public MailPath(final String mailPathStr) throws OXException {
        super();
        setMailIdentifierString(mailPathStr);
    }

    /**
     * Initializes a new {@link MailPath}
     *
     * @param accountId The account ID
     * @param folder Folder full name
     * @param uid The mail's unique ID
     */
    public MailPath(final int accountId, final String folder, final String uid) {
        super();
        this.accountId = accountId;
        this.folder = folder;
        mailID = uid;
        str = getMailPath(accountId, folder, mailID);
    }

    /**
     * Initializes a new {@link MailPath}
     *
     * @param folderArgument The full name argument; e.g. &quot;default123/INBOX&quot;
     * @param uid The mail's unique ID
     */
    public MailPath(final String folderArgument, final String uid) {
        super();
        FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderArgument);
        this.accountId = fa.getAccountId();
        this.folder = fa.getFullName();
        mailID = uid;
        str = getMailPath(accountId, folder, mailID);
    }

    /**
     * Sets specified arguments.
     *
     * @param accountId The account ID
     * @param folder Folder full name
     * @param uid The mail's unique ID
     */
    public void set(final int accountId, final String folder, final String uid) {
        this.accountId = accountId;
        this.folder = folder;
        mailID = uid;
        str = getMailPath(accountId, folder, mailID);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((mailID == null) ? 0 : mailID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailPath)) {
            return false;
        }
        MailPath other = (MailPath) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (mailID == null) {
            if (other.mailID != null) {
                return false;
            }
        } else if (!mailID.equals(other.mailID)) {
            return false;
        }
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            /*
             * Cannot occur since Cloneable is implemented
             */
            return null;
        }
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder full name
     *
     * @return The folder full name
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the folder argument; <i>default3/INBOX</i>.
     *
     * @return The folder argument
     */
    public String getFolderArgument() {
        return prepareFullname(accountId, folder);
    }

    /**
     * Gets this mail path's string representation.
     *
     * @return This mail path's string representation
     * @see #toString()
     */
    public String getStr() {
        return str;
    }

    /**
     * Gets the mail ID.
     *
     * @return The mail ID
     */
    public String getMailID() {
        return mailID;
    }

    /**
     * Sets this mail path's folder full name and mail's unique ID (for re-usage).
     *
     * @param mailPathStr The mail paths string representation
     * @return The mail path itself
     * @throws OXException If mail path's string representation does not match expected pattern
     */
    public MailPath setMailIdentifierString(final String mailPathStr) throws OXException {
        final int pos = mailPathStr.lastIndexOf(SEPERATOR);
        if (-1 == pos) {
            throw MailExceptionCode.INVALID_MAIL_IDENTIFIER.create(mailPathStr);
        }
        final FullnameArgument fa = prepareMailFolderParam(mailPathStr.substring(0, pos));
        accountId = fa.getAccountId();
        folder = fa.getFullname();
        mailID = mailPathStr.substring(pos + 1);
        str = mailPathStr;
        return this;
    }

    /**
     * Gets this mail path's string representation following pattern:<br>
     * <i>"default" + &lt;account-id&gt; + &lt;default-separator&gt; + &lt;folder-full-name&gt; + "/" + &lt;mail-id&gt;</i>
     *
     * <pre>
     * default2/INBOX/Subfolder/453&quot;
     * </pre>
     */
    @Override
    public String toString() {
        return str;
    }
}
