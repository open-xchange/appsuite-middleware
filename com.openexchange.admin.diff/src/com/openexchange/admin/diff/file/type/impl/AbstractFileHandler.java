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

package com.openexchange.admin.diff.file.type.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.admin.diff.file.type.IConfFileHandler;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link AbstractFileHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public abstract class AbstractFileHandler implements IConfFileHandler {

    /**
     * Registered installed files
     */
    protected Map<String, String> installedFiles = new ConcurrentHashMap<String, String>();

    /**
     * Registered original files
     */
    protected Map<String, String> originalFiles = new ConcurrentHashMap<String, String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFile(String fileName, String content, boolean isOriginal) {
        if (isOriginal) {
            if (installedFiles.containsKey(fileName)) {
                // TODO - handle duplicate files System.out.println(fileName + " already in the installed map");
            }
            originalFiles.put(fileName, content);
        } else {
            if (installedFiles.containsKey(fileName)) {
                // TODO - handle duplicate files System.out.println(fileName + " already in the installed map");
            }
            installedFiles.put(fileName, content);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diff) {
        getFileDiffs(diff, new HashMap<String, String>(this.originalFiles), new HashMap<String, String>(this.installedFiles));

        return getDiff(diff, this.originalFiles, this.installedFiles);
    }

    /**
     * Returns the diffs that belong to files. This method is called for each configuration file extension.
     * 
     * @param diff - the diff objet to add file diff results
     * @param lOriginalFiles - original files that should be compared
     * @param lInstalledFiles - installed files the original ones should be compared with
     */
    protected void getFileDiffs(DiffResult diff, final HashMap<String, String> lOriginalFiles, final HashMap<String, String> lInstalledFiles) {

        for (String origFile : lOriginalFiles.keySet()) {
            if (!lInstalledFiles.keySet().contains(origFile)) {
                diff.getMissingFiles().add(origFile);
            }
            lInstalledFiles.remove(origFile);
        }

        if (lInstalledFiles.size() > 0) {
            for (String installedFile : lInstalledFiles.keySet()) {
                diff.getAdditionalFiles().put(installedFile, lInstalledFiles.get(installedFile));
            }
        }
    }
}
