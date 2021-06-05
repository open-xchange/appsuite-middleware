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

package com.openexchange.userfeedback.rest.services;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;

/**
 * {@link AbstractUserFeedbackService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public abstract class AbstractUserFeedbackService extends JAXRSService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractUserFeedbackService.class);
    protected static final String DEFAULT_CONFIG_ERROR_MESSAGE = "A configuration error occurred.";

    protected AbstractUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    protected void validateParams(long start, long end) throws OXException {
        Set<String> badParams = new HashSet<>(2);
        if (start < 0L) {
            badParams.add("start");
        }
        if (end < 0L) {
            badParams.add("end");
        }
        if ((end != 0L) && (start != 0L) && (end < start)) {
            badParams.add("start");
            badParams.add("end");
        }
        if (!badParams.isEmpty()) {
            throw FeedbackExceptionCodes.INVALID_PARAMETER_VALUE.create(Strings.concat(",", badParams));
        }
    }

    protected JSONObject generateError(OXException ex) {
        JSONObject main = new JSONObject();
        try {
            ResponseWriter.addException(main, ex);
        } catch (JSONException e) {
            LOG.error("Error while generating error for client.", e);
        }
        return main;
    }
}
