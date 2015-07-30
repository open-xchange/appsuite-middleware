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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.responseRenderers.Actions;

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
 * {@link IDataWrapper}
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

    public String getDelivery();

    public void setDelivery(String delivery);

    public String getContentType();

    public void setContentType(String contentType);

    public String getContentDisposition();

    public void setContentDisposition(String contentDisposition);

    public Boolean getContentTypeByParameter();

    public void setContentTypeByParameter(Boolean contentTypeByParameter);

    public Readable getDocumentData();

    public void setDocumentData(Readable documentData);

    public long getLength();

    public void setLength(long length);

    public IFileHolder getFile();

    public void setFile(IFileHolder file);

    public HttpServletRequest getRequest();

    public void setRequest(HttpServletRequest req);

    public String getFileContentType();

    public void setFileContentType(String fileContentType);

    public String getFileName();

    public void setFileName(String fileName);

    public AJAXRequestData getRequestData();

    public void setRequestData(AJAXRequestData data);

    public HttpServletResponse getResponse();

    public void setResponse(HttpServletResponse response);

    public String getUserAgent();

    public void setUserAgent(String userAgent);

    public void addCloseable(Closeable closeable);

    public List<Closeable> getCloseables();

    public AJAXRequestResult getResult();

    public void setResult(AJAXRequestResult result);

    public AtomicReference<File> getTmpDirReference();

    public void setTmpDirReference(AtomicReference<File> tmpDirReference);

}
