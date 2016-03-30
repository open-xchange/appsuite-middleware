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

package com.openexchange.groupware.infostore.facade.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.Tools;
import com.openexchange.groupware.results.CustomizableDelta;
import com.openexchange.groupware.results.CustomizableTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.Customizer;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MetadataLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class MetadataLoader<T> {

    /**
     * Initializes a new {@link MetadataLoader}.
     */
    protected MetadataLoader() {
        super();
    }

    /**
     * Sets the additional metadata in the supplied document.
     *
     * @param document The document to set the metadata
     * @param metadata The metadata to set
     * @return The document with set metadata
     */
    protected abstract DocumentMetadata set(DocumentMetadata document, T metadata);

    /**
     * Loads additional metadata for multiple documents and puts them into a map, ready to be used for further result processing.
     *
     * @param ids The identifiers of the documents to load the metadata for
     * @param context The context
     * @return A map holding the metadata (or <code>null</code>) to a document's id
     */
    public abstract Map<Integer, T> load(Collection<Integer> ids, Context context) throws OXException;

    /**
     * Loads additional metadata for one document.
     *
     * @param id The identifier of the document to load the metadata for
     * @param context The context
     * @return The metadata (or <code>null</code> if not available)
     */
    public T load(int id, Context context) throws OXException {
        Integer identifier = Integer.valueOf(id);
        return load(Collections.singleton(identifier), context).get(identifier);
    }

    /**
     * Adds additional metadata to the documents in the supplied delta result.
     *
     * @param delta The delta result holding the documents to add the metadata for
     * @param context The context
     * @param knownMetadata A map of known metadata, or <code>null</code> if not available
     * @return A timed result holding the documents with added metadata
     */
    public Delta<DocumentMetadata> add(Delta<DocumentMetadata> delta, Context context, Map<Integer, T> knownMetadata) throws OXException {
        return new CustomizableDelta<DocumentMetadata>(delta, getMetadataCustomizer(context, knownMetadata));
    }

    /**
     * Adds additional metadata to the documents in the supplied timed result.
     *
     * @param timedResult The timed result iterator holding the documents to add the metadata for
     * @param context The context
     * @param knownMetadata A map of known metadata, or <code>null</code> if not available
     * @return A timed result holding the documents with added metadata
     */
    public TimedResult<DocumentMetadata> add(TimedResult<DocumentMetadata> timedResult, Context context, Map<Integer, T> knownMetadata) throws OXException {
        return new CustomizableTimedResult<DocumentMetadata>(timedResult, getMetadataCustomizer(context, knownMetadata));
    }

    /**
     * Adds additional metadata to the documents in the supplied timed result.
     *
     * @param timedResult The timed result iterator holding the documents to add the metadata for
     * @param context The context
     * @param ids The identifiers of the documents in the timed results to pre-fetch the required metadata
     * @return A timed result holding the documents with added metadata
     */
    public TimedResult<DocumentMetadata> add(TimedResult<DocumentMetadata> timedResult, Context context, Collection<Integer> ids) throws OXException {
        return new CustomizableTimedResult<DocumentMetadata>(timedResult, getMetadataCustomizer(context, load(ids, context)));
    }

    /**
     * Adds additional metadata to the documents in the supplied search iterator result.
     *
     * @param searchIterator The search iterator holding the documents to add the metadata for
     * @param context The context
     * @param ids The identifiers of the documents in the search iterator to pre-fetch the required metadata
     * @return A search iterator result holding the documents with added metadata
     */
    public SearchIterator<DocumentMetadata> add(SearchIterator<DocumentMetadata> searchIterator, Context context, Collection<Integer> ids) throws OXException {
        return new CustomizableSearchIterator<DocumentMetadata>(searchIterator, getMetadataCustomizer(context, load(ids, context)));
    }

    /**
     * Adds additional metadata to the supplied document.
     *
     * @param document The document to add the metadata for
     * @param context The context
     * @param knownMetadata A map of known metadata, or <code>null</code> if not available
     * @return The document with added metadata
     */
    public DocumentMetadata add(DocumentMetadata document, Context context, Map<Integer, T> knownMetadata) throws OXException {
        return add(Collections.singletonList(document), context, knownMetadata).get(0);
    }

    /**
     * Adds additional metadata to the supplied documents.
     *
     * @param documents The documents to add the metadata for
     * @param context The context
     * @param ids The identifiers of the documents in the timed results to pre-fetch the required metadata
     * @return The documents with added metadata
     * @throws OXException
     */
    public List<DocumentMetadata> add(List<DocumentMetadata> documents, Context context, Collection<Integer> ids) throws OXException {
        return add(documents, context, load(ids, context));
    }

    /**
     * Adds additional metadata to the supplied documents.
     *
     * @param documents The documents to add the metadata for
     * @param context The context
     * @param knownMetadata A map of known metadata, or <code>null</code> if not available
     * @return The documents with added metadata
     * @throws OXException
     */
    public List<DocumentMetadata> add(List<DocumentMetadata> documents, Context context, Map<Integer, T> knownMetadata) throws OXException {
        /*
         * load required metadata if not available
         */
        if (null == knownMetadata) {
            knownMetadata = load(Tools.getIDs(documents), context);
        }
        /*
         * add metadata to documents
         */
        for (DocumentMetadata document : documents) {
            set(document, knownMetadata.get(Integer.valueOf(document.getId())));
        }
        return documents;
    }

    /**
     * Creates a customizer adding additional metadata to a document, considering known metadata if possible.
     *
     * @param context The context
     * @param knownMetadata A map of known metadata, or <code>null</code> if not available
     * @return The customizer
     */
    private Customizer<DocumentMetadata> getMetadataCustomizer(final Context context, final Map<Integer, T> knownMetadata) {
        return new Customizer<DocumentMetadata>() {

            @Override
            public DocumentMetadata customize(DocumentMetadata thing) throws OXException {
                if (null != thing) {
                    return add(thing, context, knownMetadata);
                }
                return thing;
            }
        };
    }

}
