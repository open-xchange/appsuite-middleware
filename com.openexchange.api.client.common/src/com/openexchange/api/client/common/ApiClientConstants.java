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

package com.openexchange.api.client.common;

import org.apache.http.entity.ContentType;

/**
 * {@link ApiClientConstants}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiClientConstants {

    /** Static string for {@value #ACTION} */
    public final static String ACTION = "action";

    /** Static string for {@value #ANONYMOUS} */
    public final static String ANONYMOUS = "anonymous";

    /** Static string for {@value #CLIENT} */
    public final static String CLIENT = "client";

    /** Static string for the client value, {@value #CLIENT_VALUE} */
    public final static String CLIENT_VALUE = "open-xchange-appsuite-http";

    /** Static string for {@value #GUEST} */
    public final static String GUEST = "guest";

    /** Static string for {@value #LOGIN} */
    public final static String LOGIN = "login";

    /** Static string for {@value #NAME} */
    public final static String NAME = "name";

    /** Static string for {@value #PASSWORD} */
    public final static String PASSWORD = "password";

    /** Static string for {@value #RAMP_UP} */
    public final static String RAMP_UP = "rampup";

    /** Static string for {@value #SESSION} */
    public final static String SESSION = "session";

    /** Static string for {@value #SHARE} */
    public final static String SHARE = "share";

    /** Static string for {@value #STAY_SIGNED_IN} */
    public final static String STAY_SIGNED_IN = "staySignedIn";

    /** Static string for {@value #TARGET} */
    public final static String TARGET = "target";

    /** The content type for "text/javascript" */
    public static final ContentType TEXT_JAVA_SCRIPT = ContentType.create("text/javascript");

    /** Error message when a JSON array was expected by the parser but not received by the client */
    public static final String NOT_JSON_ARRAY_MSG = "Not an JSON array";

}
