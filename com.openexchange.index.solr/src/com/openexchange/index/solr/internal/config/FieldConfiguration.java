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

package com.openexchange.index.solr.internal.config;

import java.util.Set;

import com.openexchange.index.IndexField;


/**
 * {@link FieldConfiguration} - A configuration object that should be used to map between
 * the fields of OX objects and there counterparts within the solr schema. Especially it takes
 * care about fields that are localized (i.e. a language detection is performed at index-time).
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface FieldConfiguration {

    /**
     * @param indexField The index field.
     * @return <code>True</code>, if this field is localized in Solr.
     */
    boolean isLocalized(IndexField indexField);

    /**
     * Returns a set of field names that belong to this index field in the solr schema.
     * If the field is localized, all language fields are contained as well as the fallback field.
     * If the field is not localized, the set contains the solr field name according to this
     * index field. May return <code>null</code> if the field is not defined in the solr schema.
     *
     * @param indexField The index field.
     * @return The set of solr field names.
     */
    Set<String> getSolrFields(IndexField indexField);

    /**
     * Returns a set of fields that are indexed (i.e. you can search within these fields).
     * @return The set of indexed fields.
     */
    Set<? extends IndexField> getIndexedFields();

    /**
     * Returns the solr fields name that identifies a document within the index.
     * @return The field name.
     */
    String getUUIDField();

    /**
     * Gets the index field according to a given solr field name.
     * May return <code>null</code> if the field is not part of the solr schema
     * or if there is no according index field.
     *
     * @param solrField The solr field name.
     * @return The index field.
     */
    IndexField getIndexField(String solrField);

    /**
     * Gets the solr field name for a given index field.
     * If the field is localized in solr, the generic field name will be returned.
     * This is especially needed for input documents.
     *
     * @param indexField The index field.
     * @return The solr field name.
     */
    String getRawField(IndexField indexField);

}
