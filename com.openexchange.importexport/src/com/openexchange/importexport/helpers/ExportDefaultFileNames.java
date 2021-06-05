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

package com.openexchange.importexport.helpers;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ExportDefaultFileNames}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public final class ExportDefaultFileNames implements LocalizableStrings {

    /**
     * The default file name for a vcard and csv export, when using batch data
     */
    public static final String CONTACTS_NAME = "Contacts";

    /**
     * The default file name for an ical appointment export, when using batch data
     */
    public static final String ICAL_APPOINTMENT_NAME = "Appointments";

    /**
     * The default file name for an ical event export, when using batch data
     */
    public static final String ICAL_EVENT_NAME = "Events";

    /**
     * The default file name for an ical task export, when using batch data
     */
    public static final String ICAL_TASKS_NAME = "Tasks";

    /**
     * The default file name for an ical task export, when using batch data
     */
    public static final String DEFAULT_NAME = "Export";

    /**
     * Initializes a new {@link ExportDefaultFileNames}.
     */
    private ExportDefaultFileNames() {
        super();
    }

}
