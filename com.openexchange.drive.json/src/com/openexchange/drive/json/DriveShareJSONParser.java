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

package com.openexchange.drive.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.share.json.actions.ShareJSONParser;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DriveShareJSONParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DriveShareJSONParser extends ShareJSONParser {

    private static final String DIRECTORY_VERSIONS = "directoryVersions";
    private static final String FILE_VERSIONS = "fileVersions";

    /**
     * Initializes a new {@link DriveShareJSONParser}.
     */
    public DriveShareJSONParser() {
        super(Services.get());
    }

    /**
     * Parses a list of drive share targets from the supplied JSON object.
     *
     * @param jsonTargets The JSON array holding the share targets
     * @return The share targets
     */
    public List<DriveShareTarget> parseTargets(JSONObject jsonTargets) throws OXException, JSONException {
        List<DriveShareTarget> targets = new ArrayList<DriveShareTarget>();

        if (jsonTargets.has(DIRECTORY_VERSIONS) && jsonTargets.getJSONArray(DIRECTORY_VERSIONS).length() != 0) {
            targets.addAll(parseDirectoryTargets(jsonTargets.getJSONArray(DIRECTORY_VERSIONS)));
        }

        if (jsonTargets.has(FILE_VERSIONS) && jsonTargets.getJSONArray(FILE_VERSIONS).length() != 0) {
            targets.addAll(parseFileTargets(jsonTargets.getJSONArray(FILE_VERSIONS)));
        }

        return targets;
    }

    /**
     * Parses a list of drive share targets containing directories from the supplied JSON Array.
     *
     * @param jsonTargets The JSON targets
     * @return The drive share targets
     */
    public List<DriveShareTarget> parseDirectoryTargets(JSONArray jsonTargets) throws OXException, JSONException {
        if (jsonTargets == null || jsonTargets.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(DIRECTORY_VERSIONS);
        }

        List<DriveShareTarget> targets = new ArrayList<DriveShareTarget>();

        for (int i = 0; i < jsonTargets.length(); i++) {
            JSONObject jsonTarget = jsonTargets.getJSONObject(i);
            targets.add(parseTarget(jsonTarget));
        }

        return targets;
    }

    /**
     * Parses a list of drive share targets containing files from the supplied JSON Array.
     *
     * @param jsonTargets The JSON targets
     * @return The drive share targets
     */
    public List<DriveShareTarget> parseFileTargets(JSONArray jsonTargets) throws OXException, JSONException {
        if (jsonTargets == null || jsonTargets.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(FILE_VERSIONS);
        }

        List<DriveShareTarget> targets = new ArrayList<DriveShareTarget>();

        for (int i = 0; i < jsonTargets.length(); i++) {
            JSONObject jsonTarget = jsonTargets.getJSONObject(i);
            if (!jsonTarget.hasAndNotNull("name")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
            }

            targets.add(parseTarget(jsonTarget));
        }

        return targets;
    }

    /**
     * Parses a drive share target from the supplied JSON Object.
     *
     * @param jsonTarget The JSON target
     * @return The drive share target
     */
    @Override
    public DriveShareTarget parseTarget(JSONObject jsonTarget) throws OXException {
        try {
            if (!jsonTarget.hasAndNotNull("path")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }

            if (!jsonTarget.hasAndNotNull("checksum")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
            }

            DriveShareTarget target = new DriveShareTarget();
            target.setDrivePath(jsonTarget.getString("path"));
            target.setChecksum(jsonTarget.getString("checksum"));

            if (jsonTarget.hasAndNotNull("name")) {
                target.setName(jsonTarget.getString("name"));
            }
            return target;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
