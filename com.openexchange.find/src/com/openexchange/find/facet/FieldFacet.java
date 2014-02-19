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

package com.openexchange.find.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.find.common.SimpleDisplayItem;


/**
 * A {@link FieldFacet} is a special facet that has no meaningful values and provides
 * only a single filter. The facet generally references a logical field like
 * 'phone number'. Internally this logical field can map to several internal fields
 * (e.g. 'phone_private', 'phone_mobile', 'phone_business'). In clients the facet as
 * a whole can be displayed as a single item. Example: "Search for 'term' in field 'phone
 * number'".
 *
 * The queries of the facets filter contain only a single placeholder value. The client
 * takes care of providing a valid query during search time.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class FieldFacet extends Facet {

    private static final long serialVersionUID = -5699454846328204928L;

    private static final String QUERY_PLACEHOLDER = "override";

    public FieldFacet(final FacetType type, final String filterField) {
        super(type, buildValues(type, Collections.singleton(filterField)));
    }

    public FieldFacet(final FacetType type, final Set<String> filterFields) {
        super(type, buildValues(type, filterFields));
    }

    private static List<FacetValue> buildValues(FacetType type, Set<String> filterFields) {
        ArrayList<FacetValue> values = new ArrayList<FacetValue>(1);
        Filter filter = new Filter(filterFields, Collections.singleton(QUERY_PLACEHOLDER));
        values.add(new FacetValue(
            type.getName(),
            new SimpleDisplayItem(type.getName()),
            FacetValue.UNKNOWN_COUNT,
            filter));
        return values;
    }
}
