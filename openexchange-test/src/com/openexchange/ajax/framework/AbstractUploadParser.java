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

package com.openexchange.ajax.framework;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;

/**
 * This parser extracts the JSON object from the web site returned by the server
 * if an upload has been made.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractUploadParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<T> {

    public static final Pattern CALLBACK_ARG_PATTERN = Pattern.compile("\\((\\{.*?\\})\\)");

    /**
     * @param failOnError
     */
    public AbstractUploadParser(boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response getResponse(String body) throws JSONException {
        return super.getResponse(extractFromCallback(body));
    }

    protected Response getSuperResponse(String body) throws JSONException {
        return super.getResponse(body);
    }

    public static String extractFromCallback(final String body) {
        final Matcher matcher = CALLBACK_ARG_PATTERN.matcher(body);
        final String retval;
        if (matcher.find()) {
            // Parse proper response to upload request.
            retval = matcher.group(1);
        } else {
            // Try to parse normal response if something before working on upload fails.
            // For example the session.
            retval = body;
        }
        return retval;
    }
}
