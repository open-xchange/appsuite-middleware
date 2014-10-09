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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FolderID;
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
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicInfostoreDriver}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class BasicInfostoreDriver extends AbstractModuleSearchDriver {

    private static final Logger LOG = LoggerFactory.getLogger(BasicInfostoreDriver.class);

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
        /*
         * only available, if "com.openexchange.infostore" is the only account for the user
         */
        List<FileStorageAccount> allAccounts = new ArrayList<FileStorageAccount>();
        for (FileStorageService service : registry.getAllServices()) {
            allAccounts.addAll(service.getAccountManager().getAccounts(session));
        }
        return 1 == allAccounts.size() && "com.openexchange.infostore".equals(allAccounts.get(0).getFileStorageService().getId());
    }

    @Override
    public boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException {
        if (false == session.getUserConfiguration().hasInfostore()) {
            return false;
        }
        String folderId = findRequest.getFolderId();
        if (null == folderId) {
            return isValidFor(session);
        } else {
            return "com.openexchange.infostore".equals(new FolderID(folderId).getService());
        }
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes() {
        return ALL_FOLDER_TYPES;
    }

    @Override
    public SearchResult doSearch(final SearchRequest searchRequest, final ServerSession session) throws OXException {
        long beforeDetermineFolders = System.currentTimeMillis();
        List<Integer> folderIDs = determineFolderIDs(searchRequest, session);
        LOG.debug("Determined folders in {}ms.", System.currentTimeMillis() - beforeDetermineFolders);

        Metadata[] fields = getFields(searchRequest);
        SearchTerm<?> term = prepareSearchTerm(searchRequest);
        if (term == null) {
            term = new TitleTerm("*", true, true);
        }

        List<Document> results = new InfostoreSearcher(searchRequest, session, term, folderIDs, fields).search();
        return new SearchResult(-1, searchRequest.getStart(), results, searchRequest.getActiveFacets());
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        final String prefix = autocompleteRequest.getPrefix();
        final List<Facet> facets = new LinkedList<Facet>();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (false == Strings.isEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            List<String> prefixTokens = tokenize(prefix, minimumSearchCharacters);
            if (prefixTokens.isEmpty()) {
                prefixTokens = Collections.singletonList(prefix);
            }

            // Add simple factes
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
            final String fieldDate = CommonConstants.FIELD_DATE;
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

        return new AutocompleteResult(facets);
    }

    private static List<Integer> determineAllFolderIDs(ServerSession session) throws OXException {
        return Collections.emptyList();
    }

    /*
     * Returns all folders that are below the users default folder (i.e. "My Files"), including that
     * folder itself, where the user has the necessary permissions.
     */
    private static List<Integer> determinePrivateFolderIDs(ServerSession session) throws OXException {
        final Context context = session.getContext();
        final int userId = session.getUserId();
        final UserConfiguration userConfig = session.getUserConfiguration();
        final OXFolderAccess folderAccess = new OXFolderAccess(context);
        FolderObject infostoreFolder = folderAccess.getDefaultFolder(userId, FolderObject.INFOSTORE);
        FolderFilter filter = new FolderFilter() {
            @Override
            public boolean accept(FolderObject folder) throws OXException {
                EffectivePermission perm = folder.getEffectiveUserPermission(userId, userConfig);
                return perm.isFolderVisible() && perm.canReadOwnObjects();
            }
        };

        List<Integer> folderIDs = new LinkedList<Integer>();
        if (filter.accept(infostoreFolder)) {
            folderIDs.add(infostoreFolder.getObjectID());
        }

        addSubfolderIDs(folderIDs, infostoreFolder, folderAccess, context, filter);
        return folderIDs;
    }

    /*
     * Returns the system-wide public infostore folder and all folders below it, where the user
     * has the necessary permissions.
     */
    private static List<Integer> determinePublicFolderIDs(ServerSession session) throws OXException {
        final Context context = session.getContext();
        final int userId = session.getUserId();
        final UserConfiguration userConfig = session.getUserConfiguration();
        final OXFolderAccess folderAccess = new OXFolderAccess(context);
        FolderObject infostoreFolder = folderAccess.getFolderObject(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        FolderFilter filter = new FolderFilter() {
            @Override
            public boolean accept(FolderObject folder) throws OXException {
                EffectivePermission perm = folder.getEffectiveUserPermission(userId, userConfig);
                return perm.isFolderVisible() && perm.canReadOwnObjects();
            }
        };

        List<Integer> folderIDs = new LinkedList<Integer>();
        if (filter.accept(infostoreFolder)) {
            folderIDs.add(infostoreFolder.getObjectID());
        }

        addSubfolderIDs(folderIDs, infostoreFolder, folderAccess, context, filter);
        return folderIDs;
    }

    /*
     * Returns the system-wide user infostore folder including and folders below it, where the user
     * has the necessary permissions and the folder is shared (i.e. has permissions for multiple users).
     * The users own default folder is excluded. Also all folders are excluded that have been created
     * by the user itself, as they are shared to others but are private in terms of the user.
     */
    private static List<Integer> determineSharedFolderIDs(ServerSession session) throws OXException {
        final Context context = session.getContext();
        final int userId = session.getUserId();
        final UserConfiguration userConfig = session.getUserConfiguration();
        final OXFolderAccess folderAccess = new OXFolderAccess(context);
        final FolderObject userInfostoreFolder = folderAccess.getDefaultFolder(userId, FolderObject.INFOSTORE);
        FolderObject systemInfostoreFolder = folderAccess.getFolderObject(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        FolderFilter filter = new FolderFilter() {
            @Override
            public boolean accept(FolderObject folder) throws OXException {
                if (folder.getNonSystemPermissionsAsArray().length < 2 || folder.getObjectID() == userInfostoreFolder.getObjectID() || folder.getCreatedBy() == userId) {
                    return false;
                }

                EffectivePermission perm = folder.getEffectiveUserPermission(userId, userConfig);
                return perm.isFolderVisible() && perm.canReadOwnObjects();
            }
        };

        List<Integer> folderIDs = new LinkedList<Integer>();
        if (filter.accept(systemInfostoreFolder)) {
            folderIDs.add(systemInfostoreFolder.getObjectID());
        }

        addSubfolderIDs(folderIDs, systemInfostoreFolder, folderAccess, context, filter);
        return folderIDs;
    }

    private static void addSubfolderIDs(List<Integer> folderIDs, FolderObject parent, OXFolderAccess folderAccess, Context context, FolderFilter filter) throws OXException {
        try {
            for (int id : parent.getSubfolderIds(true, context)) {
                FolderObject folder = folderAccess.getFolderObject(id);
                addSubfolderIDs(folderIDs, folder, folderAccess, context, filter);
                if (filter.accept(folder)) {
                    folderIDs.add(id);
                }
            }
        } catch (SQLException e) {
            throw new OXException(e);
        }
    }

    private static interface FolderFilter {
        boolean accept(FolderObject folder) throws OXException;
    }

    /*
     * For performance and complexity reasons, the returned folder ids may not exactly match all folders of the
     * given type. In edge cases folders can be missing or folders can be contained that don't match the type.
     * See the comments within the single 'determine'-methods for details.
     */
    private static List<Integer> determineFolderIDs(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Integer> folderIDs;
        FolderType folderType = searchRequest.getFolderType();
        String requestFolderId = searchRequest.getFolderId();
        if (requestFolderId == null) {
            if (folderType == null) {
                folderIDs = determineAllFolderIDs(session);
            } else {
                switch (folderType) {
                    case PRIVATE:
                        folderIDs = determinePrivateFolderIDs(session);
                        break;

                    case PUBLIC:
                        folderIDs = determinePublicFolderIDs(session);
                        break;

                    case SHARED:
                        folderIDs = determineSharedFolderIDs(session);
                        break;
                    default:
                        folderIDs = Collections.emptyList();
                }
            }
        } else {
            try {
                folderIDs = Collections.singletonList(Integer.valueOf(requestFolderId));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(requestFolderId, Module.DRIVE.getIdentifier());
            }
        }

        return folderIDs;
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
