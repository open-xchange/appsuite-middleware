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

package com.openexchange.imap.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;
import com.openexchange.imap.command.MailMessageFetchIMAPCommand.MailMessageFetchInterceptor;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.MailMessageComparator;


/**
 * {@link RangeSortingInterceptor} - An interceptor that holds a sorted range.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class RangeSortingInterceptor implements MailMessageFetchInterceptor {

    private static final boolean SORT_ON_INSERT = false;

    private final int total;
    private final List<MailMessage> mails;
    private final MailMessageComparator comparator;
    private final IndexRange indexRange;

    /**
     * Initializes a new {@link RangeSortingInterceptor}.
     */
    public RangeSortingInterceptor(IndexRange indexRange, MailMessageComparator comparator) {
        super();
        this.indexRange = indexRange;
        this.comparator = comparator;
        total = indexRange.getEnd();
        mails = new ArrayList<>(total + 1);
    }

    /**
     * Gets the comparator.
     *
     * @return The comparator
     */
    public MailMessageComparator getComparator() {
        return comparator;
    }

    /**
     * Gets the index range.
     *
     * @return The index range
     */
    public IndexRange getIndexRange() {
        return indexRange;
    }

    @Override
    public void intercept(MailMessage mail) throws MessagingException {
        if (mail != null && mails.add(mail)) {
            // Appended mail to the end of the list
            int size = mails.size();
            if (size > 1) {
                if (SORT_ON_INSERT) {
                    int i = size - 1;
                    while (i > 0 && comparator.compare(mails.get(i - 1), mails.get(i)) > 0) {
                        int index1 = i - 1;
                        mails.set(index1, mails.set(i, mails.get(index1)));
                        i--;
                    }
                } else {
                    Collections.sort(mails, comparator);
                }
            }
            if (size > total) {
                mails.remove(total);
            }
        }
    }

    @Override
    public MailMessage[] getMails() {
        int size = mails.size();
        if (size <= 0) {
            return new MailMessage[0];
        }

        int fromIndex = indexRange.start;
        if ((fromIndex) > size) {
            // Return empty array if start is out of range
            return new MailMessage[0];
        }

        // Reset end index if out of range
        int toIndex = indexRange.end;
        if (toIndex > size) {
            toIndex = size;
        }
        MailMessage[] arr = new MailMessage[toIndex - fromIndex];
        for (int k = fromIndex, i = 0; k < toIndex; k++, i++) {
            arr[i] = mails.get(k);
        }
        return arr;
    }

}
