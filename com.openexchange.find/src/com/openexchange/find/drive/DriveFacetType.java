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

package com.openexchange.find.drive;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;


/**
 * {@link DriveFacetType} - Facet types for the drive module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DriveFacetType implements FacetType {

    CONTACTS(DriveStrings.FACET_CONTACTS),
    FOLDERS(DriveStrings.FACET_FOLDERS),
    FILE_NAME(null, true),
    FILE_TYPE(DriveStrings.FACET_FILE_TYPE),
    FILE_DESCRIPTION(null, true),
    FILE_CONTENT(null, true),
    FILE_SIZE(DriveStrings.FACET_FILE_SIZE)
    ;

    private static final Map<String, DriveFacetType> typesById = new HashMap<String, DriveFacetType>();
    static {
        for (DriveFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

    private final String displayName;

    private final boolean fieldFacet;

    private DriveFacetType(final String displayName, final boolean fieldFacet) {
        this.displayName = displayName;
        this.fieldFacet = fieldFacet;
    }

    private DriveFacetType(final String displayName) {
        this(displayName, false);
    }

    @Override
    public String getId() {
        return toString().toLowerCase();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean isFieldFacet() {
        return fieldFacet;
    }

    @Override
    public boolean appliesOnce() {
        return false;
    }

    /**
     * Gets a {@link DriveFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static DriveFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }
        return typesById.get(id);
    }

}
