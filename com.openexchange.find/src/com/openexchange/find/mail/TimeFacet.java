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

package com.openexchange.find.mail;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class TimeFacet extends ExclusiveFacet {

    private static final long serialVersionUID = -8066952323635426860L;

    public final static String FILTER_FIELD = "time";

    public final static String LAST_WEEK = "last_week";

    public final static String LAST_MONTH = "last_month";

    public final static String LAST_YEAR = "last_year";

    private static List<FacetValue> DEFAULT_VALUES = new ArrayList<FacetValue>(3);
    static {
        DEFAULT_VALUES.add(new FacetValue(
            LAST_WEEK,
            new SimpleDisplayItem(MailStrings.LAST_WEEK, true),
            FacetValue.UNKNOWN_COUNT,
            Filter.with(FILTER_FIELD, LAST_WEEK)));
        DEFAULT_VALUES.add(new FacetValue(
            LAST_MONTH,
            new SimpleDisplayItem(MailStrings.LAST_MONTH, true),
            FacetValue.UNKNOWN_COUNT,
            Filter.with(FILTER_FIELD, LAST_MONTH)));
        DEFAULT_VALUES.add(new FacetValue(
            LAST_YEAR,
            new SimpleDisplayItem(MailStrings.LAST_YEAR, true),
            FacetValue.UNKNOWN_COUNT,
            Filter.with(FILTER_FIELD, LAST_YEAR)));
    }

    public TimeFacet() {
        super(MailFacetType.TIME, DEFAULT_VALUES);
    }

}
