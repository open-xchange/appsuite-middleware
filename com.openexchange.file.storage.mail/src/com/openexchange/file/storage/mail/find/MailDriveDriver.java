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

package com.openexchange.file.storage.mail.find;

import static com.openexchange.file.storage.mail.MailDriveFileAccess.FETCH_PROFILE_VIRTUAL;
import static com.openexchange.file.storage.mail.sort.MailDriveSortUtility.performEsort;
import static com.openexchange.find.common.CommonConstants.FIELD_DATE;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.IntegerComparisonTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.SubjectTerm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.mail.AbstractMailDriveResourceAccess;
import com.openexchange.file.storage.mail.FullName;
import com.openexchange.file.storage.mail.MailDriveAccountAccess;
import com.openexchange.file.storage.mail.MailDriveConstants;
import com.openexchange.file.storage.mail.MailDriveFile;
import com.openexchange.file.storage.mail.MailDriveFileAccess;
import com.openexchange.file.storage.mail.MailDriveFileStorageService;
import com.openexchange.file.storage.search.ComparisonType;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.common.Comparison;
import com.openexchange.find.basic.drive.Constants;
import com.openexchange.find.basic.drive.FileSize;
import com.openexchange.find.basic.drive.FileType;
import com.openexchange.find.basic.drive.Utils;
import com.openexchange.find.common.CommonConstants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.DriveStrings;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetTypeLookUp;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.find.spi.SearchConfiguration;
import com.openexchange.find.util.TimeFrame;
import com.openexchange.imap.IMAPMessageStorage;
import com.openexchange.java.ConcurrentPriorityQueue;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.tools.session.ServerSession;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.SortTerm;

