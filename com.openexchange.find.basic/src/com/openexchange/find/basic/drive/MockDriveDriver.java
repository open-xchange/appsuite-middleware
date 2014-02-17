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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import static com.openexchange.java.Strings.isEmpty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.DriveStrings;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.drive.FileTypeDisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.MandatoryFilter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MockDriveDriver}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MockDriveDriver extends AbstractContactFacetingModuleSearchDriver {

    /**
     * Initializes a new {@link MockDriveDriver}.
     */
    public MockDriveDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    @Override
    public boolean isValidFor(final ServerSession session) throws OXException {
        return session.getUserPermissionBits().hasInfostore();
    }

    @Override
    public ModuleConfig getConfiguration(final ServerSession session) throws OXException {
        // Define facets
        final List<Facet> staticFacets = new LinkedList<Facet>();
        {
            final FacetValue staticFacetValue = new FacetValue("file_name", new SimpleDisplayItem("file_name"), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_name"), "override"));
            final Facet fileNameFacet = new Facet(DriveFacetType.FILE_NAME, Collections.singletonList(staticFacetValue));
            staticFacets.add(fileNameFacet);
        }
        {
            final FacetValue staticFacetValue = new FacetValue("file_description", new SimpleDisplayItem("file_description"), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_description"), "override"));
            final Facet fileNameFacet = new Facet(DriveFacetType.FILE_DESCRIPTION, Collections.singletonList(staticFacetValue));
            staticFacets.add(fileNameFacet);
        }
        {
            final FacetValue staticFacetValue = new FacetValue("file_content", new SimpleDisplayItem("file_content"), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_content"), "override"));
            final Facet fileNameFacet = new Facet(DriveFacetType.FILE_CONTENT, Collections.singletonList(staticFacetValue));
            staticFacets.add(fileNameFacet);
        }
        {
            final List<FacetValue> fileTypes = new ArrayList<FacetValue>(6);
            fileTypes.add(new FacetValue("audio", new FileTypeDisplayItem(DriveStrings.FILE_TYPE_AUDIO, FileTypeDisplayItem.Type.AUDIO), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_type"), FileTypeDisplayItem.Type.AUDIO.getIdentifier())));
            fileTypes.add(new FacetValue("documents", new FileTypeDisplayItem(DriveStrings.FILE_TYPE_DOCUMENTS, FileTypeDisplayItem.Type.DOCUMENTS), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_type"), FileTypeDisplayItem.Type.DOCUMENTS.getIdentifier())));
            fileTypes.add(new FacetValue("images", new FileTypeDisplayItem(DriveStrings.FILE_TYPE_IMAGES, FileTypeDisplayItem.Type.IMAGES), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_type"), FileTypeDisplayItem.Type.IMAGES.getIdentifier())));
            fileTypes.add(new FacetValue("other", new FileTypeDisplayItem(DriveStrings.FILE_TYPE_OTHER, FileTypeDisplayItem.Type.OTHER), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_type"), FileTypeDisplayItem.Type.OTHER.getIdentifier())));
            fileTypes.add(new FacetValue("video", new FileTypeDisplayItem(DriveStrings.FILE_TYPE_VIDEO, FileTypeDisplayItem.Type.VIDEO), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("file_type"), FileTypeDisplayItem.Type.VIDEO.getIdentifier())));
            final Facet folderTypeFacet = new Facet(DriveFacetType.FILE_TYPE, fileTypes);
            staticFacets.add(folderTypeFacet);
        }
        {
            final List<FacetValue> folderTypes = new ArrayList<FacetValue>(4);
            folderTypes.add(new FacetValue("private", new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PRIVATE, FolderTypeDisplayItem.Type.PRIVATE), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("folder_type"), FolderTypeDisplayItem.Type.PRIVATE.getIdentifier())));
            folderTypes.add(new FacetValue("public", new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PUBLIC, FolderTypeDisplayItem.Type.PUBLIC), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("folder_type"), FolderTypeDisplayItem.Type.PUBLIC.getIdentifier())));
            folderTypes.add(new FacetValue("shared", new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_SHARED, FolderTypeDisplayItem.Type.SHARED), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("folder_type"), FolderTypeDisplayItem.Type.SHARED.getIdentifier())));
            folderTypes.add(new FacetValue("external", new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_EXTERNAL, FolderTypeDisplayItem.Type.EXTERNAL), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singleton("folder_type"), FolderTypeDisplayItem.Type.EXTERNAL.getIdentifier())));
            final Facet folderTypeFacet = new Facet(CommonFacetType.FOLDER_TYPE, folderTypes);
            staticFacets.add(folderTypeFacet);
        }

        // Define mandatory filters
        final List<MandatoryFilter> mandatoryFilters = Collections.emptyList();

        // Return configuration
        return new ModuleConfig(Module.DRIVE, staticFacets, mandatoryFilters);
    }

    private static final Set<String> PERSONS_FILTER_FIELDS = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList("created_from","changed_from","author")));

    @Override
    public AutocompleteResult doAutocomplete(final AutocompleteRequest autocompleteRequest, final ServerSession session) throws OXException {
        final List<Facet> facets = new LinkedList<Facet>();

        // Add the facet for contacts that needs to be auto-completed
        {
            final List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
            final List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
            for (final Contact contact : contacts) {
                // Get appropriate E-Mail address
                String sInfo = contact.getEmail1();
                if (isEmpty(sInfo)) {
                    sInfo = contact.getEmail2();
                    if (isEmpty(sInfo)) {
                        sInfo = contact.getEmail3();
                    }
                }
                if (sInfo != null) {
                    final Filter filter = new Filter(PERSONS_FILTER_FIELDS, sInfo);
                    contactValues.add(new FacetValue(
                        prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())),
                        new ContactDisplayItem(contact),
                        FacetValue.UNKNOWN_COUNT,
                        filter));
                }

                // Get display name
                sInfo = contact.getDisplayName();
                if (!isEmpty(sInfo)) {
                    final Filter filter = new Filter(PERSONS_FILTER_FIELDS, sInfo);
                    contactValues.add(new FacetValue(
                        prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())),
                        new ContactDisplayItem(contact),
                        FacetValue.UNKNOWN_COUNT,
                        filter));
                }
            }
            facets.add(new Facet(DriveFacetType.CONTACTS, contactValues));
        }

        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult search(final SearchRequest searchRequest, final ServerSession session) throws OXException {
        final IDBasedFileAccessFactory factory = Services.requireService(IDBasedFileAccessFactory.class);
        final Field sortingField = Field.TITLE;
        final SortDirection sortingOrder = SortDirection.ASC;
        final IDBasedFileAccess fileAccess = factory.createAccess(session);

        SearchIterator<File> results = null;
        try {
            results = fileAccess.search(
                "*",
                new ArrayList<Field>(File.DEFAULT_SEARCH_FIELDS),
                FileStorageFileAccess.ALL_FOLDERS,
                sortingField,
                sortingOrder,
                searchRequest.getStart(),
                searchRequest.getStart() + searchRequest.getSize()
            );

            if (Field.CREATED_BY.equals(sortingField)) {
                final CreatedByComparator comparator = new CreatedByComparator(session.getUser().getLocale(), session.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
                results = CreatedByComparator.resort(results, comparator);
            }

            List<Document> documents = new LinkedList<Document>();
            while (results.hasNext()) {
                documents.add(new FileDocument(results.next()));
            }

            // TODO: Does ui need the numFound value? Could become expensive to implement here
            return new SearchResult(-1, searchRequest.getStart(), documents);
        } finally {
            SearchIterators.close(results);
        }
    }

}
