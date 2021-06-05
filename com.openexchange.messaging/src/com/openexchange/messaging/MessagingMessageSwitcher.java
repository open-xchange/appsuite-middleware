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

import com.openexchange.exception.OXException;

/**
 * {@link MessagingMessageSwitcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingMessageSwitcher {

    /**
     * Handles a message's identifier and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object id(Object... args) throws OXException;

    /**
     * Handles a message's folder identifier and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object folderId(Object... args) throws OXException;

    /**
     * Handles a message's folder identifier and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object contentType(Object... args) throws OXException;

    /**
     * Handles a message's from address and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object from(Object... args) throws OXException;

    /**
     * Handles a message's To address and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object to(Object... args) throws OXException;

    /**
     * Handles a message's Cc address and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object cc(Object... args) throws OXException;

    /**
     * Handles a message's Bcc address and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object bcc(Object... args) throws OXException;

    /**
     * Handles a message's subject and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object subject(Object... args) throws OXException;

    /**
     * Handles a message's size and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object size(Object... args) throws OXException;

    /**
     * Handles a message's sent date and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object sentDate(Object... args) throws OXException;

    /**
     * Handles a message's received date and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object receivedDate(Object... args) throws OXException;

    /**
     * Handles a message's flags and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object flags(Object... args) throws OXException;

    /**
     * Handles a message's thread level and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object threadLevel(Object... args) throws OXException;

    /**
     * Handles a message's disposition notification and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object dispositionNotificationTo(Object... args) throws OXException;

    /**
     * Handles a message's priority and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object priority(Object... args) throws OXException;

    /**
     * Handles a message's color label and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object colorLabel(Object... args) throws OXException;

    /**
     * Handles a message's accout name and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object accountName(Object... args) throws OXException;

    /**
     * Handles a message's body and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object body(Object... args) throws OXException;

    /**
     * Handles a message's headers and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object headers(Object... args) throws OXException;

    /**
     * Handles all fields of a message and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object full(Object... args) throws OXException;

    /**
     * Handles a message's picture URI and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object picture(Object... args) throws OXException;

    /**
     * Handles a message's URL and returns switcher's optional value.
     *
     * @param args The arguments for handling
     * @return The switcher's value or <code>null</code>
     * @throws OXException If a messaging error occurs
     */
    public Object url(final Object... args) throws OXException;

}
