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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
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

    private final List<Resource> createdEntites = new LinkedList<Resource>();

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
        boolean old = failOnError;
        setFailOnError(false);
        try {
            for (Resource res : createdEntites) {
                // TODO
            }
        } finally {
            setFailOnError(old);
        }
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
