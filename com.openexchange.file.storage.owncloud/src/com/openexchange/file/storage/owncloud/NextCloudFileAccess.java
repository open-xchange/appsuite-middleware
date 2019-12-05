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
