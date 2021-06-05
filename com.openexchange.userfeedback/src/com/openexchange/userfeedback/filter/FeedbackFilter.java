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

package com.openexchange.userfeedback.filter;

import com.openexchange.userfeedback.Feedback;
import com.openexchange.userfeedback.FeedbackMetaData;

/**
 * {@link FeedbackFilter} - Implementation of this filter will be used to reduce {@link Feedback}s based on the given criteria.
 * Start and end date will always be used to filter. Of course you will be able to use zero (0L) as a wildcard.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface FeedbackFilter {

    public static FeedbackFilter DEFAULT_FILTER = new FeedbackFilter() {

        @Override
        public boolean accept(FeedbackMetaData feedback) {
            return true;
        }

        @Override
        public String getType() {
            return "star-rating-v1";
        }

        @Override
        public long start() {
            return 0;
        }

        @Override
        public long end() {
            return 0;
        }
    };

    public boolean accept(FeedbackMetaData feedback);

    /**
     * The feedback type to query
     *
     * @return The feedback type
     */
    public String getType();

    public long start();

    public long end();

}
