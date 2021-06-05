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

package com.openexchange.mail.authenticity.impl.core.metrics;

import java.util.List;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link MailAuthenticityMetricLogger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public interface MailAuthenticityMetricLogger {

    /**
     * Logs the raw <code>Authentication-Results</code> headers and the overall result with the
     * parsed mail authentication mechanisms (known and unknown)
     * 
     * @param mailId the unique mail identifier
     * @param rawHeaders a {@link List} with the raw headers as they appear in the mail message
     * @param overallResult The {@link MailAuthenticityResult} and the parsed mechanisms
     */
    void log(String mailId, List<String> rawHeaders, MailAuthenticityResult overallResult);
}
