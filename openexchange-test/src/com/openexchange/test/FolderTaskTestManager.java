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

package com.openexchange.test;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.ConcurrentLinkedList;

/**
 * {@link FolderTaskTestManager}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 */
public class FolderTaskTestManager extends FolderTestManager {

    private AJAXClient client2;
    
    private List<FolderObject> createdItems2;    
    
    public FolderTaskTestManager(final AJAXClient client, final AJAXClient client2) {        
        super(client);
        this.client2 = client2;
        createdItems2 = new ConcurrentLinkedList<FolderObject>(); 
    }
    
    @Override
    public void cleanUp(){
        super.cleanUp();                
        deleteFolder(client2, createdItems2);
        createdItems2 = new ConcurrentLinkedList<FolderObject>();
        this.setClient(getClient());
    }
    
    /**
     * Method to clean up folders from multi-clients after a find task test was invoked
     */
    public void deleteFolder(final AJAXClient client, List<FolderObject> list){
        this.setClient(client);
        final Vector<FolderObject> deleteMe = new Vector<FolderObject>(list);       
        try {
            for (final FolderObject folder : deleteMe) {
                folder.setLastModified(new Date(Long.MAX_VALUE));
                deleteFolderOnServer(folder, Boolean.TRUE);
                if (getLastResponse().hasError()) {
                    org.slf4j.LoggerFactory.getLogger(FolderTestManager.class).warn("Unable to delete the folder with id {} in folder {} with name '{}': {}", folder.getObjectID(), folder.getParentFolderID(), folder.getFolderName(), getLastResponse().getException().getMessage());
                }
            }            
        } catch (final Exception e){
            doExceptionHandling(e, "clean-up");
        }        
    }
    
    public void rememberFolderFromClientA(final FolderObject folderObject){
        this.getCreatedItems().add(folderObject);
    }
    
    public void rememberFolderFromClientB(final FolderObject folderObject){
        createdItems2.add(folderObject);
    }

}
