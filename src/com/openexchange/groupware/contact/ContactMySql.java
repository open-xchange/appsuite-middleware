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



package com.openexchange.groupware.contact;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.date.FormatDate;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderTools;

/**
 ContactMySql
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>

 */

public class ContactMySql implements ContactSql {

	private static final Log LOG = LogFactory.getLog(ContactMySql.class);
	
	private String select = "SELECT co.intfield01" + ",co.sid,"
			+ "co.timestampfield01," + "co.field03,"
			+ "co.field04," + "co.field06,"
			+ "co.field07," + "co.field09,"
			+ "co.field10," + "co.intfield03,"
			+ "co.field79 FROM prg_contacts AS co ";

	private String where = "";
	private String order = "";
	private int user;
	private int can_read_only_own;
	private int folder;
	private String all_folders;
	private ContactSearchObject cso;
	
	private long changed_since;
	private long created_since;
	private long both_since;

	private String start_character;
	private String start_character_field;
	private String search_habit = " AND ";

	private int[][] object_id_array;
	private int userid;
	private boolean internal_user_only;
	private int objectID;
	
	Context ctx;
	SessionObject so;
	
	public ContactMySql(SessionObject so) {
		if (so != null){
			this.ctx = so.getContext();
			this.so = so;
			this.user = so.getUserConfiguration().getUserId();
		}
	}

	public ContactMySql(Context ctx, int userId) {
		this.ctx = ctx;
		this.user = userId;
	}

