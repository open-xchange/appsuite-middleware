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

package com.openexchange.file.storage.owncloud;

import static com.openexchange.java.Autoboxing.B;
import java.util.Map;
import java.util.Optional;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.owncloud.osgi.Services;
import com.openexchange.file.storage.owncloud.rest.OCCapabilities;
import com.openexchange.file.storage.owncloud.rest.OwnCloudRestClient;
import com.openexchange.file.storage.webdav.AbstractWebDAVAccountAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFolderAccess;
import com.openexchange.file.storage.webdav.WebDAVFileStorageConstants;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.file.storage.webdav.utils.WebDAVEndpointConfig;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.ManagedHttpClient;
import com.openexchange.rest.client.httpclient.util.HttpContextUtils;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link OwnCloudAccountAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudAccountAccess extends AbstractWebDAVAccountAccess {

    /** The identifier for obtaining a OwnCloud-associated HTTP client */
    public final static String HTTP_CLIENT_ID = "owncloud";

    private static final String REMOTE_PHP = "/remote.php";
    private static final Logger LOG = LoggerFactory.getLogger(OwnCloudAccountAccess.class);

    private OwnCloudRestClient restClient;

    /**
     * Initializes a new {@link OwnCloudAccountAccess}.
     *
     * @param service The {@link FileStorageService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    protected OwnCloudAccountAccess(OwnCloudFileStorageService service, @NonNull FileStorageAccount account, @NonNull Session session) {
        super(service, account, session);
    }

    private static final boolean DEACTIVATE_VERSIONING = true;

    @Override
    public Boolean supports(FileStorageCapability capability) {
        switch(capability) {
            case FILE_VERSIONS:
                if (DEACTIVATE_VERSIONING) {
                    return Boolean.FALSE;
                }
                if (isConnected() == false) {
                    try {
                        connect();
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                        return Boolean.FALSE;
                    } finally {
                        close();
                    }
                }
                try {
                    if (restClient == null) {
                        LOG.error("Missing rest client", new Exception());
                        return Boolean.FALSE;
                    }
                    OCCapabilities capabilities = restClient.getCapabilities();
                    return capabilities == null ? Boolean.FALSE : B(capabilities.supportsVersioning());
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                    return Boolean.FALSE;
                }
            default:
                return FileStorageCapabilityTools.supportsByClass(OwnCloudFileAccess.class, capability);
        }
    }

    @Override
    protected AbstractWebDAVFileAccess initWebDAVFileAccess(WebDAVClient webdavClient) throws OXException {
        return new OwnCloudFileAccess(webdavClient, this);
    }

    @SuppressWarnings("null")
    @Override
    protected AbstractWebDAVFolderAccess initWebDAVFolderAccess(WebDAVClient webdavClient) throws OXException {
        return new OwnCloudFolderAccess(webdavClient, this);
    }

    /**
     * Gets the rest client
     *
     * @return the rest client
     */
    protected Optional<OwnCloudRestClient> getRestClient() {
        return Optional.ofNullable(restClient);
    }

    @Override
    public void connect() throws OXException {
        super.connect();
        connectRestClient();
    }

    /**
     * Connects the rest client
     *
     * @throws OXException
     */
    @SuppressWarnings("null")
    protected void connectRestClient() throws OXException {
        Map<String, Object> configuration = account.getConfiguration();
        String configUrl = (String) configuration.get(WebDAVFileStorageConstants.WEBDAV_URL);
        if (Strings.isEmpty(configUrl)) {
            throw FileStorageExceptionCodes.INVALID_URL.create("not provided", "empty");
        }
        WebDAVEndpointConfig config = new WebDAVEndpointConfig.Builder(this.session, this.getWebDAVFileStorageService(), configUrl).build();
        String host = config.getUrl();

        if (Strings.isEmpty(host)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(getService().getId(), getAccountId());
        }
        if (host.contains(REMOTE_PHP) == false) {
            throw WebdavExceptionCodes.INVALID_CONFIG.create("Host url is invalid. Must contain '/remote.php'.");
        }
        String baseUri = host.substring(0, host.indexOf(REMOTE_PHP));
        if (Strings.isEmpty(baseUri)) {
            throw WebdavExceptionCodes.INVALID_CONFIG.create("Host url is invalid. Missing base uri.");
        }

        HttpContext context = getContextByAuthScheme(configuration, host);
        HttpContextUtils.addCookieStore(context, getSession(), getAccountId());
        ManagedHttpClient client = initDefaultClient();
        restClient = new OwnCloudRestClient(client, baseUri, context);
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && restClient != null;
    }

    protected @NonNull ManagedHttpClient initDefaultClient() throws OXException {
        return Services.getServiceLookup().getServiceSafe(HttpClientService.class).getHttpClient(optHttpClientId().orElse(HTTP_CLIENT_ID));
    }

    @Override
    protected Optional<String> optHttpClientId() {
        return Optional.of(HTTP_CLIENT_ID);
    }

}
