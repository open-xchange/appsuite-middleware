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

package com.openexchange.ajax.customizer.folder;

import com.openexchange.ajax.customizer.CustomizerFactory;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FolderResponseCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface FolderResponseCustomizer {
    
    public static FolderResponseCustomizer DEFAULT_CUSTOMIZER = new FolderResponseCustomizer() {

        public FolderGetCustomizer getGetCustomizer(ServerSession session) {
            return new EmptyFolderGetCustomizerAdapter();
        }

        public FolderGetPathCustomizer getGetPathCustomizer(ServerSession session) {
            return new EmptyFolderGetPathCustomizerAdapter();
        }

        public FolderRootCustomizer getRootCustomizer(ServerSession session) {
            return new EmptyFolderRootCustomizerAdapter();
        }

        public FolderSubfoldersCustomizer getSubfoldersCustomizer(ServerSession session) {
            return new EmptyFolderSubfoldersCustomizerAdapter();
        }

        public FolderUpdatesCustomizer getUpdatesCustomizer(ServerSession session) {
            return new EmptyFolderUpdatesCustomizerAdapter();
        }
        
    };
    
    public FolderRootCustomizer getRootCustomizer(ServerSession session);
    public FolderSubfoldersCustomizer getSubfoldersCustomizer(ServerSession session);
    public FolderUpdatesCustomizer getUpdatesCustomizer(ServerSession session);
    public FolderGetPathCustomizer getGetPathCustomizer(ServerSession session);
    public FolderGetCustomizer getGetCustomizer(ServerSession session);
    
    public static final class EmptyFolderGetCustomizerAdapter extends FolderGetCustomizerAdapter<EmptyFolderGetCustomizerAdapter> {

        public EmptyFolderGetCustomizerAdapter newInstance(ServerSession session) {
            return new EmptyFolderGetCustomizerAdapter();
        }
    }
    
    public static final class EmptyFolderGetPathCustomizerAdapter extends FolderGetPathCustomizerAdapter<EmptyFolderGetPathCustomizerAdapter> {

        public EmptyFolderGetPathCustomizerAdapter newInstance(ServerSession session) {
            return new EmptyFolderGetPathCustomizerAdapter();
        }
        
    }
    
    public static final class EmptyFolderRootCustomizerAdapter extends FolderRootCustomizerAdapter<EmptyFolderRootCustomizerAdapter> {

        public EmptyFolderRootCustomizerAdapter newInstance(ServerSession session) {
            return new EmptyFolderRootCustomizerAdapter();
        }
        
    }
    
    public static final class EmptyFolderSubfoldersCustomizerAdapter extends FolderSubfoldersCustomizerAdapter<EmptyFolderSubfoldersCustomizerAdapter> {

        public EmptyFolderSubfoldersCustomizerAdapter newInstance(ServerSession session) {
            return new EmptyFolderSubfoldersCustomizerAdapter();
        }
        
    }
    
    public static final class EmptyFolderUpdatesCustomizerAdapter extends FolderUpdatesCustomizerAdapter<EmptyFolderUpdatesCustomizerAdapter> {

        public EmptyFolderUpdatesCustomizerAdapter newInstance(ServerSession session) {
            return new EmptyFolderUpdatesCustomizerAdapter();
        }
        
    }
    
}
