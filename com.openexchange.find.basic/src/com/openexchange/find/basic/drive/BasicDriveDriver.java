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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FileStorageCapability;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
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
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceExceptionCode;
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
    public boolean isValidFor(ServerSession session) {
        return session.getUserConfiguration().hasInfostore();
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes() {
        return FOLDER_TYPE_NOT_SUPPORTED;
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
        if (null == fileAccessFactory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccessFactory.class.getName());
        }

        // Create file access
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);

        // Folder identifier
        String folderId = searchRequest.getFolderId();

        // Fields
        int start = searchRequest.getStart();
        List<Field> fields = DEFAULT_FIELDS;
        int[] columns = searchRequest.getColumns().getIntColumns();
        if (columns.length > 0) {
            fields = Field.get(columns);
        }

        // Search by term only if supported
        if (supportsSearchByTerm(session, fileAccess, searchRequest)) {

            // Yield search term from search request
            SearchTerm<?> term = prepareSearchTerm(searchRequest);
            if (term == null) {
                term = new TitleTerm("*", true, true);
            }

            // Search...
            List<String> folderIds = null != folderId ? Collections.singletonList(folderId) : null;
            SearchIterator<File> it = null;
            try {
                it = fileAccess.search(folderIds, term, fields, Field.TITLE, SortDirection.DEFAULT, start, start + searchRequest.getSize());
                List<Document> results = new LinkedList<Document>();
                while (it.hasNext()) {
                    results.add(new FileDocument(it.next()));
                }
                return new SearchResult(-1, start, results, searchRequest.getActiveFacets());
            } finally {
                SearchIterators.close(it);
                fileAccess.finish();
            }
        }

        // Search by simple pattern as fallback
        List<String> queries = searchRequest.getQueries();
        String pattern = null != queries && 0 < queries.size() ? queries.get(0) : "*";
        List<File> files = new LinkedList<File>();
        SearchIterator<File> it = null;
        try {
            it = fileAccess.search(pattern, fields, folderId, File.Field.TITLE, SortDirection.DEFAULT, start, start + searchRequest.getSize());
            while (it.hasNext()) {
                files.add(it.next());
            }
        } finally {
            SearchIterators.close(it);
            fileAccess.finish();
        }

        // Filter according to file type facet if defined
        String fileType = extractFileType(searchRequest.getActiveFacets(DriveFacetType.FILE_TYPE));
        if (null != fileType) {
            files = filter(files, fileType);
        }
        List<Document> results = new ArrayList<Document>(files.size());
        for (File file : files) {
            results.add(new FileDocument(file));
        }
        return new SearchResult(-1, start, results, searchRequest.getActiveFacets());
    }

    /**
     * Extracts the file type used in the filter of the supplied active facets.
     *
     * @param fileyTypeFacts The active facets holding the defined file type
     * @return The file type, or <code>null</code> if there is none
     */
    private static String extractFileType(List<ActiveFacet> facets) {
        if (null != facets && 0 < facets.size() && null != facets.get(0)) {
            ActiveFacet facet = facets.get(0);
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
    private static List<File> filter(List<File> files, String fileType) {
        if (null != files && 0 < files.size()) {
            /*
             * determine patterns to check the MIME type against
             */
            List<String> patterns;
            boolean negate;
            if (FileType.OTHER.getIdentifier().equals(fileType)) {
                negate = true;
                patterns = new ArrayList<String>();
                String[] typesToNegate = new String[] {
                    FileType.AUDIO.getIdentifier(), FileType.IMAGES.getIdentifier(), FileType.DOCUMENTS.getIdentifier(), FileType.VIDEO.getIdentifier()
                };
                for (String typeToNegate : typesToNegate) {
                    patterns.addAll(getPatternsForFileType(typeToNegate));
                }
            } else {
                negate = false;
                patterns = getPatternsForFileType(fileType);
            }
            /*
             * filter files
             */
            Iterator<File> iterator = files.iterator();
            while (iterator.hasNext()) {
                File file = iterator.next();
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
    private static boolean matchesAny(File file, List<String> patterns) {
        String mimeType = null != file.getFileMIMEType() ? file.getFileMIMEType() : MimeType2ExtMap.getContentType(file.getFileName());
        for (String regex : patterns) {
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
    private static List<String> getPatternsForFileType(String fileType) {
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
        List<String> patterns = new ArrayList<String>(wildcardPatterns.length);
        for (String wildcardPattern : wildcardPatterns) {
            patterns.add(Strings.wildcardToRegex(wildcardPattern));
        }
        return patterns;
    }

    @Override
    public AutocompleteResult doAutocomplete(final AutocompleteRequest autocompleteRequest, final ServerSession session) throws OXException {
        // The auto-complete prefix
        final String prefix = autocompleteRequest.getPrefix();

        // List of supported facets
        final List<Facet> facets = new LinkedList<Facet>();

        if (!prefix.isEmpty()) {
            List<String> prefixTokens = tokenize(prefix);
            // Add global facet
            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(Constants.FIELD_GLOBAL, prefixTokens))
                .build());

            // Add filename/type/content facets if search by term is supported
            if (supportsSearchByTerm(session, null, autocompleteRequest)) {
                facets.add(newSimpleBuilder(DriveFacetType.FILE_NAME)
                    .withFormattableDisplayItem(DriveStrings.SEARCH_IN_FILE_NAME, prefix)
                    .withFilter(Filter.of(Constants.FIELD_FILE_NAME, prefixTokens))
                    .build());
                facets.add(newSimpleBuilder(DriveFacetType.FILE_DESCRIPTION)
                    .withFormattableDisplayItem(DriveStrings.SEARCH_IN_FILE_DESC, prefix)
                    .withFilter(Filter.of(Constants.FIELD_FILE_DESC, prefixTokens))
                    .build());
                facets.add(newSimpleBuilder(DriveFacetType.FILE_CONTENT)
                    .withFormattableDisplayItem(DriveStrings.SEARCH_IN_FILE_CONTENT, prefix)
                    .withFilter(Filter.of(Constants.FIELD_FILE_CONTENT, prefixTokens))
                    .build());
            }
        }
        // Add static file type facet
        {
            final String fieldFileType = Constants.FIELD_FILE_TYPE;
            final DefaultFacet fileTypeFacet = Facets.newExclusiveBuilder(DriveFacetType.FILE_TYPE)
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

        return new AutocompleteResult(facets);
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    private List<FacetValue> getAutocompleteFiles(ServerSession session, AutocompleteRequest request) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
        if (null == fileAccessFactory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccessFactory.class.getName());
        }

        // Create file access
        IDBasedFileAccess access = fileAccessFactory.createAccess(session);

        // Compose term
        String prefix = request.getPrefix();
        List<SearchTerm<?>> terms = new LinkedList<SearchTerm<?>>();
        terms.add(new TitleTerm(prefix, true, true));
        terms.add(new FileNameTerm(prefix, true, true));
        terms.add(new DescriptionTerm(prefix, true, true));
        SearchTerm<List<SearchTerm<?>>> orTerm = new OrTerm(terms);

        // Fire search
        SearchIterator<File> it = null;
        try {
            it = access.search(Collections.<String> emptyList(), orTerm, DEFAULT_FIELDS, Field.TITLE, SortDirection.ASC, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
            List<FacetValue> facets = new LinkedList<FacetValue>();
            while (it.hasNext()) {
                File file = it.next();
                Filter fileName = new Filter(Collections.singletonList("filename"), file.getFileName());
                String facetValue = prepareFacetValueId(request.getPrefix(), session.getContextId(), file.getId());
                facets.add(new FacetValue(facetValue, new SimpleDisplayItem(file.getTitle()), FacetValue.UNKNOWN_COUNT, fileName));
            }
            return facets;
        } finally {
            SearchIterators.close(it);
            access.finish();
        }
    }

    /**
     * Gets a value indicating whether the "search by term" capability is available based on the parameters of the supplied find request.
     *
     * @param session The current session
     * @param idBasedFileAccess An existing file access reference, or <code>null</code> if not available
     * @param findRequest The find request
     * @return <code>true</code> if searching by term is supported, <code>false</code>, otherwise
     */
    private static boolean supportsSearchByTerm(ServerSession session, IDBasedFileAccess idBasedFileAccess, AbstractFindRequest findRequest) throws OXException {
        /*
         * check capability of all concrete file storage if folder ID is specified
         */
        IDBasedFileAccess fileAccess = idBasedFileAccess;
        if (null != findRequest.getFolderId()) {
            FolderID folderID = new FolderID(findRequest.getFolderId());
            if (null != fileAccess) {
                return fileAccess.supports(folderID.getService(), folderID.getAccountId(), FileStorageCapability.SEARCH_BY_TERM);
            }
            IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
            if (null == fileAccessFactory) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccessFactory.class.getName());
            }
            fileAccess = fileAccessFactory.createAccess(session);
            try {
                return fileAccess.supports(folderID.getService(), folderID.getAccountId(), FileStorageCapability.SEARCH_BY_TERM);
            } finally {
                fileAccess.finish();
            }
        }
        /*
         * check capability of all available storages, otherwise
         */
        FileStorageServiceRegistry registry = Services.getFileStorageServiceRegistry();
        if (null == registry) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageServiceRegistry.class.getName());
        }
        boolean closeFileAccess = false;
        if (null == fileAccess) {
            IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
            if (null == fileAccessFactory) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccessFactory.class.getName());
            }
            fileAccess = fileAccessFactory.createAccess(session);
            closeFileAccess = true;
        }
        try {
            for (FileStorageService service : registry.getAllServices()) {
                List<FileStorageAccount> accounts;
                if (AccountAware.class.isInstance(service)) {
                    accounts = ((AccountAware) service).getAccounts(session);
                } else {
                    accounts = service.getAccountManager().getAccounts(session);
                }
                for (FileStorageAccount account : accounts) {
                    if (false == fileAccess.supports(service.getId(), account.getId(), FileStorageCapability.SEARCH_BY_TERM)) {
                        return false;
                    }
                }
            }
        } finally {
            if (closeFileAccess) {
                fileAccess.finish();
            }
        }
        return true;
    }

}
