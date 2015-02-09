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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.filestore.sproxyd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link S3FileStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydClient.class);

    private final String baseURL;
    private final DefaultHttpClient httpClient;

    /**
     * Initializes a new {@link SproxydClient}.
     *
     * @param baseURL The base URL to use
     */
    public SproxydClient(String baseURL) {
        super();
        this.baseURL = baseURL;
        this.httpClient = HttpClients.getHttpClient(null);
    }

    /**
     * Gets the input stream of a stored file
     *
     * @param id The identifier of the file
     * @return The file's input stream
     */
    public InputStream get(UUID id) throws OXException {

        HttpGet get = new HttpGet(buildURI(id));
        try {
            HttpResponse response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            return response.getEntity().getContent();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public InputStream get(UUID id, long offset, long length) throws OXException {
        return null;
    }

    private URI buildURI(UUID id) throws OXException {
        try {
            return new URI(baseURL + UUIDs.getUnformattedString(id));
        } catch (URISyntaxException e) {
            throw FileStorageCodes.IOERROR.create(e.getMessage(), e);
        }
    }

    public UUID put(InputStream data, long length) throws OXException {
        UUID id = UUID.randomUUID();
        HttpPut put = new HttpPut(buildURI(id));
        put.setEntity(new InputStreamEntity(data, length));
        try {
            HttpResponse response = httpClient.execute(put);
            StatusLine statusLine = response.getStatusLine();


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return id;
    }

    public boolean delete(UUID id) throws OXException {

        HttpDelete delete = new HttpDelete(buildURI(id));
        try {
            HttpResponse response = httpClient.execute(delete);
            StatusLine statusLine = response.getStatusLine();

            return true;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public void delete(Collection<UUID> ids) throws OXException {
    }

}
