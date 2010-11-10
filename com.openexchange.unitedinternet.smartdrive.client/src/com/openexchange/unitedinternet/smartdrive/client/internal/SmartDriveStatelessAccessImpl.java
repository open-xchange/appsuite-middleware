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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveAccess;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResource;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResponse;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveStatelessAccess;

/**
 * {@link SmartDriveStatelessAccessImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmartDriveStatelessAccessImpl implements SmartDriveStatelessAccess {

    private final HttpClient client;

    private final SmartDriveAccess access;

    private final String requestPrefix;

    private final String userName;


    /**
     * Initializes a new {@link SmartDriveStatelessAccessImpl}.
     */
    public SmartDriveStatelessAccessImpl(final String userName, final URL smartDriveServerUrl, final HttpClient client, final SmartDriveAccess access) {
        super();
        this.access = access;
        this.client = client;
        this.userName = userName;
        String path = smartDriveServerUrl.getPath();
        if (null != path && path.length() > 0) {
            if (path.charAt(0) != '/') {
                path = '/' + path;
            }
            requestPrefix = new StringBuilder(16).append(path.endsWith("/") ? path.substring(0, path.length() - 1) : path).append("/data/").append(
                userName).append('/').toString();
        } else {
            requestPrefix = new StringBuilder(16).append("/data/").append(userName).append('/').toString();
        }
    }

    public InputStream downloadFile(String path) throws SmartDriveException {
        try {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            String downloadToken = obtainDownloadTokenFor(path);
            
            
            String downloadURL = getURI("download", path);

            GetMethod get = new GetMethod(downloadURL);

            get.setQueryString(new NameValuePair[] { new NameValuePair("token", downloadToken) });

            client.executeMethod(get);

            int status = get.getStatusCode();
            if (SC_UNAUTHORIZED == status) {
                throw SmartDriveExceptionCodes.UNAUTHORIZED.create(userName, "");
            }
            if (SC_GENERAL_ERROR == status) {
                throw SmartDriveExceptionCodes.GENERAL_ERROR.create(get.getStatusText());
            }
            return get.getResponseBodyAsStream();
        } catch (HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }

    }
    
    public void uploadFile(String directory, final String filename, final InputStream data) throws SmartDriveException {
        try {
            
            byte[] bytes = IOTools.getBytes(data);
            
            String uploadToken = obtainUploadTokenFor(directory);
            
            String uploadURL = getURI("upload");
            
            PostMethod multipartPostMethod = new PostMethod(uploadURL);

            if (directory.startsWith("/")) {
                directory = "/"+userName+directory;
            } else {
                directory = "/"+userName+"/"+directory;
            }
            
            if(!directory.endsWith("/")) {
                directory += "/";
            }

            
            MultipartRequestEntity entity = new MultipartRequestEntity(new Part[]{
                new StringPart("uploadToken", uploadToken),
                new StringPart("targetFolder", directory),
                new FilePart("file1", new ByteArrayPartSource(filename, bytes))
            }, client.getParams());
            
            multipartPostMethod.setRequestEntity(entity);
            client.executeMethod(multipartPostMethod);
            
            int status = multipartPostMethod.getStatusCode();
            if (SC_UNAUTHORIZED == status) {
                throw SmartDriveExceptionCodes.UNAUTHORIZED.create(userName, "");
            }
            if (SC_GENERAL_ERROR == status) {
                throw SmartDriveExceptionCodes.GENERAL_ERROR.create(multipartPostMethod.getStatusText());
            }
            
        } catch (HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }


    /*-
     * ---------------------------- HELPER METHODS ----------------------------
     */

    private String obtainUploadTokenFor(String path) throws SmartDriveException {
        SmartDriveResponse<List<SmartDriveResource>> response = access.getStatefulAccess().propget(path, new int[0]);
        // TODO: Error Handling
        SmartDriveResource resource = response.getResponse().get(0);
        return resource.toDirectory().getUploadToken();
    }

    
    private String obtainDownloadTokenFor(String path) throws SmartDriveException {
        SmartDriveResponse<List<SmartDriveResource>> response = access.getStatefulAccess().propget(path, new int[0]);
        // TODO: Error Handling
        SmartDriveResource resource = response.getResponse().get(0);

        return resource.getDownloadToken();
    }
    
    /**
     * Sucks method's response input stream using direct buffering.
     * 
     * @param method The method
     * @return The read byte arary
     * @throws IOException If an I/O error occurs
     */
    private static byte[] suckMethodResponse(final HttpMethodBase method) throws IOException {
        final InputStream in = method.getResponseBodyAsStream();
        try {
            final UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
            final byte[] buf = new byte[2048];
            int read;
            while ((read = in.read(buf, 0, buf.length)) >= 0) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(SmartDriveStatefulAccessImpl.class).error(e.getMessage(), e);
            }
        }
    }

    private String getURI(final String methodName, final String... pathElements) throws SmartDriveException {
        try {
            final URI uri = new URI(requestPrefix, false, "ISO-8859-1");
            final StringBuilder sb = new StringBuilder(uri.getPath()).append(methodName);
            for (final String pathElement : pathElements) {
                sb.append('/').append(pathElement);
            }
            uri.setPath(sb.toString());
            return uri.toString();
        } catch (final URIException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
