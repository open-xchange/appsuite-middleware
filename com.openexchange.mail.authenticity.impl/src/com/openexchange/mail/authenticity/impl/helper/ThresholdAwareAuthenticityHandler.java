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

package com.openexchange.mail.authenticity.impl.helper;

import java.util.Collection;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link ThresholdAwareAuthenticityHandler} - A simple authenticity handler, which only delegates if date threshold is satisfied;
 * that is message's received date is not <code>null</code> and equal to/greater than date threshold.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ThresholdAwareAuthenticityHandler implements MailAuthenticityHandler {

    /**
     * Wraps the specified authenticity handler by a threshold-aware one in case a valid date threshold is given;
     * otherwise the handler is returned as-is. If wrapped, then calls are only delegated to the authenticity handler
     * if date threshold is satisfied<b>*</b>.
     * <p>
     * <i>*) Message's received date is not <code>null</code> and equal to/greater than date threshold</i>
     *
     * @param authenticityHandler The authenticity handler to delegate to (if date threshold is satisfied)
     * @param threshold The date threshold to consider; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @return
     */
    public static MailAuthenticityHandler wrapIfApplicable(MailAuthenticityHandler authenticityHandler, long threshold) {
        return null == authenticityHandler ? null : (threshold <= 0 ? authenticityHandler : new ThresholdAwareAuthenticityHandler(authenticityHandler, threshold));
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private final MailAuthenticityHandler authenticityHandler;
    private final long threshold;

    /**
     * Initializes a new {@link ThresholdAwareAuthenticityHandler}.
     *
     * @throws IllegalArgumentException If specified threshold is less than or equal to <code>0</code> (zero)
     */
    private ThresholdAwareAuthenticityHandler(MailAuthenticityHandler authenticityHandler, long threshold) {
        super();
        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold must not be less than or equal to 0 (zero)");
        }
        this.authenticityHandler = authenticityHandler;
        this.threshold = threshold;
    }

    @Override
    public void handle(Session session, MailMessage mailMessage) {
        if (null != mailMessage) {
            if (shouldHandle(mailMessage)) {
                authenticityHandler.handle(session, mailMessage);
            } else {
                mailMessage.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
            }
        }
    }

    @Override
    public Collection<MailField> getRequiredFields() {
        return authenticityHandler.getRequiredFields();
    }

    @Override
    public Collection<String> getRequiredHeaders() {
        return authenticityHandler.getRequiredHeaders();
    }

    @Override
    public boolean isEnabled(Session session) {
        return authenticityHandler.isEnabled(session);
    }

    @Override
    public int getRanking() {
        return authenticityHandler.getRanking();
    }

    /**
     * <p>Determines whether the specified message should be handled by the {@link MailAuthenticityHandler}.</p>
     *
     * <p>Certain criteria are checked to determine that:
     * <ul>
     * <li>If the threshold is set, and if set whether the received date falls before or after that threshold (i.e. the cut-off date)</li>
     * <li>Whether the e-mail lies in Drafts, or Sent folders. If it does then it should not be handled.</li>
     * </ul>
     * </p>
     *
     * @param mailMessage the mail message
     * @return <code>true</code> if the message should be handled; <code>false</code> otherwise
     */
    private boolean shouldHandle(MailMessage mailMessage) {
        return (mailMessage.getReceivedDate() == null || mailMessage.getReceivedDate().getTime() >= threshold);
    }

}
