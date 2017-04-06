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

package com.openexchange.rest.userfeedback.actions;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;

/**
 * {@link ExportRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ExportRequest extends AbstractRestRequest {

    private String contextGroup;
    private String type;
    private long start;
    private long end;

    private String exportPath = "/userfeedback/v1/export";

    public ExportRequest(String pContextGroup, String pType) {
        this(pContextGroup, pType, 0, 0);
    }

    public ExportRequest(String pContextGroup, String pType, long pStart, long pEnd) {
        super();
        contextGroup = pContextGroup;
        type = pType;
        start = pStart;
        end = pEnd;
    }

    @Override
    public boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    public WebTarget getEndpoint(String host) {
        try {
            URI uri = new URI(host + exportPath);

            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            WebTarget baseTarget = client.target(uri);

            WebTarget target = baseTarget.path(contextGroup).path(type).path("raw");
            target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
            target.request(MediaType.APPLICATION_JSON_TYPE);

            if (start >= 0) {
                target = target.queryParam("start", start);
            }
            if (end >= 0) {
                target = target.queryParam("end", end);
            }
            return target;
        } catch (URISyntaxException e) {
            System.err.print("Unable to return endpoint: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
