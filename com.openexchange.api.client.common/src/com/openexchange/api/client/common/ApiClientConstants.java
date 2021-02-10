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
