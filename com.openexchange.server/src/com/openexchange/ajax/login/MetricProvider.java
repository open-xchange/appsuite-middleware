/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
