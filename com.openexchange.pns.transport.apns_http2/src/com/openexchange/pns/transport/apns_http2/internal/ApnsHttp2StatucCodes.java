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
