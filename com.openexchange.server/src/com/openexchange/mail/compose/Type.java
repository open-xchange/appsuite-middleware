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
public enum Type {
    /**
     * A new mail is compiled.
     */
    NEW("new"),
    /**
     * A reply to an existent mail is compiled.
     */
    REPLY("reply"),
    /**
     * A reply-all to an existent mail is compiled.
     */
    REPLY_ALL("replyall"),
    /**
     * A forward for an existent mail is compiled.
     */
    FORWARD("forward"),
    /**
     * A continuation of an existent draft (aka "edit draft") is compiled.
     */
    EDIT("edit"),
    /**
     * A copy of an existent draft template is compiled. No reference is suppose to be kept.
     */
    COPY("copy"),
    /**
     * A resend/bounce of an existent mail is compiled.
     */
    RESEND("resend");

    private final String id;

    private Type(String id) {
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

    private static final Map<String, Type> MAP;
    static {
        Type[] types = Type.values();
        ImmutableMap.Builder<String, Type> m = ImmutableMap.builderWithExpectedSize(types.length);
        for (Type type : types) {
            m.put(type.getId(), type);
        }
        MAP = m.build();
    }

    /**
     * Gets the type for specified type identifier.
     *
     * @param type The type identifier
     * @return The type associated with the given identifier or <code>null</code> if there is no such type
     */
    public static Type typeFor(String type) {
        if (null == type) {
            return null;
        }

        return MAP.get(Strings.asciiLowerCase(type));
    }
}
