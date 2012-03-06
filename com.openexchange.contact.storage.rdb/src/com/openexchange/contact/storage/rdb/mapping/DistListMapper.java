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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.mapping;

import java.util.EnumMap;

import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.internal.DistListMember;
import com.openexchange.exception.OXException;

/**
 * {@link DistListMapper} - Maps distribution list related fields to a corresponding {@link Mapping} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListMapper extends DefaultMapper<DistListMember, DistListMemberField> {
	
	public DistListMapper() {
		super();
	}
	
	@Override
	public DistListMember newInstance() {
		return new DistListMember();
	}

	@Override
	protected EnumMap<DistListMemberField, Mapping<? extends Object, DistListMember>> createMappings() {
		final EnumMap<DistListMemberField, Mapping<? extends Object, DistListMember>> mappings = new 
				EnumMap<DistListMemberField, Mapping<? extends Object, DistListMember>>(DistListMemberField.class);

		mappings.put(DistListMemberField.PARENT_CONTACT_ID, new IntegerMapping<DistListMember>("intfield01") {

            @Override
            public void set(DistListMember member, Integer value) { 
            	member.setParentContactID(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsParentContactID();
            }

            @Override
            public Integer get(DistListMember member) { 
                return member.getParentContactID();
            }
        });
		
		mappings.put(DistListMemberField.CONTACT_ID, new IntegerMapping<DistListMember>("intfield02") {

            @Override
            public void set(DistListMember member, Integer value) { 
            	member.setEntryID(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsEntryID();
            }

            @Override
            public Integer get(DistListMember member) { 
                return member.getEntryID();
            }
        });
		
		mappings.put(DistListMemberField.MAIL_FIELD, new IntegerMapping<DistListMember>("intfield03") {

            @Override
            public void set(DistListMember member, Integer value) { 
            	member.setEmailfield(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsEmailfield();
            }

            @Override
            public Integer get(DistListMember member) { 
                return member.getEmailfield();
            }
        });
		
		mappings.put(DistListMemberField.CONTACT_FOLDER_ID, new IntegerMapping<DistListMember>("intfield04") {

            @Override
            public void set(DistListMember member, Integer value) { 
            	member.setFolderID(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsFolderld();
            }

            @Override
            public Integer get(DistListMember member) { 
                return member.getFolderID();
            }
        });
		
		mappings.put(DistListMemberField.DISPLAY_NAME, new VarCharMapping<DistListMember>("field01") {

            @Override
            public void set(DistListMember member, String value) { 
            	member.setDisplayname(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsDisplayname();
            }

            @Override
            public String get(DistListMember member) { 
                return member.getDisplayname();
            }
        });
		
		mappings.put(DistListMemberField.LAST_NAME, new VarCharMapping<DistListMember>("field02") {

            @Override
            public void set(DistListMember member, String value) { 
            	member.setLastname(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsLastname();
            }

            @Override
            public String get(DistListMember member) { 
                return member.getLastname();
            }
        });
		
		mappings.put(DistListMemberField.FIRST_NAME, new VarCharMapping<DistListMember>("field03") {

            @Override
            public void set(DistListMember member, String value) { 
            	member.setFirstname(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsFistname();
            }

            @Override
            public String get(DistListMember member) { 
                return member.getFirstname();
            }
        });
		
		mappings.put(DistListMemberField.MAIL, new VarCharMapping<DistListMember>("field04") {

            @Override
            public void set(DistListMember member, String value) throws OXException { 
            	member.setEmailaddress(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsEmailfield();
            }

            @Override
            public String get(DistListMember member) { 
                return member.getEmailaddress();
            }
        });
		
		mappings.put(DistListMemberField.CONTEXT_ID, new IntegerMapping<DistListMember>("cid") {

            @Override
            public void set(DistListMember member, Integer value) { 
            	member.setContextID(value);
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsContextID();
            }

            @Override
            public Integer get(DistListMember member) { 
                return member.getContextID();
            }
        });
		
		return mappings;
	}

}
