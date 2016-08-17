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

package com.openexchange.http.grizzly.service.http;

import java.util.Arrays;
import javax.servlet.Filter;

/**
 * {@link FilterProxy} - FilterProxy object that holds the values of configured paths and ranking associated with a ServletFilter.
 * Those values are read from the ServiceReference of a Filter service once it gets added to the OSGI runtime. The paths are used to decide
 * if a Filter should be used for an incoming request and the ranking decides the position of the filter in a chain.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a> JavaDoc
 * @since v7.6.1
 */
public class FilterProxy {

    /** The filter paths. */
    private final String[] paths;

    /** Optional filter. */
    private final Filter filter;

    /** The service ranking */
    private final int ranking;

    /**
     * Initializes a new {@link FilterProxy}.
     *
     * @param filter The filter
     * @param paths The filter paths
     * @param ranking The filter ranking
     */
    public FilterProxy(Filter filter, String[] paths, int ranking) {
        super();
        this.filter = filter;
        this.paths = paths;
        this.ranking = ranking;
    }

    /**
     * Gets the ranking
     *
     * @return The ranking
     */
    public int getRanking() {
        return ranking;
    }

    /**
     * Gets the paths
     *
     * @return The paths
     */
    public String[] getPaths() {
        return paths;
    }

    /**
     * Gets the filter
     *
     * @return The filter
     */
    public Filter getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        return "FilterProxy [paths=" + Arrays.toString(paths) + ", filter=" + filter + ", ranking=" + ranking + "]";
    }

}
