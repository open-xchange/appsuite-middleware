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

package com.openexchange.resource;


/**
 * {@link ResourceEventConstants} - Constants for resource events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceEventConstants {

    /**
     * Initializes a new {@link ResourceEventConstants}.
     */
    private ResourceEventConstants() {
        super();
    }

    /**
     * The topic for update.
     */
    public static final String TOPIC_UPDATE = "com/openexchange/groupware/resource/update";

    /**
     * The topic for create.
     */
    public static final String TOPIC_CREATE = "com/openexchange/groupware/resource/insert";

    /**
     * The topic for delete.
     */
    public static final String TOPIC_DELETE = "com/openexchange/groupware/resource/delete";

    /**
     * All topics.
     */
    public static final String ALL_TOPICS = "com/openexchange/groupware/resource/*";

    // ----------------------------------------------------------------------------------------------------- //

    /**
     * The user identifier.
     */
    public static final String PROPERTY_USER_ID = "userId";

    /**
     * The context identifier.
     */
    public static final String PROPERTY_CONTEXT_ID = "contextId";

    /**
     * The resource identifier.
     */
    public static final String PROPERTY_RESOURCE_ID = "resourceId";

}
