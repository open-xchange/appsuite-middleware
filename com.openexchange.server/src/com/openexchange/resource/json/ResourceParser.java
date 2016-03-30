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

package com.openexchange.resource.json;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceParser} - Parses a {@link Resource resource} out of a JSON object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceParser {

    /**
     * Initializes a new {@link ResourceParser}
     */
    private ResourceParser() {
        super();
    }

    /**
     * Parses a {@link Resource resource} out of given JSON object
     *
     * @param jsonResource The JSON object containing the resource's data
     * @return The parsed {@link Resource resource}
     * @throws JSONException If reading from JSON object fails
     */
    public static Resource parseResource(final JSONObject jsonResource) throws JSONException {
        final Resource retval = new Resource();
        if (jsonResource.has(ResourceFields.ID) && !jsonResource.isNull(ResourceFields.ID)) {
            retval.setIdentifier(jsonResource.getInt(ResourceFields.ID));
        }
        if (jsonResource.has(ResourceFields.NAME) && !jsonResource.isNull(ResourceFields.NAME)) {
            retval.setSimpleName(jsonResource.getString(ResourceFields.NAME));
        }
        if (jsonResource.has(ResourceFields.DISPLAY_NAME) && !jsonResource.isNull(ResourceFields.DISPLAY_NAME)) {
            retval.setDisplayName(jsonResource.getString(ResourceFields.DISPLAY_NAME));
        }
        if (jsonResource.has(ResourceFields.MAIL) && !jsonResource.isNull(ResourceFields.MAIL)) {
            retval.setMail(jsonResource.getString(ResourceFields.MAIL));
        }
        if (jsonResource.has(ResourceFields.AVAILABILITY) && !jsonResource.isNull(ResourceFields.AVAILABILITY)) {
            retval.setAvailable(jsonResource.getBoolean(ResourceFields.AVAILABILITY));
        }
        if (jsonResource.has(ResourceFields.DESCRIPTION) && !jsonResource.isNull(ResourceFields.DESCRIPTION)) {
            retval.setDescription(jsonResource.getString(ResourceFields.DESCRIPTION));
        }
        return retval;
    }

}
