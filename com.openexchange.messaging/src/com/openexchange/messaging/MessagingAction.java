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
