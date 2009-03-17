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

package com.openexchange.fitnesse.folders;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.folder.tree.FolderNode;
import com.openexchange.ajax.folder.tree.RootNode;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.fitnesse.FitnesseEnvironment;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link FolderResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FolderResolver {
    
    private AJAXClient client;
    private FitnesseEnvironment environment;

    public FolderResolver(AJAXClient client, FitnesseEnvironment environment) {
        this.client = client;
        this.environment = environment;
    }
    
    public int getFolderId(String folderExpression) throws FitnesseException {
        int folderId = tryEnvironment(folderExpression);
        if(folderId != -1) {
            return folderId;
        }
        
        return tryServer(folderExpression);
    }

    private int tryServer(String folderExpression) throws FitnesseException {
        String[] path = folderExpression.split("/");
        path = eliminateEmptyElements(path);
        FolderNode node = new RootNode(client).resolve(path);
        if(node == null)
            throw new FitnesseException("Could not resolve folder. Given expression was: " + folderExpression);
        return node.getFolder().getObjectID();
    }

    /**
     * @param folderExpression
     * @return
     */
    private int tryEnvironment(String folderExpression) {
        IdentitySource identitySource = environment.getSymbol(folderExpression);
        if(identitySource == null) {
            return -1;
        }
        if(identitySource.getType() == FolderObject.class) {
            FolderObject folderObject = new FolderObject();
            identitySource.assumeIdentity(folderObject);
            return folderObject.getObjectID();
        }
        return -1;
    }

    private String[] eliminateEmptyElements(String[] path) {
        List<String> newPath = new ArrayList<String>();
        for(String pathElement : path) {
            if(! isEmpty( pathElement )) {
                newPath.add(pathElement);
            }
        }
        return newPath.toArray(new String[newPath.size()]);
    }

    private boolean isEmpty(String pathElement) {
        return pathElement.trim().equals("");
    }
}
