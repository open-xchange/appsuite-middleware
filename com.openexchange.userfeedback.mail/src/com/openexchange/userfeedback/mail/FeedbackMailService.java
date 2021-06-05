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

package com.openexchange.userfeedback.mail;

import com.openexchange.exception.OXException;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;

/**
 * 
 * {@link FeedbackMailService}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.8.4
 */
public interface FeedbackMailService {

    /**
     * Send a user feedback file to a set of recipients. All needed information is 
     * inside the {@link FeedbackMailFilter} property.
     * 
     * @param filter with all export filter information and recipients
     * @return, a result String if at least one mail is sent out
     * @throws OXException, if anything during gathering export data or sending went wrong
     */
    public String sendFeedbackMail(FeedbackMailFilter filter) throws OXException;
}
