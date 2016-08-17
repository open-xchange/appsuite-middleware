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

package com.openexchange.file.storage.boxcom.access.extended;

import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.IBoxConfig;
import com.box.boxjavalibv2.jsonparsing.IBoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.IBoxResourceHub;
import com.box.boxjavalibv2.resourcemanagers.BoxFilesManagerImpl;
import com.openexchange.file.storage.boxcom.access.NonRefreshingBoxClient;

/**
 * {@link ExtendedNonRefreshingBoxClient References the {@link BoxExtendedFilesManager} instead of the {@link BoxFilesManagerImpl} which includes the missing deleteFileVersion method.
 * The next <a link="https://github.com/box/box-java-sdk">major</a> release of the Box.com Java SDK will
 * include that functionality, though it is still in beta right now. So, we have to live with this ugly hack.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExtendedNonRefreshingBoxClient extends NonRefreshingBoxClient {

    private final BoxExtendedFilesManager filesManager;

    /**
     * Initializes a new {@link NonRefreshingBoxClient}.
     *
     * @param clientId The client id
     * @param clientSecret The client secret
     * @param hub The resource hub, use <code>null</code> for default resource hub
     * @param parser The JSON parser, use <code>null</code> for default parser
     * @param config The Box configuration. Use {@link BoxConfigBuilder} to build. Normally you only need default configuration: <code>(new BoxConfigBuilder()).build()</code>
     */
    public ExtendedNonRefreshingBoxClient(String clientId, String clientSecret, IBoxResourceHub hub, IBoxJSONParser parser, IBoxConfig config) {
        super(clientId, clientSecret, hub, parser, config);
        filesManager = new BoxExtendedFilesManager(getConfig(), getResourceHub(), getJSONParser(), getAuth(), getRestClient());
    }

    @Override
    public BoxExtendedFilesManager getFilesManager() {
        return filesManager;
    }

}
