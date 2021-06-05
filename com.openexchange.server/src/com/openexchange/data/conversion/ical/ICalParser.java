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

package com.openexchange.data.conversion.ical;

import java.io.InputStream;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
@SingletonService
public interface ICalParser {

    ParseResult<Task> parseTasks(String icalText, TimeZone defaultTZ, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    ParseResult<Task> parseTasks(InputStream ical, TimeZone defaultTZ, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

	void setLimit(int amount);

}
