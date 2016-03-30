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

package com.openexchange.mail.cache;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link PooledMailAccess} - A simple wrapper for a {@link MailAccess} instance providing {@link Delayed} methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PooledMailAccess implements Delayed {

    /**
     * Gets the pooled value for specified mailAccess carrying given time-to-live milliseconds.
     *
     * @param mailAccess The mail access
     * @param ttlMillis The time-to-live milliseconds
     * @return The pooled value
     */
    public static PooledMailAccess valueFor(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final long ttlMillis) {
        return new PooledMailAccess(mailAccess, ttlMillis);
    }

    private final long timeoutStamp;

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * Initializes a new {@link PooledMailAccess}.
     *
     * @param mailAccess The mail access
     * @param ttlMillis The time-to-live milliseconds
     */
    private PooledMailAccess(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final long ttlMillis) {
        super();
        timeoutStamp = System.currentTimeMillis() + ttlMillis;
        this.mailAccess = mailAccess;
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return TimeUnit.MILLISECONDS.equals(unit) ? (timeoutStamp - System.currentTimeMillis()) : unit.convert(
            timeoutStamp - System.currentTimeMillis(),
            TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = timeoutStamp;
        final long otherStamp = ((PooledMailAccess) o).timeoutStamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    /**
     * Gets the mail access.
     *
     * @return The mail access
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() {
        // stamp = System.currentTimeMillis();
        return mailAccess;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("PooledMailAccess [timeoutStamp=").append(timeoutStamp).append(", ");
        if (mailAccess != null) {
            builder.append("mailAccess=").append(mailAccess);
        }
        builder.append(']');
        return builder.toString();
    }

}
