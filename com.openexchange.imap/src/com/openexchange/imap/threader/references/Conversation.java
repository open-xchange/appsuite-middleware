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

package com.openexchange.imap.threader.references;

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

    private boolean messageIdsEmpty;
    private boolean referencesEmpty;

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation() {
        super();
        messages = new HashSet<MailMessageWrapper>(DEFAULT_INITIAL_CAPACITY);
        messageIds = new HashSet<String>(DEFAULT_INITIAL_CAPACITY << 1, 0.9f);
        references = new HashSet<String>(DEFAULT_INITIAL_CAPACITY << 1, 0.9f);

        messageIdsEmpty = true;
        referencesEmpty = true;
    }

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation(final MailMessage message) {
        this();
        addMessage(message);
    }

    /**
     * Initializes a new {@link Conversation}.
     */
    public Conversation(final Collection<MailMessage> messages) {
        this();
        if (null != messages) {
            for (final MailMessage message : messages) {
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
    public Conversation addMessage(final MailMessage message) {
        if (null != message) {
            addWrapper(new MailMessageWrapper(message));
        }
        return this;
    }

    private void addWrapper(final MailMessageWrapper mmw) {
        if (messages.add(mmw)) {
            final MailMessage message = mmw.message;
            final String messageId = message.getMessageId();
            if (null != messageId) {
                if (messageIds.add(messageId)) {
                    messageIdsEmpty = false;
                }
            }

            final String[] sReferences = message.getReferences();
            if (null != sReferences) {
                for (final String sReference : sReferences) {
                    if (null != sReference) {
                        if (references.add(sReference)) {
                            referencesEmpty = false;
                        }
                    }
                }
            } else {
                String inReplyTo = message.getInReplyTo();
                if (null != inReplyTo) {
                    if (references.add(inReplyTo)) {
                        referencesEmpty = false;
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
    public Conversation join(final Conversation other) {
        if (null != other) {
            final Set<MailMessageWrapper> messages = other.messages;
            for (final MailMessageWrapper mmw : messages) {
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
    public boolean referencesOrIsReferencedBy(final MailMessage message) {
        if (!this.referencesEmpty) {
            final String messageId = message.getMessageId();
            if (null != messageId && this.references.contains(messageId)) {
                return true;
            }
        }

        String[] sReferences = this.referencesEmpty ? null : message.getReferences();
        if (null != sReferences && containsAny(this.references, sReferences)) {
            return true;
        }

        if (!this.messageIdsEmpty) {
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
    public boolean referencesOrIsReferencedBy(final Conversation other) {
        if (!this.referencesEmpty) {
            if (containsAny(this.references, other.messageIds) || (other.referencesEmpty ? false : containsAny(this.references, other.references))) {
                return true;
            }
        }

        if (!other.referencesEmpty) {
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
    public boolean references(final Conversation other) {
        return this.referencesEmpty ? false : (containsAny(this.references, other.messageIds) || (other.referencesEmpty ? false : containsAny(this.references, other.references)));
    }

    /**
     * Checks if this conversation is referenced by given conversation
     *
     * @param other The other conversation
     * @return <code>true</code> if referenced-by; otherwise <code>false</code>
     */
    public boolean isReferencedBy(final Conversation other) {
        return other.referencesEmpty ? false : containsAny(this.messageIds, other.references);
    }

    /**
     * Checks if at least one element is in both collections.
     *
     * @param set The first collection, must not be <code>null</code>
     * @param col The second collection, must not be <code>null</code>
     * @return <code>true</code> if the intersection of the collections is non-empty
     */
    private static boolean containsAny(final Set<String> set, final Collection<String> col) {
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
    private static boolean containsAny(final Set<String> set, final String[] arr) {
        for (int i = arr.length; i-- > 0;) {
            if (set.contains(arr[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     *Adds the message identifiers from this conversation to given set.
     *
     * @param set The set to add the message identifiers to
     */
    public void addMessageIdsTo(Set<String> set) {
        if (!messageIdsEmpty) {
            set.addAll(messageIds);
        }
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
    public List<MailMessage> getMessages(final Comparator<MailMessage> comparator) {
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }
        final List<MailMessage> ret = new ArrayList<MailMessage>(messages.size());
        for (final MailMessageWrapper mmw : messages) {
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

        MailMessageWrapper(final MailMessage message) {
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
        public boolean equals(final Object obj) {
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
