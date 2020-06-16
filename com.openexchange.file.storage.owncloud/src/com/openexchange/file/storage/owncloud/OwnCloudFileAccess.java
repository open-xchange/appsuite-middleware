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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.l;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.FileStorageZippableFolderFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.owncloud.internal.OwnCloudFile;
import com.openexchange.file.storage.owncloud.internal.OwnCloudPermissionsUtil;
import com.openexchange.file.storage.owncloud.internal.OwnCloudSearchBody;
import com.openexchange.file.storage.owncloud.internal.SearchResult;
import com.openexchange.file.storage.owncloud.internal.SearchResult.Propstat;
import com.openexchange.file.storage.owncloud.internal.SearchResult.Response;
import com.openexchange.file.storage.owncloud.internal.SearchResult.SimpleFile;
import com.openexchange.file.storage.owncloud.rest.OwnCloudRestClient;
import com.openexchange.file.storage.webdav.AbstractWebDAVAccountAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFolderAccess;
import com.openexchange.file.storage.webdav.WebDAVFile;
import com.openexchange.file.storage.webdav.WebDAVFileStorageConstants;
import com.openexchange.file.storage.webdav.WebDAVPath;
import com.openexchange.file.storage.webdav.WebDAVUtils;
import com.openexchange.file.storage.webdav.utils.WebDAVEndpointConfig;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVResource;
import com.openexchange.webdav.client.WebDAVXmlBody;

