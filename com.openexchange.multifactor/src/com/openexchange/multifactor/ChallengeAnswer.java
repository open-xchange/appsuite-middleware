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

package com.openexchange.multifactor;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link ChallengeAnswer} is the answer to a {@link Challenge}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class ChallengeAnswer {

    private final Map<String, Object> answer;

    /**
     * Initializes a new {@link ChallengeAnswer}.
     *
     * @param asMap
     */
    public ChallengeAnswer(Map<String, Object> data) {
        this.answer = data;

    }

    /**
     * Checks if the value associated with the given {@link AnswerField} is not null
     *
     * @param field The {@link AnswerField}
     * @return true if the value is not null, false otherwise
     */
    private boolean hasField(AnswerField field) {
        return answer.get(field.getKey()) != null;
    }

    /**
     * Gets the value associated with the given {@link AnswerField}
     *
     * @param field The {@link AnswerField}
     * @return The value
     * @throws OXException If the {@link AnswerField} is unknown or if the associated value is null
     */
    public Object requireField(AnswerField field) throws OXException {
        if (!hasField(field)) {
            throw MultifactorExceptionCodes.MISSING_PARAMETER.create(field.getKey());
        }
        return answer.get(field.getKey());
    }

}
