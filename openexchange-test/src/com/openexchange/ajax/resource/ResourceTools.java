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

package com.openexchange.ajax.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.resource.actions.ResourceAllRequest;
import com.openexchange.ajax.resource.actions.ResourceAllResponse;
import com.openexchange.ajax.resource.actions.ResourceListRequest;
import com.openexchange.ajax.resource.actions.ResourceListResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceTools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ResourceTools {

    private ResourceTools() {
        super();
    }

    private static int[] getAllResourceIds(AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        ResourceAllRequest request = new ResourceAllRequest();
        ResourceAllResponse response = client.execute(request);
        return response.getIDs();
    }

    private static List<Resource> getResources(AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        ResourceListRequest listReq = new ResourceListRequest(getAllResourceIds(client));
        ResourceListResponse listResp = client.execute(listReq);
        List<Resource> resources = new ArrayList<Resource>();
        for (Resource resource : listResp.getResources()) {
            resources.add(resource);
        }
        return resources;
    }

    public static int getSomeResource(AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        int[] resourceIds = getAllResourceIds(client);
        return resourceIds[rand.nextInt(resourceIds.length)];
    }

    public static List<Resource> getResources(AJAXClient client, int count) throws OXException, IOException, SAXException, JSONException {
        List<Resource> resource = getResources(client);
        resource = extractByRandom(resource, count);
        return resource;
    }

    private static final Random rand = new Random(System.currentTimeMillis());

    public static List<Resource> extractByRandom(List<Resource> resources, int count) {
        final List<Resource> retval = new ArrayList<Resource>();
        do {
            Resource resource = resources.get(rand.nextInt(resources.size()));
            if (!retval.contains(resource)) {
                retval.add(resource);
            }
        } while (retval.size() < count && retval.size() < resources.size());
        return retval;
    }
}
