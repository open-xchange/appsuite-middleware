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

package com.openexchange.icap.header;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link OptionsICAPResponseHeader} - Defines the OPTIONS-specific ICAP response headers.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.10.2">RFC-3507, Section 4.10.2</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class OptionsICAPResponseHeader extends ICAPResponseHeader {

    /**
     * The methods that are supported by the service.
     */
    public static final String METHODS = "Methods";

    /**
     * A text description of the vendor and product name.
     */
    public static final String SERVICE = "Service";

    public static final String ENCAPSULATED = "Encapsulated";

    /**
     * A token identifying the format of the opt-body.
     */
    public static final String OPT_BODY_TYPE = "Opt-body-type";

    /**
     * The maximum number of ICAP connections the server is able to support.
     */
    public static final String MAX_CONNECTIONS = "Max-Connections";

    /**
     * The time (in seconds) for which this OPTIONS response is valid.
     * If none is specified, the OPTIONS response does not expire.
     */
    public static final String OPTIONS_TTL = "Options-TTL";

    /**
     * A short label identifying the ICAP service.
     */
    public static final String SERVICE_ID = "Service-ID";

    /**
     * A directive declaring a list of optional ICAP features that this
     * server has implemented. For example, the value "204" to
     * indicate that the ICAP server supports a 204 response.
     */
    public static final String ALLOW = "Allow";

    /**
     * The number of bytes to be sent by the ICAP client during a preview.
     */
    public static final String PREVIEW = "Preview";

    /**
     * A list of file extensions that should be previewed to the ICAP
     * server before sending them in their entirety. Multiple file extensions
     * values should be separated by commas. The wild-card value "*" specifies
     * the default behaviour for all the file extensions not specified in
     * any other Transfer-* header.
     */
    public static final String TRANSFER_PREVIEW = "Transfer-Preview";

    /**
     * A list of file extensions that should NOT be sent to the ICAP
     * server. Multiple file extensions should be separated by commas.
     */
    public static final String TRANSFER_IGNORE = "Transfer-Ignore";

    /**
     * A list of file extensions that should be sent in their entirety
     * (without preview) to the ICAP server. Multiple file extensions
     * values should be separated by commas.
     */
    public static final String TRANSFER_COMPLETE = "Transfer-Complete";

    /**
     * All ICAP response headers.
     */
    public static final Set<String> OPTIONS_RESPONSE_HEADERS;
    static {
        Set<String> set = new HashSet<>(8);
        set.addAll(RESPONSE_HEADERS);
        set.add(METHODS);
        set.add(SERVICE);
        set.add(ENCAPSULATED);
        set.add(OPT_BODY_TYPE);
        set.add(MAX_CONNECTIONS);
        set.add(OPTIONS_TTL);
        set.add(SERVICE_ID);
        set.add(ALLOW);
        set.add(PREVIEW);
        set.add(TRANSFER_PREVIEW);
        set.add(TRANSFER_IGNORE);
        set.add(TRANSFER_COMPLETE);
        OPTIONS_RESPONSE_HEADERS = Collections.unmodifiableSet(set);
    }
}
