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

package com.openexchange.http.client.apache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;

/**
 *
 * {@link ApacheMultipartPostRequestBuilder}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class ApacheMultipartPostRequestBuilder extends CommonApacheHTTPRequest<HTTPMultipartPostRequestBuilder> implements HTTPMultipartPostRequestBuilder {

    private final ManagedFileManagement fileManager;
    private final Map<String, ContentBody> parts = new HashMap<String, ContentBody>();

    private final List<ManagedFile> managedFiles = new ArrayList<ManagedFile>();

    public ApacheMultipartPostRequestBuilder(ApacheClientRequestBuilder coreBuilder, ManagedFileManagement fileManager) {
        super(coreBuilder);
        this.fileManager = fileManager;

    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, File file) throws OXException {
        parts.put(fieldName, new FileBody(file));
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, InputStream is, String contentType, String filename) throws OXException {
        parts.put(fieldName, new FileBody(partSource(is), ContentType.create(contentType, "UTF-8"), filename));
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, InputStream is, String contentType) throws OXException {
        parts.put(fieldName, new FileBody(partSource(is), ContentType.create(contentType, "UTF-8"), "data.bin"));
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, String s, String contentType, String filename) throws OXException {
        try {
            parts.put(fieldName, new FileBody(partSource(s.getBytes("UTF-8")), ContentType.create(contentType, "UTF-8"), filename));
        } catch (UnsupportedEncodingException e) {
        }
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, String s, String contentType) throws OXException {
        try {
            parts.put(fieldName, new FileBody(partSource(s.getBytes("UTF-8")), ContentType.create(contentType, "UTF-8"), "data.txt"));
        } catch (UnsupportedEncodingException e) {
        }
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, InputStream is, String contentType, long length, String filename) throws OXException {
        parts.put(fieldName, new FileBody(partSource(is), ContentType.create(contentType, "UTF-8"), filename));
        return this;
    }

    @Override
    public HTTPMultipartPostRequestBuilder stringPart(String fieldName, String fieldValue) {
        parts.put(fieldName, new StringBody(fieldValue, ContentType.TEXT_PLAIN));
        return this;
    }

    @Override
    protected HttpRequestBase createMethod(String encodedSite) {
        HttpPost m = new HttpPost(encodedSite);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Entry<String, ContentBody> entry : parts.entrySet()) {
            builder.addPart(entry.getKey(), entry.getValue());
        }
        m.setEntity(builder.build());
        return m;
    }

    private File partSource(byte[] data) throws OXException {
        return partSource(new ByteArrayInputStream(data));
    }

    private File partSource(InputStream is) throws OXException {
        ManagedFile managedFile = fileManager.createManagedFile(is);
        managedFiles.add(managedFile);
        return managedFile.getFile();
    }

    @Override
    public void done() {
        for (ManagedFile managedFile : managedFiles) {
            managedFile.delete();
        }
        managedFiles.clear();
        super.done();
    }

}
