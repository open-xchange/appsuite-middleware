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

package com.openexchange.test.resourcecache.actions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;

/**
 * {@link UploadRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UploadRequest extends AbstractResourceCacheRequest<UploadResponse> {

    private final List<FileParameter> files = new ArrayList<FileParameter>();

    private final boolean waitForAlignment;

    private String resourceId = null;

    public UploadRequest() {
        super("upload");
        this.waitForAlignment = false;
    }

    public UploadRequest(boolean waitForAlignment) {
        super("upload");
        this.waitForAlignment = waitForAlignment;
    }

    public void addFile(String fileName, String mimeType, InputStream is) {
        files.add(new FileParameter("resource_" + files.size(), fileName, is, mimeType));
    }

    public void setResourceId(String id) {
        resourceId = id;
    }

    @Override
    public Method getMethod() {
        return Method.POST;
    }

    @Override
    public Parameter[] getAdditionalParameters() {
        List<Parameter> allParams = new LinkedList<Parameter>();
        allParams.addAll(files);
        allParams.add(new URLParameter("waitForAlignment", Boolean.toString(waitForAlignment)));
        if (resourceId != null) {
            allParams.add(new URLParameter("id", resourceId));
        }

        return allParams.toArray(new Parameter[0]);
    }

    @Override
    public UploadResponseParser getParser() {
        return new UploadResponseParser(true);
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new SimpleHeader("Content-Type", "multipart/form-data") };
    }

}
