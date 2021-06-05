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
