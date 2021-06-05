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

package com.openexchange.userfeedback.internal;

import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.FeedbackTypeRegistry;

/**
 * {@link FeedbackTypeRegistryImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class FeedbackTypeRegistryImpl implements FeedbackTypeRegistry {

    private static final FeedbackTypeRegistryImpl INSTANCE = new FeedbackTypeRegistryImpl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static FeedbackTypeRegistryImpl getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------

    private final ConcurrentHashMap<String, FeedbackType> map = new ConcurrentHashMap<String, FeedbackType>(1);

    /**
     * Initializes a new {@link FeedbackTypeRegistryImpl}.
     */
    protected FeedbackTypeRegistryImpl() {
        super();
    }

    @Override
    public void registerType(FeedbackType type) {
        map.put(type.getType(), type);
    }

    @Override
    public void unregisterType(FeedbackType type) {
        map.remove(type.getType());
    }

    @Override
    public FeedbackType getFeedbackType(String type) {
        return map.get(type);
    }

}
