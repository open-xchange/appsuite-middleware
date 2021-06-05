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

package com.openexchange.groupware.upload.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * {@link IteratorOnlyServletFileUpload}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class IteratorOnlyServletFileUpload extends ServletFileUpload {

    private final ServletFileUpload delegate;

    /**
     * Initializes a new {@link IteratorOnlyServletFileUpload}.
     */
    IteratorOnlyServletFileUpload(ServletFileUpload delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public FileItemFactory getFileItemFactory() {
        return delegate.getFileItemFactory();
    }

    @Override
    public void setFileItemFactory(FileItemFactory factory) {
        delegate.setFileItemFactory(factory);
    }

    @Override
    public long getSizeMax() {
        return delegate.getSizeMax();
    }

    @Override
    public void setSizeMax(long sizeMax) {
        delegate.setSizeMax(sizeMax);
    }

    @Override
    public long getFileSizeMax() {
        return delegate.getFileSizeMax();
    }

    @Override
    public void setFileSizeMax(long fileSizeMax) {
        delegate.setFileSizeMax(fileSizeMax);
    }

    @Override
    public String getHeaderEncoding() {
        return delegate.getHeaderEncoding();
    }

    @Override
    public void setHeaderEncoding(String encoding) {
        delegate.setHeaderEncoding(encoding);
    }

    @Override
    public List<FileItem> parseRequest(HttpServletRequest req) throws FileUploadException {
        throw new FileUploadException("parseRequest() not supported");
    }

    @Override
    public FileItemIterator getItemIterator(RequestContext ctx) throws FileUploadException, IOException {
        return delegate.getItemIterator(ctx);
    }

    @Override
    public List<FileItem> parseRequest(RequestContext ctx) throws FileUploadException {
        throw new FileUploadException("parseRequest() not supported");
    }

    @Override
    public Map<String, List<FileItem>> parseParameterMap(RequestContext ctx) throws FileUploadException {
        return delegate.parseParameterMap(ctx);
    }

    @Override
    public ProgressListener getProgressListener() {
        return delegate.getProgressListener();
    }

    @Override
    public void setProgressListener(ProgressListener pListener) {
        delegate.setProgressListener(pListener);
    }

    @Override
    public Map<String, List<FileItem>> parseParameterMap(HttpServletRequest request) throws FileUploadException {
        return delegate.parseParameterMap(request);
    }

    @Override
    public FileItemIterator getItemIterator(HttpServletRequest request) throws FileUploadException, IOException {
        return delegate.getItemIterator(request);
    }

}
