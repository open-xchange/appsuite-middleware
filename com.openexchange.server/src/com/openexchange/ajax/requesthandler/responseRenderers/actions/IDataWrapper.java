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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import java.io.Closeable;
import java.util.List;
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
    String getDelivery();

    /**
     * Sets the delivery type
     *
     * @param delivery The delivery type to set
     * @return This object
     */
    IDataWrapper setDelivery(String delivery);

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    String getContentType();

    /**
     * Sets the content type
     *
     * @param contentType The content type to set
     * @return This object
     */
    IDataWrapper setContentType(String contentType);

    /**
     * Gets the content disposition
     *
     * @return The content disposition
     */
    String getContentDisposition();

    /**
     * Sets the content disposition
     *
     * @param contentDisposition The content disposition to set
     * @return This object
     */
    IDataWrapper setContentDisposition(String contentDisposition);

    /**
     * Returns contentTypeByParameter flag
     *
     * @return The contentTypeByParameter flag
     */
    Boolean getContentTypeByParameter();

    /**
     * Sets the contentTypeByParameter flag
     *
     * @param contentTypeByParameter The flag value to set
     * @return This object
     */
    IDataWrapper setContentTypeByParameter(Boolean contentTypeByParameter);

    /**
     * Gets the data as a {@link Readable}
     *
     * @return The data
     */
    Readable getDocumentData();

    /**
     * Sets the data
     *
     * @param documentData The data as a {@link Readable}
     * @return This object
     */
    IDataWrapper setDocumentData(Readable documentData);

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
    IDataWrapper setFile(IFileHolder file);

    /**
     * Gets the request
     *
     * @return The {@link HttpServletRequest}
     */
    HttpServletRequest getRequest();

    /**
     * Sets the request
     *
     * @param req The {@link HttpServletRequest}
     * @return This object
     */
    IDataWrapper setRequest(HttpServletRequest req);

    /**
     * Gets the content type of the file
     *
     * @return The content type of the file
     */
    String getFileContentType();

    /**
     * Sets the content type of the file
     *
     * @param fileContentType The content type of the file
     * @return This object
     */
    IDataWrapper setFileContentType(String fileContentType);

    /**
     * Gets the name of the file
     *
     * @return The file name
     */
    String getFileName();

    /**
     * Sets the name of the file
     *
     * @param fileName The file name
     * @return This object
     */
    IDataWrapper setFileName(String fileName);

    /**
     * Gets the request data
     *
     * @return The {@link AJAXRequestData}
     */
    AJAXRequestData getRequestData();

    /**
     * Sets the request data
     *
     * @param data The {@link AJAXRequestData}
     * @return This object
     */
    IDataWrapper setRequestData(AJAXRequestData data);

    /**
     * Gets the response object
     *
     * @return The {@link HttpServletResponse}
     */
    HttpServletResponse getResponse();

    /**
     * Sets the response object
     *
     * @param response The {@link HttpServletResponse}
     * @return This object
     */
    IDataWrapper setResponse(HttpServletResponse response);

    /**
     * Gets the user agent of the client
     *
     * @return The user agent of the client
     */
    String getUserAgent();

    /**
     * Sets the user agent of the client
     *
     * @param userAgent The user agent of the client
     * @return This object
     */
    IDataWrapper setUserAgent(String userAgent);

    /**
     * Adds a {@link Closeable} to this wrapper
     *
     * @param closeable A {@link Closeable}
     */
    void addCloseable(Closeable closeable);

    /**
     * Sets the list of {@link Closeable}s to the given list.
     *
     * @param closeables A list of {@link Closeable}s
     * @return This object
     */
    IDataWrapper setCloseAbles(List<Closeable> closeables);

    /**
     * Gets the list of {@link Closeable}s
     *
     * @return The list of {@link Closeable}s
     */
    List<Closeable> getCloseables();

    /**
     * Gets the {@link AJAXRequestResult}
     *
     * @return The {@link AJAXRequestResult}
     */
    AJAXRequestResult getResult();

    /**
     * Set the {@link AJAXRequestResult}
     *
     * @param result The {@link AJAXRequestResult}
     * @return This object
     */
    IDataWrapper setResult(AJAXRequestResult result);

}
