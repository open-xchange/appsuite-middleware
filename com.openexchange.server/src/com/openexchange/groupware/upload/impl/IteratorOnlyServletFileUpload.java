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
