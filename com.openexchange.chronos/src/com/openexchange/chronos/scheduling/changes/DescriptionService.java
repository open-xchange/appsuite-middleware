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

package com.openexchange.chronos.scheduling.changes;

import java.util.List;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.EventUpdate;

/**
 * {@link DescriptionService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface DescriptionService {

    /**
     * Describes an event update.
     *
     * @param eventUpdate The event update to describe
     * @param ignorees The fields to ignore
     * @return A list of descriptions
     */
    List<Description> describe(EventUpdate eventUpdate, EventField... ignorees);

    /**
     * Describes an event update, only considering given fields.
     *
     * @param eventUpdate The event update to describe
     * @param toDescribe The fields to describe only
     * @return A list of descriptions
     */
    List<Description> describeOnly(EventUpdate eventUpdate, EventField... toDescribe);

}
