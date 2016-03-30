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

package com.openexchange.servlet;

import javax.servlet.Filter;


/**
 * {@link Constants} - Common {@link Servlet} constants.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public interface Constants {

    /**
     * Additional property used when registering {@link Filter} services to specify the paths that a {@link Filter} should be applied to.
     * <p>
     * This property may consist of path expressions including wildcards. The path property should be provided as:
     * <ol>
     *   <li>A single String for a single path</li>
     *   <li>An array of Strings</li>
     *   <li>A Collection of of Objects that provides the path via invocation of <cod>toString()</code></li>
     * </ol>
     * if the filter.path property is missing/null the filter will be used for every incoming request.
     * </p>
     * <p>
     * The form of a path must be one of:
     * <ol>
     *   <li><strong>*</strong>: This filter will be applied to all request</li>
     *   <li>The path starts with <strong>/</strong> and ends with the <strong>/*</strong> wildcard but doesn't equal <strong>/*</strong> e.g.
     *   <strong>/a/b/*</strong>: This filter will be used for requests to all URLs starting with <strong>/a/b</strong> e.g
     *   <strong>/a/b/c</strong>, <strong>/a/b/c/d</strong> and so on</li>
     * <li>The path starts with <strong>/</strong> but doesn't end with the <strong>/*</strong> wildcard: This filter will only be used for
     * requests that match this path exactly</li>
     * </ol>
     * </p>
     */
    public static final String FILTER_PATHS = "filter.paths";

}
