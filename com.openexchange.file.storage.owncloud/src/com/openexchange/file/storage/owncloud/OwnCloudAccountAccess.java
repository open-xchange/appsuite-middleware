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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud;

import static com.openexchange.java.Autoboxing.B;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
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

    @SuppressWarnings("null")
    @Override
    public void connect() throws OXException {
        super.connect();
        Map<String, Object> configuration = account.getConfiguration();
        String login = (String) configuration.get("login");
        String password = (String) configuration.get("password");
        WebDAVEndpointConfig config = new WebDAVEndpointConfig.Builder(this.session, this.getWebDAVFileStorageService(), (String) configuration.get(WebDAVFileStorageConstants.WEBDAV_URL)).build();
        String host = config.getUrl();

        if (Strings.isEmpty(login) || Strings.isEmpty(password) || Strings.isEmpty(host)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(getService().getId(), getAccountId());
        }
        if (host.contains(REMOTE_PHP) == false) {
            throw WebdavExceptionCodes.INVALID_CONFIG.create("Host url is invalid. Must contain '/remote.php'.");
        }
        String baseUri = host.substring(0, host.indexOf(REMOTE_PHP));
        try {
            URI uri = new URI(host);
            HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));

            AuthCache authCache = new BasicAuthCache();
            authCache.put(targetHost, new BasicScheme());

            // Add AuthCache to the execution context
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            HttpContextUtils.addCookieStore(context, getSession(), getAccountId());
            ManagedHttpClient client = initDefaultClient();
            restClient = new OwnCloudRestClient(client, baseUri, context);
        } catch (URISyntaxException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(host, e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && restClient != null;
    }

    protected ManagedHttpClient initDefaultClient() throws OXException {
        return Services.getServiceLookup().getServiceSafe(HttpClientService.class).getHttpClient(optHttpClientId().orElse(HTTP_CLIENT_ID));
    }

    @Override
    protected Optional<String> optHttpClientId() {
        return Optional.of(HTTP_CLIENT_ID);
    }

}
