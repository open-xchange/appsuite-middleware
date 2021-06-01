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

package com.openexchange.ajax.resource;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.test.common.test.pool.ProvisioningService;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.ResourceData;
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
    private String uuid;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.uuid = UUID.randomUUID().toString();
        resourceApi = new ResourcesApi(getApiClient());
        ResourceData resourceData = new ResourceData();
        resourceData.setDisplayName(uuid + "-1");
        resourceData.setName("Test1");
        resourceData.setMailaddress(ProvisioningService.getMailAddress("test1", testContext.getId()));
        ResourceUpdateResponse response = resourceApi.createResource(resourceData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        resourceIds = new Integer[2];
        resourceIds[0] = response.getData().getId();
        resourceData = new ResourceData();
        resourceData.setDisplayName(uuid + "-2");
        resourceData.setName("Test2");
        resourceData.setMailaddress(ProvisioningService.getMailAddress("test2", testContext.getId()));
        response = resourceApi.createResource(resourceData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        resourceIds[1] = response.getData().getId();
    }

    @Test
    public void testUseCount() throws ApiException {
        ResourceSearchBody body = new ResourceSearchBody();
        body.setPattern(uuid);
        ResourcesResponse response = resourceApi.searchResources(body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        List<ResourceData> resources = response.getData();
        Assert.assertEquals(2, resources.size());
        int x = 0;
        // Check that resources are returned in the same order
        for (ResourceData resource : resources) {
            Assert.assertEquals(resourceIds[x++], resource.getId());
        }

        // use resource 2
        EventData eventData = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testUseCount", folderId);
        Attendee att = new Attendee();
        att.setEntity(resourceIds[1]);
        att.setCuType(CuTypeEnum.RESOURCE);
        eventData.addAttendeesItem(att);
        eventManager.createEvent(eventData, true);

        try {
            // Wait until use count has been adjusted
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        } catch (@SuppressWarnings("unused") InterruptedException e) {
            // ignore
        }

        // Check order again
        body = new ResourceSearchBody();
        body.setPattern(uuid);
        response = resourceApi.searchResources(body);
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
