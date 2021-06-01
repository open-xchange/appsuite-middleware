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

package com.openexchange.chronos.schedjoules.api.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.RESTResponseUtil;
import com.openexchange.rest.client.v2.parser.RESTResponseBodyParser;

/**
 * {@link SchedJoulesCalendarRESTResponseBodyParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendarRESTResponseBodyParser implements RESTResponseBodyParser {

    private final Set<String> contentTypes;

    /**
     * Initialises a new {@link SchedJoulesCalendarRESTResponseBodyParser}.
     */
    public SchedJoulesCalendarRESTResponseBodyParser() {
        this.contentTypes = Collections.singleton("text/calendar");
    }

    @Override
    public void parse(HttpResponse httpResponse, RESTResponse restResponse) throws OXException {
        ICalService iCalService = Services.getService(ICalService.class);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);

        try (InputStream inputStream = Streams.bufferedInputStreamFor(httpResponse.getEntity().getContent())) {
            Calendar calendar = iCalService.importICal(inputStream, parameters);
            restResponse.setResponseBody(new SchedJoulesCalendar(calendar.getName(), calendar.getEvents(), restResponse.getHeader(HttpHeaders.ETAG), RESTResponseUtil.getLastModified(restResponse)));
        } catch (IOException e) {
            throw SchedJoulesAPIExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Set<String> getContentTypes() {
        return contentTypes;
    }
}
