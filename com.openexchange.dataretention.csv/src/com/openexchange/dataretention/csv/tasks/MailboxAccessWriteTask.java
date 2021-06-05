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

package com.openexchange.dataretention.csv.tasks;

import java.util.Date;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.dataretention.csv.CSVFile;
import com.openexchange.dataretention.csv.CSVWriter.TransactionType;
import com.openexchange.exception.OXException;

/**
 * {@link MailboxAccessWriteTask} - The write task for mailbox access event.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailboxAccessWriteTask extends AbstractWriteTask {

    private static final char RECORD_TYPE_MAIL = 'M';

    /**
     * Initializes a new {@link MailboxAccessWriteTask}.
     *
     * @param retentionData The retention data to write as a CSV line
     * @param versionNumber The version number
     * @param sequenceNumber The sequence number
     * @param csvFile The CSV file
     */
    public MailboxAccessWriteTask(final RetentionData retentionData, final int versionNumber, final long sequenceNumber, final CSVFile csvFile) {
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
        sb.append(TransactionType.ACCESS.getChar()).append(';');
        {
            final String ipAddress = retentionData.getIPAddress();
            if (null != ipAddress) {
                sb.append(escape(ipAddress));
            }
            sb.append(';');
        }
        {
            final String mailboxId = retentionData.getIdentifier();
            if (null != mailboxId) {
                sb.append(escape(mailboxId));
            }
            sb.append(';');
        }
        {
            final String login = retentionData.getLogin();
            if (null != login) {
                sb.append(escape(login));
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
