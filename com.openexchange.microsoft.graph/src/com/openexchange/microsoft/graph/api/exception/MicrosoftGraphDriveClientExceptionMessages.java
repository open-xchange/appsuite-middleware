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

package com.openexchange.microsoft.graph.api.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MicrosoftGraphDriveClientExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphDriveClientExceptionMessages implements LocalizableStrings {
    
    // The copying of file %1$s failed. There is nothing we can do about it. Try again later
    public static final String ASYNC_COPY_FAILED = "The copying of file %1$s failed. There is nothing we can do about it. Try again later.";
    // Uploading the file '%1$s' in folder '%2$s' failed. There is nothing we can do about it. Try again later.
    public static final String UPLOAD_FAILED = "Uploading the file '%1$s' in folder '%2$s' failed. There is nothing we can do about it. Try again later.";
}
