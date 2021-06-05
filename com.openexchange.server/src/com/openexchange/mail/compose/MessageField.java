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

package com.openexchange.mail.compose;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link MessageField} - An enumeration of query-able message fields.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public enum MessageField {

    /**
     * The  message's "From" header
     */
    FROM("from"),
    /**
     * The  message's "Sender" header
     */
    SENDER("sender"),
    /**
     * The  message's "To" header
     */
    TO("to"),
    /**
     * The  message's "Cc" header
     */
    CC("cc"),
    /**
     * The  message's "Bcc" header
     */
    BCC("bcc"),
    /**
     * The  message's "Reply-To" header
     */
    REPLY_TO("reply_to"),
    /**
     * The  message's "Subject" header
     */
    SUBJECT("subject"),
    /**
     * The message's textual content
     */
    CONTENT("content"),
    /**
     * The message's content type; either text/html or text/plain
     */
    CONTENT_TYPE("contentType"),
    /**
     * The "attachments associated with a message
     */
    ATTACHMENTS("attachments"),
    /**
     * The shared attachments information
     */
    SHARED_ATTACCHMENTS_INFO("sharedAttachmentsInfo"),
    /**
     * The meta data
     */
    META("meta"),
    /**
     * Whether a read receipt is requested
     */
    REQUEST_READ_RECEIPT("requestReadReceipt"),
    /**
     * The message's priority
     */
    PRIORITY("priority"),
    /**
     * The security information
     */
    SECURITY("security"),
    /**
     * Signals that content is stored encrypted.
     */
    CONTENT_ENCRYPTED("contentEncrypted"),
    /**
     * The optional custom headers
     */
    CUSTOM_HEADERS("customHeaders");

    private final String identifier;

    private MessageField(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static Map<String, MessageField> FIELDS;
    static {
        MessageField[] allFields = MessageField.values();
        ImmutableMap.Builder<String, MessageField> map = ImmutableMap.builderWithExpectedSize(allFields.length);
        for (MessageField mf : allFields) {
            map.put(Strings.asciiLowerCase(mf.identifier), mf);
        }
        FIELDS = map.build();
    }

    /**
     * Gets the message field for given identifier.
     *
     * @param identifier The identifier
     * @return The associated message field or <code>null</code>
     */
    public static MessageField messageFieldFor(String identifier) {
        return Strings.isEmpty(identifier) ? null : FIELDS.get(Strings.asciiLowerCase(identifier));
    }

    /**
     *  Gets the message fields for given identifiers.
     *
     * @param identifiers The identifiers
     * @return The associated message fields;<br>
     *         <b>Attention</b>: Array element is <code>null</code> if identifier could not be mapped to a message field
     */
    public static MessageField[] messageFieldsFor(String... identifiers) {
        if (null == identifiers) {
            return new MessageField[0];
        }

        int length = identifiers.length;
        if (length <= 0) {
            return new MessageField[0];
        }

        MessageField[] retval = new MessageField[length];
        for (int i = length; i-- > 0;) {
            retval[i] = messageFieldFor(identifiers[i]);
        }
        return retval;
    }

    /**
     * Adds specified field to array if not already contained.
     *
     * @param fields The fields to check
     * @param field The field to add
     * @return The fields with given element contained
     */
    public static MessageField[] addMessageFieldIfAbsent(MessageField[] fields, MessageField field) {
        if (fields == null) {
            return null;
        }
        if (field == null) {
            return fields;
        }

        for (MessageField mf : fields) {
            if (mf == field) {
                // Already contained
                return fields;
            }
        }

        MessageField[] newFields = new MessageField[fields.length + 1];
        System.arraycopy(fields, 0, newFields, 0, fields.length);
        newFields[fields.length] = field;
        return newFields;
    }

    /**
     * Checks if given field is contained in array.
     *
     * @param fields The fields
     * @param field The field to check for
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public static boolean isContained(MessageField[] fields, MessageField field) {
        if (fields == null) {
            return false;
        }
        if (field == null) {
            return false;
        }

        for (MessageField mf : fields) {
            if (mf == field) {
                // Already contained
                return true;
            }
        }
        return false;
    }

}
