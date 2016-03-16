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

package com.openexchange.user.copy.internal.infostore;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.tools.file.external.QuotaFileStorage;
import com.openexchange.tools.file.external.QuotaFileStorageFactory;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.MockQuotaFileStorageFactory;


/**
 * {@link InfostoreCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreCopyTest extends AbstractUserCopyTest {
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;
    

    /**
     * Initializes a new {@link InfostoreCopyTest}.
     * @param name
     */
    public InfostoreCopyTest(final String name) {
        super(name);
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId(); 
    }
    
    public void testCopyInfostore() throws Exception {
        final QuotaFileStorageFactory qfsf = new MockQuotaFileStorageFactory();
        final InfostoreCopyTask copyTask = new InfostoreCopyTask(qfsf);        
        
        final Map<String, ObjectMapping<?>> mapping = getObjectMappingWithFolders();
        @SuppressWarnings("unchecked")
        final ObjectMapping<FolderObject> folderMapping = (ObjectMapping<FolderObject>) mapping.get(FolderObject.class.getName());
        final Map<DocumentMetadata, List<DocumentMetadata>> sourceDocuments = copyTask.loadInfostoreDocumentsFromDB(copyTask.detectInfostoreFolders(folderMapping), srcCon, srcCtxId);
        try {     
            DBUtils.startTransaction(dstCon);
            copyTask.copyUser(mapping);    
            dstCon.commit();
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }
        
        final List<Integer> targetFolders = new ArrayList<Integer>();
        targetFolders.add(getDestinationFolder());
        final Map<DocumentMetadata, List<DocumentMetadata>> targetDocuments = copyTask.loadInfostoreDocumentsFromDB(targetFolders, dstCon, dstCtxId);
        checkDocuments(qfsf, sourceDocuments, targetDocuments);
    }
    
    private void checkDocuments(final QuotaFileStorageFactory qfsf, final Map<DocumentMetadata, List<DocumentMetadata>> sourceDocuments, final Map<DocumentMetadata, List<DocumentMetadata>> targetDocuments) throws OXException {
        final Set<DocumentMetadata> sourceMasters = sourceDocuments.keySet();
        final Set<DocumentMetadata> targetMasters = targetDocuments.keySet();        
        
        final Map<DocumentMetadata, DocumentMetadata> mapping = checkAndGetMatchingObjects(sourceMasters, targetMasters, new InfostoreComparator());
        final QuotaFileStorage sourceStorage = qfsf.getQuotaFileStorage(getSourceContext(), FilestoreStorage.createURI(getSourceContext()));
        final QuotaFileStorage targetStorage = qfsf.getQuotaFileStorage(getDestinationContext(), FilestoreStorage.createURI(getDestinationContext()));
        
        
        for (final DocumentMetadata source : mapping.keySet()) {
            final DocumentMetadata target = mapping.get(source);
            checkAndGetMatchingObjects(sourceDocuments.get(source), targetDocuments.get(target), new InfostoreDocumentComparator(sourceStorage, targetStorage));
        }        
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "infostore", "infostore_document");
        super.tearDown();
    }   

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_infostore" };
    }
    
    private static class InfostoreComparator implements Comparator<DocumentMetadata> {

        protected InfostoreComparator() {
            super();
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final DocumentMetadata o1, final DocumentMetadata o2) {
            final boolean isEqual = AbstractUserCopyTest.checkNullOrEquals(o1.getColorLabel(), o2.getColorLabel()) &&
            AbstractUserCopyTest.checkNullOrEquals(o1.getCreationDate(), o2.getCreationDate()) &&
            AbstractUserCopyTest.checkNullOrEquals(o1.getLastModified(), o2.getLastModified());
            
            return isEqual ? 0 : -1;
        }
        
    }
    
    private static class InfostoreDocumentComparator extends InfostoreComparator {
        
        private final QuotaFileStorage sourceStorage;
        
        private final QuotaFileStorage targetStorage;
        

        public InfostoreDocumentComparator(final QuotaFileStorage sourceStorage, final QuotaFileStorage targetStorage) {
            super();
            this.sourceStorage = sourceStorage;
            this.targetStorage = targetStorage;
        }
        
        /**
         * @see com.openexchange.user.copy.internal.infostore.InfostoreCopyTest.InfostoreComparator#compare(com.openexchange.groupware.infostore.DocumentMetadata, com.openexchange.groupware.infostore.DocumentMetadata)
         */
        @Override
        public int compare(final DocumentMetadata o1, final DocumentMetadata o2) {
            if (super.compare(o1, o2) == 0) {
                final boolean isEqual = (o1.getVersion() == o2.getVersion()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getTitle(), o2.getTitle()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getURL(), o2.getURL()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getDescription(), o2.getDescription()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getCategories(), o2.getCategories()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getFileName(), o2.getFileName()) &&
                (o1.getFileSize() == o2.getFileSize()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getFileMIMEType(), o2.getFileMIMEType()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getFileMD5Sum(), o2.getFileMD5Sum()) &&
                AbstractUserCopyTest.checkNullOrEquals(o1.getVersionComment(), o2.getVersionComment());
                
                boolean filesEqual = true;
                final String sourceLocation = o1.getFilestoreLocation();
                if (isEqual && sourceLocation != null) {
                    try {
                        final String targetLocation = o2.getFilestoreLocation();
                        final InputStream sourceIS = sourceStorage.getFile(sourceLocation);
                        final InputStream targetIS = targetStorage.getFile(targetLocation);
                        
                        final byte[] buffer1 = new byte[1024];
                        final byte[] buffer2 = new byte[1024];
                        int numRead1 = 0;
                        int numRead2 = 0;
                        while (numRead1 > -1) {
                            numRead1 = sourceIS.read(buffer1);
                            numRead2 = targetIS.read(buffer2);
                            
                            if (numRead1 != numRead2) {
                                filesEqual = false;
                                break;
                            } else {
                                if (!Arrays.equals(buffer1, buffer2)) {
                                    filesEqual = false;
                                    break;
                                }
                            }
                        }

                        sourceIS.close();
                        targetIS.close();
                    } catch (final IOException e) {
                        fail("IOException: " + e.getMessage());
                    } catch (final OXException e) {
                        fail("FileStorageException: " + e.getMessage());
                    }
                }
                
                return (isEqual && filesEqual) ? 0 : -1;
            } else {
                return -1;
            }
        }
    }

}
