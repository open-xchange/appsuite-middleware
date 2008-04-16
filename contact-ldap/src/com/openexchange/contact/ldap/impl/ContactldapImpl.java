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

package com.openexchange.contact.ldap.impl;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.DebugGraphics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.contact.ldap.tools.LdapTools;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.PrefetchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * 
 * @author <a href="mailto:ben.pahne@open-xchange">Ben Pahne</a>
 */

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_CONTACTS_LDAP_IMPL_CONTACTLDAPIMPL,
		component=EnumComponent.CONTACT
)

public class ContactldapImpl implements ContactInterface {

	private static final Log LOG = LogFactory.getLog(ContactldapImpl.class);

    private LdapServer server;
    
    private Session session = null;
    
    private UserConfiguration userConfiguration;
    
	private Context oxContext;

	private static final String SETUP_ERROR_LDAP_FOLDER = "Unablel to load this LDAP addressbook. Check the configuration for this LDAP server: Folder %1$d Context %2$d";

	private static final String SETUP_ERROR_LDAP_OBJECT = "Unablel to read this Object from this LDAP addressbook. Check the configuration for this LDAP server: Object %1$d Folder %2$d Context %3$d";
	
	private static final String NOT_SUPPORTED_LDAP = "This action is not supported in this LDAP addressbook: Folder %1$d Context %2$d";
	
