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

package com.openexchange.drive;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link DriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveAction<T extends DriveVersion> extends Comparable<DriveAction<T>> {

    static final String PARAMETER_PATH = "path";
    static final String PARAMETER_MODIFIED = "modified";
    static final String PARAMETER_CREATED = "created";
    static final String PARAMETER_TOTAL_LENGTH = "totalLength";
    static final String PARAMETER_OFFSET = "offset";
    static final String PARAMETER_CONTENT_TYPE = "contentType";
    static final String PARAMETER_ERROR = "error";
    static final String PARAMETER_QUARANTINE = "quarantine";
    static final String PARAMETER_RESET = "reset";
    static final String PARAMETER_LENGTH = "length";
    static final String PARAMETER_STOP = "stop";
    static final String PARAMETER_ACKNOWLEDGE = "acknowledge";
    static final String PARAMETER_ROOT = "root";

    static final String PARAMETER_DIRECT_LINK = "directLink";
    static final String PARAMETER_DIRECT_LINK_FRAGMENTS = "directLinkFragments";
    static final String PARAMETER_PREVIEW_LINK = "previewLink";
    static final String PARAMETER_THUMBNAIL_LINK = "thumbnailLink";

    static final String PARAMETER_DATA = "data";

    static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
        PARAMETER_PATH, PARAMETER_TOTAL_LENGTH, PARAMETER_OFFSET, PARAMETER_CONTENT_TYPE, PARAMETER_ERROR, PARAMETER_QUARANTINE,
        PARAMETER_MODIFIED, PARAMETER_CREATED, PARAMETER_RESET, PARAMETER_LENGTH, PARAMETER_STOP, PARAMETER_ACKNOWLEDGE,
        PARAMETER_DIRECT_LINK, PARAMETER_DIRECT_LINK_FRAGMENTS, PARAMETER_PREVIEW_LINK, PARAMETER_THUMBNAIL_LINK, PARAMETER_DATA,
        PARAMETER_ROOT
    })));

    /**
     * Gets the action.
     *
     * @return The action
     */
    Action getAction();

    /**
     * Gets the version.
     *
     * @return The version, or <code>null</code> if not applicable
     */
    T getVersion();

    /**
     * Gets the new version.
     *
     * @return The new version, or <code>null</code> if not applicable
     */
    T getNewVersion();

    /**
     * Gets a map of additional parameters; possible parameters are defined in {@link DriveAction#PARAMETER_NAMES}.
     *
     * @return The parameters map
     */
    Map<String, Object> getParameters();

}

