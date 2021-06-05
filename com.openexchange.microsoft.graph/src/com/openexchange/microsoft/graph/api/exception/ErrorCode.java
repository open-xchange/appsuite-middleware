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

/**
 * {@link ErrorCode}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/errors#code-property">Basic Error Codes</a>
 */
public enum ErrorCode {
    /**
     * The caller doesn't have permission to perform the action.
     */
    accessDenied,
    /**
     * The app or user has been throttled.
     */
    activityLimitReached,
    /**
     * An unspecified error has occurred.
     */
    generalException,
    /**
     * The specified byte range is invalid or unavailable.
     */
    invalidRange,
    /**
     * The request is malformed or incorrect.
     */
    invalidRequest,
    /**
     * The resource could not be found.
     */
    itemNotFound,
    /**
     * Malware was detected in the requested resource.
     */
    malwareDetected,
    /**
     * The specified item name already exists.
     */
    nameAlreadyExists,
    /**
     * The action is not allowed by the system.
     */
    notAllowed,
    /**
     * The request is not supported by the system.
     */
    notSupported,
    /**
     * The resource being updated has changed since the caller last read it, usually an eTag mismatch.
     */
    resourceModified,
    /**
     * The delta token is no longer valid, and the app must reset the sync state
     */
    resyncRequired,
    /**
     * The service is not available. Try the request again after a delay. There may be a Retry-After header.
     */
    serviceNotAvailable,
    /**
     * The user has reached their quota limit.
     */
    quotaLimitReached,
    /**
     * The caller is not authenticated.
     */
    unauthenticated;
}
