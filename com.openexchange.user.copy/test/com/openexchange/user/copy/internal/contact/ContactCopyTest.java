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

package com.openexchange.user.copy.internal.contact;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;


/**
 * {@link ContactCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactCopyTest extends AbstractUserCopyTest {
    
    private static final EnumSet<ContactField> IGNORE_FIELDS = EnumSet.of(ContactField.OBJECT_ID, ContactField.CREATED_BY, ContactField.MODIFIED_BY, ContactField.FOLDER_ID, ContactField.CONTEXTID, ContactField.INTERNAL_USERID);
    
    private int srcUsrId;

    private int dstUsrId;
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;
    

    /**
     * Initializes a new {@link ContactCopyTest}.
     * @param name
     */
    public ContactCopyTest(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        dstUsrId = getDestinationUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId(); 
    }
    
    public void testContactCopy() throws Exception {
        final ContactCopyTask copyTask = new ContactCopyTask();
        final List<Integer> originFolderIds = loadFolderIdsFromDB(srcCon, srcCtxId, srcUsrId);
        final List<ContactField> contactFields = copyTask.getCleanedContactFields();
        final Map<Integer, Contact> originContacts = copyTask.loadContactsFromDB(contactFields, originFolderIds, srcCon, srcCtxId, srcUsrId);
        copyTask.loadImagesFromDB(originContacts, srcCon, srcCtxId);
        copyTask.loadDistributionListsFromDB(originContacts, srcCon, srcCtxId);

        DBUtils.startTransaction(dstCon);
        IntegerMapping mapping = null;
        try {
            mapping = copyTask.copyUser(getObjectMappingWithFolders());
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }        
        dstCon.commit();        
        
        final List<Integer> targetFolderIds = new ArrayList<Integer>();
        targetFolderIds.add(getDestinationFolder());
        final Map<Integer, Contact> targetContacts = copyTask.loadContactsFromDB(contactFields, targetFolderIds, dstCon, dstCtxId, dstUsrId);
        copyTask.loadImagesFromDB(targetContacts, dstCon, dstCtxId);
        copyTask.loadDistributionListsFromDB(targetContacts, dstCon, dstCtxId);        
        checkContacts(originContacts, targetContacts, mapping);
    }
    
    private void checkContacts(final Map<Integer, Contact> originContacts, final Map<Integer, Contact> targetContacts, final IntegerMapping mapping) throws OXException {
        if (originContacts.size() != targetContacts.size()) {
            fail("Origin and Target Map sizes were different.");
        }
        
        if (mapping.getSourceKeys().size() != originContacts.size()) {
            fail("Origin and Mapping Map sizes were different.");
        }
        
        for (final Integer sourceId : mapping.getSourceKeys()) {
            final Integer destinationId = mapping.getDestination(sourceId);
            if (destinationId == null) {
                fail("Destination was null for Source Id " + sourceId);
            }
            
            final Contact sourceContact = originContacts.get(sourceId);
            final Contact targetContact = targetContacts.get(destinationId);
            if (sourceContact == null) {
                fail("Source Contact was null.");
            }
            
            if (targetContact == null) {
                fail("target Contact was null.");
            }
            
            compareContacts(sourceContact, targetContact);
            checkImages(sourceContact, targetContact);
            checkDLists(sourceContact, targetContact);
        }
    }
    
    private void checkImages(final Contact sourceContact, final Contact targetContact) {        
        final boolean isEqual = Arrays.equals(sourceContact.getImage1(), targetContact.getImage1()) &&   
        checkNullOrEquals(sourceContact.getImageLastModified(), targetContact.getImageLastModified()) &&
        checkNullOrEquals(sourceContact.getImageContentType(), targetContact.getImageContentType());
        
        if (!isEqual) {
            fail("Images were not equal. Source " + sourceContact.getObjectID() + ", Target: " + targetContact.getObjectID());
        }
    }

    private void compareContacts(final Contact sourceContact, final Contact targetContact) throws OXException {
        final ContactSwitcher getter = new ContactGetter();
        for (final ContactField field : ContactField.values()) {
            if (field.isDBField() && !IGNORE_FIELDS.contains(field)) {
                final Object sourceObject = field.doSwitch(getter, sourceContact);
                final Object targetObject = field.doSwitch(getter, targetContact);
                
                if (!field.equals(ContactField.NUMBER_OF_IMAGES)) {
                    /*
                     * Due to inconsistent data number of images may be wrong.
                     * We ignore this here.
                     */
                    assertEquals("Objects were not equal for field " + field.getReadableName() + " (" + field.getNumber() + ").", sourceObject, targetObject);
                }                
            }
        }
    }
    
    private void checkDLists(final Contact sourceContact, final Contact targetContact) {
        if (sourceContact.getDistributionList() == null && targetContact.getDistributionList() == null) {
            return;
        }
        
        final DistributionListEntryObject[] sourceDListArr = sourceContact.getDistributionList();
        final List<DistributionListEntryObject> sourceDList = Arrays.asList(sourceDListArr);
        final DistributionListEntryObject[] targetDListArr = targetContact.getDistributionList();
        final List<DistributionListEntryObject> targetDList = Arrays.asList(targetDListArr);
        
        checkAndGetMatchingObjects(sourceDList, targetDList, new DListComparator());
    }

    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "prg_contacts", "prg_contacts_image", "prg_dlist");
        super.tearDown();
    }

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_contact" };
    }
    
    private static final class DListComparator implements Comparator<DistributionListEntryObject> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final DistributionListEntryObject o1, final DistributionListEntryObject o2) {
            final boolean isEqual = 
                checkNullOrEquals(o1.getDisplayname(), o2.getDisplayname()) &&
                checkNullOrEquals(o1.getFirstname(), o2.getFirstname()) &&
                checkNullOrEquals(o1.getLastname(), o2.getLastname()) &&
                checkNullOrEquals(o1.getEmailaddress(), o2.getEmailaddress());
            
            return isEqual ? 0 : -1;
        }
        
        private boolean checkNullOrEquals(final String str1, final String str2) {
            if (str1 == null) {
                if (str2 != null) {
                    return false;
                }
                
                return true;                
            } else {
                if (str2 == null) {
                    return false;
                }
                
                return str1.equals(str2);
            }
        }        
    }
}
