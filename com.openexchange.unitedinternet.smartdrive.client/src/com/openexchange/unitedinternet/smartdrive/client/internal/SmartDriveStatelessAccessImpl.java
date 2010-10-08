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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveAccess;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
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

    private String downloadToken;

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
            requestPrefix =
                new StringBuilder(16).append(path.endsWith("/") ? path.substring(0, path.length() - 1) : path).append("/data/").append(
                    userName).append('/').toString();
        } else {
            requestPrefix = new StringBuilder(16).append("/data/").append(userName).append('/').toString();
        }
    }

    private String getDownloadToken() throws SmartDriveException {
        if (null == downloadToken) {
            downloadToken = (String) client.getParams().getParameter(HTTP_CLIENT_PARAM_DOWNLOAD_TOKEN);
            if (null == downloadToken) {
                downloadToken = access.getStatefulAccess().obtainDownloadToken();
            }
        }
        return downloadToken;
    }

    public InputStream downloadFile(final String filePath) throws SmartDriveException {
        try {
            final String uriStr = getURI("download", filePath);
            final GetMethod method = new GetMethod(uriStr);
            try {
                /*
                 * Add token
                 */
                final NameValuePair nvp = new NameValuePair("token", getDownloadToken());
                method.setQueryString(new NameValuePair[] { nvp });
                /*
                 * Execute method
                 */
                client.executeMethod(method);
                /*
                 * Return as stream
                 */
                return new SmartDriveInputStream(method);
            } catch (final HttpException e) {
                throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
            } catch (final IOException e) {
                throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } // No finally-clause, closed in SmartDriveInputStream instance when invoking close()
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*-
     * ---------------------------- HELPER METHODS ----------------------------
     */

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
