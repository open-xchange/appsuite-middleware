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

package com.google.android.gcm;


/**
 * {@link Endpoint} - The enumeration for end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Endpoint {

    /**
     * The API end-point for <a href="https://firebase.google.com/docs/cloud-messaging/http-server-ref">FCM</a>; <code>"https://fcm.googleapis.com/fcm/send"</code>
     */
    FCM("FCM", Constants.FCM_SEND_ENDPOINT),
    ;

    private final String name;
    private final String endpoint;

    private Endpoint(String name, String endpoint) {
        this.name= name;
        this.endpoint = endpoint;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the API end-point
     *
     * @return The API end-point
     */
    public String getEndpoint() {
        return endpoint;
    }

}
