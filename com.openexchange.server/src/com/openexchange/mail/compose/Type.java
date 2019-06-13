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
 *    trademarks of the OX Software GmbH. group of companies.
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
