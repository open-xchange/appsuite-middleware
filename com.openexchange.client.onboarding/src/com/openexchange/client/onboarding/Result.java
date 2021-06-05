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

package com.openexchange.client.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Result} - A result when an on-boarding configuration has been successfully executed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Result {

    /** The special DENY result, which will lead to a {@link OnboardingExceptionCodes#EXECUTION_DENIED denied execution} error. */
    public static Result DENY = new Result() {

        @Override
        public ResultReply getReply() {
            return ResultReply.DENY;
        }

        @Override
        public ResultObject getResultObject(OnboardingRequest request, Session session) {
            return null;
        }
    };

    // ------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets this result's reply
     *
     * @return The reply
     */
    ResultReply getReply();

    /**
     * Gets the result object with respect to specified action.
     *
     * @param request The on-boarding request
     * @param session The session providing user data
     * @throws OXException If result cannot be returned
     */
    ResultObject getResultObject(OnboardingRequest request, Session session) throws OXException;

}
