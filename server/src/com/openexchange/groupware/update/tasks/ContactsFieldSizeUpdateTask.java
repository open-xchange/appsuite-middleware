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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * ContactsChangedFromUpdateTask
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 *
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class ContactsFieldSizeUpdateTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
    .getLog(ContactsFieldSizeUpdateTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(ContactsFieldSizeUpdateTask.class);
    
    /**
     * Default constructor
     */
    public ContactsFieldSizeUpdateTask() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
     */
    public int addedWithVersion() {
        return 15;
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

    private static final String STR_INFO = "Performing update task 'ContactsFieldSizeUpdateTask'";

    //private static final String SQL_QUERY = "SELECT created_from,changed_from,cid FROM prg_contacts WHERE changed_from IS NULL";

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema,
     *      int)
     */


    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
    	correctTable("prg_contacts", contextId);
    	correctTable("del_contacts", contextId);    
    }
    
    
    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 1 },
            msg = { "An SQL error occurred while performing task ContactsFieldSizeUpdateTask: %1$s." }
    )

    public void correctTable(final String sqltable, final int contextId) throws AbstractOXException {

    	final HashMap<String,Integer> columnRefer = new HashMap<String, Integer>();  	
    	columnRefer.put("field01", 320);
    	columnRefer.put("field02", 128);
    	columnRefer.put("field03", 128);
    	columnRefer.put("field04", 128);
    	columnRefer.put("field05", 64);
    	columnRefer.put("field06", 64);
    	columnRefer.put("field07", 256);
    	columnRefer.put("field08", 64);
    	columnRefer.put("field09", 64);
    	columnRefer.put("field10", 64);
    	columnRefer.put("field11", 64);
    	columnRefer.put("field12", 64);
    	columnRefer.put("field13", 64);
    	columnRefer.put("field14", 64);
    	columnRefer.put("field15", 64);
    	columnRefer.put("field16", 64);
    	columnRefer.put("field17", 5680);
    	columnRefer.put("field18", 512);
    	columnRefer.put("field19", 128);
    	columnRefer.put("field20", 128);
    	columnRefer.put("field21", 64);
    	columnRefer.put("field22", 64);
    	columnRefer.put("field23", 256);
    	columnRefer.put("field24", 64);
    	columnRefer.put("field25", 128);
    	columnRefer.put("field26", 64);
    	columnRefer.put("field27", 64);
    	columnRefer.put("field28", 64);
    	columnRefer.put("field29", 64);
    	columnRefer.put("field30", 128);
    	columnRefer.put("field31", 64);
    	columnRefer.put("field32", 64);
    	columnRefer.put("field33", 64);
    	columnRefer.put("field34", 5192);
    	columnRefer.put("field35", 64);
    	columnRefer.put("field36", 64);
    	columnRefer.put("field37", 256);
    	columnRefer.put("field38", 64);
    	columnRefer.put("field39", 64);
    	columnRefer.put("field40", 64);
    	columnRefer.put("field41", 64);
    	columnRefer.put("field42", 64);
    	columnRefer.put("field43", 64);
    	columnRefer.put("field44", 128);
    	columnRefer.put("field45", 64);
    	columnRefer.put("field46", 64);
    	columnRefer.put("field47", 64);
    	columnRefer.put("field48", 64);
    	columnRefer.put("field49", 64);
    	columnRefer.put("field50", 64);
    	columnRefer.put("field51", 64);
    	columnRefer.put("field52", 64);
    	columnRefer.put("field53", 64);
    	columnRefer.put("field54", 64);
    	columnRefer.put("field55", 64);
    	columnRefer.put("field56", 64);
    	columnRefer.put("field57", 64);
    	columnRefer.put("field58", 64);
    	columnRefer.put("field59", 64);
    	columnRefer.put("field60", 64);
    	columnRefer.put("field61", 64);
    	columnRefer.put("field62", 64);
    	columnRefer.put("field63", 64);
    	columnRefer.put("field64", 64);
    	columnRefer.put("field65", 256);
    	columnRefer.put("field66", 256);
    	columnRefer.put("field67", 256);
    	columnRefer.put("field68", 128);
    	columnRefer.put("field69", 1024);
    	columnRefer.put("field70", 64);
    	columnRefer.put("field71", 64);
    	columnRefer.put("field72", 64);
    	columnRefer.put("field73", 64);
    	columnRefer.put("field74", 64);
    	columnRefer.put("field75", 64);
    	columnRefer.put("field76", 64);
    	columnRefer.put("field77", 64);
    	columnRefer.put("field78", 64);
    	columnRefer.put("field79", 64);
    	columnRefer.put("field80", 64);
    	columnRefer.put("field81", 64);
    	columnRefer.put("field82", 64);
    	columnRefer.put("field83", 64);
    	columnRefer.put("field84", 64);
    	columnRefer.put("field85", 64);
    	columnRefer.put("field86", 64);
    	columnRefer.put("field87", 64);
    	columnRefer.put("field88", 64);
    	columnRefer.put("field89", 64);
    	columnRefer.put("field90", 320);
    	columnRefer.put("field91", 64);
    	columnRefer.put("field92", 64);
    	columnRefer.put("field93", 64);
    	columnRefer.put("field94", 64);
    	columnRefer.put("field95", 64);
    	columnRefer.put("field96", 64);
    	columnRefer.put("field97", 64);
    	columnRefer.put("field98", 64);
    	columnRefer.put("field99", 64);  	

    	final HashMap<String,Integer> toChange = new HashMap<String, Integer>();
    	final HashMap<String,Integer> toDelete = new HashMap<String, Integer>();
    	
    	
    	if (LOG.isInfoEnabled()) {
            LOG.info(STR_INFO);
        }
        Connection writeCon = null;
        final PreparedStatement stmt = null;
        Statement st = null;
        final ResultSet rs = null;
        try {

            writeCon = Database.get(contextId, true);
            try {
                st = writeCon.createStatement();

                final DatabaseMetaData metadata = writeCon.getMetaData();
                final ResultSet resultSet = metadata.getColumns(null, null, sqltable, null);
                while (resultSet.next()) {
                	final String name = resultSet.getString("COLUMN_NAME");
                	//String type = resultSet.getString("TYPE_NAME");
                	final int size = resultSet.getInt("COLUMN_SIZE");
				
                	if (null != columnRefer.get(name)){               		
                		if (name.equals("field91") || 
                			name.equals("field92") || 
                			name.equals("field93") || 
                			name.equals("field94") || 
                			name.equals("field95") || 
                			name.equals("field96") || 
                			name.equals("field97") || 
                			name.equals("field98") || 
                			name.equals("field99") ){
                			toDelete.put(name, 1);
                		} else {
                			final int si = columnRefer.get(name).intValue();
                			if (si != size){
                				LOG.warn("CHANGE FIELD "+sqltable+"."+name+" WITH SIZE "+size+" TO NEW SIZE "+si);
                				toChange.put(name, si);
                			} else {
                				LOG.info("FIELD "+sqltable+"."+name+" WITH SIZE "+size+" IS CORRECT "+si);                			
                			}
                		}
                	//} else {
                		//LOG.warn(name+" with size "+size+" | This field is not mapped for updateTasks! ContactsFieldSizeUpdateTask");
                	}               
                	//System.out.println("CN - "+name+" : "+type+" | "+size);
                }

                boolean done = false;
                Iterator<String> it = null;
                if (toDelete.size() > 0){
     	           
                	StringBuilder sb2 = new StringBuilder("ALTER TABLE "+sqltable+" ");
                	it = toDelete.keySet().iterator();
                	while (it.hasNext()){
                		final String key = it.next();
                		//int value = (Integer)toDelete.get(key).intValue();
                		sb2.append("DROP COLUMN "+key+", ");
                		done = true;
                	}
                	if (done) {
						sb2 = new StringBuilder(sb2.substring(0, sb2.lastIndexOf(",")));
					}  
                	
                    LOG.warn("CHANGING SQL DELETE FIELDS-> "+sb2);
                	st.addBatch(sb2.toString());
                }              

                
                done = false;

                if (toChange.size() > 0){
                    
                	StringBuilder sb = new StringBuilder("ALTER TABLE "+sqltable+" ");
                	it = toChange.keySet().iterator();
                	while (it.hasNext()){
                		final String key = it.next();
                		final int value = toChange.get(key).intValue();
                		sb.append("MODIFY "+key+" varchar("+value+"), ");
                		done = true;
                	}
                	if (done) {
						sb = new StringBuilder(sb.substring(0, sb.lastIndexOf(",")));
					}
                
                    LOG.warn("CHANGING SQL FIELD SIZE-> "+sb);
                    st.addBatch(sb.toString());
               	}             
                
                st.executeBatch();
                st.close();
                
            } catch (final SQLException e) {
                throw EXCEPTION.create(1, e, e.getMessage());
            }
        } finally {
            closeSQLStuff(rs, stmt);
            closeSQLStuff(null, st);
            if (writeCon != null) {
                Database.back(contextId, true, writeCon);
            }
        }
    }
    
}
