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

package com.openexchange.dataretention.csv;

import java.util.Arrays;
import java.util.Date;
import com.openexchange.dataretention.RetentionData;

/**
 * {@link CSVRetentionData} - The CSV data retention's implementation of {@link RetentionData}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSVRetentionData implements RetentionData {

    private Date startTime;

    private String identifier;

    private String login;

    private String senderAddress;

    private String[] recipients;

    private String ipAddress;

    /**
     * Initializes a new {@link CSVRetentionData}.
     */
    public CSVRetentionData() {
        super();
    }

    @Override
    public String getIPAddress() {
        return ipAddress;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String[] getRecipientAddresses() {
        return recipients;
    }

    @Override
    public String getSenderAddress() {
        return senderAddress;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setIPAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void setLogin(final String login) {
        this.login = login;
    }

    @Override
    public void setRecipientAddresses(final String[] addresses) {
        recipients = new String[addresses.length];
        System.arraycopy(addresses, 0, recipients, 0, addresses.length);
    }

    @Override
    public void setSenderAddress(final String sender) {
        senderAddress = sender;
    }

    @Override
    public void setStartTime(final Date startTime) {
        this.startTime = new Date(startTime.getTime());
    }

    @Override
    public String toString() {
        final String na = "not available";
        final StringBuilder sb = new StringBuilder(128).append("Start-Time=").append(startTime == null ? na : startTime.toString());
        sb.append(" Identifier=").append(identifier == null ? na : identifier);
        sb.append(" Login=").append(login == null ? na : login);
        sb.append(" IP-Address=").append(ipAddress == null ? na : ipAddress);
        sb.append(" Sender-Address=").append(senderAddress == null ? na : senderAddress);
        sb.append(" Recipients=").append(recipients == null ? na : Arrays.toString(recipients));
        return sb.toString();
    }
}
