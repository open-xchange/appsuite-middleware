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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailFetchListenerResult} - The result when <code>MailFetchListener.onAfterFetch()</code> was invoked.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailFetchListenerResult {

    /**
     * This enum represents the possible replies that a fetch listener can return.
     * <p>
     * Based on the order that the ListenerReply values are declared,
     * ListenerReply.ACCEPT.compareTo(ListenerReply.DENY) will return
     * a positive value.
     */
    public enum ListenerReply {
        /** Further processing is denied due to an error */
        DENY,
        /** Mails successfully processed or not. But in any case go ahead with next in chain */
        NEUTRAL,
        /** Mails successfully processed, but stop further processing */
        ACCEPT;
    }

    // ---------------------------------------------------------------------------------------------------------

    /**
     * Creates a copy from given result
     *
     * @param result The result to copy
     * @return The copy result
     */
    public static MailFetchListenerResult copy(MailFetchListenerResult result) {
        return copy(result, result.cacheable);
    }

    /**
     * Creates a copy from given result with individual flag whether processed mails are allowed to be cached.
     *
     * @param result The result to copy
     * @param cacheable Whether processed mails are allowed to be cached
     * @return The copy result
     */
    public static MailFetchListenerResult copy(MailFetchListenerResult result, boolean cacheable) {
        return new MailFetchListenerResult(result.mails, result.error, cacheable, result.reply);
    }

    /**
     * Creates a deny result for specified error.
     *
     * @param error The error
     * @return The deny result
     */
    public static MailFetchListenerResult deny(OXException error) {
        return new MailFetchListenerResult(null, error, false, ListenerReply.DENY);
    }

    /**
     * Creates an accept result for specified mails.
     *
     * @param mails The processed mails
     * @param cacheable <code>true</code> if processed mails are allowed to be cached; otherwise <code>false</code>
     * @return  The accept result
     */
    public static MailFetchListenerResult accept(MailMessage[] mails, boolean cacheable) {
        return new MailFetchListenerResult(mails, null, cacheable, ListenerReply.ACCEPT);
    }

    /**
     * Creates a neutral result for specified mails.
     *
     * @param mails The (possibly) processed mails
     * @param cacheable <code>true</code> if processed mails are allowed to be cached; otherwise <code>false</code>
     * @return  The neutral result
     */
    public static MailFetchListenerResult neutral(MailMessage[] mails, boolean cacheable) {
        return new MailFetchListenerResult(mails, null, cacheable, ListenerReply.NEUTRAL);
    }

    // ----------------------------------------------------------------------------------------------------------

    private final MailMessage[] mails;
    private final ListenerReply reply;
    private final OXException error;
    private final boolean cacheable;

    /**
     * Initializes a new {@link MailFetchListenerResult}.
     */
    private MailFetchListenerResult(MailMessage[] mails, OXException error, boolean cacheable, ListenerReply reply) {
        super();
        this.mails = mails;
        this.error = error;
        this.cacheable = cacheable;
        this.reply = reply;
    }

    /**
     * Gets the error.
     * <p>
     * Only available in case {@link #getReply()} return {@link ListenerReply#DENY}
     *
     * @return The error or <code>null</code>
     */
    public OXException getError() {
        return error;
    }

    /**
     * Gets the processed mails
     * <p>
     * Only available in case {@link #getReply()} return {@link ListenerReply#ACCEPT}
     *
     * @return The mails or <code>null</code>
     */
    public MailMessage[] getMails() {
        return mails;
    }

    /**
     * Gets the reply.
     *
     * @return The reply
     */
    public ListenerReply getReply() {
        return reply;
    }

    /**
     * Checks whether processed mails are allowed to be cached.
     *
     * @return The cacheable flag
     */
    public boolean isCacheable() {
        return cacheable;
    }

}
