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
