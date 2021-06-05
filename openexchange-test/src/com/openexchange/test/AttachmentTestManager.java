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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.attach.actions.AllRequest;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.AttachResponse;
import com.openexchange.ajax.attach.actions.DetachRequest;
import com.openexchange.ajax.attach.actions.DetachResponse;
import com.openexchange.ajax.attach.actions.GetDocumentResponse;
import com.openexchange.ajax.attach.actions.GetResponse;
import com.openexchange.ajax.attach.actions.ListRequest;
import com.openexchange.ajax.attach.actions.ListResponse;
import com.openexchange.ajax.attach.actions.UpdatesRequest;
import com.openexchange.ajax.attach.actions.UpdatesResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.search.Order;

/**
 * {@link AttachmentTestManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class AttachmentTestManager implements TestManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentTestManager.class);

    private final List<AttachmentMetadata> createdEntities = new ArrayList<AttachmentMetadata>();

    private AJAXClient client;

    private TimeZone timezone;

    private AbstractAJAXResponse lastResponse;

    private boolean failOnError;

    private Exception lastException;

    @SuppressWarnings("unused")
    public AttachmentTestManager(AJAXClient client) {
        this.setClient(client);

        try {
            timezone = client.getValues().getTimeZone();
        } catch (OXException e) {
            // wait for finally block
        } catch (IOException e) {
            // wait for finally block
        } catch (JSONException e) {
            // wait for finally block
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getTimeZone("Europe/Berlin");
            }
        }
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setLastResponse(AbstractAJAXResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    public void setLastException(Exception lastException) {
        lastException.printStackTrace();
        this.lastException = lastException;
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    @Override
    public boolean hasLastException() {
        return lastException != null;
    }

    @Override
    public void cleanUp() {
        List<AttachmentMetadata> objects = new ArrayList<AttachmentMetadata>(createdEntities.size());
        for (AttachmentMetadata metadata : createdEntities) {
            objects.add(metadata);
        }
        for (AttachmentMetadata attachment : objects) {
            try {
                detach(attachment, new int[] { attachment.getId() });
                if (getLastResponse().hasError()) {
                    org.slf4j.LoggerFactory.getLogger(AttachmentTestManager.class).warn("Unable to delete the attachment with id {}, attachedId {} in folder {} with name '{}': {}", I(attachment.getId()), I(attachment.getAttachedId()), I(attachment.getFolderId()), attachment.getFilename(), getLastResponse().getException().getMessage());
                }

            } catch (OXException | IOException | JSONException e) {
                LOG.error("Unable to remove attachment!", e);
            }
        }
    }

    public int attach(AttachmentMetadata attachment, String fileName, InputStream data, String mimeType) throws OXException, IOException, JSONException {
        AttachRequest attachRequest = new AttachRequest(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), fileName, data, mimeType);
        AttachResponse response = client.execute(attachRequest);
        extractInfo(response);
        attachment.setId(response.getId());
        if (doesFailOnError() || response.getId() != 0) {
            createdEntities.add(attachment);
        }
        return response.getId();
    }

    public void detach(AttachmentMetadata attachment, int[] versions) throws OXException, IOException, JSONException {
        DetachRequest detachRequest = new DetachRequest(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), versions);
        DetachResponse response = client.execute(detachRequest);
        extractInfo(response);

        createdEntities.remove(attachment);
    }
    public void list(int folderId, int objectId, int moduleId, int[] attachmentIds, int[] columns) throws OXException, IOException, JSONException {
        ListRequest listRequest = new ListRequest(folderId, objectId, moduleId, attachmentIds, columns);
        ListResponse response = client.execute(listRequest);
        extractInfo(response);
    }

    public AttachmentMetadata get(int folderId, int attached, int module, int id) throws OXException, IOException, JSONException {
        com.openexchange.ajax.attach.actions.GetRequest request = new com.openexchange.ajax.attach.actions.GetRequest(folderId, attached, module, id);
        GetResponse response = client.execute(request);

        extractInfo(response);
        return response.getAttachment();
    }

    private void extractInfo(AbstractAJAXResponse response) {
        setLastResponse(response);
        if (response.hasError()) {
            setLastException(response.getException());
        }
    }

    public String document(int folderId, int attachedId, int moduleId, int objectId) throws IllegalStateException, IOException, OXException, JSONException {
        com.openexchange.ajax.attach.actions.GetDocumentRequest request = new com.openexchange.ajax.attach.actions.GetDocumentRequest(folderId, objectId, moduleId, attachedId);
        GetDocumentResponse response = client.execute(request);

        extractInfo(response);
        return response.getContentAsString();
    }

    public void all(int folderId, int attachedId, int moduleId, int[] columns, int sort, Order order) throws OXException, IOException, JSONException {
        AllRequest allRequest = new AllRequest(folderId, attachedId, moduleId, columns, sort, order);
        AllResponse allResponse = client.execute(allRequest);
        extractInfo(allResponse);
    }

    public void updates(int folderId, int objectId, int moduleId, int[] columns, long timestamp) throws OXException, IOException, JSONException {
        UpdatesRequest request = new UpdatesRequest(folderId, objectId, moduleId, columns, timestamp);
        UpdatesResponse response = client.execute(request);
        extractInfo(response);
    }

    public List<AttachmentMetadata> getCreatedEntities() {
        return createdEntities;
    }
}
