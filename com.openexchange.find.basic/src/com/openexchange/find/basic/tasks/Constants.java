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

package com.openexchange.find.basic.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.6.0
 */
public class Constants {

    public final static String FIELD_TITLE = "title";

    public final static String FIELD_DESCRIPTION = "description";

    public final static String FIELD_STATUS = "status";

    public final static String FIELD_LOCATION ="location";

    public final static String FIELD_ATTACHMENT_NAME = "attachment_name";

    public final static String FIELD_TYPE = "type";

    public final static String FIELD_PARTICIPANT = "participant";

    public final static List<String> PARTICIPANTS = Collections.singletonList(FIELD_PARTICIPANT);

    public static final Set<String> QUERY_FIELDS = ImmutableSet.of(FIELD_TITLE, FIELD_DESCRIPTION, FIELD_ATTACHMENT_NAME);

}
