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

package com.openexchange.ajax.login;

import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;

/**
 * {@link MetricProvider} provides metric information about the current request which will be published to a monitoring system
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class MetricProvider {

    public static final String STATUS_UNKNOWN = "UNKNOWN";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_HTTP_PREFIX = "HTTP_";

    private String status;

    /**
     * Initializes a new {@link MetricProvider} with a state of {@link MetricProvider#STATUS_UNKNOWN}
     */
    public MetricProvider() {
        this.status = STATUS_UNKNOWN;
    }

    /**
     * Gets the request status, i.e. whether or not the respective request was successful or not
     *
     * @return The current status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Checks whether or not the current state is {@link MetricProvider#STATUS_UNKNOWN}
     *
     * @return True, if the current state is {@link MetricProvider#STATUS_UNKNOWN}, false otherwise
     */
    public boolean isStateUnknown() {
        return status == STATUS_UNKNOWN;
    }

    /**
     * Records a status
     *
     * @param status The status to record, i.e. sets whether or not the respective request was successful or not
     */
    public void record(String status) {
        this.status = status;
    }

    /**
     * Records an unknown request status
     */
    public void recordUnknown() {
        record(STATUS_UNKNOWN);
    }

    /**
     * Records a successful request
     */
    public void recordSuccess() {
        record(STATUS_OK);
    }

    /**
     * Records an OXException, i.e. marks the request as failed with the category of the given OXEception
     *
     * @param exception The OXException to record
     */
    public void recordException(OXException exception) {
        if (exception != null) {
            record(exception.getCategory().toString());
        }
    }

    /**
     * Records an OXExceptionCode, i.e. marks the request as failed with the category of the given error code
     *
     * @param errorCode The error code to record
     */
    public void recordErrorCode(OXExceptionCode errorCode) {
        if (errorCode != null) {
            record(errorCode.getCategory().toString());
        }
    }

    /**
     * Records a HTTP Status code
     *
     * @param statusCode The status code to record
     */
    public void recordHTTPStatus(int statusCode) {
        status = STATUS_HTTP_PREFIX + Integer.toString(statusCode);
    }
}
