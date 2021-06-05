/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.threader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.MailMessageComparator;

/**
 * {@link Conversation} - Encapsulates a list of messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Conversation {

    /**
     * The default comparator.
     */
    private static final MailMessageComparator COMPARATOR_DESC = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final Set<MailMessageWrapper> messages;
    private final Set<String> messageIds;
    private final Set<String> references;

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation() {
        super();
        messages = new HashSet<MailMessageWrapper>(DEFAULT_INITIAL_CAPACITY);
        messageIds = new HashSet<String>(DEFAULT_INITIAL_CAPACITY << 1, 0.9f);
        references = new HashSet<String>(DEFAULT_INITIAL_CAPACITY << 1, 0.9f);
    }

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation(Conversation copy) {
        super();
        // Must not be null
        messages = copy.messages;
        messageIds = copy.messageIds;
        references = copy.references;
    }

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation(MailMessage message) {
        this();
        addMessage(message);
    }

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation(Collection<MailMessage> messages) {
        this();
        if (null != messages) {
            for (MailMessage message : messages) {
                addMessage(message);
            }
        }
    }

    /**
     * Adds given message to this conversation
     *
     * @param message The message to add
     * @return This conversation with message added
     */
    public Conversation addMessage(MailMessage message) {
        if (null != message) {
            addWrapper(new MailMessageWrapper(message));
        }
        return this;
    }

    private void addWrapper(MailMessageWrapper mmw) {
        if (messages.add(mmw)) {
            final MailMessage message = mmw.message;
            final String messageId = message.getMessageId();
            if (null != messageId) {
                messageIds.add(messageId);
            }
            /*
            final String inReplyTo = message.getInReplyTo();
            if (null != inReplyTo) {
                references.add(inReplyTo);
            }
            */
            final String[] sReferences = message.getReferences();
            if (null != sReferences) {
                for (String sReference : sReferences) {
                    if (null != sReference) {
                        references.add(sReference);
                    }
                }
            }
        }
    }

    /**
     * Joins this conversation with other conversation.
     *
     * @param other The other conversation to join with
     * @return This conversation
     */
    public Conversation join(Conversation other) {
        if (null != other) {
            final Set<MailMessageWrapper> messages = other.messages;
            for (MailMessageWrapper mmw : messages) {
                addWrapper(mmw);
            }
        }
        return this;
    }

    /**
     * Checks if this conversation references OR is referenced by given message
     *
     * @param message The message
     * @return <code>true</code> if references or referenced-by; otherwise <code>false</code>
     */
    public boolean referencesOrIsReferencedBy(MailMessage message) {
        if (!this.references.isEmpty()) {
            final String messageId = message.getMessageId();
            if (null != messageId && this.references.contains(messageId)) {
                return true;
            }
        }

        String[] sReferences = this.references.isEmpty() ? null : message.getReferences();
        if (null != sReferences && containsAny(this.references, sReferences)) {
            return true;
        }

        if (!this.messageIds.isEmpty()) {
            if (null == sReferences) {
                sReferences = message.getReferences();
            }
            if (null != sReferences && containsAny(this.messageIds, sReferences)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this conversation references OR is referenced by given conversation
     *
     * @param other The other conversation
     * @return <code>true</code> if references or referenced-by; otherwise <code>false</code>
     */
    public boolean referencesOrIsReferencedBy(Conversation other) {
        if (!this.references.isEmpty()) {
            if (containsAny(this.references, other.messageIds) || (other.references.isEmpty() ? false : containsAny(this.references, other.references))) {
                return true;
            }
        }

        if (!other.references.isEmpty()) {
            if (containsAny(this.messageIds, other.references)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this conversation references to given conversation
     *
     * @param other The other conversation possibly referenced
     * @return <code>true</code> if references; otherwise <code>false</code>
     */
    public boolean references(Conversation other) {
        return this.references.isEmpty() ? false : (containsAny(this.references, other.messageIds) || (other.references.isEmpty() ? false : containsAny(this.references, other.references)));
    }

    /**
     * Checks if this conversation is referenced by given conversation
     *
     * @param other The other conversation
     * @return <code>true</code> if referenced-by; otherwise <code>false</code>
     */
    public boolean isReferencedBy(Conversation other) {
        return other.references.isEmpty() ? false : containsAny(this.messageIds, other.references);
    }

    /**
     * Checks if at least one element is in both collections.
     *
     * @param set The first collection, must not be <code>null</code>
     * @param col The second collection, must not be <code>null</code>
     * @return <code>true</code> if the intersection of the collections is non-empty
     */
    private static boolean containsAny(Set<String> set, Collection<String> col) {
        final Iterator<String> it = col.iterator();
        for (int i = col.size(); i-- > 0;) {
            if (set.contains(it.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if at least one element is in both collections.
     *
     * @param set The first collection, must not be <code>null</code>
     * @param arr The second collection, must not be <code>null</code>
     * @return <code>true</code> if the intersection of the collections is non-empty
     */
    private static boolean containsAny(Set<String> set, String[] arr) {
        for (int i = arr.length; i-- > 0;) {
            if (set.contains(arr[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the messages.
     *
     * @return The messages with default sorting
     */
    public List<MailMessage> getMessages() {
        return getMessages(COMPARATOR_DESC);
    }

    /**
     * Gets the messages.
     *
     * @param comparator The comparator used for sorting listed messages
     * @return The messages with given sorting
     */
    public List<MailMessage> getMessages(Comparator<MailMessage> comparator) {
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }
        final List<MailMessage> ret = new ArrayList<MailMessage>(messages.size());
        for (MailMessageWrapper mmw : messages) {
            ret.add(mmw.message);
        }
        Collections.sort(ret, null == comparator ? COMPARATOR_DESC : comparator);
        return ret;
    }

    /**
     * Simple wrapper class for having a message as hash key.
     * <p>
     * The key is built from its identifier and folder full name.
     */
    private static final class MailMessageWrapper {

        final MailMessage message;
        private final int hash;

        MailMessageWrapper(MailMessage message) {
            super();
            this.message = message;
            final String id = message.getMailId();
            final String fullName = message.getFolder();
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MailMessageWrapper)) {
                return false;
            }
            final MailMessageWrapper other = (MailMessageWrapper) obj;
            if (message.getMailId() == null) {
                if (other.message.getMailId() != null) {
                    return false;
                }
            } else if (!message.getMailId().equals(other.message.getMailId())) {
                return false;
            }
            if (message.getFolder() == null) {
                if (other.message.getFolder() != null) {
                    return false;
                }
            } else if (!message.getFolder().equals(other.message.getFolder())) {
                return false;
            }
            return true;
        }
    } // End of class MailMessageWrapper

}
