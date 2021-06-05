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

package com.openexchange.test.fixtures.transformators;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

public class StatusTransformator implements Transformator {

    @Override
    public Object transform(final String value) throws OXException {
        if ("NOT_STARTED".equalsIgnoreCase(value)) {
            return I(Task.NOT_STARTED);
        } else if ("IN_PROGRESS".equalsIgnoreCase(value) || "IN PROGRESS".equalsIgnoreCase(value)) {
            return I(Task.IN_PROGRESS);
        } else if ("DONE".equalsIgnoreCase(value)) {
            return I(Task.DONE);
        } else if ("WAITING".equalsIgnoreCase(value)) {
            return I(Task.WAITING);
        } else if ("DEFERRED".equalsIgnoreCase(value)) {
            return I(Task.DEFERRED);
        } else {
            throw OXException.general("Unknown Status: " + value);
        }

    }
}
