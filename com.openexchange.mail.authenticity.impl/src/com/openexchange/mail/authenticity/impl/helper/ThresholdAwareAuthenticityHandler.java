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
