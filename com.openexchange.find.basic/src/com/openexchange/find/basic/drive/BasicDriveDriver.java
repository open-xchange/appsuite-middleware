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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link BasicDriveDriver}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class BasicDriveDriver extends AbstractModuleSearchDriver {

    private final Field[] fields = new File.Field[] { Field.ID, Field.MODIFIED_BY, Field.LAST_MODIFIED, Field.FOLDER_ID, Field.TITLE,
        Field.FILENAME, Field.FILE_MIMETYPE, Field.FILE_SIZE, Field.VERSION, Field.LOCKED_UNTIL};

    private final IDBasedFileAccessFactory fileAccessFactory;
//    private final IDBasedFolderAccessFactory folderAccessFactory;

    /**
     * Initializes a new {@link BasicDriveDriver}.
     * @throws OXException
     */
    public BasicDriveDriver() throws OXException {
        super();
        fileAccessFactory = Services.getIdBasedFileAccessFactory();
//        folderAccessFactory = Services.getIdBasedFolderAccessFactory();
    }

    @Override
    public boolean isValidFor(ServerSession session) {
        return true;
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        if (null != fileAccess) {
            List<Document> result = new LinkedList<Document>();
            for (Filter filter : searchRequest.getFilters()) {
                SearchTerm<?> searchTerm = Utils.termFor(filter);
                SearchIterator<File> it = fileAccess.search(searchTerm, Arrays.asList(fields), File.Field.TITLE, SortDirection.DEFAULT,
                    FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                while (it.hasNext()) {
                    File file = it.next();
                    result.add(new FileDocument(file));
                }
                return new SearchResult(result.size(), 0, result);
            }
        }
        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFileAccess.class.getName());
    }

    @Override
    public AutocompleteResult doAutocomplete(final AutocompleteRequest autocompleteRequest, final ServerSession session) throws OXException {
        final List<Facet> facets = new LinkedList<Facet>();
        facets.add(new Facet(DriveFacetType.FILE_NAME, getAutocompleteFiles(session, autocompleteRequest)));
        return new AutocompleteResult(facets);
    }

    @Override
    public ModuleConfig getConfiguration(final ServerSession session) {
        Facet folderNameFacet = new FieldFacet(DriveFacetType.FOLDERS, Constants.FIELD_FOLDER_TYPE);
        Facet fileNameFacet = new FieldFacet(DriveFacetType.FILE_NAME, Constants.FIELD_FILE_NAME);
        // Define facets
        final List<Facet> facets = new LinkedList<Facet>();
        facets.add(fileNameFacet);
        facets.add(folderNameFacet);
        return new ModuleConfig(Module.DRIVE, facets);
    }

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    private List<FacetValue> getAutocompleteFiles(ServerSession session, AutocompleteRequest request) throws OXException {
        IDBasedFileAccess access = fileAccessFactory.createAccess(session);
        SearchTerm<String> titleTerm = new TitleTerm(request.getPrefix(), true, true);
        SearchTerm<String> filenameTerm = new FileNameTerm(request.getPrefix(), true, true);
        List<SearchTerm<?>> terms = new LinkedList<SearchTerm<?>>();
        terms.add(titleTerm);
        terms.add(filenameTerm);
        SearchTerm<List<SearchTerm<?>>> orTerm = new OrTerm(terms);
        SearchIterator<File> it = access.search(orTerm, Arrays.asList(fields), Field.TITLE, SortDirection.ASC, FileStorageFileAccess.NOT_SET,
            FileStorageFileAccess.NOT_SET);
        List<FacetValue> facets = new LinkedList<FacetValue>();
        while (it.hasNext()) {
            File file = it.next();
            Filter fileName = new Filter(Collections.singletonList("filename"), file.getFileName());
            if (null != fileName) {
                String facetValue = prepareFacetValueId(request.getPrefix(), session.getContextId(), file.getId());
                facets.add(new FacetValue(facetValue, new SimpleDisplayItem(file.getTitle()), FacetValue.UNKNOWN_COUNT, fileName));
            }
        }
        return facets;
    }

}
