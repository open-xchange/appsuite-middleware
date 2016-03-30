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

package com.openexchange.user.copy.internal.attachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.file.external.QuotaFileStorageFactory;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.MockQuotaFileStorageFactory;


/**
 * {@link AttachmentCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AttachmentCopyTest extends AbstractUserCopyTest {
    
    private int srcUsrId;
        
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;
    
    private static final int TARGET_APPOINTMENT = 1;
    
    private static final int TARGET_CONTACT = 2;
    
    private static final int TARGET_TASK = 3;
    
    private static final String SELECT_OBJECT_IDS = "SELECT id, module, attached FROM prg_attachment WHERE cid = ? AND created_by = ?";


    /**
     * Initializes a new {@link AttachmentCopyTest}.
     * @param name
     */
    public AttachmentCopyTest(final String name) {
        super(name);
    }
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId(); 
    }
    
    
    public void testAttachmentCopy() throws Exception {      
        final QuotaFileStorageFactory qfsf = new MockQuotaFileStorageFactory();
        final AttachmentCopyTask copyTask = new AttachmentCopyTask(qfsf);
        
        final Map<String, ObjectMapping<?>> mapping = getBasicObjectMapping();
        fillObjectMappingAndGetAttachmentIds(srcCon, srcCtxId, srcUsrId, mapping);
        final IntegerMapping taskMapping = (IntegerMapping) mapping.get(Task.class.getName());
        final IntegerMapping appointmentMapping = (IntegerMapping) mapping.get(Appointment.class.getName());
        final IntegerMapping contactMapping = (IntegerMapping) mapping.get(Contact.class.getName());
        final List<Integer> appointmentIds = new ArrayList<Integer>(appointmentMapping.getSourceKeys());
        final List<Integer> contactIds = new ArrayList<Integer>(contactMapping.getSourceKeys());
        final List<Integer> taskIds = new ArrayList<Integer>(taskMapping.getSourceKeys());
        final List<Attachment> originAttachments = copyTask.loadAttachmentsFromDB(srcCon, srcCtxId, appointmentIds, contactIds, taskIds);
        
        try {     
            DBUtils.startTransaction(dstCon);
            copyTask.copyUser(mapping);    
            dstCon.commit();
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }
        
        final List<Integer> targetAppointments = Arrays.asList(new Integer(TARGET_APPOINTMENT));
        final List<Integer> targetTasks = Arrays.asList(new Integer(TARGET_TASK));
        final List<Integer> targetContacts = Arrays.asList(new Integer(TARGET_CONTACT));
        final List<Attachment> targetAttachments = copyTask.loadAttachmentsFromDB(dstCon, dstCtxId, targetAppointments, targetContacts, targetTasks);
        checkAndGetMatchingObjects(originAttachments, targetAttachments, new AttachmentComparator());
    }
    
    private void fillObjectMappingAndGetAttachmentIds(final Connection con, final int cid, final int uid, final Map<String, ObjectMapping<?>> mapping) throws SQLException {
        final IntegerMapping appointmentMapping = new IntegerMapping();
        final IntegerMapping contactMapping = new IntegerMapping();
        final IntegerMapping taskMapping = new IntegerMapping();
        
        mapping.put(Appointment.class.getName(), appointmentMapping);
        mapping.put(Contact.class.getName(), contactMapping);
        mapping.put(Task.class.getName(), taskMapping);
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {            
            stmt = con.prepareStatement(SELECT_OBJECT_IDS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);            
            
            rs = stmt.executeQuery();        
            while (rs.next()) {
                final int module = rs.getInt(2);
                final int attached = rs.getInt(3);
                
                switch (module) {            
                    case Types.APPOINTMENT:
                    appointmentMapping.addMapping(attached, TARGET_APPOINTMENT);
                    break;
                    
                    case Types.CONTACT:
                    contactMapping.addMapping(attached, TARGET_CONTACT);
                    break;
                    
                    case Types.TASK:
                    taskMapping.addMapping(attached, TARGET_TASK);
                    break;
                    
                    default:
                    break;
                }
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    } 
    
    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "prg_attachment");
        super.tearDown();
    }    

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_attachment" };
    }
    
    private static final class AttachmentComparator implements Comparator<Attachment> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final Attachment origin, final Attachment target) {
            if (origin.getComment() == null) {
                if (target.getComment() != null) {
                    return -1;
                }            
            } else {
                if (target.getComment() == null) {
                    return -1;
                } else {
                    if (!origin.getComment().equals(target.getComment())) {
                        return -1;
                    }
                }
            }
            
            final boolean isEqual = 
               origin.getCreationDate().equals(target.getCreationDate()) && 
               origin.getFileMIMEType().equals(target.getFileMIMEType()) &&
               origin.getFilesize() == target.getFilesize() &&
               origin.getFilename().equals(target.getFilename()) &&
               origin.getModuleId() == target.getModuleId() &&
               origin.getRtfFlag() == target.getRtfFlag();
            
            return isEqual ? 0 : -1;
        }
        
    }

}
