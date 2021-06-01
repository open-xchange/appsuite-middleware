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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.resource.actions.ResourceGetRequest;
import com.openexchange.ajax.resource.actions.ResourceGetResponse;
import com.openexchange.ajax.resource.actions.ResourceListRequest;
import com.openexchange.ajax.resource.actions.ResourceListResponse;
import com.openexchange.ajax.resource.actions.ResourceNewResponse;
import com.openexchange.ajax.resource.actions.ResourceSearchRequest;
import com.openexchange.ajax.resource.actions.ResourceSearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * This was just a quick&dirty implementation to get other tests running that use resources.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ResourceTestManager implements TestManager {

    private final AJAXClient client;

    private boolean failOnError;

    private Throwable lastException;

    private AbstractAJAXResponse lastResponse;

    private Date lastModified;

    private AJAXClient getClient() {
        return client;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    private void setLastException(Throwable t) {
        this.lastException = t;
    }

    @Override
    public Throwable getLastException() {
        return this.lastException;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return this.lastResponse;
    }

    @Override
    public boolean hasLastException() {
        return this.lastException != null;
    }

    @Override
    public void setFailOnError(boolean doFail) {
        this.failOnError = doFail;
    }

    private void setLastModification(Date timestamp) {
        this.lastModified = timestamp;
    }

    public Date getLastModification() {
        return this.lastModified;
    }

    private void setLastResponse(AbstractAJAXResponse response) {
        this.lastResponse = response;
    }

    public ResourceTestManager(AJAXClient client) {
        this.client = client;
    }

    @Override
    public void cleanUp() {
        // nothing to do
    }

    public Resource generateDefaultResource() {
        final Resource resource = new Resource();
        resource.setAvailable(true);
        resource.setMail("my.resource@domain.tdl");
        resource.setSimpleName("MySimpleResourceIdentifier");
        resource.setDisplayName("Resource 1337");
        resource.setDescription("MySimpleResourceIdentifier - Resource 1337");
        return resource;
    }

    public List<Resource> search(String pattern) throws JSONException {
        ResourceSearchResponse response = execute(new ResourceSearchRequest(pattern, getFailOnError()));
        extractInfo(response);
        return response.getResources();
    }

    public List<Resource> list(int[] ids) throws JSONException, OXException {
        ResourceListResponse response = execute(new ResourceListRequest(ids));
        extractInfo(response);
        return Arrays.asList(response.getResources());
    }

    public Resource get(int id) throws JSONException, OXException {
        ResourceGetResponse response = execute(new ResourceGetRequest(id, false));
        extractInfo(response);
        return response.getResource();
    }

    protected <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) {
        try {
            return getClient().execute(request);
        } catch (OXException e) {
            setLastException(e);
            if (failOnError) {
                fail("AjaxException during resource creation: " + e.getLocalizedMessage());
            }
        } catch (IOException e) {
            setLastException(e);
            if (failOnError) {
                fail("IOException during resource creation: " + e.getLocalizedMessage());
            }
        } catch (JSONException e) {
            setLastException(e);
            if (failOnError) {
                fail("JsonException during resource creation: " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    protected void updateResource(Resource res, ResourceNewResponse response) throws JSONException {
        res.setLastModified(response.getTimestamp());
        res.setIdentifier(response.getID());
    }

    protected void extractInfo(AbstractAJAXResponse response) {
        setLastResponse(response);
        setLastModification(response.getTimestamp());
        if (response.hasError()) {
            setLastException(response.getException());
        }
    }
}
