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

package com.openexchange.messaging;

/**
 * {@link MessagingAction} - A messaging action examined in <code>perform()</code> method.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MessagingAction {

    /**
     * The action type.
     */
    public static enum Type {
        /**
         * The action requires no message and no arguments.
         */
        NONE,
        /**
         * The action requires a message from storage identified by given arguments.
         */
        STORAGE,
        /**
         * The action requires a given message.
         */
        MESSAGE;
    }

    /**
     * The action name.
     */
    private final String name;

    /**
     * The action type.
     */
    private final Type type;

    /**
     * The name of the (possibly) following messaging action.
     */
    private final String follower;

    /**
     * Initializes a new {@link MessagingAction}.
     *
     * @param name The name
     * @param type The type
     */
    public MessagingAction(final String name, final Type type) {
        this(name, type, null);
    }

    /**
     * Initializes a new {@link MessagingAction}.
     *
     * @param name The name
     * @param type The type
     * @param follower The follower action name; may be <code>null</code> to indicate no following action
     */
    public MessagingAction(final String name, final Type type, final String follower) {
        super();
        this.name = name;
        this.type = type;
        this.follower = follower;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the follower or <code>null</code> to indicate no following action.
     *
     * @return The follower or <code>null</code> to indicate no following action
     */
    public String getFollower() {
        return follower;
    }

}
