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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.find.basic.drive;

import static com.openexchange.find.basic.drive.Utils.prepareSearchTerm;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonConstants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.DriveStrings;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.find.spi.SearchConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicDriveDriver}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class BasicDriveDriver extends AbstractModuleSearchDriver {

    private static final List<Field> DEFAULT_FIELDS = new ArrayList<Field>(10);
    static {
        Collections.addAll(DEFAULT_FIELDS,
            Field.FOLDER_ID, Field.META, Field.ID, Field.LAST_MODIFIED,
            Field.TITLE, Field.FILENAME, Field.FILE_MIMETYPE, Field.FILE_SIZE,
            Field.LOCKED_UNTIL, Field.MODIFIED_BY, Field.VERSION);
    }

    /**
     * Initializes a new {@link BasicDriveDriver}.
     */
    public BasicDriveDriver() {
        super();
    }

    @Override
    public boolean isValidFor(final ServerSession session) {
        return session.getUserConfiguration().hasInfostore();
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String serviceId = getAccountAccess(autocompleteRequest, session).getService().getId();
        if (FileID.INFOSTORE_SERVICE_ID.equals(serviceId)) {
            UserPermissionBits userPermissionBits = session.getUserPermissionBits();
            if (userPermissionBits.hasFullSharedFolderAccess()) {
                return ALL_FOLDER_TYPES;
            }

            Set<FolderType> types = EnumSet.noneOf(FolderType.class);
            types.add(FolderType.PRIVATE);
            types.add(FolderType.PUBLIC);
            return types;
        }

        return FOLDER_TYPE_NOT_SUPPORTED;
    }

    @Override
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        SearchConfiguration config = new SearchConfiguration();
        List<FileStorageAccount> accounts = getFileStorageAccounts(session);
        if (accounts.size() > 1) {
            config.setRequiresAccount();
        }
        return config;
    }

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        FileStorageAccountAccess accountAccess = getAccountAccess(autocompleteRequest, session);
        boolean supportsSearchByTerm = supportsSearchByTerm(accountAccess);
        String prefix = autocompleteRequest.getPrefix();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);

        List<Facet> facets = new LinkedList<Facet>();
        if (Strings.isNotEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            List<String> prefixTokens = tokenize(prefix, minimumSearchCharacters);
            if (prefixTokens.isEmpty()) {
                prefixTokens = Collections.singletonList(prefix);
            }

            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(Constants.FIELD_GLOBAL, prefixTokens))
                .build());

            if (supportsSearchByTerm) {
                facets.add(newSimpleBuilder(DriveFacetType.FILE_NAME)
                    .withFormattableDisplayItem(DriveStrings.SEARCH_IN_FILE_NAME, prefix)
                    .withFilter(Filter.of(Constants.FIELD_FILE_NAME, prefixTokens))
                    .build());
                facets.add(newSimpleBuilder(DriveFacetType.FILE_DESCRIPTION)
                    .withFormattableDisplayItem(DriveStrings.SEARCH_IN_FILE_DESC, prefix)
                    .withFilter(Filter.of(Constants.FIELD_FILE_DESC, prefixTokens))
                    .build());
            }
        }

        addFileTypeFacet(facets);

        if (supportsSearchByTerm) {
            addFileSizeFacet(facets);
            addDateFacet(facets);
        }

        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        FileStorageAccountAccess accountAccess = getAccountAccess(searchRequest, session);
        accountAccess.connect();
        try {
            // Determine account-relative folder ID
            String folderId = searchRequest.getFolderId();
            if (folderId != null) {
                FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
                if (!folderAccess.exists(new FolderID(searchRequest.getFolderId()).getFolderId())) {
                    throw FindExceptionCode.INVALID_FOLDER_ID.create(folderId, Module.DRIVE.getIdentifier());
                }
            }

            List<Field> fields = DEFAULT_FIELDS;
            int[] columns = searchRequest.getColumns().getIntColumns();
            if (columns.length > 0) {
                fields = Field.get(columns);
            }

            IDBasedFolderAccess folderAccess = Services.getIdBasedFolderAccessFactory().createAccess(session);
            IDBasedFileAccess fileAccess = Services.getIdBasedFileAccessFactory().createAccess(session);

            if (supportsSearchByTerm(accountAccess)) {
                return advancedSearch(searchRequest, session, accountAccess, folderAccess, fileAccess, fields);
            }

            return simpleSearch(searchRequest, session, accountAccess, folderAccess, fileAccess, fields);
        } finally {
            accountAccess.close();
        }
    }

    private SearchResult advancedSearch(SearchRequest searchRequest, ServerSession session, FileStorageAccountAccess accountAccess, IDBasedFolderAccess folderAccess, IDBasedFileAccess fileAccess, List<Field> fields) throws OXException {
        // Search by term only if supported
        SearchTerm<?> term = prepareSearchTerm(searchRequest);
        if (term == null) {
            term = new TitleTerm("*", true, true);
        }

        String folderID = searchRequest.getFolderId();
        if (Strings.isEmpty(folderID)) {
            if (null != searchRequest.getFolderType()) {
                if (false == FileID.INFOSTORE_SERVICE_ID.equals(accountAccess.getService().getId())) {
                    throw FindExceptionCode.INVALID_FOLDER_TYPE.create(searchRequest.getFolderType());
                }
                String infostoreId;
                switch (searchRequest.getFolderType()) {
                    case PUBLIC:
                        infostoreId = String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
                        break;
                    case SHARED:
                        infostoreId = String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                        break;
                    default:
                        infostoreId = accountAccess.getFolderAccess().getPersonalFolder().getId();
                        break;
                }
                folderID = new FolderID(FileID.INFOSTORE_SERVICE_ID, FileID.INFOSTORE_ACCOUNT_ID, infostoreId).toUniqueID();
            } else if (null != accountAccess) {
                folderID = new FolderID(accountAccess.getService().getId(), accountAccess.getAccountId(), accountAccess.getRootFolder().getId()).toUniqueID();
            } else {
                folderID = FileStorageFileAccess.ALL_FOLDERS;
            }
        }

        List<Document> results = new ArrayList<Document>();
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = fileAccess.search(folderID, true, term, fields, Field.TITLE, SortDirection.DEFAULT, searchRequest.getStart(), searchRequest.getStart() + searchRequest.getSize());
            while (searchIterator.hasNext()) {
                results.add(new FileDocument(searchIterator.next()));
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return new SearchResult(-1, searchRequest.getStart(), results, searchRequest.getActiveFacets());
    }

    private SearchResult simpleSearch(SearchRequest searchRequest, ServerSession session, FileStorageAccountAccess accountAccess, IDBasedFolderAccess folderAccess, IDBasedFileAccess fileAccess, List<Field> fields) throws OXException {
        // Search by simple pattern as fallback and filter folders manually
        List<String> queries = searchRequest.getQueries();
        String pattern = null != queries && 0 < queries.size() ? queries.get(0) : "*";
        String folderID = searchRequest.getFolderId();
        if (Strings.isEmpty(folderID)) {
            if (null != accountAccess) {
                folderID = new FolderID(accountAccess.getService().getId(), accountAccess.getAccountId(), accountAccess.getRootFolder().getId()).toUniqueID();
            } else {
                folderID = FileStorageFileAccess.ALL_FOLDERS;
            }
        }
        List<File> files;
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = fileAccess.search(pattern, fields, folderID, true, File.Field.TITLE, SortDirection.DEFAULT, searchRequest.getStart(), searchRequest.getStart() + searchRequest.getSize());
            files = SearchIterators.asList(searchIterator);
        } finally {
            SearchIterators.close(searchIterator);
        }
        // Filter according to file type facet if defined
        String fileType = extractFileType(searchRequest.getActiveFacets(DriveFacetType.FILE_TYPE));
        if (null != fileType) {
            files = filter(files, fileType);
        }
        List<Document> results = new ArrayList<Document>(files.size());
        for (final File file : files) {
            results.add(new FileDocument(file));
        }
        return new SearchResult(-1, searchRequest.getStart(), results, searchRequest.getActiveFacets());
    }

    private FileStorageAccountAccess getAccountAccess(AbstractFindRequest request, ServerSession session) throws OXException {
        List<FileStorageAccount> accounts = getFileStorageAccounts(session);
        String account = request.getAccountId();
        if (accounts.size() > 1) {
            if (account == null) {
                throw FindExceptionCode.MISSING_MANDATORY_FACET.create(CommonFacetType.ACCOUNT.getId());
            }

            return getAccountAccess(account, session);
        }

        if (account == null) {
            FileStorageAccount defaultAccount = accounts.get(0);
            return defaultAccount.getFileStorageService().getAccountAccess(defaultAccount.getId(), session);
        }

        return getAccountAccess(account, session);
    }

    private FileStorageAccountAccess getAccountAccess(String account, ServerSession session) throws OXException {
        List<String> idParts = IDMangler.unmangle(account);
        if (idParts.size() != 2) {
            throw FindExceptionCode.INVALID_ACCOUNT_ID.create(account, Module.DRIVE.getIdentifier());
        }

        FileStorageService fsService = Services.getFileStorageServiceRegistry().getFileStorageService(idParts.get(0));
        return fsService.getAccountAccess(idParts.get(1), session);
    }

    /**
     * Extracts the file type used in the filter of the supplied active facets.
     *
     * @param fileyTypeFacts The active facets holding the defined file type
     * @return The file type, or <code>null</code> if there is none
     */
    private static String extractFileType(final List<ActiveFacet> facets) {
        if (null != facets && 0 < facets.size() && null != facets.get(0)) {
            final ActiveFacet facet = facets.get(0);
            if (DriveFacetType.FILE_TYPE.equals(facet.getType()) && null != facet.getFilter() && null != facet.getFilter().getQueries() &&
                0 < facet.getFilter().getQueries().size() && null != facet.getFilter().getQueries().get(0)) {
                return facet.getFilter().getQueries().get(0);
            }
        }
        return null;
    }

    /**
     * Filters a list of files based on a specific file type.
     *
     * @param files The files to filter
     * @param fileType The file type identifier
     * @return The filtered list
     */
    private static List<File> filter(final List<File> files, final String fileType) {
        if (null != files && 0 < files.size()) {
            /*
             * determine patterns to check the MIME type against
             */
            List<String> patterns;
            boolean negate;
            if (FileType.OTHER.getIdentifier().equals(fileType)) {
                negate = true;
                patterns = new ArrayList<String>();
                final String[] typesToNegate = new String[] {
                    FileType.AUDIO.getIdentifier(), FileType.IMAGES.getIdentifier(), FileType.DOCUMENTS.getIdentifier(), FileType.VIDEO.getIdentifier()
                };
                for (final String typeToNegate : typesToNegate) {
                    patterns.addAll(getPatternsForFileType(typeToNegate));
                }
            } else {
                negate = false;
                patterns = getPatternsForFileType(fileType);
            }
            /*
             * filter files
             */
            final Iterator<File> iterator = files.iterator();
            while (iterator.hasNext()) {
                final File file = iterator.next();
                if (matchesAny(file, patterns)) {
                    if (negate) {
                        iterator.remove();
                    }
                } else if (false == negate) {
                    iterator.remove();
                }
            }
        }
        return files;
    }

    /**
     * Gets a value indicating whether the supplied file's MIME type matches any of the specified patterns.
     *
     * @param file The file to check
     * @param patterns The patterns to check the file's MIME type against
     * @return <code>true</code> if the file's MIME type matches at least one of the supplied patterns, <code>false</code>, otherwise
     */
    private static boolean matchesAny(final File file, final List<String> patterns) {
        final String mimeType = null != file.getFileMIMEType() ? file.getFileMIMEType() : MimeType2ExtMap.getContentType(file.getFileName());
        for (final String regex : patterns) {
            if (Pattern.matches(regex, mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates patterns to match against filenames based on the file types defined by the supplied active facets.
     *
     * @param fileyType The file type to get the patterns for
     * @return The patterns, or an empty array if there are none
     */
    private static List<String> getPatternsForFileType(final String fileType) {
        String[] wildcardPatterns;
        if (FileType.DOCUMENTS.getIdentifier().equals(fileType)) {
            wildcardPatterns = Constants.FILETYPE_PATTERNS_DOCUMENTS;
        } else if (FileType.VIDEO.getIdentifier().equals(fileType)) {
            wildcardPatterns = Constants.FILETYPE_PATTERNS_VIDEO;
        } else if (FileType.AUDIO.getIdentifier().equals(fileType)) {
            wildcardPatterns = Constants.FILETYPE_PATTERNS_AUDIO;
        } else if (FileType.IMAGES.getIdentifier().equals(fileType)) {
            wildcardPatterns = Constants.FILETYPE_PATTERNS_IMAGES;
        } else {
            wildcardPatterns = new String[0];
        }
        final List<String> patterns = new ArrayList<String>(wildcardPatterns.length);
        for (final String wildcardPattern : wildcardPatterns) {
            patterns.add(Strings.wildcardToRegex(wildcardPattern));
        }
        return patterns;
    }

    private void addFileTypeFacet(List<Facet> facets) {
        String fieldFileType = Constants.FIELD_FILE_TYPE;
        DefaultFacet fileTypeFacet = Facets.newExclusiveBuilder(DriveFacetType.FILE_TYPE)
            .addValue(FacetValue.newBuilder(FileType.AUDIO.getIdentifier())
                .withLocalizableDisplayItem(DriveStrings.FILE_TYPE_AUDIO)
                .withFilter(Filter.of(fieldFileType, FileType.AUDIO.getIdentifier()))
                .build())
            .addValue(FacetValue.newBuilder(FileType.DOCUMENTS.getIdentifier())
                .withLocalizableDisplayItem(DriveStrings.FILE_TYPE_DOCUMENTS)
                .withFilter(Filter.of(fieldFileType, FileType.DOCUMENTS.getIdentifier()))
                .build())
            .addValue(FacetValue.newBuilder(FileType.IMAGES.getIdentifier())
                .withLocalizableDisplayItem(DriveStrings.FILE_TYPE_IMAGES)
                .withFilter(Filter.of(fieldFileType, FileType.IMAGES.getIdentifier()))
                .build())
            .addValue(FacetValue.newBuilder(FileType.OTHER.getIdentifier())
                .withLocalizableDisplayItem(DriveStrings.FILE_TYPE_OTHER)
                .withFilter(Filter.of(fieldFileType, FileType.OTHER.getIdentifier()))
                .build())
            .addValue(FacetValue.newBuilder(FileType.VIDEO.getIdentifier())
                .withLocalizableDisplayItem(DriveStrings.FILE_TYPE_VIDEO)
                .withFilter(Filter.of(fieldFileType, FileType.VIDEO.getIdentifier()))
                .build())
            .build();
        facets.add(fileTypeFacet);
    }

    private void addFileSizeFacet(List<Facet> facets) {
        String fieldFileSize = Constants.FIELD_FILE_SIZE;
        facets.add(Facets.newExclusiveBuilder(DriveFacetType.FILE_SIZE)
            .addValue(FacetValue.newBuilder(FileSize.MB1.getSize())
                .withSimpleDisplayItem(FileSize.MB1.getSize())
                .withFilter(Filter.of(fieldFileSize, FileSize.MB1.getSize()))
                .build())
            .addValue(FacetValue.newBuilder(FileSize.MB10.getSize())
                .withSimpleDisplayItem(FileSize.MB10.getSize())
                .withFilter(Filter.of(fieldFileSize, FileSize.MB10.getSize()))
                .build())
            .addValue(FacetValue.newBuilder(FileSize.MB100.getSize())
                .withSimpleDisplayItem(FileSize.MB100.getSize())
                .withFilter(Filter.of(fieldFileSize, FileSize.MB100.getSize()))
                .build())
            .addValue(FacetValue.newBuilder(FileSize.GB1.getSize())
                .withSimpleDisplayItem(FileSize.GB1.getSize())
                .withFilter(Filter.of(fieldFileSize, FileSize.GB1.getSize()))
                .build())
            .build());
    }

    private void addDateFacet(List<Facet> facets) {
        String fieldDate = CommonConstants.FIELD_DATE;
        facets.add(Facets.newExclusiveBuilder(CommonFacetType.DATE)
            .addValue(FacetValue.newBuilder(CommonConstants.QUERY_LAST_WEEK)
                .withLocalizableDisplayItem(CommonStrings.LAST_WEEK)
                .withFilter(Filter.of(fieldDate, CommonConstants.QUERY_LAST_WEEK))
                .build())
            .addValue(FacetValue.newBuilder(CommonConstants.QUERY_LAST_MONTH)
                .withLocalizableDisplayItem(CommonStrings.LAST_MONTH)
                .withFilter(Filter.of(fieldDate, CommonConstants.QUERY_LAST_MONTH))
                .build())
            .addValue(FacetValue.newBuilder(CommonConstants.QUERY_LAST_YEAR)
                .withLocalizableDisplayItem(CommonStrings.LAST_YEAR)
                .withFilter(Filter.of(fieldDate, CommonConstants.QUERY_LAST_YEAR))
                .build())
            .build());
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    private static boolean supportsSearchByTerm(FileStorageAccountAccess accountAccess) throws OXException {
        if (accountAccess instanceof CapabilityAware) {
            Boolean supported = ((CapabilityAware) accountAccess).supports(FileStorageCapability.SEARCH_BY_TERM);
            if (null != supported) {
                return supported.booleanValue();
            }
        }

        accountAccess.connect();
        try {
            FileStorageFileAccess fileAccess = accountAccess.getFileAccess();
            return FileStorageCapabilityTools.supports(fileAccess, FileStorageCapability.SEARCH_BY_TERM);
        } finally {
            accountAccess.close();
        }
    }

    /**
     * Determines all file storage accounts of the session user.
     *
     * @param session The session
     * @return The list of accounts
     */
    private static List<FileStorageAccount> getFileStorageAccounts(ServerSession session) throws OXException {
        FileStorageServiceRegistry registry = Services.getFileStorageServiceRegistry();
        List<FileStorageService> services = registry.getAllServices();
        List<FileStorageAccount> accounts = new ArrayList<>(services.size());
        for (FileStorageService service : services) {
            List<FileStorageAccount> serviceAccounts = AccountAware.class.isInstance(service) ? ((AccountAware) service).getAccounts(session) : service.getAccountManager().getAccounts(session);
            accounts.addAll(serviceAccounts);
        }
        return accounts;
    }

}
