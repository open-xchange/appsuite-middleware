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

package com.openexchange.drive.impl.actions;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.file.storage.File;

/**
 * {@link DynamicMetadataFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DynamicMetadataFileAction extends AbstractFileAction {

    protected final SyncSession session;
    private File metadata;
    private AbstractFileAction dependingAction;

    /**
     * Initializes a new {@link DynamicMetadataFileAction}. The initial meta data is taken from the newFile parameter if possible.
     *
     * @param session The sync session
     * @param file The file
     * @param newFile the new file
     * @param comparison The comparison
     * @param path The path
     */
    public DynamicMetadataFileAction(SyncSession session, FileVersion file, ServerFileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path) {
        this(session, file, newFile, comparison, path, null != newFile ? newFile.getFile() : null);
    }

    /**
     * Initializes a new {@link DynamicMetadataFileAction}.
     *
     * @param session The sync session
     * @param file The file
     * @param newFile The new file
     * @param comparison The comparison
     * @param path The path
     * @param serverFile The server file to get the initial metadata from
     */
    public DynamicMetadataFileAction(SyncSession session, FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, File serverFile) {
        super(file, newFile, comparison);
        parameters.put(PARAMETER_PATH, path);
        this.session = session;
        this.metadata = serverFile;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = super.getParameters();
        /*
         * overwrite metadata with dependent action if available
         */
        if (null != this.dependingAction) {
            FileVersion fileVersion = dependingAction.getResultingVersion();
            if (null != fileVersion && ServerFileVersion.class.isInstance(fileVersion)) {
                this.metadata = ((ServerFileVersion)fileVersion).getFile();
            }
        }
        /*
         * inject current metadata to parameters
         */
        if (null != this.metadata) {
            parameters = new HashMap<String, Object>(parameters);
            applyMetadataParameters(metadata, session);
        }
        return parameters;
    }

    /**
     * Sets the depending action
     *
     * @param action The depending action to set
     */
    public void setDependingAction(AbstractFileAction action) {
        this.dependingAction = action;
    }

}

