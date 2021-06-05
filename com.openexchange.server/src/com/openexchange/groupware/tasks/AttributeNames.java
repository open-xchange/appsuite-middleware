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

package com.openexchange.groupware.tasks;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AttributeNames}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AttributeNames implements LocalizableStrings {

    private AttributeNames() {
        super();
    }

    // Task attribute name
    public static final String ACTUAL_COSTS = "Current costs";

    // Task attribute name
    public static final String ACTUAL_DURATION = "Current duration";

    // Task attribute name
    public static final String BILLING_INFORMATION = "Billing information";

    // Task attribute name
    public static final String CATEGORIES = "Categories";

    // Task attribute name
    public static final String COMPANIES = "Companies";

    // Task attribute name
    public static final String CURRENCY = "Currency";

    // Task attribute name
    public static final String DESCRIPTION = "Description";

    // Task attribute name
    public static final String FILENAME = "File name";

    // Task attribute name
    public static final String TARGET_COSTS = "Estimated costs";

    // Task attribute name
    public static final String TARGET_DURATION = "Estimated duration";

    // Task attribute name
    public static final String TITLE = "Title";

    // Task attribute name
    public static final String TRIP_METER = "Trip meter";

    // Task attribute name
    public static final String UID = "UID";
}
