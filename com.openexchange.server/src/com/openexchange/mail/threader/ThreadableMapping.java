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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;

/**
 * {@link ThreadableMapping} - A <code>Message-Id</code> and <code>References</code> mapping from specified {@code MailMessage} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadableMapping {

    private static final class MessageKey {
        final String fullName;
        final String id;
        final int hash;

        MessageKey(String id, String fullName) {
            super();
            this.id = id;
            this.fullName = fullName;
            // Hash
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
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
            if (!(obj instanceof MessageKey)) {
                return false;
            }
            MessageKey other = (MessageKey) obj;
            if (fullName == null) {
                if (other.fullName != null) {
                    return false;
                }
            } else if (!fullName.equals(other.fullName)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    } // End of class MessageKey

    private static MessageKey keyFor(MailMessage mailMessage) {
        if (null == mailMessage) {
            return null;
        }
        return new MessageKey(mailMessage.getMailId(), mailMessage.getFolder());
    }

    private final Map<String, List<MailMessage>> refsMap;
    private final Map<String, List<MailMessage>> messageIdMap;

    /**
     * Initializes a new {@link ThreadableMapping}.
     */
    public ThreadableMapping(int capacity) {
        super();
        refsMap = new HashMap<String, List<MailMessage>>(capacity << 1, 0.9f);
        messageIdMap = new HashMap<String, List<MailMessage>>(capacity, 0.9f);
    }

    /**
     * Checks specified {@link Iterable} and adds elements to <code>thread</code> if appropriate.
     *
     * @param toCheck The {@link Iterable} to check
     * @param thread The thread to add into
     * @return Whether <code>thread</code> has been changed as a result of this call
     */
    public boolean checkFor(Iterable<MailMessage> toCheck, List<MailMessage> thread) {
        boolean changed = false;
        // Set for existing Message-Ids
        final Set<String> existingMessageIds = new HashSet<String>(thread.size());
        for (MailMessage mailMessage : thread) {
            existingMessageIds.add(mailMessage.getMessageId());
        }
        // Set for already processed ones
        final Set<MessageKey> processed = new HashSet<MessageKey>(thread.size());
        for (MailMessage mail : toCheck) {
            final String messageId = mail.getMessageId();
            if (null != messageId) {
                // Those mails that refer to specified mail
                final List<MailMessage> referencees = refsMap.get(messageId);
                if (null != referencees) {
                    for (MailMessage candidate : referencees) {
                        if (!existingMessageIds.contains(candidate.getMessageId()) && processed.add(keyFor(candidate))) {
                            thread.add(candidate);
                            changed = true;
                        }
                    }
                }
            }
            /*
            final String inReplyTo = mail.getInReplyTo();
            if (null != inReplyTo) {
                // Those mails that are referenced by specified mail
                final List<MailMessage> references = messageIdMap.get(inReplyTo);
                if (null != references) {
                    for (MailMessage candidate : references) {
                        if (processed.add(keyFor(candidate))) {
                            thread.add(candidate);
                            changed = true;
                        }
                    }
                }
            }
             */
            final String[] sReferences = mail.getReferences();
            if (null != sReferences) {
                for (String sReference : sReferences) {
                    // Those mails that are referenced by specified mail
                    final List<MailMessage> references = messageIdMap.get(sReference);
                    if (null != references) {
                        for (MailMessage candidate : references) {
                            if (!existingMessageIds.contains(candidate.getMessageId()) && processed.add(keyFor(candidate))) {
                                thread.add(candidate);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Gets those {@code MailMessage} instances whose <code>References</code> header contain specified <code>Message-Id</code> header.
     *
     * @param messageId The <code>Message-Id</code> header
     * @return The {@code MailMessage} instances
     */
    public Set<MailMessage> getRefs(String messageId) {
        final List<MailMessage> list = refsMap.get(messageId);
        return list == null ? Collections.<MailMessage> emptySet() : new LinkedHashSet<MailMessage>(list);
    }

    /**
     * Gets the {@code MailMessage} instances whose <code>Message-Id</code> header matches given <code>Message-Id</code> header
     *
     * @param messageId The <code>Message-Id</code> header
     * @return The {@code MailMessage} instances
     */
    public Set<MailMessage> getMessageId(String messageId) {
        final List<MailMessage> list = messageIdMap.get(messageId);
        return list == null ? Collections.<MailMessage> emptySet() : new LinkedHashSet<MailMessage>(list);
    }

    /**
     * Fills this mapping with specified {@code MailMessage} instances.
     *
     * @param mails The {@code MailMessage} instances
     * @return This mapping
     */
    public ThreadableMapping initWith(List<MailMessage> mails) {
        fill(mails, messageIdMap, refsMap);
        return this;
    }

    private static void fill(List<MailMessage> mails, Map<String, List<MailMessage>> messageIdMap, Map<String, List<MailMessage>> refsMap) {
        final String hdrMessageId = MessageHeaders.HDR_MESSAGE_ID;
        for (MailMessage current : mails) {
            final String[] refs = current.getReferences();
            if (null != refs) {
                for (String reference : refs) {
                    if (!com.openexchange.java.Strings.isEmpty(reference)) {
                        List<MailMessage> list = refsMap.get(reference);
                        if (null == list) {
                            list = new LinkedList<MailMessage>();
                            refsMap.put(reference, list);
                        }
                        list.add(current);
                    }
                }
            }
            final String messageId = current.getFirstHeader(hdrMessageId);
            if (!com.openexchange.java.Strings.isEmpty(messageId)) {
                List<MailMessage> list = messageIdMap.get(messageId);
                if (null == list) {
                    list = new LinkedList<MailMessage>();
                    messageIdMap.put(messageId, list);
                }
                list.add(current);
            }
        }
    }
}
