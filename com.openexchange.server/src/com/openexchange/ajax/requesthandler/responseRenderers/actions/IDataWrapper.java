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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;

/**
 * {@link IDataWrapper} is a wrapper for data used in implementations of {@link IFileResponseRendererAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public interface IDataWrapper {

    /** The default in-memory threshold of 1MB. */
    static final int DEFAULT_IN_MEMORY_THRESHOLD = 1024 * 1024; // 1MB
    static final int INITIAL_CAPACITY = 8192;
    static final String PARAMETER_CONTENT_DISPOSITION = "content_disposition";
    static final String PARAMETER_CONTENT_TYPE = "content_type";
    static final String DOWNLOAD = "download";
    static final String VIEW = "view";
    static final String SAVE_AS_TYPE = "application/octet-stream";

    /**
     * Gets the delivery type
     *
     * @return The delivery type
     */
    public String getDelivery();

    /**
     * Sets the delivery type
     *
     * @param delivery The delivery type to set
     * @return This object
     */
    public IDataWrapper setDelivery(String delivery);

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    public String getContentType();

    /**
     * Sets the content type
     *
     * @param contentType The content type to set
     * @return This object
     */
    public IDataWrapper setContentType(String contentType);

    /**
     * Gets the content disposition
     *
     * @return The content disposition
     */
    public String getContentDisposition();

    /**
     * Sets the content disposition
     *
     * @param contentDisposition The content disposition to set
     * @return This object
     */
    public IDataWrapper setContentDisposition(String contentDisposition);

    /**
     * Returns contentTypeByParameter flag
     *
     * @return The contentTypeByParameter flag
     */
    public Boolean getContentTypeByParameter();

    /**
     * Sets the contentTypeByParameter flag
     *
     * @param contentTypeByParameter The flag value to set
     * @return This object
     */
    public IDataWrapper setContentTypeByParameter(Boolean contentTypeByParameter);

    /**
     * Gets the data as a {@link Readable}
     *
     * @return The data
     */
    public Readable getDocumentData();

    /**
     * Sets the data
     *
     * @param documentData The data as a {@link Readable}
     * @return This object
     */
    public IDataWrapper setDocumentData(Readable documentData);

    /**
     * Gets the length of the data
     * 
     * @return The length in bytes
     */
    public long getLength();

    /**
     * Sets the length of the data
     * 
     * @param length The length in bytes
     * @return This object
     */
    public IDataWrapper setLength(long length);

    /**
     * Gets the file
     * 
     * @return The file as a {@link IFileHolder}
     */
    public IFileHolder getFile();

    /**
     * Sets the file
     * 
     * @param file The file as a {@link IFileHolder}
     * @return This object
     */
    public IDataWrapper setFile(IFileHolder file);

    /**
     * Gets the request
     * 
     * @return The {@link HttpServletRequest}
     */
    public HttpServletRequest getRequest();

    /**
     * Sets the request
     * 
     * @param req The {@link HttpServletRequest}
     * @return This object
     */
    public IDataWrapper setRequest(HttpServletRequest req);

    /**
     * Gets the content type of the file
     * 
     * @return The content type of the file
     */
    public String getFileContentType();

    /**
     * Sets the content type of the file
     * 
     * @param fileContentType The content type of the file
     * @return This object
     */
    public IDataWrapper setFileContentType(String fileContentType);

    /**
     * Gets the name of the file
     * 
     * @return The file name
     */
    public String getFileName();

    /**
     * Sets the name of the file
     * 
     * @param fileName The file name
     * @return This object
     */
    public IDataWrapper setFileName(String fileName);

    /**
     * Gets the request data
     * 
     * @return The {@link AJAXRequestData}
     */
    public AJAXRequestData getRequestData();

    /**
     * Sets the request data
     * 
     * @param data The {@link AJAXRequestData}
     * @return This object
     */
    public IDataWrapper setRequestData(AJAXRequestData data);

    /**
     * Gets the response object
     * 
     * @return The {@link HttpServletResponse}
     */
    public HttpServletResponse getResponse();

    /**
     * Sets the response object
     * 
     * @param response The {@link HttpServletResponse}
     * @return This object
     */
    public IDataWrapper setResponse(HttpServletResponse response);

    /**
     * Gets the user agent of the client
     * 
     * @return The user agent of the client
     */
    public String getUserAgent();

    /**
     * Sets the user agent of the client
     * 
     * @param userAgent The user agent of the client
     * @return This object
     */
    public IDataWrapper setUserAgent(String userAgent);

    /**
     * Adds a {@link Closeable} to this wrapper
     * 
     * @param closeable A {@link Closeable}
     */
    public void addCloseable(Closeable closeable);

    /**
     * Sets the list of {@link Closeable}s to the given list.
     * 
     * @param closeables A list of {@link Closeable}s
     * @return This object
     */
    public IDataWrapper setCloseAbles(List<Closeable> closeables);

    /**
     * Gets the list of {@link Closeable}s
     * 
     * @return The list of {@link Closeable}s
     */
    public List<Closeable> getCloseables();

    /**
     * Gets the {@link AJAXRequestResult}
     * 
     * @return The {@link AJAXRequestResult}
     */
    public AJAXRequestResult getResult();

    /**
     * Set the {@link AJAXRequestResult}
     * 
     * @param result The {@link AJAXRequestResult}
     * @return This object
     */
    public IDataWrapper setResult(AJAXRequestResult result);

    /**
     * Gets an {@link AtomicReference} to the temporal directory.
     * 
     * @return The {@link AtomicReference}
     */
    public AtomicReference<File> getTmpDirReference();

    /**
     * Sets the temporal directory
     * 
     * @param tmpDirReference The {@link AtomicReference}
     * @return This object
     */
    public IDataWrapper setTmpDirReference(AtomicReference<File> tmpDirReference);

}
