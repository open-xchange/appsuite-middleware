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

package com.openexchange.fitnesse.environment;

import java.util.List;
import com.openexchange.ajax.folder.tree.FolderNode;
import com.openexchange.ajax.folder.tree.FolderNodeVisitor;
import com.openexchange.ajax.folder.tree.RegularFolderNode;
import com.openexchange.ajax.folder.tree.RootNode;
import com.openexchange.fitnesse.FitnesseEnvironment;
import com.openexchange.fitnesse.SlimTableTable;
import com.openexchange.fitnesse.folders.FolderResolver;

import static com.openexchange.fitnesse.wrappers.FitnesseResult.green;
import static fitnesse.util.ListUtility.list;
/**
 * {@link ShowFolders}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ShowFolders implements SlimTableTable {

    public List doTable(List<List<String>> table) throws Exception {
        FitnesseEnvironment env = FitnesseEnvironment.getInstance();
            
        if (table.size() > 0) {
            FolderResolver resolver = new FolderResolver(env.getClient(), env);
            int folderId = resolver.getFolderId(table.get(0).get(0));
            return list(list(green(dump(new RegularFolderNode(folderId, env.getClient())))));
        } else {
            return list(list(green(dump(new RootNode(env.getClient())))));
        }
    }

    private String dump(FolderNode node) {
        final StringBuilder b = new StringBuilder();
        node.recurse(new FolderNodeVisitor() {

            public void visit(int depth, FolderNode folder) {
                for(int i = 0; i < depth; i++) {
                    b.append("-");
                }
                if(folder.isRoot()) {
                    b.append("ROOT");
                } else if(null != folder.getFolder()){
                    b.append(folder.getFolder().getFolderName());
                }
                b.append("<br></br>");
            }
            
        });
        return b.toString();
    }

}
