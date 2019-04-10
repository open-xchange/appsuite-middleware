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

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.ResourceData;
import com.openexchange.testing.httpclient.models.ResourceListElement;
import com.openexchange.testing.httpclient.models.ResourceSearchBody;
import com.openexchange.testing.httpclient.models.ResourceUpdateResponse;
import com.openexchange.testing.httpclient.models.ResourcesResponse;
import com.openexchange.testing.httpclient.modules.ResourcesApi;

/**
 * {@link ResourceUseCountTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class ResourceUseCountTest extends AbstractChronosTest {

    private ResourcesApi resourceApi;
    private Integer[] resourceIds;
    private Long timestamp;
    /**
     * Initializes a new {@link ResourceUseCountTest}.
     */
    public ResourceUseCountTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resourceApi = new ResourcesApi(getApiClient());
        ResourceData resourceData = new ResourceData();
        resourceData.setDisplayName("Test1");
        resourceData.setName("Test1");
        resourceData.setMailaddress("test1@oxdb.local");
        ResourceUpdateResponse response = resourceApi.createResource(getSessionId(), resourceData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        resourceIds = new Integer[2];
        resourceIds[0] = response.getData().getId();
        resourceData = new ResourceData();
        resourceData.setDisplayName("Test2");
        resourceData.setName("Test2");
        resourceData.setMailaddress("test2@oxdb.local");
        response = resourceApi.createResource(getSessionId(), resourceData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        resourceIds[1] = response.getData().getId();
        timestamp = response.getTimestamp();
    }


    @Override
    public void tearDown() throws Exception {
        if (resourceIds != null) {
            List<ResourceListElement> resourcesToDelete = new ArrayList<>(resourceIds.length);
            for (Integer id : resourceIds) {
                ResourceListElement element = new ResourceListElement();
                element.setId(id);
                resourcesToDelete.add(element);
            }
            resourceApi.deleteResources(getSessionId(), timestamp, resourcesToDelete);
        }
        super.tearDown();
    }

    @Test
    public void testUseCount() throws ApiException {
        ResourceSearchBody body = new ResourceSearchBody();
        body.setPattern("Test");
        ResourcesResponse response = resourceApi.searchResources(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        List<ResourceData> resources = response.getData();
        Assert.assertEquals(2, resources.size());
        int x = 0;
        // Check that resources are returned in the same order
        for (ResourceData resource : resources) {
            Assert.assertEquals(resourceIds[x++], resource.getId());
        }

        // use resource 2
        EventData eventData = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testUseCount", folderId);
        Attendee att = new Attendee();
        att.setEntity(resourceIds[1]);
        att.setCuType(CuTypeEnum.RESOURCE);
        eventData.addAttendeesItem(att);
        eventManager.createEvent(eventData, true);

        // Check order again
        body = new ResourceSearchBody();
        body.setPattern("Test");
        response = resourceApi.searchResources(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        resources = response.getData();
        Assert.assertEquals(2, resources.size());
        x = 1;
        // Check that resources are returned in the inverse order now
        for (ResourceData resource : resources) {
            Assert.assertEquals(resourceIds[x--], resource.getId());
        }

    }

}
