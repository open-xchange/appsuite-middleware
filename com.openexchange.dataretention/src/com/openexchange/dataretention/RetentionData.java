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

package com.openexchange.dataretention;

import java.util.Date;

/**
 * {@link RetentionData} - The retention data to store.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RetentionData {

    /**
     * Gets the UTC start time.
     *
     * @return The UTC start time or <code>null</code> if not available.
     */
    public Date getStartTime();

    /**
     * Sets the UTC start time.
     *
     * @param startTime The UTC start time.
     */
    public void setStartTime(Date startTime);

    /**
     * Gets the client IP address.
     *
     * @return The client IP address or <code>null</code> if not available.
     */
    public String getIPAddress();

    /**
     * Sets the client IP address.
     *
     * @param ipAddress The client IP address
     */
    public void setIPAddress(String ipAddress);

    /**
     * Gets the mailbox identifier.
     *
     * @return The mailbox identifier or <code>null</code> if not available.
     */
    public String getIdentifier();

    /**
     * Sets the mailbox identifier.
     *
     * @param identifier The mailbox identifier.
     */
    public void setIdentifier(String identifier);

    /**
     * Gets the login identifier.
     * <p>
     * Only applicable to mailbox access event.
     *
     * @return The login identifier or <code>null</code> if not available.
     */
    public String getLogin();

    /**
     * Sets the login identifier.
     * <p>
     * Only applicable to mailbox access event.
     *
     * @param login The login identifier.
     */
    public void setLogin(String login);

    /**
     * Gets the sender's email address.
     * <p>
     * Only applicable to mail transport event.
     *
     * @return The sender's email address or <code>null</code> if not available.
     */
    public String getSenderAddress();

    /**
     * Sets the sender's email address.
     * <p>
     * Only applicable to mail transport event.
     *
     * @param sender The sender's email address.
     */
    public void setSenderAddress(String sender);

    /**
     * Gets the recipients' email addresses.
     * <p>
     * Only applicable to mail transport event.
     *
     * @return The recipients' email addresses or an empty array if not available.
     */
    public String[] getRecipientAddresses();

    /**
     * Sets the recipients' email addresses.
     * <p>
     * Only applicable to mail transport event.
     *
     * @param addresses The recipients' email addresses.
     */
    public void setRecipientAddresses(String[] addresses);
}