/**
 * {@link MailDriveDriver}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailDriveDriver extends ServiceTracker<ModuleSearchDriver, ModuleSearchDriver> implements ModuleSearchDriver, FacetTypeLookUp {

    private static final List<Field> DEFAULT_FIELDS = new ArrayList<Field>(10);
    static {
        Collections.addAll(DEFAULT_FIELDS,
            Field.FOLDER_ID, Field.META, Field.ID, Field.LAST_MODIFIED,
            Field.TITLE, Field.FILENAME, Field.FILE_MIMETYPE, Field.FILE_SIZE,
            Field.LOCKED_UNTIL, Field.MODIFIED_BY, Field.VERSION);
    }

    private final MailDriveFileStorageService mailDriveService;
    private final ConcurrentPriorityQueue<RankedService<ModuleSearchDriver>> trackedDrivers;
    private final int myRanking;
    private ServiceRegistration<ModuleSearchDriver> registration; // non-volatile, protected by synchronized blocks

    /**
     * Initializes a new {@link MailDriveDriver}.
     */
    public MailDriveDriver(MailDriveFileStorageService mailDriveService, BundleContext context) {
        super(context, ModuleSearchDriver.class, null);
        trackedDrivers = new ConcurrentPriorityQueue<RankedService<ModuleSearchDriver>>();
        myRanking = ModuleSearchDriver.RANKING_SUPERIOR;
        this.mailDriveService = mailDriveService;
    }

    @Override
    public FacetType facetTypeFor(String id) {
        return MailDriveFacetType.getById(id);
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public synchronized ModuleSearchDriver addingService(ServiceReference<ModuleSearchDriver> reference) {
        int ranking = RankedService.getRanking(reference);
        if (ranking >= myRanking) {
            // Higher or equal ranking... ignore.
            return null;
        }

        ModuleSearchDriver driver = context.getService(reference);
        if (Module.DRIVE != driver.getModule()) {
            // Not a "Drive" driver...
            context.ungetService(reference);
            return null;
        }

        trackedDrivers.offer(new RankedService<ModuleSearchDriver>(driver, ranking));

        if (null == registration) {
            Dictionary<String, Object> props = new Hashtable<>(2);
            props.put(org.osgi.framework.Constants.SERVICE_RANKING, Integer.valueOf(myRanking));
            registration = context.registerService(ModuleSearchDriver.class, this, props);
        }

        return driver;
    }

    @Override
    public void modifiedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        if (Module.DRIVE == driver.getModule()) {
            trackedDrivers.remove(new RankedService<ModuleSearchDriver>(driver, RankedService.getRanking(reference)));

            if (trackedDrivers.isEmpty() && null != registration) {
                registration.unregister();
                registration = null;
            }
        }
        context.ungetService(reference);
    }

    /**
     * Gets the currently available {@code ModuleSearchDriver} instance having the highest rank.
     *
     * @return The highest-ranked {@code ModuleSearchDriver} instance or <code>null</code>
     * @throws OXException If no such service is currently available
     */
    private ModuleSearchDriver delegate() throws OXException {
        RankedService<ModuleSearchDriver> rankedService = trackedDrivers.peek();
        if (null == rankedService) {
            // About to shut-down
            throw FindExceptionCode.UNEXPECTED_ERROR.create("'Drive' search driver is about to shut-down");
        }
        return rankedService.service;
    }

    // ---------------------------------------------------------------------------------------------------------

    private boolean isMailDriveAccount(String accountId) throws OXException {
        if (Strings.isEmpty(accountId)) {
            return false;
        }

        List<String> idParts = IDMangler.unmangle(accountId);
        if (idParts.size() != 2) {
            throw FindExceptionCode.INVALID_ACCOUNT_ID.create(accountId, Module.DRIVE.getIdentifier());
        }

        return (MailDriveConstants.ID.equals(idParts.get(0)) && MailDriveConstants.ACCOUNT_ID.equals(idParts.get(1)));
    }

    private String getMailDriveFolderId(String folderId) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return null;
        }

        FolderID compositeFolderId = new FolderID(folderId);
        if ((!MailDriveConstants.ID.equals(compositeFolderId.getService()) || !MailDriveConstants.ACCOUNT_ID.equals(compositeFolderId.getAccountId()))) {
            throw FindExceptionCode.INVALID_FOLDER_ID.create(folderId, Module.DRIVE.getIdentifier());
        }

        return compositeFolderId.getFolderId();
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return delegate().isValidFor(session);
    }

    @Override
    public boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException {
        return delegate().isValidFor(session, findRequest);
    }

    @Override
    public boolean isValidFor(ServerSession session, List<FacetInfo> facetInfos) throws OXException {
        if (null == facetInfos) {
            return isValidFor(session);
        }

        for (FacetInfo facetInfo : facetInfos) {
            if ("account".equals(facetInfo.getType())) {
                return isMailDriveAccount(facetInfo.getValue());
            }
        }

        return false;
    }

    @Override
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        return delegate().getSearchConfiguration(session);
    }

    @Override
    public AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String accountId = autocompleteRequest.getAccountId();
        if (false == isMailDriveAccount(accountId)) {
            return delegate().autocomplete(autocompleteRequest, session);
        }

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
                .withFilter(Filter.of(MailDriveFindConstants.FIELD_GLOBAL, prefixTokens))
                .build());

            facets.add(newSimpleBuilder(MailDriveFacetType.FILE_NAME)
                .withFormattableDisplayItem(MailDriveStrings.SEARCH_IN_FILE_NAME, prefix)
                .withFilter(Filter.of(MailDriveFindConstants.FIELD_FILE_NAME, prefixTokens))
                .build());
            facets.add(newSimpleBuilder(MailDriveFacetType.FROM)
                .withFormattableDisplayItem(MailDriveStrings.SEARCH_IN_FROM, prefix)
                .withFilter(Filter.of(MailDriveFindConstants.FIELD_FROM, prefixTokens))
                .build());
            facets.add(newSimpleBuilder(MailDriveFacetType.TO)
                .withFormattableDisplayItem(MailDriveStrings.SEARCH_IN_TO, prefix)
                .withFilter(Filter.of(MailDriveFindConstants.FIELD_TO, prefixTokens))
                .build());
            facets.add(newSimpleBuilder(MailDriveFacetType.SUBJECT)
                .withFormattableDisplayItem(MailDriveStrings.SEARCH_IN_SUBJECT, prefix)
                .withFilter(Filter.of(MailDriveFindConstants.FIELD_SUBJECT, prefixTokens))
                .build());
        }

        addFileTypeFacet(facets);

        addFileSizeFacet(facets);
        addDateFacet(facets);

        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        String accountId = searchRequest.getAccountId();
        if (false == isMailDriveAccount(accountId)) {
            return delegate().search(searchRequest, session);
        }

        // Determine the full names to search in
        List<FullName> fullNames;
        {
            String folderId = searchRequest.getFolderId();
            if (null == folderId) {
                fullNames = mailDriveService.getFullNameCollectionFor(session).asList();
            } else {
                // E.g. "maildrive://0/all"
                folderId = getMailDriveFolderId(folderId);
                FullName fullName = MailDriveAccountAccess.optFolderId(folderId, mailDriveService.getFullNameCollectionFor(session));
                if (null == fullName) {
                    throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(folderId, MailDriveConstants.ACCOUNT_ID, MailDriveConstants.ID, session.getUserId(), session.getContextId());
                }
                fullNames = Collections.singletonList(fullName);
            }
        }

        // The search fields
        List<Field> fields = DEFAULT_FIELDS;
        int[] columns = searchRequest.getColumns().getIntColumns();
        if (columns.length > 0) {
            fields = Field.get(columns);
        }
        if (!fields.contains(Field.TITLE)) {
            fields.add(Field.TITLE);
        }

        // Get search term
        SearchTerm searchTerm = prepareSearchTerm(searchRequest);

        // Start/end range
        int start = searchRequest.getStart();
        int end = searchRequest.getStart() + searchRequest.getSize();

        // Establish mail access
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        IMAPMessageStorage messageStorage = null;
        List<File> files = null;
        boolean sorted = false;
        boolean sliced = false;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            messageStorage = AbstractMailDriveResourceAccess.getImapMessageStorageFrom(mailAccess);

            boolean hasEsort;
            {
                Map<String, String> caps = messageStorage.getImapConfig().asMap();
                hasEsort = (caps.containsKey("ESORT") && (caps.containsKey("CONTEXT=SEARCH") || caps.containsKey("CONTEXT=SORT")));
            }

            IMAPStore imapStore = messageStorage.getImapStore();

            int userId = session.getUserId();
            String rootFolderId = MailFolder.DEFAULT_FOLDER_ID;

            files = new LinkedList<File>();
            if (fullNames.size() > 1) {
                // Search over multiple virtual attachments folders
                int realStart = 0; // <-- In case of searching in multiple folders, start must always be 0 (zero).
                for (FullName fullName : fullNames) {
                    IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullName.getFullName());
                    imapFolder.open(Folder.READ_ONLY);
                    try {
                        if (imapFolder.getMessageCount() > 0) {
                            // Check for ESORT
                            Message[] messages;
                            if (hasEsort) {
                                messages = performEsort(getSortTermsBy(searchRequest), searchTerm, realStart, end, imapFolder);

                                // Check if ESORT succeeded
                                if (null == messages) {
                                    messages = imapFolder.search(searchTerm);
                                }
                            } else {
                                messages = imapFolder.search(searchTerm);
                            }

                            // Fetch messages
                            imapFolder.fetch(messages, FETCH_PROFILE_VIRTUAL);

                            int i = 0;
                            for (int k = messages.length; k-- > 0;) {
                                IMAPMessage message = (IMAPMessage) messages[i++];
                                long uid = message.getUID();
                                if (uid < 0) {
                                    uid = imapFolder.getUID(message);
                                }
                                MailDriveFile mailDriveFile = MailDriveFile.parse(message, fullName.getFolderId(), Long.toString(uid), userId, rootFolderId, fields);
                                if (null != mailDriveFile) {
                                    mailDriveFile.setId(new FileID(com.openexchange.file.storage.mail.MailDriveConstants.ID, com.openexchange.file.storage.mail.MailDriveConstants.ACCOUNT_ID, fullName.getFolderId(), Long.toString(uid)).toUniqueID());
                                    mailDriveFile.setFolderId(new FolderID(com.openexchange.file.storage.mail.MailDriveConstants.ID, com.openexchange.file.storage.mail.MailDriveConstants.ACCOUNT_ID, fullName.getFolderId()).toUniqueID());
                                    files.add(mailDriveFile);
                                }
                            }
                        }
                    } finally {
                        imapFolder.close(false);
                    }
                }
            } else {
                // Only one virtual attachments folder to search in
                FullName fullName = fullNames.get(0);
                IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullName.getFullName());
                imapFolder.open(Folder.READ_ONLY);
                try {
                    if (imapFolder.getMessageCount() > 0) {
                        // Check for ESORT
                        Message[] messages;
                        if (hasEsort) {
                            messages = performEsort(getSortTermsBy(searchRequest), searchTerm, start, end, imapFolder);

                            // Check if ESORT succeeded
                            if (null == messages) {
                                messages = imapFolder.search(searchTerm);
                            } else {
                                sorted = true;
                                sliced = true;
                            }
                        } else {
                            messages = imapFolder.search(searchTerm);
                        }

                        // Fetch messages
                        imapFolder.fetch(messages, FETCH_PROFILE_VIRTUAL);

                        int i = 0;
                        for (int k = messages.length; k-- > 0;) {
                            IMAPMessage message = (IMAPMessage) messages[i++];
                            long uid = message.getUID();
                            if (uid < 0) {
                                uid = imapFolder.getUID(message);
                            }
                            MailDriveFile mailDriveFile = MailDriveFile.parse(message, fullName.getFolderId(), Long.toString(uid), userId, rootFolderId, fields);
                            if (null != mailDriveFile) {
                                mailDriveFile.setId(new FileID(com.openexchange.file.storage.mail.MailDriveConstants.ID, com.openexchange.file.storage.mail.MailDriveConstants.ACCOUNT_ID, fullName.getFolderId(), Long.toString(uid)).toUniqueID());
                                mailDriveFile.setFolderId(new FolderID(com.openexchange.file.storage.mail.MailDriveConstants.ID, com.openexchange.file.storage.mail.MailDriveConstants.ACCOUNT_ID, fullName.getFolderId()).toUniqueID());
                                files.add(mailDriveFile);
                            }
                        }
                    }
                } finally {
                    imapFolder.close(false);
                }
            }
        } catch (MessagingException e) {
            throw messageStorage.handleMessagingException(e);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            MailAccess.closeInstance(mailAccess);
        }

        // Empty?
        if (files.isEmpty()) {
            return new SearchResult(-1, searchRequest.getStart(), Collections.<Document> emptyList(), searchRequest.getActiveFacets());
        }

        // Check whether to sort manually
        if (false == sorted) {
            MailDriveFileAccess.sort(files, Field.TITLE, SortDirection.DEFAULT);
        }

        // Check whether to slice manually
        if (false == sliced) {
            int size = files.size();
            if ((start) > size) {
                // Out of range
                files = Collections.emptyList();
            } else {
                // Reset end index if out of range
                int toIndex = end;
                if (toIndex >= size) {
                    toIndex = size;
                }
                files = files.subList(start, toIndex);
            }

            // Empty after slicing?
            if (files.isEmpty()) {
                return new SearchResult(-1, searchRequest.getStart(), Collections.<Document> emptyList(), searchRequest.getActiveFacets());
            }
        }


        List<Document> results = new ArrayList<Document>(files.size());
        for (File file : files) {
            results.add(new FileDocument(file));
        }
        return new SearchResult(-1, searchRequest.getStart(), results, searchRequest.getActiveFacets());
    }

    private SortTerm[] getSortTermsBy(SearchRequest searchRequest) {
        /*-
         * Sort attachments by name
         * SORT (SUBJECT) ...
         *
         * Sort attachments by received date
         * SORT (ARRIVAL) ...
         *
         * Sort attachments by size
         * SORT (SIZE) ...
         */

        SortTerm sortTerm = SortTerm.SUBJECT;
        return new SortTerm[] {sortTerm};
    }

    // ------------------------------------------------ Facets stuff ----------------------------------------------------------------- //

    private void addFileTypeFacet(List<Facet> facets) {
        String fieldFileType = MailDriveFindConstants.FIELD_FILE_TYPE;
        DefaultFacet fileTypeFacet = Facets.newExclusiveBuilder(MailDriveFacetType.FILE_TYPE)
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
        String fieldFileSize = MailDriveFindConstants.FIELD_FILE_SIZE;
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

    // ------------------------------------------------ Parsing stuff ----------------------------------------------------------------- //

    private static enum OP {
        AND, OR
    }

    private static Pair<OP, OP> operationsFor(MailDriveFacetType type) {
        OP fieldOP = OP.OR;
        OP queryOP = OP.OR;
        switch (type) {
            case FILE_NAME:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            case FROM:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            case SUBJECT:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            case TO:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            default:
                break;
        }

        return new Pair<OP, OP>(fieldOP, queryOP);
    }

    private SearchTerm prepareSearchTerm(SearchRequest searchRequest) throws OXException {
        List<SearchTerm> facetTerms = new LinkedList<SearchTerm>();

        for (MailDriveFacetType facetType : MailDriveFacetType.values()) {
            List<ActiveFacet> facets = searchRequest.getActiveFacets(facetType);
            if (facets != null && !facets.isEmpty()) {
                Pair<OP, OP> ops = operationsFor(facetType);

                List<Filter> filters = new LinkedList<Filter>();
                for (ActiveFacet facet : facets) {
                    Filter filter = facet.getFilter();
                    if (filter != Filter.NO_FILTER) {
                        filters.add(filter);
                    }
                }

                facetTerms.add(prepareFilterTerm(filters, ops.getFirst(), ops.getSecond()));
            }
        }

        List<ActiveFacet> dateFacets = searchRequest.getActiveFacets(CommonFacetType.DATE);
        if (dateFacets != null && !dateFacets.isEmpty()) {
            ActiveFacet dateFacet = dateFacets.get(0);
            Filter dateFilter = dateFacet.getFilter();
            if (dateFilter == Filter.NO_FILTER) {
                String timeFramePattern = dateFacet.getValueId();
                TimeFrame timeFrame = TimeFrame.valueOf(timeFramePattern);
                if (timeFrame == null) {
                    throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(timeFramePattern, FIELD_DATE);
                }

                Comparison fromComparison;
                Comparison toComparison;
                if (timeFrame.isInclusive()) {
                    fromComparison = Comparison.GREATER_EQUALS;
                    toComparison = Comparison.LOWER_EQUALS;
                } else {
                    fromComparison = Comparison.GREATER_THAN;
                    toComparison = Comparison.LOWER_THAN;
                }

                long from = timeFrame.getFrom();
                long to = timeFrame.getTo();
                if (to < 0L) {
                    facetTerms.add(buildDateTerm(fromComparison, from));
                }

                SearchTerm fromTerm = buildDateTerm(fromComparison, from);
                SearchTerm toTerm = buildDateTerm(toComparison, to);
                facetTerms.add(new AndTerm(fromTerm, toTerm));
            } else {
                facetTerms.add(prepareFilterTerm(Collections.singletonList(dateFilter), OP.OR, OP.OR));
            }
        }

        SearchTerm queryTerm = prepareQueryTerm(searchRequest.getQueries());
        SearchTerm facetTerm = null;
        if (!facetTerms.isEmpty()) {
            if (facetTerms.size() == 1) {
                facetTerm = facetTerms.get(0);
            } else {
                facetTerm = newJunctorTermFor(OP.AND, facetTerms);
            }
        }

        if (facetTerm == null || queryTerm == null) {
            return (facetTerm == null) ? queryTerm : facetTerm;
        }

        return new AndTerm(queryTerm, facetTerm);
    }

    private SearchTerm prepareQueryTerm(List<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return termFor(MailDriveFindConstants.QUERY_FIELDS, queries, OP.OR, OP.AND);
    }

    private SearchTerm prepareFilterTerm(List<Filter> filters, OP fieldOP, OP queryOP) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        int size = filters.size();
        if (size == 1) {
            return termFor(filters.get(0), fieldOP, queryOP);
        }

        List<SearchTerm> terms = new ArrayList<SearchTerm>(size);
        for (final Filter filter : filters) {
            terms.add(termFor(filter, fieldOP, queryOP));
        }
        return new AndTerm(terms.toArray(new SearchTerm[terms.size()]));
    }

    private SearchTerm termFor(Filter filter, OP fieldOP, OP queryOP) throws OXException {
        if (null == filter) {
            return null;
        }

        final List<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        final List<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries, fieldOP, queryOP);
    }

    private SearchTerm termFor(List<String> fields, List<String> queries, OP fieldOP, OP queryOP) throws OXException {
        int size = fields.size();
        if (size > 1) {
            List<SearchTerm> terms = new ArrayList<SearchTerm>(size);
            for (String field : fields) {
                SearchTerm term = termForField(field, queries, queryOP);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            return newJunctorTermFor(fieldOP, terms);
        }

        return termForField(fields.iterator().next(), queries, queryOP);
    }

    private SearchTerm termForField(String field, List<String> queries, OP queryOP) throws OXException {
        int size = queries.size();
        if (size > 1) {
            List<SearchTerm> terms = new ArrayList<SearchTerm>(size);
            for (String query : queries) {
                SearchTerm term = termForQuery(field, query);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            return newJunctorTermFor(queryOP, terms);
        }

        return termForQuery(field, queries.iterator().next());
    }

    private SearchTerm termForQuery(String field, String query) throws OXException {
        if (Strings.isEmpty(field) || Strings.isEmpty(query)) {
            return null;
        }

        /*-
         * Search for attachments
         * by filename: SEARCH SUBJECT ...
         * by sender: SEARCH FROM ...
         * by subject: SEARCH HEADER X-Original-Subject ...
         * by type
         * SEARCH HEADER Content-Type ...
         * or e.g. SEARCH BODYSTRUCTURE (TYPE ... SUBTYPE ...)
         * by size: SEARCH LARGER ... SMALLER ...
         * by received date: SEARCH SINCE ... BEFORE ..
         */

        if (MailDriveFindConstants.FIELD_GLOBAL.equals(field)) {
            List<SearchTerm> terms = new ArrayList<SearchTerm>(4);
            terms.add(new SubjectTerm(query));
            terms.add(new FromStringTerm(query));
            terms.add(new RecipientStringTerm(RecipientType.TO, query));
            return new OrTerm(terms.toArray(new SearchTerm[terms.size()]));
        } else if (MailDriveFindConstants.FIELD_FILE_NAME.equals(field)) {
            return new SubjectTerm(query);
        } else if (MailDriveFindConstants.FIELD_FROM.equals(field)) {
            return new FromStringTerm(query);
        } else if (MailDriveFindConstants.FIELD_TO.equals(field)) {
            return new RecipientStringTerm(RecipientType.TO, query);
        } else if (MailDriveFindConstants.FIELD_SUBJECT.equals(field)) {
            return new HeaderTerm("X-Original-Subject", query);
        } else if (MailDriveFindConstants.FIELD_FILE_TYPE.equals(field)) {
            return buildFileTypeTerm(query);
        } else if (MailDriveFindConstants.FIELD_FILE_SIZE.equals(field)) {
            long numberOfBytes = Utils.parseFilesizeQuery(query);
            int comparison = parseComparisonType(query);
            return new SizeTerm(comparison, (int) numberOfBytes);
        } else if (CommonConstants.FIELD_DATE.equals(field)) {
            Pair<Comparison, Long> pair = Utils.parseDateQuery(query);
            return buildDateTerm(pair.getFirst(), pair.getSecond().longValue());
        }
        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

    private int parseComparisonType(String query) throws OXException {
        ComparisonType comparisonType = Utils.parseComparisonType(query);
        switch (comparisonType) {
            case EQUALS:
                return IntegerComparisonTerm.EQ;
            case GREATER_THAN:
                return IntegerComparisonTerm.GT;
            case LESS_THAN:
                return IntegerComparisonTerm.LT;
            default:
                throw FindExceptionCode.PARSING_ERROR.create(query);
        }
    }

    private SearchTerm buildFileTypeTerm(String query) {
        String[] patterns;
        if (FileType.DOCUMENTS.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_DOCUMENTS;
        } else if (FileType.IMAGES.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_IMAGES;
        } else if (FileType.VIDEO.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_VIDEO;
        } else if (FileType.AUDIO.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_AUDIO;
        } else if (FileType.OTHER.getIdentifier().equals(query)) {
            // negate all other patterns
            String[][] patternsToNegate = {
                Constants.FILETYPE_PATTERNS_DOCUMENTS,
                Constants.FILETYPE_PATTERNS_IMAGES,
                Constants.FILETYPE_PATTERNS_VIDEO,
                Constants.FILETYPE_PATTERNS_AUDIO
            };
            List<SearchTerm> searchTerms = new ArrayList<SearchTerm>();
            for (String[] toNegate : patternsToNegate) {
                for (String pattern : toNegate) {
                    searchTerms.add(new HeaderTerm("Content-Type", pattern));
                }
            }
            return new NotTerm(newJunctorTermFor(OP.OR, searchTerms));
        } else {
            patterns = null;
        }
        if (null == patterns || 0 == patterns.length) {
            return new HeaderTerm("Content-Type", query); // fall back to query
        } else if (1 == patterns.length) {
            return new HeaderTerm("Content-Type", patterns[0]);
        } else {
            List<SearchTerm> searchTerms = new ArrayList<SearchTerm>(patterns.length);
            for (String pattern : patterns) {
                searchTerms.add(new HeaderTerm("Content-Type", pattern));
            }
            return newJunctorTermFor(OP.OR, searchTerms);
        }
    }

    private SearchTerm buildDateTerm(Comparison comparison, long timestamp) {
        switch (comparison) {
            case EQUALS:
                return new ReceivedDateTerm(DateTerm.EQ, new Date(timestamp));
            case GREATER_EQUALS:
                return new ReceivedDateTerm(DateTerm.GE, new Date(timestamp));
            case GREATER_THAN:
                return new ReceivedDateTerm(DateTerm.GT, new Date(timestamp));
            case LOWER_EQUALS:
                return new ReceivedDateTerm(DateTerm.LE, new Date(timestamp));
            case LOWER_THAN:
                return new ReceivedDateTerm(DateTerm.LT, new Date(timestamp));
            default:
                return null;
        }
    }

    private SearchTerm newJunctorTermFor(OP op, List<SearchTerm> terms) {
        if (op == OP.OR) {
            return new OrTerm(terms.toArray(new SearchTerm[terms.size()]));
        }
        return new AndTerm(terms.toArray(new SearchTerm[terms.size()]));
    }

}