	public String getWhere() {

		StringBuilder sb = new StringBuilder(" WHERE co.cid = "+ctx.getContextId()+" AND ");

		// Can read only own objects in folder
		if (can_read_only_own != 0) {
			sb.append(new StringBuilder(" (co.created_from = ").append(can_read_only_own).append(") AND "));
		}

		// only internal user
		if (internal_user_only){
			sb.append(" (co.userid is not null) AND (fid = "+FolderObject.SYSTEM_LDAP_FOLDER_ID+") AND ");
		}
		
		// get a user by id
		if (userid > 0){
			sb.append(new StringBuilder(" (co.userid = "+userid+") AND "));
		}
		
		// range search in time for field changed_from
		if (changed_since > 0) {
			sb.append(new StringBuilder(" (co.changing_date >= " + changed_since+ ") AND "));
		}

		// range search in time for field created_from
		if (created_since > 0) {
			sb.append(new StringBuilder(" (co.creating_date >= " + created_since + ") AND "));
		}

		// range search in time for field created_from and changed_from
		if (created_since > 0) {
			sb.append(new StringBuilder(" (co.creating_date >= " + both_since
					+ " OR (co.changed_from >= " + both_since
					+ ")) AND "));
		}

		// get an object by id
		if (objectID > 0){
				sb.append(new StringBuilder(" (co.intfield01 = "+objectID+") AND "));
		}
		
		// get a bunch of objects by id
		if (object_id_array != null &&  object_id_array.length > 0){
			sb.append(" ( ");
			for (int i=0;i<object_id_array.length;i++){
				final int oidx = object_id_array[i][0];
				final int fidx = object_id_array[i][1];
				sb.append(new StringBuilder(" (co.intfield01 = "+oidx+" AND co.fid = "+fidx+") "));
				if (i < (object_id_array.length-1)){
					sb.append(" OR ");
				}
			}
			sb.append(" ) AND ");
		}
			
		// Only contacts with a given startcharacter
		if (start_character != null) {
			String field = null;
			if (start_character_field == null){
				field = ContactConfig.getProperty("contact_first_letter_field");
			}else{
				field = start_character_field;
			}
			String p = start_character;

			if (p.trim().equals(".") || p.trim().equals("#")) {
				sb.append(new StringBuilder(" (((" + field	+ " < '0%') OR (" + field	+ " > 'z%')) AND (" + field	+ " NOT LIKE 'z%')) AND "));
			} else if (
					p.trim().equals("0") || 
					p.trim().equals("1") || 
					p.trim().equals("2") || 
					p.trim().equals("3") || 
					p.trim().equals("4") || 
					p.trim().equals("5") || 
					p.trim().equals("6") || 
					p.trim().equals("7") || 
					p.trim().equals("8") ||					
					p.trim().equals("9")){
				sb.append(new StringBuilder(" (((" + field	+ " > '0%') AND (" + field	+ " < 'a%')) AND (" + field	+ " NOT LIKE 'a%')) AND "));
			} else if (!p.trim().equals(".") && !p.trim().equals("all")) {
				sb.append(new StringBuilder(" (UPPER(" + field + ") LIKE UPPER('"	+ p + "%')) AND "));
			}
		}


		if (cso != null){
			
			sb.append(" ( ");
			
			/*********************** * search all fields * ***********************/ 
			
			if (cso.getPattern() != null && cso.getPattern().length() > 0){
				cso.setDisplayName(cso.getPattern());
			}
			
			if(cso.getDynamicSearchField() != null && cso.getDynamicSearchField().length > 0){			
				int[] fields = cso.getDynamicSearchField();
				String[] values = cso.getDynamicSearchFieldValue();
				
				for (int i=0;i<fields.length;i++){
					if ( (fields[i] == ContactObject.ANNIVERSARY) || (fields[i] == ContactObject.BIRTHDAY)){
						String field = "";
						if (fields[i] == ContactObject.ANNIVERSARY){
							field = Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName();
						}else if (fields[i] == ContactObject.BIRTHDAY){
							field = Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName();
						}
						String value = values[i];
						sb.append(new StringBuilder(" ( co." + field + " LIKE " + value + ") " + search_habit	+ ' '));						
					} else	if ( fields[i] == ContactObject.NUMBER_OF_DISTRIBUTIONLIST || fields[i] == ContactObject.NUMBER_OF_LINKS ) {
						String field = "";
						if (fields[i] == ContactObject.NUMBER_OF_DISTRIBUTIONLIST){
							field = Contacts.mapping[ContactObject.NUMBER_OF_DISTRIBUTIONLIST].getDBFieldName();
						}else if (fields[i] == ContactObject.NUMBER_OF_LINKS){
							field = Contacts.mapping[ContactObject.NUMBER_OF_LINKS].getDBFieldName();
						}
						String value = values[i];						
						sb.append(new StringBuilder('(' + "co." + field + " = "+value+") "  + search_habit	+ ' '));						
					} else	if ( fields[i] == ContactObject.CATEGORIES ){	
						String field = Contacts.mapping[ContactObject.CATEGORIES].getDBFieldName();
						String value = values[i];
						
						if (!value.equals("*")) {
							value = value.replace('*', '%');
							value = value.replace('?', '_');
							value = value.replaceAll("'", "\\\\'");

							if (value.indexOf(',') != -1) {
								StringTokenizer sr = new StringTokenizer(value, ",");
								sb.append('(');

								while (sr.hasMoreElements()) {
									String t = sr.nextToken().trim();
									sb.append(new StringBuilder(" ( co." + field + " LIKE '%" + t.toUpperCase() + "%'  OR "));
								}
								String x = sb.toString();
								sb = new StringBuilder(x.substring(0, x.length() - 3));
								sb.append(new StringBuilder(") " + search_habit + ' '));						
							}
						}
					} else {
						String field = Contacts.mapping[fields[i]].getDBFieldName();
						String value = values[i];
						
						if (!value.equals("*")) {
							value = value.replace('*', '%');
							value = value.replace('?', '_');
							value = value.replaceAll("'", "\\\\'");

							if (value.indexOf('%') != -1) {
								sb.append(new StringBuilder("( co." + field + " LIKE '" + value+ "' " + search_habit + ' '));
							} else {
								sb.append(new StringBuilder("( co." + field + " LIKE '%" + value+ "%' "+ search_habit + ' '));
							}
						}
					}
				}
			}

			/*********************** * search ranges * ***********************/ 
			
			if(cso.getAnniversaryRange() != null && cso.getAnniversaryRange().length > 0){
				Date[] d = cso.getAnniversaryRange();
				try {
					String a = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					String b = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					sb.append(getRangeSearch(Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName(),a,b,search_habit));
				} catch (Exception e) {
					LOG.error("Could not Format Anniversary Date for Range Search! ",e);
				}
			}
			if(cso.getBirthdayRange() != null && cso.getBirthdayRange().length > 0){			
				Date[] d = cso.getBirthdayRange();
				try {
					String a = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					String b = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					sb.append(getRangeSearch(Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName(),a,b,search_habit));
				} catch (Exception e) {
					LOG.error("Could not Format Birthday Date for Range Search! ",e);
				}
			}
			if(cso.getBusinessPostalCodeRange() != null && cso.getBusinessPostalCodeRange().length > 0){			
				String[] x = cso.getBusinessPostalCodeRange();
				sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_BUSINESS].getDBFieldName(),x[0],x[1],search_habit));
			}
			if(cso.getCreationDateRange() != null && cso.getCreationDateRange().length > 0){			
				Date[] d = cso.getCreationDateRange();
				try {
					String a = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					String b = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					sb.append(getRangeSearch(Contacts.mapping[ContactObject.CREATION_DATE].getDBFieldName(),a,b,search_habit));
				} catch (Exception e) {
					LOG.error("Could not Format Creating_Date Date for Range Search! ",e);
				}
			}
			if(cso.getLastModifiedRange() != null && cso.getLastModifiedRange().length > 0){			
				Date[] d = cso.getLastModifiedRange();
				try {
					String a = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					String b = new FormatDate(so.getLanguage().toLowerCase(), so.getLanguage().toUpperCase()).formatDateForPostgres(d[0], false);
					sb.append(getRangeSearch(Contacts.mapping[ContactObject.LAST_MODIFIED].getDBFieldName(),a,b,search_habit));
				} catch (Exception e) {
					LOG.error("Could not Format LastModified Date for Range Search! ",e);
				}
			}
			if(cso.getNumberOfEmployeesRange() != null && cso.getNumberOfEmployeesRange().length > 0){			
				String[] x = cso.getNumberOfEmployeesRange();
				sb.append(getRangeSearch(Contacts.mapping[ContactObject.NUMBER_OF_EMPLOYEE].getDBFieldName(),x[0],x[1],search_habit));
			}
			if(cso.getOtherPostalCodeRange() != null && cso.getOtherPostalCodeRange().length > 0){			
				String[] x = cso.getOtherPostalCodeRange();
				sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_OTHER].getDBFieldName(),x[0],x[1],search_habit));
			}
			if(cso.getPrivatePostalCodeRange() != null && cso.getPrivatePostalCodeRange().length > 0){			
				String[] x = cso.getPrivatePostalCodeRange();
				sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_HOME].getDBFieldName(),x[0],x[1],search_habit));
			}
			if(cso.getSalesVolumeRange() != null && cso.getSalesVolumeRange().length > 0){			
				String[] x = cso.getSalesVolumeRange();
				sb.append(getRangeSearch(Contacts.mapping[ContactObject.SALES_VOLUME].getDBFieldName(),x[0],x[1],search_habit));
			}

			/*********************** * search single field * ***********************/ 
			
			if(cso.getGivenName() != null && cso.getGivenName().length() > 0){	
				String field = Contacts.mapping[ContactObject.GIVEN_NAME].getDBFieldName();

				String value = cso.getGivenName();
				value = value.replace('*', '%');
				value = value.replace('?', '_');
				value = value.replaceAll("'", "\\\\'");

				if (value.indexOf('%') != -1) {
					sb.append(new StringBuilder('(' + "co." + field + " LIKE '" + value + "') " + search_habit	+ ' '));
				} else {
					sb.append(new StringBuilder('(' + "co." + field + " LIKE '%" + value + "%') " + search_habit	+ ' '));
				}
			}
			if(cso.getDisplayName() != null && cso.getDisplayName().length() > 0){			
				String field = Contacts.mapping[ContactObject.DISPLAY_NAME].getDBFieldName();

				String value = cso.getDisplayName();
				value = value.replace('*', '%');
				value = value.replace('?', '_');
				value = value.replaceAll("'", "\\\\'");

				if (value.indexOf('%') != -1) {
					sb.append(new StringBuilder('(' + "co." + field + " LIKE '" + value + "') " + search_habit	+ ' '));
				} else {
					sb.append(new StringBuilder('(' + "co." + field + " LIKE '%" + value + "%') " + search_habit	+ ' '));
				}
			}
			if(cso.getCatgories() != null && cso.getCatgories().length() > 0){			
				String field = Contacts.mapping[ContactObject.CATEGORIES].getDBFieldName();
				String value = cso.getCatgories();

				if (!value.equals("*")) {
					value = value.replace('*', '%');
					value = value.replace('?', '_');
					value = value.replaceAll("'", "\\\\'");

					if (value.indexOf(',') != -1) {
						StringTokenizer sr = new StringTokenizer(value, ",");
						sb.append('(');

						while (sr.hasMoreElements()) {
							String t = sr.nextToken().trim();
							sb.append(new StringBuilder("( co." + field+ " LIKE '%" + t + "%') OR "));
						}
						String x = sb.toString();
						sb = new StringBuilder(x.substring(0, x.length() - 3));
						sb.append(new StringBuilder(") " + search_habit + ' '));						
					}
				}
			}
			
			if(cso.getCompany() != null && cso.getCompany().length() > 0){			
				String field = Contacts.mapping[ContactObject.COMPANY].getDBFieldName();

				String value = cso.getCompany();
				value = value.replace('*', '%');
				value = value.replace('?', '_');
				value = value.replaceAll("'", "\\\\'");

				if (value.indexOf('%') != -1) {
					sb.append(new StringBuilder("( co." + field + " LIKE '" + value + "' ) " + search_habit	+ ' '));
				} else {
					sb.append(new StringBuilder("( co." + field + " LIKE '%" + value + "%' ) " + search_habit + ' '));
				}
			}
			
			String tmpp = sb.toString().trim();
			if (tmpp.lastIndexOf('(') == (tmpp.length()-1)) {
				sb = new StringBuilder(tmpp.substring(0,tmpp.length() -2)+' ');
			} else {
				if (sb.toString().lastIndexOf(search_habit) != -1){
					sb = new StringBuilder(sb.substring(0, sb.lastIndexOf(search_habit)));
				}
				sb.append(") AND ");
			}
			
			/*********************** * search in all folder or subfolder * ***********************/ 
			
			if (cso.isAllFolders()){
				folder = -1;
				sb.append(new StringBuilder(' '+cso.getAllFolderSQLINString()+" AND "));

				/**
				 * TODO
				 *  Search In Subfolder
				 */ 
				
				/*
			} else if (cso.isSubfolderSearch()){

				Connection readcon = null;
				try{
					readcon = DBPool.pickup(ctx);
					sb.append(" ( fid in ( ");
					sb.append(OXFolderTools.getSubfolderList(cso.getFolder(), user, memberingroup, ctx, readcon));
					sb.append(") AND ");
				}catch (Exception e){
					LOG.error("An Error occurred during readconnection fetch: ",e);			
				} finally {
					if (readcon != null) {
						DBPool.closeReaderSilent(ctx, readcon);
					}
				}
				*/
			} else if (cso.getFolder() != -1){
				sb.append(new StringBuilder(" (co.fid = " + cso.getFolder() + ") AND "));
			}
			
		}
		
		// Normal Folder
		if (folder != 0 && folder != -1) {
			sb.append(new StringBuilder(" (co.fid = " + folder + ") AND "));
		}

		sb.append(' ');

		String remove = sb.toString();
		if (remove.lastIndexOf("AND") != -1){
			remove = remove.substring(0, remove.lastIndexOf("AND"));
		}
		/*
		 *  Private Flag 
		 */
		where = remove + " AND ((co.pflag = 1 and co.created_from = " + user	+ ") OR (co.pflag is null))";

		return where;
	}
	
	public String getOrder() {
		return order;
	}
	
	public String getSqlCommand() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSelect());
		String where = getWhere();
		if (all_folders != null && all_folders.length() > 1){
			sb.append(all_folders);
		}
		sb.append(where);
		sb.append(getOrder());
		LOG.debug(new StringBuilder("ContactSQL Query: ").append(sb.toString()));
		//System.out.println(new StringBuilder("ContactSQL Query: "+sb.toString()));
		return sb.toString();
	}

	public String getSelect() {
		return this.select;
	}

	public void setSelect(final String select) {
		this.select = select;
	}

	public void setOrder(final String order) {
		this.order = order;
	}

	public void setWhere(final String where) {
		this.where = where;
	}

	public void setFolder(final int folder) {
		this.folder = folder;
	}

	public void setObjectID(final int objectID) {
		this.objectID = objectID;
	}
	
	public void setReadOnlyOwnFolder(final int onlyown) {
		this.can_read_only_own = onlyown;
	}

	public void setContactSearchObject(final ContactSearchObject cso){
		this.cso = cso;
	}

	public void setObjectArray(final int[][] object_id) {
		this.object_id_array = object_id;
	}

	public void getInternalUsers() {
		this.internal_user_only = true;
	}

	public void setInternalUser(final int userid) {
		this.userid = userid;
	}

	public void setStartCharacter(final String start_character) {
		this.start_character = start_character;
	}

	public void setStartCharacterField(final int field) {
		int[] x = new int[1];
		x[0] = field;
		this.start_character_field = buildContactSelectString(x);
	}
	public void setStartCharacterField(final String field) {
		this.start_character_field = field;
	}
	
	public void setSearchHabit(final String habit) {
		this.search_habit = habit;
	}

	public void getAllChangedSince(final long chs) {
		this.changed_since = chs;
	}

	public void getAllCreatedSince(final long crs)  {
		this.created_since =crs;
	}

	public void getAllSince(final long bs) {
		this.both_since = bs;
	}
	
	public String buildContactSelectString(final int cols[])  {
		final StringBuffer sb = new StringBuffer();

		for (int a=0;a<cols.length;a++){
			if (Contacts.mapping[cols[a]] != null){
				sb.append(new StringBuilder("co."+Contacts.mapping[cols[a]].getDBFieldName()+','));
			}else{
				LOG.warn("UNKNOWN FIELD -> "+cols[a]);
			}
		}

		return sb.toString().substring(0, sb.length() - 1);
	}

	public String getRangeSearch(final String field, final String a, final String b, final String sh){
		StringBuffer sb = new StringBuffer();

		String von = "*";
		String bis = "*";

		if (a != null && a.length() > 0 && !a.equals("*")) {
			von = a;
		}
		if (b != null && b.length() > 0 && !b.equals("*")) {
			bis = b;
		}
		if (!von.equals("*")) {
			sb.append(new StringBuilder("co." + field + " >= '" + von + "' " + sh+ ' '));
		}
		if (!bis.equals("*")) {
			sb.append(new StringBuilder("co." + field + " <= '" + bis + "' " + sh+ ' '));
		}
		return sb.toString();
	}

	public StringBuilder buildAllFolderSearchString(int user, int[] group, SessionObject so, Connection readcon) throws SQLException, DBPoolingException, OXException, SearchIteratorException{
		
		SearchIterator si;
		try{
			si = OXFolderTools.getAllVisibleFoldersIteratorOfModule(user, group, so.getUserConfiguration().getAccessibleModules(), FolderObject.CONTACT, so.getContext());
		} catch (OXException e){
			throw e;
		}
			
        EffectivePermission oclp = null;
        FolderObject fo = null;

        StringBuilder read_all = new StringBuilder();
        StringBuilder read_own = new StringBuilder();
        
        StringBuilder tmp;
        
        while (si.hasNext()) {
            fo  = (FolderObject)si.next();
            oclp = fo.getEffectiveUserPermission(user, so.getUserConfiguration());            
           
            if (!oclp.canReadAllObjects() && oclp.canReadOwnObjects()){
            	tmp = new StringBuilder(Integer.toString(fo.getObjectID())).append(',');
            	read_own.append(tmp);  
            } else if (oclp.canReadAllObjects()){
            	tmp = new StringBuilder(Integer.toString(fo.getObjectID())).append(',');
            	read_all.append(tmp);  
            }
        }
                
        StringBuilder result = new StringBuilder("");
        
        if (read_all.length() > 0 && read_own.length() > 0){
        	read_all.deleteCharAt(read_all.lastIndexOf(","));
        	read_own.deleteCharAt(read_own.lastIndexOf(","));
        	result.append(new StringBuilder("((co.fid IN ("+read_all+")) OR (co.fid IN ("+read_own+") and co.created_from = "+user+" ))"));
        } else if (read_all.length() < 1&& read_own.length() > 0){
        	read_own.deleteCharAt(read_own.lastIndexOf(","));
        	result.append(new StringBuilder("(co.fid IN ("+read_own+") AND co.created_from = "+user+" )"));
        } else if (read_all.length() > 0&& read_own.length() < 1){
        	read_all.deleteCharAt(read_all.lastIndexOf(","));
        	result.append(new StringBuilder("(co.fid IN ("+read_all+")) "));
        } else {
        	result.append("(co.fid IN (-1)) ");
        }

		return result;
	}
	
	/*************************************************************************/
	
	private static String rightsSelectString = "SELECT co.intfield01,co.intfield02,co.intfield03,co.intfield04,co.fid,co.created_from,co.pflag,co.cid FROM prg_contacts AS co ";
	
	public String iFgetRightsSelectString(){
		return rightsSelectString;
	}
	
	public String iFgetFolderSelectString(int fid, int cid){
		return new StringBuilder(rightsSelectString +" where fid = "+fid+" AND cid = "+cid).toString();
	}
	
	public String iFgetNumberOfContactsString(){
		return "SELECT COUNT(co.intfield01) FROM prg_contacts AS co ";
	}
	
	public String iFgetRightsSelectString(int uid, int cid){
		return new StringBuilder(rightsSelectString +" where created_from = "+uid+" AND cid = "+cid).toString();
	}
	
	public String iFcontainsForeignObjectInFolder(int fid, int uid,int cid){
		return new StringBuilder(" SELECT intfield01 FROM prg_contacts where fid = "+fid+" AND cid = "+cid+" AND created_from != "+uid+" AND ((pflag = 1 and created_from != " + uid+ ") OR (pflag is null))").toString();
	}
	
	public String iFdeleteDistributionListEntriesByIds(int cid){
		return new StringBuilder("DELETE FROM prg_dlist where intfield01 = ? AND intfield02 IS NULL AND intfield03 IS NULL AND cid = ").append(cid).toString();
	}

	public String iFfillDistributionListArray(int id, int cid){
		return new StringBuilder("Select intfield01, intfield02, intfield03, intfield04, field01, field02, field03, field04 from prg_dlist where intfield01 = "+id+" AND cid = "+cid).toString();
	}
	
	public String iFwriteDistributionListArrayInsert(){
		return "INSERT INTO prg_dlist (intfield01, intfield02, intfield03, field01, field02, field03, field04, cid, intfield04) VALUES (?,?,?,?,?,?,?,?,?)";
	}
	
	public String iFupdateDistributionListEntriesByIds(){
		return "UPDATE prg_dlist set intfield01 = ?, intfield02 = ?, intfield03 = ?, intfield04 = ?, field01 = ?, field02 = ?, field03 = ?, field04 = ? WHERE (intfield01 = ?) AND (intfield02 = ?) AND (intfield03 = ?) AND (cid = ?)";
	}
	
	public String iFdeleteDistributionListEntriesByIds2(){
		return "DELETE FROM prg_dlist where intfield01 = ? AND intfield02 = ? AND intfield03 = ? AND  cid = ?";
	}
	
	public String iFgetFillLinkArrayString(int id, int cid){
		return new StringBuilder("Select intfield01, intfield02, field01, field02 from prg_contacts_linkage where intfield01 = "+ id + " AND cid = "+cid).toString();
	}
	
	public String iFwriteContactLinkArrayInsert(){
		return "INSERT INTO prg_contacts_linkage (intfield01, intfield02, field01, field02, cid) VALUES (?,?,?,?,?)";
	}
	
	public String iFgetdeleteLinkEntriesByIdsString(){
		return "DELETE FROM prg_contacts_linkage where intfield01 = ? AND intfield02 = ? AND cid = ?";
	}
	
	public String iFgetContactImageLastModified(int id, int cid) {
		return new StringBuilder("SELECT changing_date from prg_contacts_image WHERE intfield01 = "+id+" AND cid = "+cid).toString();
	}
	
	public String iFgetContactImage(int contact_id, int cid){
		return new StringBuilder("SELECT image1, changing_date, mime_type  from prg_contacts_image WHERE intfield01 = "+contact_id+" AND cid = "+cid).toString();
	}
	
	public String iFwriteContactImage()  {
		return new StringBuilder("INSERT INTO prg_contacts_image (intfield01, image1, mime_type, cid, changing_date) VALUES (?,?,?,?,"+System.currentTimeMillis()+')').toString();
	}
	
	public String iFupdateContactImageString()  {
		return new StringBuilder("UPDATE prg_contacts_image SET intfield01 = ?, image1 = ?, mime_type = ?, cid = ?, changing_date = "+System.currentTimeMillis()+" WHERE intfield01 = ? AND cid = ? ").toString();
	}
	
	public StringBuilder iFperformContactStorageInsert(StringBuilder insert_fields, StringBuilder insert_values, int user, long lmd, int cid, int id){
		StringBuilder insert = new StringBuilder("INSERT INTO prg_contacts ("+	insert_fields + "created_from,"+ "changed_from," + "creating_date," + "changing_date," + "intfield01,"+"cid "+ ") VALUES ( " + insert_values.toString() + user + ','
			+ user + ',' +lmd+','+ lmd +',' + id + ','+cid+") ");
		return insert;
	}
	
	public StringBuilder iFperformContactStorageUpdate(StringBuilder update, long lmd, int id, int cid){
		StringBuilder updater = new StringBuilder("UPDATE prg_contacts SET " + update + "changed_from = " + user + ',' + "changing_date =  "+lmd + " WHERE intfield01 = "+ id+ " AND cid = "+cid );
		return updater;
	}
	
	public StringBuilder iFgetContactById(StringBuilder sb){
		sb = new StringBuilder("SELECT ").append(sb);
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from prg_contacts AS co ");
		return sb;
	}
	
	public String iFdeleteContactObject(int oid, int cid){
		return new StringBuilder("SELECT fid, created_from, changing_date, pflag from prg_contacts where intfield01 = "+oid+" AND cid = "+cid).toString();
	}
	
	public StringBuilder iFgetColsStringFromDeleteTable(int[] cols){
		StringBuilder sb = new StringBuilder("SELECT "+ buildContactSelectString(cols)+ ",co.fid,co.cid,co.created_from,co.creating_date,co.changed_from,co.changing_date, co.intfield01 from del_contacts AS co ");
		return sb;
	}
	
	public StringBuilder iFgetColsString(int[] cols){
		StringBuilder sb = new StringBuilder("SELECT "+ buildContactSelectString(cols)+ ",co.fid,co.cid,co.created_from,co.creating_date,co.changed_from,co.changing_date, co.intfield01 from prg_contacts AS co ");
		return sb;
	}
	
	public void iFdeleteContact(int id, int cid, Statement del) throws SQLException {
		StringBuilder tmp = new StringBuilder("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = " + id + " AND  cid = "+cid);
		LOG.debug(tmp.toString());
		del.execute(tmp.toString());
		
		tmp = new StringBuilder("DELETE FROM prg_contacts WHERE cid = " + cid + " AND intfield01 = " + id);
		LOG.debug(tmp.toString());
		del.execute(tmp.toString());
		
		tmp = new StringBuilder("UPDATE del_contacts SET changing_date = "+System.currentTimeMillis()+" WHERE cid = "+cid+" AND intfield01 = "+id);
		LOG.debug(tmp.toString());
		del.execute(tmp.toString());
	}
	
	public void iFtrashContactsFromFolder(boolean deleteit,Statement del, int oid, int cid) throws SQLException{
		StringBuilder tmp;
		if (deleteit) {
			tmp = new StringBuilder("DELETE FROM prg_contacts WHERE cid = " + cid + " AND intfield01 = " + oid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
		} else {
			
			tmp = new StringBuilder("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = " + oid + " AND  cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());

			tmp = new StringBuilder("DELETE FROM prg_contacts WHERE cid = " + cid + " AND intfield01 = " + oid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
		}
	}
	public String iFtrashContactsFromFolderUpdateString(int fid, int cid){
		return new StringBuilder("UPDATE del_contacts SET changing_date = "+System.currentTimeMillis()+" WHERE cid = "+cid+" AND fid = "+fid).toString();
	}
	
	public void iFtrashDistributionList(boolean delete, int id, int cid, Statement smt) throws SQLException{
		if (delete){
			LOG.debug(new StringBuilder("DELETE from prg_dlist where intfield01 = "+id+" AND cid = "+cid));
			smt.execute("DELETE from prg_dlist where intfield01 = "+id+" AND cid = "+cid);
		}else{
			LOG.debug(new StringBuilder("INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = " + id + " AND  cid = "+cid));
			smt.execute("INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = " + id + " AND  cid = "+cid);
			LOG.debug(new StringBuilder("DELETE FROM prg_dlist WHERE cid = " + cid + " AND intfield01 = " + id));
			smt.execute("DELETE FROM prg_dlist WHERE cid = " + cid + " AND intfield01 = " + id);
		}
	}
	
	public void iFtrashLinks(boolean delete,Statement smt, int id, int cid) throws SQLException {
		StringBuilder tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = "+id+" OR intfield02 = "+id+") AND cid = "+cid);
		LOG.debug(tmp.toString());
		smt.execute(tmp.toString());

	}
	
	public void iFtrashImage(boolean delete, Statement smt, int id, int cid) throws SQLException {
		StringBuilder tmp;
		if (delete){
			tmp = new StringBuilder("DELETE from prg_contacts_image where intfield01 = "+id+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			smt.execute(tmp.toString());
		} else {
			tmp = new StringBuilder("INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = " + id + " AND  cid = "+cid);
			LOG.debug(tmp.toString());
			smt.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts_image where intfield01 = "+id+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			smt.execute(tmp.toString());
		}
	}
	
	public void iFtrashAllUserContacts(boolean delete, Statement del, int cid, int oid, int uid, ResultSet rs, SessionObject so) throws SQLException {
		
		StringBuilder tmp;
		
		if (delete){
			tmp = new StringBuilder("DELETE from prg_dlist where intfield01 = "+oid+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = "+oid+" OR intfield02 = "+oid+") AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts_image where intfield01 = "+oid+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts WHERE cid = "+cid+" AND intfield01 = "+oid);
			LOG.debug(tmp.toString());
            // FIXME quick fix. deleteRow doesn't work because del.execute creates new resultset
            del.execute(tmp.toString());
			// rs.deleteRow();
			
		} else {
			/*
			tmp = new StringBuilder("INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = " + oid + " AND  cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts_image where intfield01 = "+oid+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = " + oid + " AND  cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE FROM prg_dlist WHERE cid = " + cid + " AND intfield01 = " + oid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = "+oid+" OR intfield02 = "+oid+") AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
			tmp = new StringBuilder("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = " + oid + " AND  cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
            tmp = new StringBuilder("DELETE from prg_contacts WHERE cid = "+cid+" AND intfield01 = "+oid);
            LOG.debug(tmp.toString());
            del.execute(tmp.toString());
			// rs.deleteRow();
			
			tmp = new StringBuilder("UPDATE del_contacts SET changed_from = "+so.getContext().getMailadmin()+", created_from = "+so.getContext().getMailadmin()+", changing_date = "+System.currentTimeMillis()+" WHERE intfield01 = "+oid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			*/
			
			tmp = new StringBuilder("UPDATE prg_contacts SET changed_from = "+so.getContext().getMailadmin()+", created_from = "+so.getContext().getMailadmin()+", changing_date = "+System.currentTimeMillis()+" WHERE intfield01 = "+oid+" AND cid = "+cid);
			LOG.debug(tmp.toString());
			del.execute(tmp.toString());
			
		}
	}
	
	public void iFtrashAllUserContactsDeletedEntries(Statement del, int cid, int uid, SessionObject so) throws SQLException {
		StringBuilder tmp = new StringBuilder("UPDATE del_contacts SET changed_from = "+so.getContext().getMailadmin()+", created_from = "+so.getContext().getMailadmin()+", changing_date = "+System.currentTimeMillis()+" WHERE created_from = "+uid+" and cid = "+cid);
		LOG.debug(tmp.toString());
		del.execute(tmp.toString());
	}
}
