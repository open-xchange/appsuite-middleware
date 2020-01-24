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

    @Override
    public void intercept(MailMessage mail) throws MessagingException {
        if (mails.add(mail)) {
            int size = mails.size();
            if (size > 1) {
                Collections.sort(mails, comparator);
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
        int toIndex = indexRange.end;
        if ((fromIndex) > size) {
            // Return empty array if start is out of range
            return new MailMessage[0];
        }

        // Reset end index if out of range
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
