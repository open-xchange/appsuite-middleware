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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.openexchange.api2.OXException;
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * ContactsChangedFromUpdateTask
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 *
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class ContactsRepairLinksAttachments implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
    .getLog(ContactsRepairLinksAttachments.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(ContactsRepairLinksAttachments.class);
    
    /**
     * Default constructor
     */
    public ContactsRepairLinksAttachments() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
     */
    public int addedWithVersion() {
        return 17;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#getPriority()
     */
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String STR_INFO = "Performing update task 'ContactsRepairLinksAttachments'";

    //private static final String SQL_QUERY = "SELECT created_from,changed_from,cid FROM prg_contacts WHERE changed_from IS NULL";

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema,
     *      int)
     */

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
    	if (LOG.isInfoEnabled()) {
            LOG.info(STR_INFO);
        }
    	correctContacts(contextId);
    	correctLinks(contextId);
    	correctAttachments(contextId);    
    }
    
    private void correctContacts(int contextId) throws AbstractOXException {
        Connection writeCon = null;
        Statement st = null;
        ResultSet resultSet = null;
        try {
            writeCon = Database.get(contextId, true);           
            st = writeCon.createStatement();               
            resultSet = st.executeQuery("SELECT "+"" +
            			"prg_contacts.intfield01, "+
            			"prg_contacts.fid, "+
            			"prg_contacts.cid, "+
            			"prg_contacts.pflag, "+
            			"oxfolder_tree.fuid, "+
            			"oxfolder_tree.fname "+
            			"FROM prg_contacts LEFT JOIN oxfolder_tree ON "+
            			"prg_contacts.fid = oxfolder_tree.fuid AND prg_contacts.cid = oxfolder_tree.cid "+
            			"WHERE oxfolder_tree.fuid is NULL");

            int id = 0;
            int cid = 0;
            int fid = 0;
            int pflag = 0;
            Context ctx = null;
            FolderObject fo = null;
            boolean delete = false;
            OXFolderAccess oxfs = null;
            
            while (resultSet.next()) {
            	
            	delete = false;
            	id = resultSet.getInt("intfield01"); 
            	cid = resultSet.getInt("cid");
            	fid = resultSet.getInt("fid");
            	pflag = resultSet.getInt("pflag");
            	
            	try {
            		ctx = ContextStorage.getInstance().loadContext(cid);
            		if (null == ctx){
            			delete = true;
            		} else {
            			if (id == ctx.getMailadmin()){
            				delete = true;
            			} else {
            				if (pflag == 0){
            					Statement tmp =null;
            					try {      						
            						oxfs = new OXFolderAccess(writeCon, ctx);            				
            						fo = oxfs.getDefaultFolder(ctx.getMailadmin(), FolderObject.CONTACT);
            						final int admin_folder = fo.getObjectID();            					
            						final ContactSql cs = new ContactMySql(ctx, ctx.getMailadmin());
            						//System.out.println("MOVIN CONTACT "+id+" CID "+cid);
            						tmp = writeCon.createStatement();
            						cs.iFgiveUserContacToAdmin(tmp, id, admin_folder, ctx);
            					} catch (final Exception oxee) {
            						oxee.printStackTrace();
            						LOG.error("ERROR: It was not possible to move this contact (without paren folder) to the admin address book!."
    										+ "This contact will be deleted."
    										+ "Context "
    										+ ctx.getContextId()
    										+ " Folder "
    										+ fid + " Contact" + id);

            						delete = true;
            					} finally {
            			            closeSQLStuff(null, tmp);
            					}
            				} else{
            					delete = true;
            				}
            			}
            		}
            	} catch (ContextException ce){
            		delete = true;
            		LOG.info("MARKED CONTACT "+id+" IN CONTEXT "+cid+" TO GET DELETED BECAUSE THE CONTEXT IS GONE");
            	}
            	
            	if (delete){
            		//System.out.println("DELETE CONTACT "+id+" IN CONTEXT "+cid+" ");
            		LOG.info("DELETE CONTACT "+id+" IN CONTEXT "+cid+" BECAUSE THE CONTEXT IS GONE");
            		st.addBatch("DELETE FROM prg_contacts WHERE intfield01 = "+id+" AND cid = "+cid);
            	}
            }
            st.executeBatch();
            
            	
        } catch (final SQLException e) {
        	throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            closeSQLStuff(resultSet, st);
            if (writeCon != null) {
                Database.back(contextId, true, writeCon);
            }
        }
	}

	public boolean checkContactExistence(Connection writeCon, int id, int cid){
    	boolean killit = false;
    	ResultSet rs = null;
    	Statement st = null;
    	try{
    		st = writeCon.createStatement();
    		rs = st.executeQuery("SELECT intfield01 FROM prg_contacts WHERE intfield01 = "+id+" AND cid = "+cid);
   		
    		while (rs.next()){
    				int chk = rs.getInt(1);
    				if (chk == id){
    					killit = true;
    				}
    		}
    	} catch (SQLException sqle){
    		LOG.error("UNABLE TO FETCH THIS CONTACT: ID "+id+" CID "+cid);
    		sqle.printStackTrace();
    	} finally {
            closeSQLStuff(rs, st);
    	}
    	return killit;
    }
    
    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 1 },
            msg = { "An SQL error occurred while performing task ContactsRepairLinksAttachments: %1$s." }
    )
    public void correctLinks(final int contextId) throws AbstractOXException {
            	

        Connection writeCon = null;
        Statement st = null;
        ResultSet resultSet = null;
        try {
            writeCon = Database.get(contextId, true);
           
            st = writeCon.createStatement();
            int id1 = 0;
            int mod1 = 0;
            int id2 = 0;
            int mod2 = 0;
            int cid = 0;
            boolean deleteit = false;
                
            resultSet = st.executeQuery("SELECT firstid, firstmodule, secondid, secondmodule, cid FROM prg_links WHERE firstmodule = "+Types.CONTACT+" OR secondmodule = "+Types.CONTACT);

            while (resultSet.next()) {
            	
            	id1 = resultSet.getInt("firstid"); 
            	mod1 = resultSet.getInt("firstmodule"); 
            	id2 = resultSet.getInt("secondid");
            	mod2 = resultSet.getInt("secondmodule");
            	deleteit = false;
            	cid = resultSet.getInt("cid");
                
            	if (mod1 == Types.CONTACT){
            		if (!checkContactExistence(writeCon, id1, cid)){
            			deleteit = true;
            		}
            	}
            	if (mod2 == Types.CONTACT && !deleteit){
            		if (!checkContactExistence(writeCon, id2, cid)){
            			deleteit = true;
            		}
            	}
            	
            	if (deleteit){
            		//System.out.println("DELETE LINK: ID1="+id1+" ID2="+id2+" CID="+cid);
            		st.addBatch("DELETE FROM prg_links WHERE firstid = "+id1+" AND secondid = "+id2+" AND firstmodule = "+mod1+" AND secondmodule = "+mod2+" AND cid = "+cid);
            	}
            }
             
            st.executeBatch();
            st.close();
                
        } catch (final SQLException e) {
        	throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            closeSQLStuff(resultSet, st);
            if (writeCon != null) {
                Database.back(contextId, true, writeCon);
            }
        }
    }
    
    public void correctAttachments(final int contextId) throws AbstractOXException {
    	

        Connection writeCon = null;
        Statement st = null;
        ResultSet resultSet = null;
        
        try {
            writeCon = Database.get(contextId, true);
           
            st = writeCon.createStatement();
            int id = 0;
            int mod = 0;
            int cid = 0;
            
            int attachId = 0;    
            
            resultSet = st.executeQuery("SELECT "+
            			"prg_attachment.attached, "+
            			"prg_attachment.cid, "+
            			"prg_attachment.module, "+
            			"prg_attachment.id "+
            			"FROM prg_attachment "+
            			"LEFT JOIN prg_contacts ON "+
            			"prg_attachment.attached = prg_contacts.intfield01 AND prg_attachment.cid = prg_contacts.cid "+
            			"WHERE prg_attachment.module = "+Types.CONTACT+" AND prg_contacts.intfield01 IS NULL");

           
            while (resultSet.next()) {
            	
            	id = resultSet.getInt("attached"); 
            	mod = resultSet.getInt("module");             	
            	cid = resultSet.getInt("cid");
            	attachId = resultSet.getInt("id");

            	//System.out.println("DELETE ATTACHMENT: ID="+id+" CID="+cid);
            	deleteAttachments(-1, id, mod, new int[]{ attachId }, cid);

            }                
        } catch (final SQLException e) {
        	throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            closeSQLStuff(resultSet, st);
            if (writeCon != null) {
                Database.back(contextId, true, writeCon);
            }
        }
    }
    
	private static final AttachmentBase ATTACHMENT_BASE = new AttachmentBaseImpl(new DBPoolProvider());
    
	private final void deleteAttachments(final int parentFolderID, final int objectID, final int type, final int[] attachIds, final int contextId) {

		try {
			Context ctx = ContextStorage.getInstance().loadContext(contextId);
            ATTACHMENT_BASE.startTransaction();			
			ATTACHMENT_BASE.detachFromObject(parentFolderID, objectID, type, attachIds, ctx, null, null);
			ATTACHMENT_BASE.commit();
		} catch (TransactionException e) {
			rollback(e);
		} catch (OXException e) {
			rollback(e);
		} catch (ContextException e) {
            LOG.error(e);
        } finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e);
			}
		}
	}
	
	private void rollback(AbstractOXException x) {
		try {
			ATTACHMENT_BASE.rollback();
		} catch (TransactionException e) {
			LOG.error(e);
		}
		LOG.error(x);
	}

    
}
