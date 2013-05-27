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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.filemanagement.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DistributedFileManagementImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileManagementImpl implements DistributedFileManagement {

    private static final int TIMEOUT = 10000;

    private static AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    private static final String PATH = "distributedFiles";

    public static void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        DistributedFileManagementImpl.REFERENCE.set(hazelcastInstance);
    }

    // ---------------------------------- Member stuff -------------------------------------- //

    private final String mapName;
    private final String address;
    private final ServiceLookup services;

    public DistributedFileManagementImpl(ServiceLookup services, String address, String mapName) {
        super();
        this.services = services;
        this.address = address;
        this.mapName = mapName;
    }

    @Override
    public void register(String id) throws OXException {
        map().put(id, getURI());
    }

    @Override
    public void unregister(String id) throws OXException {
        map().remove(id);
    }

    @Override
    public InputStream get(String id) throws OXException {
        String url = map().get(id);
        InputStream retval = null;
        if (url != null) {
            try {
                retval = loadFile("http://" + url + "/" + id);
            } catch (IOException e) {
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            }
        }

        return retval;
    }

    @Override
    public void touch(String id) throws OXException {
        String url = map().get(id);
        if (url != null) {
            try {
                URL remoteUrl = new URL("http://" + url + "/" + id);
                HttpURLConnection con = (HttpURLConnection) remoteUrl.openConnection();
                con.setRequestMethod("POST");
                con.connect();
            } catch (IOException e) {
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public boolean exists(String id) throws OXException {
        return map().containsKey(id);
    }

    @Override
    public void remove(String id) throws OXException {
        String url = map().get(id);
        if (url != null) {
            try {
                URL remoteUrl = new URL("http://" + url + "/" + id);
                HttpURLConnection con = (HttpURLConnection) remoteUrl.openConnection();
                con.setRequestMethod("DELETE");
                con.connect();
            } catch (IOException e) {
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    private String getURI() {
        return address + PATH;
    }

    private IMap<String, String> map() throws OXException {
        HazelcastInstance hazelcastInstance = REFERENCE.get();
        if (hazelcastInstance == null || !hazelcastInstance.getLifecycleService().isRunning()) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        return hazelcastInstance.getMap(mapName);
    }

    private InputStream loadFile(String url) throws IOException {
        URL remoteUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) remoteUrl.openConnection();
        con.setRequestMethod("GET");
        con.setReadTimeout(TIMEOUT);
        con.connect();

        boolean close = true;
        try {
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final InputStream in = con.getInputStream();
                close = false;
                return in;
            }
            return null;
        } finally {
            if (close) {
                try {
                    // The only way to close a HttpURLConnection
                    con.getInputStream().close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

}
