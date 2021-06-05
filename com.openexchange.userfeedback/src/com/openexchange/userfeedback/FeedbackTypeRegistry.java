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

package com.openexchange.userfeedback;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link FeedbackTypeRegistry} is a registry for {@link FeedbackType} implementations
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
@SingletonService
public interface FeedbackTypeRegistry {

    /**
     * Register a {@link FeedbackType}
     * @param type The {@link FeedbackType} to register
     */
    public void registerType(FeedbackType type);

    /**
     * Unregister a {@link FeedbackType}
     * @param type The {@link FeedbackType} to unregister
     */
    public void unregisterType(FeedbackType type);

    /**
     * Retrieves the {@link FeedbackType} for the given type
     * @param type The feedback type
     * @return The {@link FeedbackType} or null
     */
    public FeedbackType getFeedbackType(String type);

}
