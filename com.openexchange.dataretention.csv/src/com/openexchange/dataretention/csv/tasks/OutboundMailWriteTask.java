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

package com.openexchange.dataretention.csv.tasks;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.dataretention.csv.CSVFile;
import com.openexchange.dataretention.csv.CSVWriter.TransactionType;

/**
 * {@link OutboundMailWriteTask} - The write task for outbound mail event.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutboundMailWriteTask extends AbstractWriteTask {

    private static final char RECORD_TYPE_MAIL = 'M';

    /**
     * Initializes a new {@link OutboundMailWriteTask}.
     *
     * @param retentionData The retention data to write as a CSV line
     * @param versionNumber The version number
     * @param sequenceNumber The sequence number
     * @param csvFile The CSV file
     */
    public OutboundMailWriteTask(final RetentionData retentionData, final int versionNumber, final long sequenceNumber, final CSVFile csvFile) {
        super(retentionData, RECORD_TYPE_MAIL, versionNumber, sequenceNumber, csvFile);
    }

    private String generateCSVLine() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(recordType).append(versionNumber).append(';');
        sb.append(sequenceNumber).append(';');
        {
            final Date dateTime = retentionData.getStartTime();
            if (null != dateTime) {
                sb.append(msec2sec(dateTime.getTime()));
            }
            sb.append(';');
        }
        sb.append(TransactionType.OUTBOUND.getChar()).append(';');
        {
            final String ipAddress = retentionData.getIPAddress();
            if (null != ipAddress) {
                sb.append(escape(ipAddress));
            }
            sb.append(';');
        }
        {
            final String senderId = retentionData.getIdentifier();
            if (null != senderId) {
                sb.append(escape(senderId));
            }
            sb.append(';');
        }
        {
            final String senderAddress = retentionData.getSenderAddress();
            if (null != senderAddress) {
                sb.append(escape(senderAddress));
            }
            sb.append(';');
        }
        {
            final String[] recipients = retentionData.getRecipientAddresses();
            if (null != recipients && recipients.length > 0) {
                sb.append(escape(recipients[0]));
                for (int i = 1; i < recipients.length; i++) {
                    sb.append(',').append(escape(recipients[i]));
                }
            }
            sb.append(';');
        }
        sb.append('\n');
        return sb.toString();
    }

    @Override
    protected String getCSVLine() throws OXException {
        return generateCSVLine();
    }

}