	private static final String NOT_ALLOWED_LDAP = "You are not allowed to see this addressbook! Folder %1$d Context %1$d";
	
	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactldapImpl.class);

	public ContactldapImpl(String file){
		try {
			this.server = LdapTools.readLdapConfigurationFile(file);
		} catch (Exception e) {
			System.out.println("Unable to load LDAP Addressbook Configuration File: "+file);
			e.printStackTrace();
		}
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="0",
			exceptionId=0,
			msg=SETUP_ERROR_LDAP_FOLDER
	)
	public SearchIterator<?> getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException {
        
		/* Check Read Rights */
		checkReadRights(session);
		
		SearchIterator<?> si = null;
		            
        try{	
        	DirContext ctx = new InitialDirContext( LdapTools.buildAuthEnvironment(server, session) );
            SearchControls sc = null;
            
            String[] ldap_cols = new String[cols.length+2];
            for (int i=0; i<cols.length;i++){
            	ldap_cols[i] = server.getFieldMapping()[cols[i]][1];
            	//if (null == server.getFieldMapping()[cols[i]][1] || server.getFieldMapping()[cols[i]][1].equals("null")){
            	//}
            }  
            ldap_cols[ldap_cols.length-1] = server.getFieldMapping()[ContactObject.CREATION_DATE][1];
            ldap_cols[ldap_cols.length-2] = server.getFieldMapping()[ContactObject.LAST_MODIFIED][1];
            
            if (server.isSubfolderListActive()){
                sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(ldap_cols);
            }
            
            String filter = LdapTools.replaceTag(server.getListFilter(), "[letter]","");
            
            NamingEnumeration<SearchResult> result = ctx.search(server.getAddressbookDN(),filter,sc);
            
            si = new ContactLdapIterator(result, cols, ctx);

        } catch (NamingException ne){
        	ne.printStackTrace();
        	throw EXCEPTIONS.create(0, folderId, session.getContextId(), ne);
		}
    	
        return new PrefetchIterator(si);
	}

	public int getFolderId() {
		return server.getFolderId();
	}

	public int getNumberOfContacts(int folderId) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="1",
			exceptionId=1,
			msg=SETUP_ERROR_LDAP_OBJECT
	)
	public ContactObject getObjectById(int objectId, int inFolder) throws OXException {
		
		/* Check Read Rights */
		checkReadRights(session);
		
		ContactObject co = new ContactObject();

		String[] cols_ldap = new String[650];
		
		int cnt = 0;
		int[] cols = new int[650];
		for (int i=0;i<650;i++){				
			if (Contacts.mapping[i] != null){
				cols[cnt] = i;
				
				if (null != server.getFieldMapping()[i] && null != server.getFieldMapping()[i][1])
					cols_ldap[cnt] = server.getFieldMapping()[i][1]; 
				
				cnt++;
			}
		}
		
		String[] colsldapfinal = new String[cnt];
		System.arraycopy(cols_ldap, 0, colsldapfinal, 0, cnt);
		
		int[] colsfinal = new int[cnt];
		System.arraycopy(cols, 0, colsfinal, 0, cnt);
		
		cols = colsfinal;
		
        try{	
            DirContext ctx = new InitialDirContext( LdapTools.buildAuthEnvironment(server, session) );
            SearchControls sc = null;
            
            if (server.isSubfolderListActive()){
                sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(colsldapfinal);
            }
            
            String filter = LdapTools.replaceTag(server.getContactsFilter(), "[user_ids]","("+server.field_mapping[ContactObject.OBJECT_ID][1]+"="+objectId+")");
            NamingEnumeration<SearchResult> result = ctx.search(server.getAddressbookDN(),filter,sc);
            
            if (result.hasMore()){
            	co = convertLdap2ContactObject(result, cols);
        	}
            
            ctx.close();
        } catch (NamingException ne){
        	throw EXCEPTIONS.create(1, objectId, inFolder, session.getContextId(), ne);
		}
        
		return co;
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="2",
			exceptionId=2,
			msg=SETUP_ERROR_LDAP_FOLDER
	)
	public SearchIterator<?> getObjectsById(int[][] objectIdAndInFolder, int[] cols) throws OXException, Exception {
		
		/* Check Read Rights */
		checkReadRights(session);
		
		SearchIterator<?> si = null;
		            
        try{	
            DirContext ctx = new InitialDirContext( LdapTools.buildAuthEnvironment(server, session) );
            SearchControls sc = null;
            
            String[] ldap_cols = new String[cols.length+2];
            for (int i=0; i<cols.length;i++){
            	ldap_cols[i] = server.getFieldMapping()[cols[i]][1];
            }  
            ldap_cols[ldap_cols.length-1] = server.getFieldMapping()[ContactObject.CREATION_DATE][1];
            ldap_cols[ldap_cols.length-2] = server.getFieldMapping()[ContactObject.LAST_MODIFIED][1];
            
            if (server.isSubfolderListActive()){
                sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(ldap_cols);
            }
            
            StringBuilder user_ids = new StringBuilder();
            for (int x=0;x<objectIdAndInFolder.length;x++){
            	user_ids.append("("+server.field_mapping[ContactObject.OBJECT_ID][1]+"="+objectIdAndInFolder[x][0]+")");
            }
            String filter = LdapTools.replaceTag(server.getContactsFilter(), "[user_ids]",user_ids.toString());
            NamingEnumeration<SearchResult> result = ctx.search(server.getAddressbookDN(),filter,sc);

           	si = new ContactLdapIterator(result, cols, ctx);

        } catch (NamingException ne){
        	throw EXCEPTIONS.create(2, server.getFolderId(), session.getContextId(), ne);
		}

        return new PrefetchIterator(si);
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="3",
			exceptionId=3,
			msg=NOT_SUPPORTED_LDAP
	)
	public SearchIterator<?> getContactsByExtendedSearch(ContactSearchObject searchobject, int orderBy, String orderDir, int[] cols) throws OXException {
    	throw EXCEPTIONS.create(3, server.getFolderId(), session.getContextId());
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="4",
			exceptionId=4,
			msg=NOT_SUPPORTED_LDAP
	)
	public SearchIterator<?> getDeletedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
    	throw EXCEPTIONS.create(4, server.getFolderId(), session.getContextId());
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="5",
			exceptionId=5,
			msg=SETUP_ERROR_LDAP_FOLDER
	)
	public SearchIterator<?> getModifiedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {

		/* Check Read Rights */
		checkReadRights(session);
		
		DateFormat indfm = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
		indfm.setTimeZone(TimeZone.getTimeZone("UTC"));
		String since_string = indfm.format(since);		
		
		SearchIterator<?> si = null;
	        
        try{	
            DirContext ctx = new InitialDirContext( LdapTools.buildAuthEnvironment(server, session) );
            SearchControls sc = null;
            
            String[] ldap_cols = new String[cols.length+2];
            for (int i=0; i<cols.length;i++){
            	ldap_cols[i] = server.getFieldMapping()[cols[i]][1];

            }
            ldap_cols[ldap_cols.length-1] = server.getFieldMapping()[ContactObject.CREATION_DATE][1];
            ldap_cols[ldap_cols.length-2] = server.getFieldMapping()[ContactObject.LAST_MODIFIED][1];
            
            if (server.isSubfolderListActive()){
                sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(ldap_cols);
            }
            
            String filter = LdapTools.replaceTag(server.getModifiedListFilter(), "[last_modified]",since_string);    
            NamingEnumeration<SearchResult> result = ctx.search(server.getAddressbookDN(),filter,sc);
            
           	LOG.debug("getModifiedContactsInFolder - Loading LDAP Contacts with this filter: "+filter);
            si = new ContactLdapIterator(result, cols, ctx);

        } catch (NamingException ne){
        	throw EXCEPTIONS.create(5, folderId, session.getContextId(), ne);
		}

        return new PrefetchIterator(si);
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="6",
			exceptionId=6,
			msg=SETUP_ERROR_LDAP_FOLDER
	)
	public SearchIterator<?> searchContacts(String searchpattern, boolean startletter, int folderId, int orderBy, String orderDir, int[] cols) throws OXException {

		/* Check Read Rights */
		checkReadRights(session);
		
		SearchIterator<?> si = null;
		            
        try{	
        	DirContext ctx = new InitialDirContext( LdapTools.buildAuthEnvironment(server, session) );
            SearchControls sc = null;
            
            String[] ldap_cols = new String[cols.length+2];
            for (int i=0; i<cols.length;i++){
            	ldap_cols[i] = server.getFieldMapping()[cols[i]][1];
            	//if (null == server.getFieldMapping()[cols[i]][1] || server.getFieldMapping()[cols[i]][1].equals("null")){
            	//}
            }  
            ldap_cols[ldap_cols.length-1] = server.getFieldMapping()[ContactObject.CREATION_DATE][1];
            ldap_cols[ldap_cols.length-2] = server.getFieldMapping()[ContactObject.LAST_MODIFIED][1];
            
            if (server.isSubfolderListActive()){
                sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(ldap_cols);
            }
            
            if (!startletter && !searchpattern.startsWith("*") && !searchpattern.endsWith("*") && !searchpattern.equals("*"))
            	searchpattern = new StringBuilder().append("*"+searchpattern+"*").toString();
            	
            String filter = "";
            if (startletter) {
            	filter = LdapTools.replaceTag(server.getListFilter(), "[letter]",searchpattern);
            } else {
            	filter = LdapTools.replaceTag(server.getSearchFilter(),"[searchpattern]",searchpattern);
            } 
            
            System.out.println("Loading with Filter --> "+filter);
            
            NamingEnumeration<SearchResult> result = ctx.search(server.getAddressbookDN(),filter,sc);

           	si = new ContactLdapIterator(result, cols, ctx);
        } catch (NamingException ne){
        	throw EXCEPTIONS.create(6, folderId, session.getContextId(), ne);
		}
    	
        return new PrefetchIterator(si);
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="7",
			exceptionId=7,
			msg=NOT_SUPPORTED_LDAP
	)
	public ContactObject getUserById(int userId) throws OXException {
    	throw EXCEPTIONS.create(7, server.getFolderId(), session.getContextId());
	}
	
	public LdapServer getLdapServer() {
		return server;
	}
	    
	private ContactObject convertLdap2ContactObject(NamingEnumeration<?> ne, final int[] cols) throws OXException, NamingException {
		
        SearchResult sr = (SearchResult) ne.next();
        javax.naming.directory.Attribute fetch_data;
        Attributes attribs = sr.getAttributes();
       
        
        String tmp;
        ContactObject co = new ContactObject();
    	String location = sr.getName()+","+server.getAddressbookDN();
    	
        if (LOG.isDebugEnabled()){
        	LOG.debug("Load Contact "+location);
        }
        
        for (int i=0; i<cols.length;i++){
            
        	if (
        			(cols[i] == ContactObject.IMAGE1_CONTENT_TYPE) || 
        			(cols[i] == ContactObject.IMAGE_LAST_MODIFIED) || 
        			(cols[i] == ContactObject.NUMBER_OF_IMAGES) ||
        			(cols[i] == ContactObject.PRIVATE_FLAG) ||
        			(cols[i] == ContactObject.NUMBER_OF_DISTRIBUTIONLIST) ||
        			(cols[i] == ContactObject.MARK_AS_DISTRIBUTIONLIST) ||
        			(cols[i] == ContactObject.DISTRIBUTIONLIST) ||
        			(cols[i] == ContactObject.COLOR_LABEL) ||     			
        			(cols[i] == ContactObject.DEFAULT_ADDRESS) ||
        			(cols[i] == ContactObject.INTERNAL_USERID) ||
        			(cols[i] == ContactObject.LINKS) ||
        			(cols[i] == ContactObject.FILE_AS) ||
        			(cols[i] == ContactObject.NUMBER_OF_LINKS) ||
        			(cols[i] == ContactObject.NUMBER_OF_ATTACHMENTS)
        			
        	){
        		continue;
        	}
        	
            if (LOG.isDebugEnabled()){
            	if (null == server.getFieldMapping()[cols[i]][1]){
            		LOG.debug("LDAP INFO LOADING COLS:"+cols[i]+" which is "+Contacts.mapping[cols[i]].getDBFieldName()+" WAS NOT FOUND");
            		if (null != Contacts.mapping[cols[i]].getReadableTitle()){
            			LOG.debug("This is -> "+Contacts.mapping[cols[i]].getReadableTitle());
            		}
        		}
            }

        	try{        		
        		if (cols[i] == ContactObject.FOLDER_ID){
        			tmp = String.valueOf(server.getFolderId());
        		} else if (cols[i] == ContactObject.CREATED_BY){
            		tmp = String.valueOf(oxContext.getMailadmin());
        		} else if (cols[i] == ContactObject.MODIFIED_BY){
            		tmp = String.valueOf(oxContext.getMailadmin());
            		
        		} else if (cols[i] == ContactObject.BIRTHDAY || cols[i] == ContactObject.ANNIVERSARY){
        			tmp = "";
        			try{
        				fetch_data = attribs.get(server.getFieldMapping()[cols[i]][1]);
        				tmp = (String)fetch_data.get();
        			} catch (NullPointerException eee){ }
        			try {
        				if (!tmp.equals("")){
        					DateFormat indfm = new SimpleDateFormat(server.getBirthdayTimeformat());
        					indfm.setTimeZone(TimeZone.getTimeZone("UTC"));
        					Date d = indfm.parse(tmp);
        					if (cols[i] == ContactObject.BIRTHDAY)
        						co.setBirthday(d);
        					else if (cols[i] == ContactObject.ANNIVERSARY)
        						co.setAnniversary(d);
        				}
        			} catch (Exception eee){
        				LOG.warn("Unable to load BIRTHDAY or ANNIVERSARY from LDAP Contact Addressbook: Context: "+session.getContextId()+" Entry: "+location);
        			}
        			
        		} else if (cols[i] == ContactObject.CREATION_DATE || cols[i] == ContactObject.LAST_MODIFIED){
        			tmp = "";
        			try{
        				fetch_data = attribs.get(server.getFieldMapping()[cols[i]][1]);
        				tmp = (String)fetch_data.get();
        			} catch (NullPointerException eee){ }
        			try {
        				if (!tmp.equals("")){
        					DateFormat indfm = new SimpleDateFormat(server.getDateTimeformat());
        					indfm.setTimeZone(TimeZone.getTimeZone("UTC"));
        					Date d = indfm.parse(tmp);
        					if (cols[i] == ContactObject.CREATION_DATE)
        						co.setCreationDate(d);
        					else if (cols[i] == ContactObject.LAST_MODIFIED)
        						co.setLastModified(d);
        				}
        			} catch (Exception eee){
        				LOG.warn("Unable to load CREATION_DATE or LAST_MODIFIED from LDAP Contact Addressbook: Context: "+session.getContextId()+" Entry: "+location);
        			}
        		} else if (cols[i] == ContactObject.IMAGE1){
        			tmp = "";
    				byte[] img = null;
    				javax.naming.directory.Attribute attr = null;
        			try{
        				if (LOG.isDebugEnabled())
        					LOG.debug("TRY TO LOAD IMAGE -> "+server.getFieldMapping()[ContactObject.IMAGE1][1]);
        				
        				attr = attribs.get(server.getFieldMapping()[ContactObject.IMAGE1][1]);
        				img = (byte[])attr.get();
        			} catch (NullPointerException eee){ }
        			try {
        				co.setImage1(img);
        				co.setImageContentType("image/jpg");
        			} catch (Exception eee){
        				LOG.warn("Unable to load LDAP addressbook image: Folder"+server.getFolderId()+" Context "+session.getContextId()+" Location"+location);
        			}
        		}else{        			
        			fetch_data = attribs.get(server.getFieldMapping()[cols[i]][1]);
        			tmp = (String)fetch_data.get();
        		} 
        		
        		Contacts.mapping[cols[i]].setValueAsString(tmp, co);
                
            }catch(Exception npe){
            	if (LOG.isDebugEnabled())
            		LOG.debug("Unable to load LDAP field: Field "+cols[i]+" Folder"+server.getFolderId()+" Context "+session.getContextId()+" Location"+location);
            }
        }
        
        co.setDisplayName(co.getSurName()+", "+co.getGivenName());
        
		return co;
	}
	
	
	private class ContactLdapIterator implements SearchIterator<Object> {
        
        private ContactObject nexto; 
        private ContactObject pre;

        private int[] cols; 
        private boolean first = true;
        private NamingEnumeration<?> ne;
        private javax.naming.Context ctx;

		private ContactLdapIterator(NamingEnumeration<?> ne, int[] cols, javax.naming.Context ctx) throws OXException, NamingException {
			this.cols = cols;
			this.ne = ne;
			this.ctx = ctx;
			
			if (ne.hasMore()) {
    			nexto = convertLdap2ContactObject(ne, cols);
			}
		}
		
		@OXThrows(
				category=Category.CODE_ERROR,
				desc="16",
				exceptionId=16,
				msg="An error occured during the close of an LDAP addressbook: Folder %1$d Context %2$d"
		)
		public void close() throws SearchIteratorException {
			try {
				ctx.close();
			} catch (NamingException e) {
				throw EXCEPTIONS.createSearchIteratorException(16, server.getFolderId(), session.getContextId());
			}
		}

		public boolean hasNext() {
			if (!first){
				nexto = pre;	
			}
			return nexto != null; 
		}

		public boolean hasSize() {
			return false;
		}

		@OXThrows(
				category=Category.CODE_ERROR,
				desc="17",
				exceptionId=17,
				msg="An error occured during the load of an LDAP addressbook: Folder %1$d Context %2$d"
		)
		public Object next() throws OXException {
			try {
				if (ne.hasMore()) {
					pre = convertLdap2ContactObject(ne, cols);
				} else {
					pre = null;
				}
			} catch (NamingException e) {
				throw EXCEPTIONS.create(17, server.getFolderId(), session.getContextId());
			}
	    	if (first) {
	    		first = false;
	    	}

	    	return nexto;
		}

		public int size() {
			return 0;
		}
	}

	@OXThrowsMultiple(
			category={ Category.SETUP_ERROR,
					   Category.SETUP_ERROR,
					   Category.CODE_ERROR,
					   Category.SETUP_ERROR,
					   Category.SETUP_ERROR},
			desc={"8", "9","10","11","12"},
			exceptionId={8,9,10,11,12},
			msg={ NOT_ALLOWED_LDAP,		
				  NOT_ALLOWED_LDAP,
				  ContactException.INIT_CONNECTION_FROM_DBPOOL,
				  NOT_ALLOWED_LDAP,
				  NOT_ALLOWED_LDAP}
	)
	private void checkReadRights(Session session) throws OXException {

		if (oxContext.getContextId() != server.getContext()){
			throw EXCEPTIONS.createOXPermissionException(8, server.getFolderId(), session.getContextId());
		}
		
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(oxContext);
			
			final FolderObject contactFolder = new OXFolderAccess(readCon, oxContext).getFolderObject(server.getFolderId());
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXPermissionException(9, server.getFolderId(), session.getContextId());
			}
			
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(oxContext, readCon);
			}
			throw e;
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(10,e);
		}	
			
		try {
			final EffectivePermission oclPerm = new OXFolderAccess(readCon, oxContext).getFolderPermission(server.getFolderId(), session.getUserId(), userConfiguration);
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXPermissionException(11, server.getFolderId(), session.getContextId());
			}
			if (!oclPerm.canReadAllObjects()) {
				throw EXCEPTIONS.createOXPermissionException(12, server.getFolderId(), session.getContextId());
			}
		} catch (OXException e) {
			throw e;
		}  finally {
			if (readCon != null) {
				DBPool.closeReaderSilent(oxContext, readCon);
			}
		}
	}
	
	@OXThrows(
			category=Category.CODE_ERROR,
			desc="13",
			exceptionId=13,
			msg="Unable to load this LDAP addressbook: %1$s"
	)
	public void setSession(Session s) throws ContactException {
		this.session = s;
		try{
			this.oxContext = (Context)ContextStorage.getStorageContext(session);
			this.userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), oxContext);
		} catch (ContextException ce){
			throw EXCEPTIONS.create(13, server.getFolderId());
		}
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="14",
			exceptionId=14,
			msg=NOT_SUPPORTED_LDAP
	)
	public void deleteContactObject(int oid, int fuid, Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
    	throw EXCEPTIONS.create(14, server.getFolderId(), session.getContextId());
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="15",
			exceptionId=15,
			msg=NOT_SUPPORTED_LDAP
	)
	public void insertContactObject(ContactObject co) throws OXException {
    	throw EXCEPTIONS.create(15, server.getFolderId(), session.getContextId());
	}

	@OXThrows(
			category=Category.SETUP_ERROR,
			desc="16",
			exceptionId=16,
			msg=NOT_SUPPORTED_LDAP
	)
	public void updateContactObject(ContactObject co, int fid, Date d) throws OXException, OXConcurrentModificationException, ContactException {
    	throw EXCEPTIONS.create(16, server.getFolderId(), session.getContextId());
	}
	
} 
