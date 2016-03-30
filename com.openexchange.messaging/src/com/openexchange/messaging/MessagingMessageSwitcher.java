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
