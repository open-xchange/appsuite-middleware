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

package com.openexchange.folderstorage.messaging;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.messaging.contentType.DraftsContentType;
import com.openexchange.folderstorage.messaging.contentType.MessagingContentType;
import com.openexchange.folderstorage.messaging.contentType.SentContentType;
import com.openexchange.folderstorage.messaging.contentType.SpamContentType;
import com.openexchange.folderstorage.messaging.contentType.TrashContentType;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolder.DefaultFolderType;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link MessagingFolderImpl} - A messaging folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingFolderImpl extends AbstractFolder {

    private static final long serialVersionUID = 6445442372690458946L;

    /**
     * The messaging folder content type.
     */
    public static enum MessagingFolderType {
        NONE(MessagingContentType.getInstance(), 0),
        ROOT(MessagingContentType.getInstance(), 0),
        INBOX(MessagingContentType.getInstance(), 13), // FolderObject.MESSAGING
        DRAFTS(DraftsContentType.getInstance(), 14),
        SENT(SentContentType.getInstance(), 15),
        SPAM(SpamContentType.getInstance(), 16),
        TRASH(TrashContentType.getInstance(), 17),
        MESSAGING(MessagingContentType.getInstance(), 13);

        private final ContentType contentType;

        private final int type;

        private MessagingFolderType(final ContentType contentType, final int type) {
            this.contentType = contentType;
            this.type = type;
        }

        /**
         * Gets the content type associated with this mail folder type.
         *
         * @return The content type
         */
        public ContentType getContentType() {
            return contentType;
        }

        /**
         * Gets the type.
         *
         * @return The type
         */
        public int getType() {
            return type;
        }

    }

    private MessagingFolderType messagingFolderType;
    private boolean cacheable;
    private String localizedName;

    /**
     * Initializes an empty {@link MessagingFolderImpl}.
     */
    public MessagingFolderImpl() {
        super();
    }

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * Initializes a new {@link MessagingFolderImpl} from given messaging folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param messagingFolder The underlying messaging folder
     * @param accountId The account identifier
     * @param serviceId The service identifier
     * @param fullnameProvider The (optional) fullname provider
     */
    public MessagingFolderImpl(final MessagingFolder messagingFolder, final int accountId, final String serviceId, final User user, final DefaultFolderFullnameProvider fullnameProvider) {
        super();
        final String fullname = messagingFolder.getId();
        id = MessagingFolderIdentifier.getFQN(serviceId, accountId, fullname);
        name = messagingFolder.getName();
        super.accountId = IDMangler.mangle(serviceId, Integer.toString(accountId));
        final boolean isRootFolder = messagingFolder.isRootFolder();
        if (isRootFolder) { // Root folder
            parent = PRIVATE_FOLDER_ID;
        } else {
            parent = MessagingFolderIdentifier.getFQN(serviceId, accountId, messagingFolder.getParentId());
        }
        {
            final List<MessagingPermission> messagingPermissions = messagingFolder.getPermissions();
            final int size = messagingPermissions.size();
            permissions = new Permission[size];
            for (int i = 0; i < size; i++) {
                permissions[i] = new MessagingPermissionImpl(messagingPermissions.get(i));
            }
        }
        type = SystemType.getInstance();
        subscribed = messagingFolder.isSubscribed();
        subscribedSubfolders = messagingFolder.hasSubscribedSubfolders();
        capabilities = parseCaps(messagingFolder.getCapabilities());
        {
            final String value =
                isRootFolder ? "" : new StringBuilder(16).append('(').append(messagingFolder.getMessageCount()).append('/').append(
                    messagingFolder.getUnreadMessageCount()).append(')').toString();
            summary = value;
        }
        deefault = /* messagingFolder.isDefaultFolder(); */0 == accountId && messagingFolder.isDefaultFolder();
        total = messagingFolder.getMessageCount();
        nu = messagingFolder.getNewMessageCount();
        unread = messagingFolder.getUnreadMessageCount();
        deleted = messagingFolder.getDeletedMessageCount();
        if (messagingFolder.containsDefaultFolderType()) {
            messagingFolderType = TYPES.get(messagingFolder.getDefaultFolderType());
            switch (messagingFolderType) {
            case DRAFTS:
                localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.DRAFTS);
                break;
            case SENT:
                localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.SENT);
                break;
            case SPAM:
                localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.SPAM);
                break;
            case TRASH:
                localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.TRASH);
                break;
            default:
                localizedName = null;
                break;
            }
        } else if (messagingFolder.isRootFolder()) {
            messagingFolderType = MessagingFolderType.ROOT;
        } else if (null != fullname) {
            try {
                if (fullname.equals(fullnameProvider.getDraftsFolder())) {
                    localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.DRAFTS);
                    messagingFolderType = MessagingFolderType.DRAFTS;
                } else if (fullname.equals(fullnameProvider.getINBOXFolder())) {
                    messagingFolderType = MessagingFolderType.INBOX;
                } else if (fullname.equals(fullnameProvider.getSentFolder())) {
                    localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.SENT);
                    messagingFolderType = MessagingFolderType.SENT;
                } else if (fullname.equals(fullnameProvider.getSpamFolder())) {
                    localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.SPAM);
                    messagingFolderType = MessagingFolderType.SPAM;
                } else if (fullname.equals(fullnameProvider.getTrashFolder())) {
                    localizedName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.TRASH);
                    messagingFolderType = MessagingFolderType.TRASH;
                } else {
                    messagingFolderType = MessagingFolderType.NONE;
                }
            } catch (final OXException e) {
                org.slf4j.LoggerFactory.getLogger(MessagingFolderImpl.class).error("", e);
                messagingFolderType = MessagingFolderType.NONE;
            }
        } else {
            messagingFolderType = MessagingFolderType.NONE;
        }
        /*
         * Trash folder must not be cacheable
         */
        cacheable = !messagingFolder.isDefaultFolder() || !messagingFolderType.equals(MessagingFolderType.TRASH);
    }

    @Override
    public Object clone() {
        final MessagingFolderImpl clone = (MessagingFolderImpl) super.clone();
        clone.cacheable = cacheable;
        return clone;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public ContentType getContentType() {
        return messagingFolderType.getContentType();
    }

    @Override
    public int getDefaultType() {
        return messagingFolderType.getType();
    }

    @Override
    public void setDefaultType(final int defaultType) {
        // Nothing to do
    }

    @Override
    public Type getType() {
        return MailType.getInstance();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    @Override
    public void setType(final Type type) {
        // Nothing to do
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        final String localizedName = this.localizedName;
        if (null == localizedName) {
            return name;
        }
        return localizedName;
    }

    private static final Map<IgnoreCaseString, Integer> KNOWN_CAPS;

    private static final Map<DefaultFolderType, MessagingFolderType> TYPES;

    static {
        final Map<IgnoreCaseString, Integer> m = new HashMap<IgnoreCaseString, Integer>(16);

        m.put(IgnoreCaseString.valueOf("ACL"), Integer.valueOf(1)); // MailCapabilities.BIT_PERMISSIONS
        m.put(IgnoreCaseString.valueOf(MessagingFolder.CAPABILITY_PERMISSIONS), Integer.valueOf(1)); // MailCapabilities.BIT_PERMISSIONS

        m.put(IgnoreCaseString.valueOf("THREAD=REFERENCES"), Integer.valueOf(2)); // MailCapabilities.BIT_THREAD_REFERENCES
        m.put(IgnoreCaseString.valueOf("THREAD_REFERENCES"), Integer.valueOf(2)); // MailCapabilities.BIT_THREAD_REFERENCES
        m.put(IgnoreCaseString.valueOf("THREAD-REFERENCES"), Integer.valueOf(2)); // MailCapabilities.BIT_THREAD_REFERENCES

        m.put(IgnoreCaseString.valueOf(MessagingFolder.CAPABILITY_QUOTA), Integer.valueOf(4)); // MailCapabilities.BIT_QUOTA

        m.put(IgnoreCaseString.valueOf(MessagingFolder.CAPABILITY_SORT), Integer.valueOf(8)); // MailCapabilities.BIT_SORT

        m.put(IgnoreCaseString.valueOf("SUBSCRIPTION"), Integer.valueOf(16)); // MailCapabilities.BIT_SUBSCRIPTION

        m.put(IgnoreCaseString.valueOf("THREAD=ORDEREDSUBJECT"), Integer.valueOf(32)); // IMAPCapabilities.BIT_THREAD_ORDEREDSUBJECT

        m.put(IgnoreCaseString.valueOf("IMAP4"), Integer.valueOf(64)); // IMAPCapabilities.BIT_IMAP4

        m.put(IgnoreCaseString.valueOf("IMAP4rev1"), Integer.valueOf(128)); // IMAPCapabilities.BBIT_IMAP4_REV1

        m.put(IgnoreCaseString.valueOf("UIDPLUS"), Integer.valueOf(256)); // IMAPCapabilities.BIT_UIDPLUS

        m.put(IgnoreCaseString.valueOf("NAMESPACE"), Integer.valueOf(512)); // IMAPCapabilities.BIT_NAMESPACE

        m.put(IgnoreCaseString.valueOf("IDLE"), Integer.valueOf(1024)); // IMAPCapabilities.BIT_IDLE

        m.put(IgnoreCaseString.valueOf("CHILDREN"), Integer.valueOf(2048)); // IMAPCapabilities.BIT_CHILDREN

        KNOWN_CAPS = Collections.unmodifiableMap(m);

        final Map<DefaultFolderType, MessagingFolderType> m2 = new HashMap<DefaultFolderType, MessagingFolderType>(8);

        m2.put(DefaultFolderType.CONFIRMED_HAM, MessagingFolderType.NONE);
        m2.put(DefaultFolderType.CONFIRMED_SPAM, MessagingFolderType.NONE);
        m2.put(DefaultFolderType.DRAFTS, MessagingFolderType.DRAFTS);
        m2.put(DefaultFolderType.INBOX, MessagingFolderType.INBOX);
        m2.put(DefaultFolderType.NONE, MessagingFolderType.NONE);
        m2.put(DefaultFolderType.SENT, MessagingFolderType.SENT);
        m2.put(DefaultFolderType.SPAM, MessagingFolderType.SPAM);
        m2.put(DefaultFolderType.TRASH, MessagingFolderType.TRASH);
        m2.put(DefaultFolderType.MESSAGING, MessagingFolderType.MESSAGING);

        TYPES = Collections.unmodifiableMap(m2);
    }

    /**
     * Parses given capabilities to an <code>int</code> value.
     *
     * @param caps The capabilities to parse
     * @return The resulting <code>int</code> value
     */
    public static int parseCaps(final Set<String> caps) {
        int retval = 0;
        for (final String cap : caps) {
            final Integer bit = KNOWN_CAPS.get(IgnoreCaseString.valueOf(cap));
            retval = null == bit ? retval : retval | bit.intValue();
        }
        return retval;
    }

    private static final class IgnoreCaseString implements Serializable, Cloneable, Comparable<IgnoreCaseString>, CharSequence {

        private static final long serialVersionUID = -74324024218963602L;

        /**
         * Initializes a new header name from specified character sequence.
         * <p>
         * Yields significantly better space and time performance by caching frequently requested headers.
         *
         * @param s The character sequence
         * @return The new header name.
         */
        static IgnoreCaseString valueOf(final CharSequence s) {
            return new IgnoreCaseString(s.toString());
        }

        private final String s;

        private final int hashcode;

        /**
         * No direct instantiation
         */
        private IgnoreCaseString(final String s) {
            super();
            this.s = s;
            hashcode = s.toLowerCase(Locale.ENGLISH).hashCode();
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                /*
                 * Cannot not occur since Cloneable is implemented
                 */
                throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
            }
        }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof IgnoreCaseString)) {
                return s.equalsIgnoreCase(((IgnoreCaseString) other).s);
            }
            if ((other instanceof String)) {
                return s.equalsIgnoreCase((String) other);
            }
            return false;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public int hashCode() {
            return hashcode;
        }

        @Override
        public int compareTo(final IgnoreCaseString other) {
            return s.compareToIgnoreCase(other.s);
        }

        @Override
        public char charAt(final int index) {
            return s.charAt(index);
        }

        @Override
        public int length() {
            return s.length();
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return s.subSequence(start, end);
        }

    } // End of class IgnoreCaseString

}
