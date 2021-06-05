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
 * The compose type
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Type {

    private final String id;

    /**
     * Initializes a new {@link Type}.
     *
     * @param id The identifier
     */
    private Type(String id) {
        super();
        this.id = id;
    }

    /**
     * Gets the type identifier.
     *
     * @return The type identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * A new mail is compiled.
     */
    public static final Type NEW = new Type("new");

    /**
     * A reply to an existent mail is compiled.
     */
    public static final Type REPLY = new Type("reply");

    /**
     * A reply-all to an existent mail is compiled.
     */
    public static final Type REPLY_ALL = new Type("replyall");

    /**
     * A forward for an existent mail is compiled.
     */
    public static final Type FORWARD = new Type("forward");

    /**
     * A continuation of an existent draft (aka "edit draft") is compiled.
     */
    public static final Type EDIT = new Type("edit");

    /**
     * A copy of an existent draft template is compiled. No reference is suppose to be kept.
     */
    public static final Type COPY = new Type("copy");

    /**
     * A resend/bounce of an existent mail is compiled.
     */
    public static final Type RESEND = new Type("resend");

    /**
     * A new SMS message is compiled.
     */
    public static final Type SMS = new Type("sms");

    /**
     * A new FAX message is compiled.
     */
    public static final Type FAX = new Type("fax");

    private static final Map<String, Type> TYPES;

    static {
        ImmutableMap.Builder<String, Type> m = ImmutableMap.builderWithExpectedSize(10);
        m.put(Type.COPY.getId(), Type.COPY);
        m.put(Type.EDIT.getId(), Type.EDIT);
        m.put(Type.FAX.getId(), Type.FAX);
        m.put(Type.FORWARD.getId(), Type.FORWARD);
        m.put(Type.NEW.getId(), Type.NEW);
        m.put(Type.REPLY.getId(), Type.REPLY);
        m.put(Type.REPLY_ALL.getId(), Type.REPLY_ALL);
        m.put(Type.RESEND.getId(), Type.RESEND);
        m.put(Type.SMS.getId(), Type.SMS);
        TYPES = m.build();
    }

    /**
     * Gets the type for specified type identifier.
     *
     * @param type The type identifier
     * @return The type associated with the given identifier
     */
    public static Type typeFor(String id) {
        if (Strings.isEmpty(id)) {
            throw new IllegalArgumentException("Identifier must not be null or empty");
        }

        Type t = TYPES.get(Strings.asciiLowerCase(id));
        return t == null ? new Type(id) : t;
    }
}
