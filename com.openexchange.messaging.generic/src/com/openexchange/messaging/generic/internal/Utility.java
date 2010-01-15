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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.generic.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.internet.MailDateFormat;

/**
 * {@link Utility} - Utility class for <i>com.openexchange.messaging.generic</i> bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    private static final ConcurrentMap<String, Future<MailDateFormat>> MDF_MAP = new ConcurrentHashMap<String, Future<MailDateFormat>>();

    private static final MailDateFormat DEFAULT_MAIL_DATE_FORMAT;

    static {
        DEFAULT_MAIL_DATE_FORMAT = new MailDateFormat();
        DEFAULT_MAIL_DATE_FORMAT.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

    /**
     * Gets the default {@link MailDateFormat} instance configured with GMT time zone.
     * <p>
     * Note that returned instance of {@link MailDateFormat} is shared, therefore use a surrounding synchronized block to preserve thread
     * safety:
     * 
     * <pre>
     * ...
     * final MailDateFormat mdf = Utility.getDefaultMailDateFormat();
     * synchronized(mdf) {
     *  mdf.format(date);
     * }
     * ...
     * </pre>
     * 
     * @return The default {@link MailDateFormat} instance configured with GMT time zone
     */
    public static MailDateFormat getDefaultMailDateFormat() {
        return DEFAULT_MAIL_DATE_FORMAT;
    }

    /**
     * Gets the {@link MailDateFormat} for specified time zone identifier.
     * <p>
     * Note that returned instance of {@link MailDateFormat} is shared, therefore use a surrounding synchronized block to preserve thread
     * safety:
     * 
     * <pre>
     * ...
     * final MailDateFormat mdf = Utility.getMailDateFormat(timeZoneId);
     * synchronized(mdf) {
     *  mdf.format(date);
     * }
     * ...
     * </pre>
     * 
     * @param timeZoneId The time zone identifier
     * @return The {@link MailDateFormat} for specified time zone identifier
     */
    public static MailDateFormat getMailDateFormat(final String timeZoneId) {
        Future<MailDateFormat> future = MDF_MAP.get(timeZoneId);
        if (null == future) {
            final FutureTask<MailDateFormat> ft = new FutureTask<MailDateFormat>(new Callable<MailDateFormat>() {

                public MailDateFormat call() throws Exception {
                    final MailDateFormat mdf = new MailDateFormat();
                    mdf.setTimeZone(TimeZoneUtils.getTimeZone(timeZoneId));
                    return mdf;
                }
            });
            future = MDF_MAP.putIfAbsent(timeZoneId, ft);
            if (null == future) {
                future = ft;
                ft.run();
            }
        }
        try {
            return future.get();
        } catch (final InterruptedException e) {
            org.apache.commons.logging.LogFactory.getLog(Utility.class).error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            org.apache.commons.logging.LogFactory.getLog(Utility.class).error(cause.getMessage(), cause);
            return DEFAULT_MAIL_DATE_FORMAT;
        }
    }

}
