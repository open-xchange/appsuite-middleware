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

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.owncloud.internal.NextCloudSearchBody;
import com.openexchange.file.storage.owncloud.internal.SearchResult;
import com.openexchange.file.storage.owncloud.internal.SearchResult.Propstat;
import com.openexchange.file.storage.owncloud.internal.SearchResult.Response;
import com.openexchange.file.storage.webdav.WebDAVPath;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVXmlBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NextCloudFileAccess} extends the {@link OwnCloudFileAccess}.
 * <p>
 *  Nextcloud is mostly compatible with owncloud, but requires some specific search handling
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class NextCloudFileAccess extends OwnCloudFileAccess {

    private static final Logger LOG = LoggerFactory.getLogger(NextCloudFileAccess.class);
    private static final String NEXTCLOUD_ROOT_SEARCH_PATH = "/remote.php/dav/";

    private final String nextcloudUser;

    /**
     * Initializes a new {@link NextCloudFileAccess}.
     *
     * @param webdavClient The webdav client to use
     * @param accountAccess The {@link OwnCloudAccountAccess}
     * @throws OXException
     */
    public NextCloudFileAccess(WebDAVClient webdavClient, OwnCloudAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
        Map<String, Object> configuration = accountAccess.getAccount().getConfiguration();
        nextcloudUser = (String) configuration.get("login");
    }

    @Override
    protected String getVersionsPath(String fileId) {
        return String.format("/remote.php/dav/versions/%s/versions/%s/", nextcloudUser, fileId);
    }

    private String getSearchFolder() {
        return String.format("/files/%s/", nextcloudUser);
    }

    @Override
    protected List<File> convertToFiles(List<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }
        List<File> result = new ArrayList<>();
        for (Response resp : responses) {
            Optional<Propstat> propstat = resp.getPropstat();

            //@formatter:off
            final boolean isColection = propstat.isPresent() && propstat.get().getFile().get().isCollection() ||
                                        new WebDAVPath(resp.getHref()).isCollection();
            //@formatter:on

            if (propstat.isPresent() && !isColection) {
                // Map the nextcloud's search representation to the file representation
                // /remote.php/dav/files/admin/Photos/Nut.jpg // -> /remote.php/webdav/Photos/Nut.jpg
                final String href = resp.getHref().replace(String.format("/remote.php/dav/files/%s/", nextcloudUser), rootPath.toString());
                result.add(convertToOwnCloudFile(href, propstat.get().getFile().get()));
            }
        }
        return result;
    }

    @Override
    protected List<File> performSearch(String folderId, String pattern, int start, int end, Set<QName> props) throws OXException {
        WebDAVXmlBody body = new NextCloudSearchBody(getSearchFolder(), pattern);
        List<File> result = client.<List<File>> search(NEXTCLOUD_ROOT_SEARCH_PATH, body, (doc) -> {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(SearchResult.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                SearchResult searchResult = (SearchResult) jaxbUnmarshaller.unmarshal(doc);
                return convertToFiles(searchResult.getFiles());
            } catch (JAXBException e) {
                // should never happen
                LOG.error("Unable to parse search result", e);
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
            }
        }, null);
        return result;
    }
}
