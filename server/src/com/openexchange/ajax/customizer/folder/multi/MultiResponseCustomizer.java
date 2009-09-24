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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.customizer.folder.multi;

import com.openexchange.ajax.customizer.folder.FolderGetCustomizer;
import com.openexchange.ajax.customizer.folder.FolderGetPathCustomizer;
import com.openexchange.ajax.customizer.folder.FolderResponseCustomizer;
import com.openexchange.ajax.customizer.folder.FolderRootCustomizer;
import com.openexchange.ajax.customizer.folder.FolderSubfoldersCustomizer;
import com.openexchange.ajax.customizer.folder.FolderUpdatesCustomizer;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MultiResponseCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MultiResponseCustomizer implements FolderResponseCustomizer {

    private MultiGetCustomizer get = new MultiGetCustomizer();
    private MultiGetPathCustomizer getPath = new MultiGetPathCustomizer();
    private MultiRootCustomizer root = new MultiRootCustomizer();
    private MultiSubfoldersCustomizer subfolders = new MultiSubfoldersCustomizer();
    private MultiUpdatesCustomizer updates = new MultiUpdatesCustomizer();
    
    public FolderGetCustomizer getGetCustomizer(ServerSession session) {
        return get.copyAsNeeded(session);
    }

    public FolderGetPathCustomizer getGetPathCustomizer(ServerSession session) {
        return getPath.copyAsNeeded(session);
    }

    public FolderRootCustomizer getRootCustomizer(ServerSession session) {
        return root.copyAsNeeded(session);
    }

    public FolderSubfoldersCustomizer getSubfoldersCustomizer(ServerSession session) {
        return subfolders.copyAsNeeded(session);
    }

    public FolderUpdatesCustomizer getUpdatesCustomizer(ServerSession session) {
        return updates.copyAsNeeded(session);
    }
    
    public void addFolderGetCustomizer(FolderGetCustomizer customizer) {
        get.addCustomizer(customizer);
    }

    public void addFolderGetPathCustomizer(FolderGetPathCustomizer customizer) {
        getPath.addCustomizer(customizer);
    }

    public void addFolderRootCustomizer(FolderRootCustomizer customizer) {
        root.addCustomizer(customizer);
    }

    public void addFolderSubfoldersCustomizer(FolderSubfoldersCustomizer customizer) {
        subfolders.addCustomizer(customizer);
    }

    public void addFolderUpdatesCustomizer(FolderUpdatesCustomizer customizer) {
        updates.addCustomizer(customizer);
    }
    public void removeFolderGetCustomizer(FolderGetCustomizer customizer) {
        get.removeCustomizer(customizer);
    }

    public void removeFolderGetPathCustomizer(FolderGetPathCustomizer customizer) {
        getPath.removeCustomizer(customizer);
    }

    public void removeFolderRootCustomizer(FolderRootCustomizer customizer) {
        root.removeCustomizer(customizer);
    }

    public void removeFolderSubfoldersCustomizer(FolderSubfoldersCustomizer customizer) {
        subfolders.removeCustomizer(customizer);
    }

    public void removeFolderUpdatesCustomizer(FolderUpdatesCustomizer customizer) {
        updates.removeCustomizer(customizer);
    }
    
}
