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

package com.openexchange.groupware.container;

import java.util.Arrays;


/**
 * {@link ChangeExceptionsDiffer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ChangeExceptionsDiffer extends Differ<CalendarObject> {

    @SuppressWarnings("unchecked")
    @Override
    public Difference getDifference(CalendarObject original, CalendarObject update) {
        if (!update.containsChangeExceptions()) {
            return null;
        }

        if (!original.containsChangeExceptions() && update.containsChangeExceptions()) {
            if (update.getChangeException() == null) {
                return null;
            }
            Difference difference = new Difference(CalendarObject.CHANGE_EXCEPTIONS);
            difference.getAdded().addAll(Arrays.asList(update.getChangeException()));
            return difference;
        }

        if (original.getChangeException() == update.getChangeException()) {
            return null;
        }

        if (original.getChangeException() == null) {
            Difference difference = new Difference(CalendarObject.CHANGE_EXCEPTIONS);
            difference.getAdded().addAll(Arrays.asList(update.getChangeException()));
            return difference;
        }

        if (update.getChangeException() == null) {
            Difference difference = new Difference(CalendarObject.CHANGE_EXCEPTIONS);
            difference.getRemoved().addAll(Arrays.asList(original.getChangeException()));
            return difference;
        }

        Difference retval = isArrayDifferent(original.getChangeException(), update.getChangeException());
        if (retval != null) {
            retval.setField(CalendarObject.CHANGE_EXCEPTIONS);
        }
        return retval;
    }

    @Override
    public int getColumn() {
        return CalendarObject.CHANGE_EXCEPTIONS;
    }

}
