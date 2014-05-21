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

import static com.openexchange.find.basic.SimpleTokenizer.tokenize;
import static com.openexchange.find.basic.drive.Utils.prepareSearchTerm;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.Autoboxing.I2i;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.infostore.ToInfostoreTermVisitor;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.drive.DriveConstants;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.DriveStrings;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicInfostoreDriver}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class BasicInfostoreDriver extends AbstractModuleSearchDriver {

    private static final Metadata[] DEFAULT_FIELDS = new Metadata[] {
        Metadata.FOLDER_ID_LITERAL, Metadata.META_LITERAL, Metadata.ID_LITERAL, Metadata.LAST_MODIFIED_LITERAL,
        Metadata.TITLE_LITERAL, Metadata.FILENAME_LITERAL, Metadata.FILE_MIMETYPE_LITERAL, Metadata.FILE_SIZE_LITERAL,
        Metadata.LOCKED_UNTIL_LITERAL, Metadata.MODIFIED_BY_LITERAL, Metadata.VERSION_LITERAL };

    /**
     * Initializes a new {@link BasicInfostoreDriver}.
     */
    public BasicInfostoreDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        if (!session.getUserConfiguration().hasInfostore()) {
            return false;
        }
        FileStorageServiceRegistry registry = Services.getFileStorageServiceRegistry();
        if (null == registry) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageServiceRegistry.class.getName());
        }
        List<FileStorageService> services = registry.getAllServices();
        for (FileStorageService service : services) {
            List<FileStorageAccount> accounts = service.getAccountManager().getAccounts(session);
            if (accounts.size() == 0 || accounts.size() > 1) {
                return false;
            }
            if (!"com.openexchange.infostore".equals(service.getId())) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes() {
        return ALL_FOLDER_TYPES;
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        InfostoreSearchEngine searchEngine = Services.getInfostoreSearchEngine();
        if (null == searchEngine) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(InfostoreFacade.class.getName());
        }
        IDBasedFolderAccessFactory folderAccessFactory = Services.getIdBasedFolderAccessFactory();
        if (null == folderAccessFactory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFolderAccessFactory.class.getName());
        }

        List<Integer> folderIds = determineFolderIds(searchRequest, session);
        Metadata[] fields = getFields(searchRequest);
        SearchTerm<?> term = prepareSearchTerm(searchRequest);
        if (term == null) {
            term = new TitleTerm("*", true, true);
        }

        ToInfostoreTermVisitor visitor = new ToInfostoreTermVisitor();
        term.visit(visitor);

        SearchIterator<DocumentMetadata> it = null;
        try {
            final int start = searchRequest.getStart();
            it = searchEngine.search(
                I2i(folderIds),
                visitor.getInfstoreTerm(),
                fields,
                Metadata.TITLE_LITERAL,
                0,
                start,
                start + searchRequest.getSize(),
                session.getContext(),
                session.getUser(),
                session.getUserPermissionBits());
            final List<Document> results = new LinkedList<Document>();
            while (it.hasNext()) {
                final DocumentMetadata doc = it.next();
                results.add(new FileDocument(Utils.documentMetadata2File(doc)));
            }
            return new SearchResult(-1, start, results, searchRequest.getActiveFacets());
        } finally {
            SearchIterators.close(it);
        }
    }

    private List<Integer> determineFolderIds(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Integer> folderIds;
        String requestFolderId = searchRequest.getFolderId();
        if (requestFolderId == null) {
            FolderType folderType = searchRequest.getFolderType();
            if (folderType != null) {
                folderIds = new LinkedList<Integer>();
                int userId = session.getUser().getId();
                SearchIterator<FolderObject> visibleFolders = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
                    userId,
                    session.getUserConfiguration().getGroups(),
                    session.getUserConfiguration().getAccessibleModules(),
                    FolderObject.INFOSTORE,
                    session.getContext());

                while (visibleFolders.hasNext()) {
                    FolderObject folder = visibleFolders.next();
                    if (folder.getType(userId) == folderType.getIntIdentifier()) {
                        folderIds.add(folder.getObjectID());
                    }
                }
            } else {
                folderIds = Collections.emptyList();
            }
        } else {
            try {
                folderIds = Collections.singletonList(Integer.valueOf(requestFolderId));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(requestFolderId, Module.DRIVE.getIdentifier());
            }
        }

        return folderIds;
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        final String prefix = autocompleteRequest.getPrefix();
        final List<Facet> facets = new LinkedList<Facet>();
        if (!prefix.isEmpty()) {
            List<String> prefixTokens = tokenize(prefix);
            // Add simple factes
            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withFormattableDisplayItem(DriveStrings.FACET_GLOBAL, prefix)
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

        // Add static file size facet
        {
            final String fieldFileSize = Constants.FIELD_FILE_SIZE;
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

        // Add static time facet
        {
            final String fieldTime = Constants.FIELD_TIME;
            facets.add(Facets.newExclusiveBuilder(DriveFacetType.TIME)
                .addValue(FacetValue.newBuilder(DriveConstants.FACET_VALUE_LAST_WEEK)
                    .withLocalizableDisplayItem(DriveStrings.LAST_WEEK)
                    .withFilter(Filter.of(fieldTime, DriveConstants.FACET_VALUE_LAST_WEEK))
                    .build())
                .addValue(FacetValue.newBuilder(DriveConstants.FACET_VALUE_LAST_MONTH)
                    .withLocalizableDisplayItem(DriveStrings.LAST_MONTH)
                    .withFilter(Filter.of(fieldTime, DriveConstants.FACET_VALUE_LAST_MONTH))
                    .build())
                .addValue(FacetValue.newBuilder(DriveConstants.FACET_VALUE_LAST_YEAR)
                    .withLocalizableDisplayItem(DriveStrings.LAST_YEAR)
                    .withFilter(Filter.of(fieldTime, DriveConstants.FACET_VALUE_LAST_YEAR))
                    .build())
                .build());
        }

        return new AutocompleteResult(facets);
    }

    private static Metadata[] getFields(SearchRequest searchRequest) {
        Metadata[] fields = DEFAULT_FIELDS;
        int[] columns = searchRequest.getColumns();
        if (columns != null) {
            List<Metadata> tmp = new ArrayList<Metadata>(columns.length);
            for (int c : columns) {
                Metadata field = Metadata.get(c);
                if (field != null) {
                    tmp.add(field);
                }
            }
            fields = tmp.toArray(new Metadata[tmp.size()]);
        }

        return fields;
    }

}
