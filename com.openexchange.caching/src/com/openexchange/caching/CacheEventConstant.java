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

package com.openexchange.caching;


/**
 * {@link CacheEventConstant}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class CacheEventConstant {

    /**
     * Initializes a new {@link CacheEventConstant}.
     */
    private CacheEventConstant() {
        super();
    }

    /** The property name for cache region (value is of type <code>java.lang.String</code>) */
    public static final String PROP_REGION = "region";

    /** The property name for cache key (value is of type <code>java.io.Serializable</code>) */
    public static final String PROP_KEY = "key";

    /** The property name for cache group (value is of type <code>java.lang.String</code>) */
    public static final String PROP_GROUP = "group";

    /** The property name for cache operation (value is of type <code>java.lang.String</code>) */
    public static final String PROP_OPERATION = "operation";

    /** The property name for exceeded cache element event (value is of type <code>java.lang.Boolean</code>) */
    public static final String PROP_EXCEEDED = "exceeded";

    /** The topic for cache element removal */
    public static final String TOPIC_REMOVE = "com/openexchange/cache/remove";

    /** The topic for cache cleansing */
    public static final String TOPIC_CLEAR = "com/openexchange/cache/clear";

    /**
     * Gets all known cache event topics.
     *
     * @return The topics
     */
    public static String[] getTopics() {
        return new String[] { TOPIC_REMOVE, TOPIC_CLEAR };
    }

}
