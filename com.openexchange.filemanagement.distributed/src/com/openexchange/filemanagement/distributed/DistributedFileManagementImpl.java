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

package com.openexchange.filemanagement.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link DistributedFileManagementImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileManagementImpl implements DistributedFileManagement {

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 3000;

    private static AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    public static void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        DistributedFileManagementImpl.REFERENCE.set(hazelcastInstance);
    }

    // ---------------------------------- Member stuff -------------------------------------- //

    private final String mapName;
    private final String address;
    private final ServiceLookup services;
    private final BundleContext bundleContext;
    private ServiceRegistration<HazelcastInstanceNotActiveException> hzDownRegistration;
    private final Runnable shutDownTask;

    /**
     * Initializes a new {@link DistributedFileManagementImpl}.
     */
    public DistributedFileManagementImpl(ServiceLookup services, String address, String mapName, BundleContext bundleContext, Runnable shutDownTask) {
        super();
        this.services = services;
        this.address = address;
        this.mapName = mapName;
        this.bundleContext = bundleContext;
        this.shutDownTask = shutDownTask;
    }

    /**
     * Performs necessary clean-up actions before destroying this distributed file management service.
     */
    public synchronized void cleanUp() {
        ServiceRegistration<HazelcastInstanceNotActiveException> hzDownRegistration = this.hzDownRegistration;
        if (null != hzDownRegistration) {
            hzDownRegistration.unregister();
            this.hzDownRegistration = null;
        }
    }

    private String encodeId(String rawId) throws OXException {
        return services.getServiceSafe(DistributedFileUtils.class).encodeId(rawId);
    }

    /**
     * Handles special {@link HazelcastInstanceNotActiveException} exception instance.
     *
     * @param hzDown The {@link HazelcastInstanceNotActiveException} exception instance signaling a broken Hazelcast cluster
     */
    protected synchronized void handleHzNotActiveException(HazelcastInstanceNotActiveException hzDown) {
        if (null == hzDownRegistration) {
            hzDownRegistration = bundleContext.registerService(HazelcastInstanceNotActiveException.class, hzDown, null);
            LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(500L, TimeUnit.MILLISECONDS));
            shutDownTask.run();
        }
    }

    @Override
    public void register(String id) throws OXException {
        try {
            map().put(id, getURI());
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void unregister(String id) throws OXException {
        try {
            map().remove(id);
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream get(String id) throws OXException {
        try {
            String url = map().get(id);
            if (url != null) {
                try {
                    return loadFile("http://" + url + "/" + encodeId(id));
                } catch (IOException e) {
                    throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
                }
            }

            return null;
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void touch(String id) throws OXException {
        try {
            String url = map().get(id);
            if (url != null) {
                try {
                    URL remoteUrl = new URL("http://" + url + "/" + encodeId(id));
                    HttpURLConnection con = (HttpURLConnection) remoteUrl.openConnection();
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(CONNECT_TIMEOUT);
                    con.connect();
                } catch (IOException e) {
                    throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
                }
            }
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean exists(final String id, long timeout, TimeUnit unit) throws OXException, TimeoutException {
        Future<Boolean> f = services.getService(ThreadPoolService.class).getExecutor().submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws OXException {
                try {
                    return Boolean.valueOf(map().containsKey(id));
                } catch (HazelcastInstanceNotActiveException e) {
                    handleHzNotActiveException(e);
                    throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } catch (RuntimeException e) {
                    throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });

        try {
            return f.get(timeout, unit).booleanValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, "Thread interrupted");
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (TimeoutException e) {
            f.cancel(true);
            throw e;
        }
    }

    @Override
    public boolean exists(String id) throws OXException {
        try {
            return map().containsKey(id);
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void remove(String id) throws OXException {
        try {
            String url = map().get(id);
            if (url != null) {
                try {
                    URL remoteUrl = new URL("http://" + url + "/" + encodeId(id));
                    HttpURLConnection con = (HttpURLConnection) remoteUrl.openConnection();
                    con.setRequestMethod("DELETE");
                    con.setConnectTimeout(CONNECT_TIMEOUT);
                    con.connect();
                } catch (IOException e) {
                    throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
                }
            }
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String getURI() {
        return address + PATH;
    }

    /**
     * Gets the associated Hazelcast map.
     *
     * @return The Hazelcast map
     * @throws OXException If Hazelcast map cannot be returned
     */
    protected IMap<String, String> map() throws OXException {
        HazelcastInstance hazelcastInstance = REFERENCE.get();
        if (hazelcastInstance == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        try {
            return hazelcastInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleHzNotActiveException(e);
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private InputStream loadFile(String url) throws IOException {
        URL remoteUrl = new URL(url);

        HttpURLConnection con = null;
        InputStream in = null;
        try {
            con = (HttpURLConnection) remoteUrl.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            con.connect();

            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            in = con.getInputStream();
            InputStream retval = in;
            in = null; // Avoid premature closing
            return retval;
        } finally {
            if (in != null) {
                Streams.close(in);
                if (con != null) {
                    con.disconnect();
                }
            }
        }
    }

}