/**
 * {@link OwnCloudFileAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudFileAccess extends AbstractWebDAVFileAccess implements FileStorageVersionedFileAccess, ThumbnailAware, FileStorageZippableFolderFileAccess {

    private static final Logger LOG = LoggerFactory.getLogger(OwnCloudFileAccess.class);

    private static final String NS_OWNCLOUD = "http://owncloud.org/ns";
    private static final String NS_PREFIX_OWNCLOUD = "oc";

    public static final QName OC_FILEID = new QName(NS_OWNCLOUD, "fileid", NS_PREFIX_OWNCLOUD);
    public static final QName OC_SHARE_TYPES = new QName(NS_OWNCLOUD, "share-types", NS_PREFIX_OWNCLOUD);
    public static final QName OC_FAVORITE = new QName(NS_OWNCLOUD, "favorite", NS_PREFIX_OWNCLOUD);
    public static final QName DAV_RESOURCE_TYPE = new QName("DAV:", "resourcetype", "a");

    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 150;

    private String rootUrl;

    /**
     * Initializes a new {@link OwnCloudFileAccess}.
     *
     * @param webdavClient The {@link WebDAVClient}
     * @param account The {@link FileStorageAccount}
     * @param session The user session
     * @param accountAccess The {@link AbstractWebDAVAccountAccess}
     * @param folderAccess The {@link AbstractWebDAVFolderAccess}
     * @throws OXException in case the root url is missing
     */
    public OwnCloudFileAccess(WebDAVClient webdavClient, OwnCloudAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
        if (account.getConfiguration().containsKey(WebDAVFileStorageConstants.WEBDAV_URL)) {
            rootUrl = new WebDAVEndpointConfig.Builder(this.session, accountAccess.getWebDAVFileStorageService(), (String) account.getConfiguration().get(WebDAVFileStorageConstants.WEBDAV_URL)).build().getUrl();
        } else {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(WebDAVFileStorageConstants.ID, WebDAVFileStorageConstants.WEBDAV_URL);
        }
    }

    /**
     * Gets the WebDAV root URL
     *
     * @return The WebDav root URL
     */
    protected String getRootUrl() {
       return rootUrl;
    }


    @Override
    protected Set<QName> getPropertiesToQuery(Collection<Field> requestedFields) {
        Set<QName> props = super.getPropertiesToQuery(requestedFields);
        if (requestedFields == null || requestedFields.contains(Field.COLOR_LABEL)) {
            props.add(OC_FAVORITE);
        }
        props.add(OC_SHARE_TYPES);
        props.add(OC_FILEID);
        return props;
    }

    @Override
    protected WebDAVFile getMetadata(String folderId, String id, String version, List<Field> fields) throws OXException {
        if (CURRENT_VERSION == version) {
            return super.getMetadata(folderId, id, version, fields);
        }
        checkVersioningSupport();
        return getVersionFile(folderId, id, version, fields);
    }

    /**
     * Checks if versioning is supported and throws an exception in case it is not.
     *
     * @throws OXException
     */
    private void checkVersioningSupport() throws OXException {
        Optional<OwnCloudRestClient> rest = getRestAccess();
        String msg = "Versioning is not supported";
        if (rest.orElseThrow(() -> new UnsupportedOperationException(msg)).getCapabilities().supportsVersioning() == false) {
            throw new UnsupportedOperationException(msg);
        }
    }

    /**
     * Returns the given version of the file
     *
     * @param folderId The folder id
     * @param id The id of the file
     * @param version The version
     * @param fields The queried fields
     * @return The {@link WebDAVFile}
     * @throws OXException in case the file doesn't exists or in case of other errors
     */
    private WebDAVFile getVersionFile(String folderId, String id, String version, List<Field> fields) throws OXException {
        if (CURRENT_VERSION == version) {
            return getMetadata(folderId, id, version, fields);
        }
        checkVersioningSupport();
        WebDAVFile master = getMetadata(folderId, id, CURRENT_VERSION, Collections.emptyList());
        String pathStr = getVersionsPath(((OwnCloudFile)master).getFileId());
        WebDAVPath path = new WebDAVPath(URI.create(pathStr + version));

        List<WebDAVResource> resources = client.propFind(path.toString(), 0, getPropertiesToQuery(fields), null);
        WebDAVResource resource = WebDAVUtils.find(resources, path);
        if (resource == null) {
            throw FileStorageExceptionCodes.FILE_VERSION_NOT_FOUND.create(version, id, folderId);
        }
        return getVersionFile(master, resource);
    }

    /**
     * Gets the {@link WebDAVFile} for a version of the master {@link WebDAVFile}
     *
     * @param master The master {@link WebDAVFile}
     * @param r The {@link WebDAVResource} of the version
     * @return The {@link WebDAVFile} for the version
     */
    private WebDAVFile getVersionFile(WebDAVFile master, WebDAVResource r) {
        OwnCloudFile file = new OwnCloudFile(master, r);
        file.setLastModified(r.getModifiedDate());
        file.setCreated(r.getCreationDate());
        file.setFileSize(null != r.getContentLength() ? l(r.getContentLength()) : -1L);
        file.setVersion(r.getHref().substring(r.getHref().lastIndexOf("/") + 1));
        return file;
    }


    @Override
    protected WebDAVFile getWebDAVFile(WebDAVResource resource) throws OXException {
        WebDAVFile webDAVFile = super.getWebDAVFile(resource);
        if (getRestAccess().isPresent()) {
            Optional<OwnCloudEntityResolver> resolver = getResolver();
            if (resolver.isPresent()) {
                String path = resource.getHref().substring(rootPath.toURI().toString().length());
                List<FileStorageObjectPermission> perms = OwnCloudPermissionsUtil.getPermissions(path, resource, getRestAccess().get(), resolver.get());
                webDAVFile.setObjectPermissions(perms);
            }
        }
        return new OwnCloudFile(webDAVFile, resource);
    }

    private Optional<OwnCloudEntityResolver> getResolver() {
        return ((OwnCloudFileStorageService) getAccountAccess().getService()).getResolver();
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        if (CURRENT_VERSION == version) {
            return super.exists(folderId, id, version);
        }
        checkVersioningSupport();
        WebDAVPath path = getVersionPath(folderId, id, version);
        return client.exists(path.toString(), null);
    }

    /**
     * Gets the path to the version
     *
     * @param folderId The folder id of the master file
     * @param id The ox id of the master file
     * @return The path to the version
     * @throws OXException
     */
    private WebDAVPath getVersionPath(String folderId, String id, String version) throws OXException {
        WebDAVFile master = getMetadata(folderId, id, CURRENT_VERSION, Collections.emptyList());
        String path = getVersionsPath(((OwnCloudFile)master).getFileId());
        return new WebDAVPath(URI.create(path + version));
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (CURRENT_VERSION == version) {
            return super.copy(source, version, destFolder, update, newFil, modifiedFields);
        }
        checkVersioningSupport();
        WebDAVFile master = getMetadata(source.getFolder(), source.getId(), CURRENT_VERSION, Collections.emptyList());
        String path = getVersionsPath(((OwnCloudFile)master).getFileId());

        WebDAVPath targetPath;
        if (null == update || Strings.isEmpty(update.getFileName()) || null != modifiedFields && false == modifiedFields.contains(Field.FILENAME)) {
            /*
             * take over target filename from source file
             */
            targetPath = getWebDAVPath(destFolder).append(master.getId(), false);
        } else {
            /*
             * use filename from supplied metadata
             */
            if (Strings.isEmpty(update.getFileName())) {
                throw FileStorageExceptionCodes.MISSING_FILE_NAME.create();
            }
            targetPath = getWebDAVPath(destFolder).append(update.getFileName(), false);
        }
        try {
            client.copy(path, targetPath.toString(), null);
        } catch (OXException e) {
            //            if (indicatesHttpStatus(e, HttpStatus.SC_PRECONDITION_FAILED)) {
            //                throw FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create(e);
            //            }
            throw e;
        }
        IDTuple id = getFileId(targetPath);
        if (null != update) {
            File toUpdate = new DefaultFile(update);
            toUpdate.setFolderId(id.getFolder());
            toUpdate.setId(id.getId());
            saveFileMetadata(toUpdate, DISTANT_FUTURE, modifiedFields);
        }
        return id;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        if (CURRENT_VERSION == version) {
            return super.getDocument(folderId, id, version);
        }
        checkVersioningSupport();
        WebDAVPath path = getVersionPath(folderId, id, version);
        return client.get(path.toString(), null);
    }

    @Override
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        throw new UnsupportedOperationException("Removal of versions is not supported");
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        return getVersions(folderId, id, Arrays.asList(Field.values()));
    }

    /**
     * Creates the versions path for this fileid
     *
     * @param fileId The owncloud/nextcloud fileid
     * @return The versions path for this file
     */
    protected String getVersionsPath(String fileId) {
        return String.format("/remote.php/dav/meta/%s/v/", fileId);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
       return getVersions(folderId, id, fields, null, SortDirection.ASC);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        checkVersioningSupport();
        WebDAVFile master = getMetadata(folderId, id, CURRENT_VERSION, null);
        String fileId = ((OwnCloudFile) master).getFileId();
        Set<QName> props = getPropertiesToQuery(fields);
        List<WebDAVResource> resources = client.propFind(getVersionsPath(fileId), 1, props, null);
        List<File> ret = new ArrayList<>(resources.size());
        for (WebDAVResource r : resources) {
            if (!r.isCollection()) {
                ret.add(getVersionFile(master, r));
            }
        }
        master.setVersion(String.valueOf(-1));
        ret.add(master);
        sort(ret, sort, order);
        return new FileTimedResult(ret);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
       return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    private Optional<OwnCloudRestClient> getRestAccess() {
        return ((OwnCloudAccountAccess) accountAccess).getRestClient();
    }

    /**
     * Performs a file search
     *
     * @param folderId The folderId
     * @param pattern The search term
     * @param start The start value for pagination
     * @param end The end value for pagination
     * @param props The fields to query
     * @return A list of found files
     * @throws OXException
     */
    protected List<File> performSearch(String folderId, String pattern, int start, int end, Set<QName> props) throws OXException {
        WebDAVXmlBody body = new OwnCloudSearchBody(pattern, start, end, props);
        List<File> result = client.<List<File>>report(getWebDAVPath(folderId).toString(), body, (doc) -> {
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


    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        Optional<OwnCloudRestClient> rest = getRestAccess();
        if (rest.isPresent()) {
            int min = rest.get().getCapabilities().getSearchMinLength();
            if (min > pattern.length()) {
                throw FileStorageExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.create(I(min));
            }
        }

        if (isRoot(folderId) == false) {
            return super.search(pattern, fields, folderId, includeSubfolders, sort, order, start, end);
        }
        Set<QName> props = getPropertiesToQuery(fields);
        props.add(DAV_RESOURCE_TYPE);
        List<File> result = performSearch(folderId, pattern, start, end, props);
        return new SearchIteratorAdapter<File>(result.iterator());
    }

    /**
     * Converts search results to a list of files
     *
     * @param responses The responses
     * @return A list of files
     */
    protected List<File> convertToFiles(List<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }
        List<File> result = new ArrayList<>();
        for(Response resp: responses) {
            Optional<Propstat> propstat = resp.getPropstat();
            if (propstat.isPresent() && propstat.get().getFile().get().isCollection() == false) {
                result.add(convertToOwnCloudFile(resp.getHref(), propstat.get().getFile().get()));
            }
        }
        return result;
    }

    /**
     * Converts the given {@link SimpleFile} to an {@link OwnCloudFile}
     *
     * @param href The href of the response
     * @param file The {@link SimpleFile}
     * @return The {@link OwnCloudFile}
     */
    protected OwnCloudFile convertToOwnCloudFile(String href, SimpleFile file) {
        WebDAVPath path = new WebDAVPath(href);
        IDTuple idTuple = getFileId(path);
        WebDAVFile webdavfile = new WebDAVFile();
        webdavfile.setId(idTuple.getId());
        webdavfile.setFolderId(idTuple.getFolder());
        webdavfile.setFileName(path.getName());
        webdavfile.setNumberOfVersions(-1);
        webdavfile.setLastModified(file.getLastModified());
        webdavfile.setEtag(file.getEtag());
        if (file.getSize() != null) {
            webdavfile.setFileSize(file.getSize().intValue());
        }
        return new OwnCloudFile(webdavfile, file.getFileId(), file.getEtag());
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        if (version == CURRENT_VERSION) {
            WebDAVPath path = getWebDAVPath(folderId, id);
            return client.get(addParameter(path.toString(), false, Optional.empty()), null);
        }
        checkVersioningSupport();
        return client.get(addParameter(getVersionPath(folderId, id, version).toString(), false, Optional.empty()), null);
    }

    /**
     * Adds the thumbnail parameters to the string
     *
     * @param path The path
     * @param etag The optional etag
     * @return The path containing the parameters
     */
    private String addParameter(String path, boolean keepAspectRatio, Optional<String> etag) {
        StringBuilder b = new StringBuilder(path);
        if (etag.isPresent()) {
            b.append("?c=").append(etag);
        } else {
            b.append("?");
        }
        return b.append("&a=").append(keepAspectRatio ? 1 : 0).append("&x=").append(THUMBNAIL_WIDTH).append("&y=").append(THUMBNAIL_HEIGHT).append("&forceIcon=0&preview=1").toString();
    }

}
