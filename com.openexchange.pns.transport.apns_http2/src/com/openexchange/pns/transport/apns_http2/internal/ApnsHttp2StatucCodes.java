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

package com.openexchange.pns.transport.apns_http2.internal;

/**
 * {@link ApnsHttp2StatucCodes} - Status codes for an APNs response taken from <a href="https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingwithAPNs.html#//apple_ref/doc/uid/TP40008194-CH11-SW15">here</a>.
 * <p>
 * <table>
 * <tr>
 * <th>Status code</th><th>Description</th>
 * </tr>
 * <tr>
 * <td>200</td><td>Success</td>
 * </tr>
 * <tr>
 * <td>400</td><td>Bad request</td>
 * </tr>
 * <tr>
 * <td>403</td><td>There was an error with the certificate or with the provider authentication token</td>
 * </tr>
 * <tr>
 * <td>405</td><td>The request used a bad <code>method</code> value. Only <code>POST</code> requests are supported.</td>
 * </tr>
 * <tr>
 * <td>410</td><td>The device token is no longer active for the topic.</td>
 * </tr>
 * <tr>
 * <td>413</td><td>The notification payload was too large.</td>
 * </tr>
 * <tr>
 * <td>429</td><td>The server received too many requests for the same device token.</td>
 * </tr>
 * <tr>
 * <td>500</td><td>Internal server error</td>
 * </tr>
 * <tr>
 * <td>503</td><td>The server is shutting down and unavailable.</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public enum ApnsHttp2StatucCodes {

    /** 200 - Success */
    SUCCESS(200, "Success"),
    /** 400 - Bad request */
    BAD_REQUEST(400, "Bad request"),
    /** 403 - There was an error with the certificate or with the provider authentication token */
    CERTIFICATE_OR_TOKEN_ERROR(403, "There was an error with the certificate or with the provider authentication token"),
    /** 405 - The request used a bad <code>method</code> value. Only <code>POST</code> requests are supported. */
    BAD_METHOD(405, "The request used a bad method value. Only POST requests are supported."),
    /** 410 - The device token is no longer active for the topic. */
    TOKEN_INACTIVE(410, "The device token is no longer active for the topic."),
    /** 413 - The notification payload was too large. */
    PAYLOAD_TOO_LARGE(413, "The notification payload was too large."),
    /** 429 - The server received too many requests for the same device token. */
    TOO_MANY_REQUESTS(429, "The server received too many requests for the same device token."),
    /** 500 - Internal server error */
    INTERNAL_ERROR(500, "Internal server error"),
    /** 503 -The server is shutting down and unavailable */
    SHUTTING_DOWN(503, "The server is shutting down and unavailable"),
    ;

    private final int statusCode;
    private final String description;

    private ApnsHttp2StatucCodes(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    /**
     * Gets the (HTTP) status code
     *
     * @return The status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
}
