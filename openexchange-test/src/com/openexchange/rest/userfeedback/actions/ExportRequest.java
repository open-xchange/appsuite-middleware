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

package com.openexchange.rest.userfeedback.actions;

import static com.openexchange.java.Autoboxing.L;
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

    private final String contextGroup;
    private final String type;
    private final long start;
    private final long end;

    private final String exportPath = "/userfeedback/v1/export";

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
                target = target.queryParam("start", L(start));
            }
            if (end >= 0) {
                target = target.queryParam("end", L(end));
            }
            return target;
        } catch (URISyntaxException e) {
            System.err.print("Unable to return endpoint: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
