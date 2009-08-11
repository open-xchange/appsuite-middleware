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

package com.openexchange.publish.microformats;

import java.util.Date;
import java.util.Set;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.publish.Publication;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.publish.microformats.tools.InfostoreTemplateUtils;
import junit.framework.TestCase;


/**
 * {@link InfostoreTemplateUtilsTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreTemplateUtilsTest extends TestCase {
    
    
    
    public void testFileURL() {
        SimContext context = new SimContext(1337);
        
        Publication publication = new Publication();
        publication.getConfiguration().put("siteName", "my/nice/site");
        publication.setContext(context);
        
        InfostoreTemplateUtils utils = new InfostoreTemplateUtils();
        
        String url = utils.getFileURL(publication, document);
        
        assertEquals("/publications/files/1337/my/nice/site/23/current", url);
    }
    
    public void testFileURLWithSecret() {
        
        SimContext context = new SimContext(1337);
        
        Publication publication = new Publication();
        publication.getConfiguration().put("siteName", "my/nice/site");
        publication.setContext(context);
        publication.getConfiguration().put("secret", "verySecret");
        
        InfostoreTemplateUtils utils = new InfostoreTemplateUtils();
        
        String url = utils.getFileURL(publication, document);
        
        assertEquals("/publications/files/1337/my/nice/site/23/current?secret=verySecret", url);
    }
    
    
    
    private DocumentMetadata document = new DocumentMetadata(){

        public String getCategories() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getColorLabel() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getContent() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getCreatedBy() {
            // TODO Auto-generated method stub
            return 0;
        }

        public Date getCreationDate() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDescription() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFileMD5Sum() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFileMIMEType() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFileName() {
            // TODO Auto-generated method stub
            return null;
        }

        public long getFileSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getFilestoreLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        public long getFolderId() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getId() {
            return 23;
        }

        public Date getLastModified() {
            // TODO Auto-generated method stub
            return null;
        }

        public Date getLockedUntil() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getModifiedBy() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumberOfVersions() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getProperty(String key) {
            // TODO Auto-generated method stub
            return null;
        }

        public Set<String> getPropertyNames() {
            // TODO Auto-generated method stub
            return null;
        }

        public long getSequenceNumber() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getTitle() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getURL() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getVersion() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getVersionComment() {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean isCurrentVersion() {
            // TODO Auto-generated method stub
            return false;
        }

        public void setCategories(String categories) {
            // TODO Auto-generated method stub
            
        }

        public void setColorLabel(int color) {
            // TODO Auto-generated method stub
            
        }

        public void setCreatedBy(int cretor) {
            // TODO Auto-generated method stub
            
        }

        public void setCreationDate(Date creationDate) {
            // TODO Auto-generated method stub
            
        }

        public void setDescription(String description) {
            // TODO Auto-generated method stub
            
        }

        public void setFileMD5Sum(String sum) {
            // TODO Auto-generated method stub
            
        }

        public void setFileMIMEType(String type) {
            // TODO Auto-generated method stub
            
        }

        public void setFileName(String fileName) {
            // TODO Auto-generated method stub
            
        }

        public void setFileSize(long length) {
            // TODO Auto-generated method stub
            
        }

        public void setFilestoreLocation(String string) {
            // TODO Auto-generated method stub
            
        }

        public void setFolderId(long folderId) {
            // TODO Auto-generated method stub
            
        }

        public void setId(int id) {
            // TODO Auto-generated method stub
            
        }

        public void setIsCurrentVersion(boolean bool) {
            // TODO Auto-generated method stub
            
        }

        public void setLastModified(Date now) {
            // TODO Auto-generated method stub
            
        }

        public void setLockedUntil(Date lockedUntil) {
            // TODO Auto-generated method stub
            
        }

        public void setModifiedBy(int lastEditor) {
            // TODO Auto-generated method stub
            
        }

        public void setNumberOfVersions(int numberOfVersions) {
            // TODO Auto-generated method stub
            
        }

        public void setTitle(String title) {
            // TODO Auto-generated method stub
            
        }

        public void setURL(String url) {
            // TODO Auto-generated method stub
            
        }

        public void setVersion(int version) {
            // TODO Auto-generated method stub
            
        }

        public void setVersionComment(String string) {
            // TODO Auto-generated method stub
            
        }
        
    };
    
}
