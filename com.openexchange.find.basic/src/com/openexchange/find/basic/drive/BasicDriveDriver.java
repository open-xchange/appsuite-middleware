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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
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
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
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
    public SearchResult doSearch(final SearchRequest searchRequest, final ServerSession session) throws OXException {
        final IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
        if (null == fileAccessFactory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccessFactory.class.getName());
        }

        // Create file access
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);

        // Yield search term from search request
        SearchTerm<?> term = prepareSearchTerm(searchRequest);
        if (term == null) {
            term = new TitleTerm("*", true, true);
        }

        // Folder identifiers
        final String folderId = searchRequest.getFolderId();
        List<String> folderIds;
        if (folderId == null) {
            folderIds = Collections.emptyList();
        } else {
            folderIds = Collections.singletonList(folderId);
        }

        // Fields
        List<Field> fields = DEFAULT_FIELDS;
        int[] columns = searchRequest.getColumns();
        if (columns != null) {
            fields = Field.get(columns);
        }

        // Search...
        SearchIterator<File> it = null;
        try {
            final int start = searchRequest.getStart();
            it = fileAccess.search(folderIds, term, fields, File.Field.TITLE, SortDirection.DEFAULT, start, start + searchRequest.getSize());
            final List<Document> results = new LinkedList<Document>();
            while (it.hasNext()) {
                final File file = it.next();
                results.add(new FileDocument(file));
            }
            return new SearchResult(-1, start, results, searchRequest.getActiveFacets());
        } finally {
            SearchIterators.close(it);
        }
    }

    @Override
    public AutocompleteResult doAutocomplete(final AutocompleteRequest autocompleteRequest, final ServerSession session) throws OXException {
        // The auto-complete prefix
        final String prefix = autocompleteRequest.getPrefix();

        // List of supported facets
        final List<Facet> facets = new LinkedList<Facet>();

        if (!prefix.isEmpty()) {
            List<String> prefixTokens = tokenize(prefix);
            // Add simple facets
            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(Constants.FIELD_GLOBAL, prefixTokens))
                .build());
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
        final IDBasedFileAccessFactory fileAccessFactory = Services.getIdBasedFileAccessFactory();
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
        }
    }

}
