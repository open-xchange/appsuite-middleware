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

package com.openexchange.contact.ldap.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.session.Session;

/**
 * 
 * @author <a href="mailto:ben.pahne@open-xchange">Ben Pahne</a>
 */

public class LdapTools {

	private static final Log LOG = LogFactory.getLog(LdapTools.class);
	
    public static final Namespace ld = Namespace.getNamespace("L","LDAP:");
    public static final Namespace ox = Namespace.getNamespace("ox","http://www.open-xchange.org");
	
    public static LdapServer readLdapConfigurationFile(String f) throws OXException, JDOMException, IOException, Exception{
		
		LdapServer server = new LdapServer();
		
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        
        SAXBuilder sxb = new SAXBuilder();
        
        Document doc = sxb.build(isr);
        Element deep = doc.getRootElement();
        
        server = new LdapServer();
        
        try{
            server.setLdapName(deep.getChild("SERVER_NAME", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("SERVER NAME NOT FOUND IN XML TREE");
        }
        
        try{
            server.setServerIP(deep.getChild("SERVER_IP", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("SERVER IP NOT FOUND IN XML TREE");
        }
        
        try{
            server.setPort(deep.getChild("LDAP_PORT", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("LDAP PORT NOT FOUND IN XML TREE");
        }

        try{
            server.setPassword(deep.getChild("PASSWORD", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("PASSWORD NOT FOUND IN XML TREE");
        }
        
        try{
            server.setAddressbookDN(deep.getChild("ADDRESSBOOK_DN", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("ADDRESSBOOK_DN NOT FOUND IN XML TREE");
        }
        
        try{
            server.setBaseDN(deep.getChild("BASE_DN", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("BASE_DN NOT FOUND IN XML TREE");
        }
        
        try{
            server.setBindDN(deep.getChild("BIND_DN", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("BIND_DN NOT FOUND IN XML TREE");
        }
        
        try{
        	server.setBindUser(deep.getChild("BIND_USER", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("BIND_DN NOT FOUND IN XML TREE");
        }
        
        try{
            server.setFolderId(new Integer(deep.getChild("OX_FOLDER_ID", ld).getValue()).intValue());
        }catch (NullPointerException npe){
            throw new Exception("OX_FOLDER_ID NOT FOUND IN XML TREE");
        }
        
        try {
        	server.setListFilter(deep.getChild("LIST_FILTER_RULE", ld).getValue());
        } catch (NullPointerException npe) {
        	throw new Exception("LIST_FILTER_RULE NOT FOUND IN XML TREE");
        }
        
        try {
        	server.setModifiedListFilter(deep.getChild("CHANGE_SINCE_FILTER_RULE", ld).getValue());
        } catch (NullPointerException npe) {
        	throw new Exception("CHANGE_SINCE_FILTER_RULE NOT FOUND IN XML TREE");
        }
        
        try {
        	server.setContactsFilter(deep.getChild("CONTACT_FILTER_RULE", ld).getValue());
        } catch (NullPointerException npe) {
        	throw new Exception("CONTACT_FILTER_RULE NOT FOUND IN XML TREE");
        }
        
        try {
        	server.setSearchFilter(deep.getChild("SEARCH_FILTER_RULE", ld).getValue());
        } catch (NullPointerException npe) {
        	throw new Exception("SEARCH_FILTER_RULE NOT FOUND IN XML TREE");
        }
        
        try {
        	server.setContext(deep.getChild("CONTEXT", ld).getValue());
        } catch (NullPointerException npe) {
        	throw new Exception("CONTEXT NOT FOUND IN XML TREE");
        }
        
        try{
            server.setBirthdayTimeformat(deep.getChild("BIRTHDAY-TIMEFORMAT", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("BIRTHDAY-TIMEFORMAT NOT FOUND IN XML TREE");
        }

        try{
            server.setDateTimeformat(deep.getChild("DATE-TIMEFORMAT", ld).getValue());
        }catch (NullPointerException npe){
            throw new Exception("DATE-TIMEFORMAT NOT FOUND IN XML TREE");
        }
        
        try{
        	String tmp = deep.getChild("INCLUDE_SUBFOLDER", ld).getValue().trim().toLowerCase();
        	if (tmp.equals("yes") || tmp.equals("on") || tmp.equals("true"))
        		server.activateSubfolderList(true);
        }catch (NullPointerException npe){
        	throw new Exception("INCLUDE_SUBFOLDER NOT FOUND IN XML TREE");
        }
        
        try{
        	String auth = deep.getChild("AUTH_TYPE", ld).getValue();       	
        	if (auth.toLowerCase().equals("user")){
            	server.setAuthType(LdapServer.AUTH_USER);
        	} else if (auth.trim().toLowerCase().equals("admin")){
            	server.setAuthType(LdapServer.AUTH_ADMIN);
        	} else if (auth.trim().toLowerCase().equals("anonymous")){
            	server.setAuthType(LdapServer.AUTH_ANONYMOUS);	
        	} else {
        		server.setAuthType(LdapServer.AUTH_ANONYMOUS);
        	}
        }catch (NullPointerException npe){
            throw new Exception("AUTH TYPE NOT FOUND IN XML TREE");
        }
		
        server.setFieldMapping(readFieldMapping(deep));
        
        return server;
	}
	
	private static String[][] readFieldMapping(Element deep) {
		
		Element mapping = deep.getChild("field_mapping", ld);
		String[][] map = new String[700][2];
		
		try{
			map[ContactObject.OBJECT_ID][0] = Contacts.mapping[ContactObject.OBJECT_ID].getDBFieldName();
			map[ContactObject.OBJECT_ID][1] = mapping.getChild("OBJECT_ID", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("OBJECT_ID not found in xml.");
		}
		
		try{
			map[ContactObject.DISPLAY_NAME][0] = Contacts.mapping[ContactObject.DISPLAY_NAME].getDBFieldName();
			map[ContactObject.DISPLAY_NAME][1] = mapping.getChild("DISPLAYNAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("DISPLAYNAME not found in xml.");
		}
		
		try{
			map[ContactObject.SUR_NAME][0] = Contacts.mapping[ContactObject.SUR_NAME].getDBFieldName();
			map[ContactObject.SUR_NAME][1] = mapping.getChild("LAST_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("LAST_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.GIVEN_NAME][0] = Contacts.mapping[ContactObject.GIVEN_NAME].getDBFieldName();
			map[ContactObject.GIVEN_NAME][1] = mapping.getChild("FIRST_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("FIRST_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.MIDDLE_NAME][0] = Contacts.mapping[ContactObject.MIDDLE_NAME].getDBFieldName();
			map[ContactObject.MIDDLE_NAME][1] = mapping.getChild("SECOND_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.SUFFIX][0] = Contacts.mapping[ContactObject.SUFFIX].getDBFieldName();
			map[ContactObject.SUFFIX][1] = mapping.getChild("SUFFIX", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SUFFIX not found in xml.");
		}
		
		try{
			map[ContactObject.TITLE][0] = Contacts.mapping[ContactObject.TITLE].getDBFieldName();
			map[ContactObject.TITLE][1] = mapping.getChild("TITLE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("TITLE not found in xml.");
		}
		
		try{
			map[ContactObject.STREET_HOME][0] = Contacts.mapping[ContactObject.STREET_HOME].getDBFieldName();
			map[ContactObject.STREET_HOME][1] = mapping.getChild("STREET", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("STREET not found in xml.");
		}
		
		try{
			map[ContactObject.POSTAL_CODE_HOME][0] = Contacts.mapping[ContactObject.POSTAL_CODE_HOME].getDBFieldName();
			map[ContactObject.POSTAL_CODE_HOME][1] = mapping.getChild("POSTAL_CODE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("POSTAL_CODE not found in xml.");
		}
		
		try{
			map[ContactObject.CITY_HOME][0] = Contacts.mapping[ContactObject.CITY_HOME].getDBFieldName();
			map[ContactObject.CITY_HOME][1] = mapping.getChild("CITY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("CITY not found in xml.");
		}
		
		try{
			map[ContactObject.STATE_HOME][0] = Contacts.mapping[ContactObject.STATE_HOME].getDBFieldName();
			map[ContactObject.STATE_HOME][1] = mapping.getChild("STATE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("STATE not found in xml.");
		}
		
		try{
			map[ContactObject.COUNTRY_HOME][0] = Contacts.mapping[ContactObject.COUNTRY_HOME].getDBFieldName();
			map[ContactObject.COUNTRY_HOME][1] = mapping.getChild("COUNTRY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("COUNTRY not found in xml.");
		}
		
		try{
			map[ContactObject.BIRTHDAY][0] = Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName();
			map[ContactObject.BIRTHDAY][1] = mapping.getChild("BIRTHDAY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BIRTHDAY not found in xml.");
		}
		
		try{
			map[ContactObject.ANNIVERSARY][0] = Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName();
			map[ContactObject.ANNIVERSARY][1] = mapping.getChild("ANNIVERSARY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("ANNIVERSARY not found in xml.");
		}
		
		try{
			map[ContactObject.MARITAL_STATUS][0] = Contacts.mapping[ContactObject.MARITAL_STATUS].getDBFieldName();
			map[ContactObject.MARITAL_STATUS][1] = mapping.getChild("MARITAL_STATUS", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("MARITAL_STATUS not found in xml.");
		}
		
		try{
			map[ContactObject.NUMBER_OF_CHILDREN][0] = Contacts.mapping[ContactObject.NUMBER_OF_CHILDREN].getDBFieldName();
			map[ContactObject.NUMBER_OF_CHILDREN][1] = mapping.getChild("NUMBER_OF_CHILDREN", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("NUMBER_OF_CHILDREN not found in xml.");
		}
		
		try{
			map[ContactObject.PROFESSION][0] = Contacts.mapping[ContactObject.PROFESSION].getDBFieldName();
			map[ContactObject.PROFESSION][1] = mapping.getChild("PROFESSION", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PROFESSION not found in xml.");
		}
		
		try{
			map[ContactObject.NICKNAME][0] = Contacts.mapping[ContactObject.NICKNAME].getDBFieldName();
			map[ContactObject.NICKNAME][1] = mapping.getChild("NICKNAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("NICKNAME not found in xml.");
		}
		
		try{
			map[ContactObject.SPOUSE_NAME][0] = Contacts.mapping[ContactObject.SPOUSE_NAME].getDBFieldName();
			map[ContactObject.SPOUSE_NAME][1] = mapping.getChild("SPOUSE_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SPOUSE_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.NOTE][0] = Contacts.mapping[ContactObject.NOTE].getDBFieldName();
			map[ContactObject.NOTE][1] = mapping.getChild("NOTE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("NOTE not found in xml.");
		}
		
		try{
			map[ContactObject.COMPANY][0] = Contacts.mapping[ContactObject.COMPANY].getDBFieldName();
			map[ContactObject.COMPANY][1] = mapping.getChild("COMPANY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("COMPANY not found in xml.");
		}
		
		try{
			map[ContactObject.DEPARTMENT][0] = Contacts.mapping[ContactObject.DEPARTMENT].getDBFieldName();
			map[ContactObject.DEPARTMENT][1] = mapping.getChild("DEPARTMENT", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("DEPARTMENT not found in xml.");
		}
		
		try{
			map[ContactObject.POSITION][0] = Contacts.mapping[ContactObject.POSITION].getDBFieldName();
			map[ContactObject.POSITION][1] = mapping.getChild("POSITION", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("POSITION not found in xml.");
		}
		
		try{
			map[ContactObject.EMPLOYEE_TYPE][0] = Contacts.mapping[ContactObject.EMPLOYEE_TYPE].getDBFieldName();
			map[ContactObject.EMPLOYEE_TYPE][1] = mapping.getChild("EMPLOYEE_TYPE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("EMPLOYEE_TYPE not found in xml.");
		}
		
		try{
			map[ContactObject.ROOM_NUMBER][0] = Contacts.mapping[ContactObject.ROOM_NUMBER].getDBFieldName();
			map[ContactObject.ROOM_NUMBER][1] = mapping.getChild("ROOM_NUMBER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("ROOM_NUMBER not found in xml.");
		}
		
		try{
			map[ContactObject.STREET_BUSINESS][0] = Contacts.mapping[ContactObject.STREET_BUSINESS].getDBFieldName();
			map[ContactObject.STREET_BUSINESS][1] = mapping.getChild("BUSINESS_STREET", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_STREET not found in xml.");
		}
		
		try{
			map[ContactObject.POSTAL_CODE_BUSINESS][0] = Contacts.mapping[ContactObject.POSTAL_CODE_BUSINESS].getDBFieldName();
			map[ContactObject.POSTAL_CODE_BUSINESS][1] = mapping.getChild("BUSINESS_POSTAL_CODE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_POSTAL_CODE not found in xml.");
		}
		
		try{
			map[ContactObject.CITY_BUSINESS][0] = Contacts.mapping[ContactObject.CITY_BUSINESS].getDBFieldName();
			map[ContactObject.CITY_BUSINESS][1] = mapping.getChild("BUSINESS_CITY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_CITY not found in xml.");
		}
		
		try{
			map[ContactObject.STATE_BUSINESS][0] = Contacts.mapping[ContactObject.STATE_BUSINESS].getDBFieldName();
			map[ContactObject.STATE_BUSINESS][1] = mapping.getChild("BUSINESS_STATE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_STATE not found in xml.");
		}
		
		try{
			map[ContactObject.COUNTRY_BUSINESS][0] = Contacts.mapping[ContactObject.COUNTRY_BUSINESS].getDBFieldName();
			map[ContactObject.COUNTRY_BUSINESS][1] = mapping.getChild("BUSINESS_COUNTRY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_COUNTRY not found in xml.");
		}
		
		try{
			map[ContactObject.NUMBER_OF_EMPLOYEE][0] = Contacts.mapping[ContactObject.NUMBER_OF_EMPLOYEE].getDBFieldName();
			map[ContactObject.NUMBER_OF_EMPLOYEE][1] = mapping.getChild("NUMBER_OF_EMPLOYEE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("NUMBER_OF_EMPLOYEE not found in xml.");
		}
		
		try{
			map[ContactObject.SALES_VOLUME][0] = Contacts.mapping[ContactObject.SALES_VOLUME].getDBFieldName();
			map[ContactObject.SALES_VOLUME][1] = mapping.getChild("SALES_VOLUME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SALES_VOLUME not found in xml.");
		}
		
		try{
			map[ContactObject.TAX_ID][0] = Contacts.mapping[ContactObject.TAX_ID].getDBFieldName();
			map[ContactObject.TAX_ID][1] = mapping.getChild("TAX_ID", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("TAX_ID not found in xml.");
		}
		
		try{
			map[ContactObject.COMMERCIAL_REGISTER][0] = Contacts.mapping[ContactObject.COMMERCIAL_REGISTER].getDBFieldName();
			map[ContactObject.COMMERCIAL_REGISTER][1] = mapping.getChild("COMMERCIAL_REGISTER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("COMMERCIAL_REGISTER not found in xml.");
		}
		
		try{
			map[ContactObject.BRANCHES][0] = Contacts.mapping[ContactObject.BRANCHES].getDBFieldName();
			map[ContactObject.BRANCHES][1] = mapping.getChild("BRANCHES", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BRANCHES not found in xml.");
		}
		
		try{
			map[ContactObject.BUSINESS_CATEGORY][0] = Contacts.mapping[ContactObject.BUSINESS_CATEGORY].getDBFieldName();
			map[ContactObject.BUSINESS_CATEGORY][1] = mapping.getChild("BUSINESS_CATEGORY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("BUSINESS_CATEGORY not found in xml.");
		}
		
		try{
			map[ContactObject.INFO][0] = Contacts.mapping[ContactObject.INFO].getDBFieldName();
			map[ContactObject.INFO][1] = mapping.getChild("MORE_INFO", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("MORE_INFO not found in xml.");
		}
		
		try{
			map[ContactObject.MANAGER_NAME][0] = Contacts.mapping[ContactObject.MANAGER_NAME].getDBFieldName();
			map[ContactObject.MANAGER_NAME][1] = mapping.getChild("MANAGERS_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("MANAGERS_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.ASSISTANT_NAME][0] = Contacts.mapping[ContactObject.ASSISTANT_NAME].getDBFieldName();
			map[ContactObject.ASSISTANT_NAME][1] = mapping.getChild("ASSISTANTS_NAME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("ASSISTANTS_NAME not found in xml.");
		}
		
		try{
			map[ContactObject.STREET_OTHER][0] = Contacts.mapping[ContactObject.STREET_OTHER].getDBFieldName();
			map[ContactObject.STREET_OTHER][1] = mapping.getChild("SECOND_STREET", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_STREET not found in xml.");
		}
		
		try{
			map[ContactObject.POSTAL_CODE_OTHER][0] = Contacts.mapping[ContactObject.POSTAL_CODE_OTHER].getDBFieldName();
			map[ContactObject.POSTAL_CODE_OTHER][1] = mapping.getChild("SECOND_POSTAL_CODE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_POSTAL_CODE not found in xml.");
		}
		
		try{
			map[ContactObject.CITY_OTHER][0] = Contacts.mapping[ContactObject.CITY_OTHER].getDBFieldName();
			map[ContactObject.CITY_OTHER][1] = mapping.getChild("SECOND_CITY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_CITY not found in xml.");
		}
		
		try{
			map[ContactObject.STATE_OTHER][0] = Contacts.mapping[ContactObject.STATE_OTHER].getDBFieldName();
			map[ContactObject.STATE_OTHER][1] = mapping.getChild("SECOND_STATE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_STATE not found in xml.");
		}
		
		try{
			map[ContactObject.COUNTRY_OTHER][0] = Contacts.mapping[ContactObject.COUNTRY_OTHER].getDBFieldName();
			map[ContactObject.COUNTRY_OTHER][1] = mapping.getChild("SECOND_COUNTRY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("SECOND_COUNTRY not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_ASSISTANT][0] = Contacts.mapping[ContactObject.TELEPHONE_ASSISTANT].getDBFieldName();
			map[ContactObject.TELEPHONE_ASSISTANT][1] = mapping.getChild("PHONE_ASSISTANT", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_ASSISTANT not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_BUSINESS1][0] = Contacts.mapping[ContactObject.TELEPHONE_BUSINESS1].getDBFieldName();
			map[ContactObject.TELEPHONE_BUSINESS1][1] = mapping.getChild("PHONE_BUSINESS", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_BUSINESS not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_BUSINESS2][0] = Contacts.mapping[ContactObject.TELEPHONE_BUSINESS2].getDBFieldName();
			map[ContactObject.TELEPHONE_BUSINESS2][1] = mapping.getChild("PHONE_BUSINESS2", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_BUSINESS2 not found in xml.");
		}
		
		try{
			map[ContactObject.FAX_BUSINESS][0] = Contacts.mapping[ContactObject.FAX_BUSINESS].getDBFieldName();
			map[ContactObject.FAX_BUSINESS][1] = mapping.getChild("FAX_BUSINESS", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("FAX_BUSINESS not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_CALLBACK][0] = Contacts.mapping[ContactObject.TELEPHONE_CALLBACK].getDBFieldName();
			map[ContactObject.TELEPHONE_CALLBACK][1] = mapping.getChild("CALLBACK", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("CALLBACK not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_CAR][0] = Contacts.mapping[ContactObject.TELEPHONE_CAR].getDBFieldName();
			map[ContactObject.TELEPHONE_CAR][1] = mapping.getChild("PHONE_CAR", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_CAR not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_COMPANY][0] = Contacts.mapping[ContactObject.TELEPHONE_COMPANY].getDBFieldName();
			map[ContactObject.TELEPHONE_COMPANY][1] = mapping.getChild("PHONE_COMPANY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_COMPANY not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_HOME1][0] = Contacts.mapping[ContactObject.TELEPHONE_HOME1].getDBFieldName();
			map[ContactObject.TELEPHONE_HOME1][1] = mapping.getChild("PHONE_HOME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_HOME not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_HOME2][0] = Contacts.mapping[ContactObject.TELEPHONE_HOME2].getDBFieldName();
			map[ContactObject.TELEPHONE_HOME2][1] = mapping.getChild("PHONE_HOME2", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_HOME2 not found in xml.");
		}
		
		try{
			map[ContactObject.FAX_HOME][0] = Contacts.mapping[ContactObject.FAX_HOME].getDBFieldName();
			map[ContactObject.FAX_HOME][1] = mapping.getChild("FAX_HOME", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("FAX_HOME not found in xml.");
		}
		
		try{
			map[ContactObject.CELLULAR_TELEPHONE1][0] = Contacts.mapping[ContactObject.CELLULAR_TELEPHONE1].getDBFieldName();
			map[ContactObject.CELLULAR_TELEPHONE1][1] = mapping.getChild("MOBILE1", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("MOBILE1 not found in xml.");
		}
		
		try{
			map[ContactObject.CELLULAR_TELEPHONE2][0] = Contacts.mapping[ContactObject.CELLULAR_TELEPHONE2].getDBFieldName();
			map[ContactObject.CELLULAR_TELEPHONE2][1] = mapping.getChild("MOBILE2", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("MOBILE2 not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_OTHER][0] = Contacts.mapping[ContactObject.TELEPHONE_OTHER].getDBFieldName();
			map[ContactObject.TELEPHONE_OTHER][1] = mapping.getChild("PHONE_OTHER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PHONE_OTHER not found in xml.");
		}
		
		try{
			map[ContactObject.FAX_OTHER][0] = Contacts.mapping[ContactObject.FAX_OTHER].getDBFieldName();
			map[ContactObject.FAX_OTHER][1] = mapping.getChild("FAX_OTHER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("FAX_OTHER not found in xml.");
		}
		
		try{
			map[ContactObject.EMAIL1][0] = Contacts.mapping[ContactObject.EMAIL1].getDBFieldName();
			map[ContactObject.EMAIL1][1] = mapping.getChild("EMAIL1", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("EMAIL1 not found in xml.");
		}
		
		try{
			map[ContactObject.EMAIL2][0] = Contacts.mapping[ContactObject.EMAIL2].getDBFieldName();
			map[ContactObject.EMAIL2][1] = mapping.getChild("EMAIL2", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("EMAIL2 not found in xml.");
		}
		
		try{
			map[ContactObject.EMAIL3][0] = Contacts.mapping[ContactObject.EMAIL3].getDBFieldName();
			map[ContactObject.EMAIL3][1] = mapping.getChild("EMAIL3", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("EMAIL3 not found in xml.");
		}
		
		try{
			map[ContactObject.URL][0] = Contacts.mapping[ContactObject.URL].getDBFieldName();
			map[ContactObject.URL][1] = mapping.getChild("URL", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("URL not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_ISDN][0] = Contacts.mapping[ContactObject.TELEPHONE_ISDN].getDBFieldName();
			map[ContactObject.TELEPHONE_ISDN][1] = mapping.getChild("ISDN", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("ISDN not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_PAGER][0] = Contacts.mapping[ContactObject.TELEPHONE_PAGER].getDBFieldName();
			map[ContactObject.TELEPHONE_PAGER][1] = mapping.getChild("PAGER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PAGER not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_PRIMARY][0] = Contacts.mapping[ContactObject.TELEPHONE_PRIMARY].getDBFieldName();
			map[ContactObject.TELEPHONE_PRIMARY][1] = mapping.getChild("PRIMARY", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("PRIMARY not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_RADIO][0] = Contacts.mapping[ContactObject.TELEPHONE_RADIO].getDBFieldName();
			map[ContactObject.TELEPHONE_RADIO][1] = mapping.getChild("RADIO", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("RADIO not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_TELEX][0] = Contacts.mapping[ContactObject.TELEPHONE_TELEX].getDBFieldName();
			map[ContactObject.TELEPHONE_TELEX][1] = mapping.getChild("TELEX", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("TELEX not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_TTYTDD][0] = Contacts.mapping[ContactObject.TELEPHONE_TTYTDD].getDBFieldName();
			map[ContactObject.TELEPHONE_TTYTDD][1] = mapping.getChild("TTY_TDD", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("TTY_TDD not found in xml.");
		}
		
		try{
			map[ContactObject.INSTANT_MESSENGER1][0] = Contacts.mapping[ContactObject.INSTANT_MESSENGER1].getDBFieldName();
			map[ContactObject.INSTANT_MESSENGER1][1] = mapping.getChild("INSTANT_MESSENGER", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("INSTANT_MESSENGER not found in xml.");
		}
		
		try{
			map[ContactObject.INSTANT_MESSENGER2][0] = Contacts.mapping[ContactObject.INSTANT_MESSENGER2].getDBFieldName();
			map[ContactObject.INSTANT_MESSENGER2][1] = mapping.getChild("INSTANT_MESSENGER2", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("INSTANT_MESSENGER2 not found in xml.");
		}
		
		try{
			map[ContactObject.TELEPHONE_IP][0] = Contacts.mapping[ContactObject.TELEPHONE_IP].getDBFieldName();
			map[ContactObject.TELEPHONE_IP][1] = mapping.getChild("IP_PHONE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("IP_PHONE not found in xml.");
		}
		
		try{
			map[ContactObject.CATEGORIES][0] = Contacts.mapping[ContactObject.CATEGORIES].getDBFieldName();
			map[ContactObject.CATEGORIES][1] = mapping.getChild("CATEGORIES", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("CATEGORIES not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD01][0] = Contacts.mapping[ContactObject.USERFIELD01].getDBFieldName();
			map[ContactObject.USERFIELD01][1] = mapping.getChild("USERFIELD01", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD01 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD02][0] = Contacts.mapping[ContactObject.USERFIELD02].getDBFieldName();
			map[ContactObject.USERFIELD02][1] = mapping.getChild("USERFIELD02", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD02 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD03][0] = Contacts.mapping[ContactObject.USERFIELD03].getDBFieldName();
			map[ContactObject.USERFIELD03][1] = mapping.getChild("USERFIELD03", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD03 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD04][0] = Contacts.mapping[ContactObject.USERFIELD04].getDBFieldName();
			map[ContactObject.USERFIELD04][1] = mapping.getChild("USERFIELD04", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD04 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD05][0] = Contacts.mapping[ContactObject.USERFIELD05].getDBFieldName();
			map[ContactObject.USERFIELD05][1] = mapping.getChild("USERFIELD05", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD05 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD06][0] = Contacts.mapping[ContactObject.USERFIELD06].getDBFieldName();
			map[ContactObject.USERFIELD06][1] = mapping.getChild("USERFIELD06", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD06 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD07][0] = Contacts.mapping[ContactObject.USERFIELD07].getDBFieldName();
			map[ContactObject.USERFIELD07][1] = mapping.getChild("USERFIELD07", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD07 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD08][0] = Contacts.mapping[ContactObject.USERFIELD08].getDBFieldName();
			map[ContactObject.USERFIELD08][1] = mapping.getChild("USERFIELD08", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD08 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD09][0] = Contacts.mapping[ContactObject.USERFIELD09].getDBFieldName();
			map[ContactObject.USERFIELD09][1] = mapping.getChild("USERFIELD09", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD09 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD10][0] = Contacts.mapping[ContactObject.USERFIELD10].getDBFieldName();
			map[ContactObject.USERFIELD10][1] = mapping.getChild("USERFIELD10", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD10 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD11][0] = Contacts.mapping[ContactObject.USERFIELD11].getDBFieldName();
			map[ContactObject.USERFIELD11][1] = mapping.getChild("USERFIELD11", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD11 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD12][0] = Contacts.mapping[ContactObject.USERFIELD12].getDBFieldName();
			map[ContactObject.USERFIELD12][1] = mapping.getChild("USERFIELD12", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD12 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD13][0] = Contacts.mapping[ContactObject.USERFIELD13].getDBFieldName();
			map[ContactObject.USERFIELD13][1] = mapping.getChild("USERFIELD13", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD13 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD14][0] = Contacts.mapping[ContactObject.USERFIELD14].getDBFieldName();
			map[ContactObject.USERFIELD14][1] = mapping.getChild("USERFIELD14", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD14 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD15][0] = Contacts.mapping[ContactObject.USERFIELD15].getDBFieldName();
			map[ContactObject.USERFIELD15][1] = mapping.getChild("USERFIELD15", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD15 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD16][0] = Contacts.mapping[ContactObject.USERFIELD16].getDBFieldName();
			map[ContactObject.USERFIELD16][1] = mapping.getChild("USERFIELD16", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD16 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD17][0] = Contacts.mapping[ContactObject.USERFIELD17].getDBFieldName();
			map[ContactObject.USERFIELD17][1] = mapping.getChild("USERFIELD17", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD17 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD18][0] = Contacts.mapping[ContactObject.USERFIELD18].getDBFieldName();
			map[ContactObject.USERFIELD18][1] = mapping.getChild("USERFIELD18", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD10 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD19][0] = Contacts.mapping[ContactObject.USERFIELD19].getDBFieldName();
			map[ContactObject.USERFIELD19][1] = mapping.getChild("USERFIELD19", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD19 not found in xml.");
		}
		
		try{
			map[ContactObject.USERFIELD20][0] = Contacts.mapping[ContactObject.USERFIELD20].getDBFieldName();
			map[ContactObject.USERFIELD20][1] = mapping.getChild("USERFIELD20", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("USERFIELD20 not found in xml.");
		}
		
		try{
			map[ContactObject.IMAGE1][0] = Contacts.mapping[ContactObject.IMAGE1].getDBFieldName();
			map[ContactObject.IMAGE1][1] = mapping.getChild("IMAGE1", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("IMAGE1 not found in xml.");
		}
		
		try{
			map[ContactObject.CREATION_DATE][0] = Contacts.mapping[ContactObject.CREATION_DATE].getDBFieldName();
			map[ContactObject.CREATION_DATE][1] = mapping.getChild("CREATION_DATE", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("CREATION_DATE not found in xml.");
		}
		
		try{
			map[ContactObject.LAST_MODIFIED][0] = Contacts.mapping[ContactObject.LAST_MODIFIED].getDBFieldName();
			map[ContactObject.LAST_MODIFIED][1] = mapping.getChild("LAST_MODIFIED", ox).getValue();
		} catch (NullPointerException npe){
			LOG.warn("LAST_MODIFIED not found in xml.");
		}
		
		map[ContactObject.FOLDER_ID][0] = Contacts.mapping[ContactObject.FOLDER_ID].getDBFieldName();
		map[ContactObject.FOLDER_ID][1] = Contacts.mapping[ContactObject.FOLDER_ID].getDBFieldName();

		map[ContactObject.CREATED_BY][0] = Contacts.mapping[ContactObject.CREATED_BY].getDBFieldName();
		map[ContactObject.CREATED_BY][1] = Contacts.mapping[ContactObject.CREATED_BY].getDBFieldName();

		map[ContactObject.MODIFIED_BY][0] = Contacts.mapping[ContactObject.MODIFIED_BY].getDBFieldName();
		map[ContactObject.MODIFIED_BY][1] = Contacts.mapping[ContactObject.MODIFIED_BY].getDBFieldName();

		map[ContactObject.CONTEXTID][0] = Contacts.mapping[ContactObject.CONTEXTID].getDBFieldName();
		map[ContactObject.CONTEXTID][1] = Contacts.mapping[ContactObject.CONTEXTID].getDBFieldName();

		
		return map;
	}
    
	public static Hashtable<String, String> buildAuthEnvironment(LdapServer server, Session session){
		
        Hashtable<String,String> env = new Hashtable<String, String>( 10 );
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://"+server.getServerIP()+":"+server.getPort()+"/"+server.getBaseDN());
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        
        if (server.getAuthType() == LdapServer.AUTH_ADMIN){
        	String bind_dn = replaceTag(server.getBindDN(),"[bind_user]",server.getBindUser()); 
       	
            env.put( Context.SECURITY_AUTHENTICATION, "simple" );
            env.put( Context.SECURITY_PRINCIPAL, bind_dn );
            env.put( Context.SECURITY_CREDENTIALS, server.getPassword() );
        }else if (server.getAuthType() == LdapServer.AUTH_USER){
            try{
            	if (server.getBindUser().equals("fred")){
            		server.setBindUser("ben.pahne");
            		server.setPassword("nase");
            	}

            	String user = session.getLoginName();
            	if (user.indexOf('@') != -1)
            		user = user.substring(0, user.indexOf('@'));
            	            	
 				String bind_dn = replaceTag(server.getBindDN(),"[bind_user]",user); 
		
                env.put( Context.SECURITY_AUTHENTICATION, "simple" );
                env.put( Context.SECURITY_PRINCIPAL, bind_dn );
                env.put( Context.SECURITY_CREDENTIALS, session.getPassword() );
                
            }catch (Exception e){
                e.printStackTrace();
            }
        }
		
		return env;
	}
	
    public static String replaceTag(String originalString, String searchTag, String replaceTag) {
    	StringBuffer retval = new StringBuffer(originalString);

    	int pos = indexOf(retval, searchTag);
    	while (pos != -1) {
    	    retval.replace(pos,pos+searchTag.length(),replaceTag);
    	    pos = indexOf(retval, searchTag, pos+replaceTag.length());
    	}
        
    	return retval.toString();
    }
    private static int indexOf(StringBuffer sb, char search, int fromIdx) {
    	int retval = fromIdx;
    	while (retval < sb.length() && sb.charAt(retval) != search) {
    	    retval++;
    	}
    	return (retval == sb.length() ? -1 : retval);
    }
    private static int indexOf(StringBuffer sb, String search) {
    	return indexOf(sb, search, 0);
        }

    private static int indexOf(StringBuffer sb, String search, int fromIdx) {
    	if (search.length() == 0) {
    	    return fromIdx;
    	}
    	int retval = fromIdx;
    	boolean found = false;
    	char firstchar = search.charAt(0);
    	do {
    	    retval = indexOf(sb, firstchar, retval);
    	    if (retval != -1) {
    		found = search.equals(sb.substring(retval, retval+search.length()));
    		if (!found) {
    		    retval++;
    		}
    	    } else {
    		break;
    	    }
    	} while (!found);
    	return retval;
    }
	
}
