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

package com.openexchange.rest.client.httpclient;

import java.util.Optional;
import com.openexchange.version.VersionService;

/**
 * {@link DefaultHttpClientConfigProvider} - Provider with the most basic and common used functions
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DefaultHttpClientConfigProvider extends AbstractHttpClientModifer implements SpecificHttpClientConfigProvider {

    private final String clientId;

    /**
     * Initializes a new {@link DefaultHttpClientConfigProvider}.
     *
     * @param clientId The identifier of the HTTP client
     * @param userAgent The user agent to use for the client
     */
    public DefaultHttpClientConfigProvider(String clientId, String userAgent) {
        super(userAgent);
        this.clientId = clientId;
    }

    /**
     * Initializes a new {@link DefaultHttpClientConfigProvider}.
     *
     * @param clientId The identifier of the HTTP client
     * @param userAgent The user agent to use for the client
     * @param optionalVersionService The optional version service. Will be used to append the version to the user agent
     */
    public DefaultHttpClientConfigProvider(String clientId, String userAgent, Optional<VersionService> optionalVersionService) {
        super(userAgent + getVersion(optionalVersionService));
        this.clientId = clientId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    private static String getVersion(Optional<VersionService> optionalVersionService) {
        return optionalVersionService.isPresent() ? optionalVersionService.get().getVersionString() : "<unknown version>";
    }

}
