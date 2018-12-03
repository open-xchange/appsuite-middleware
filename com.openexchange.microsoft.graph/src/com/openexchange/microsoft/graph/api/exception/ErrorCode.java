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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
