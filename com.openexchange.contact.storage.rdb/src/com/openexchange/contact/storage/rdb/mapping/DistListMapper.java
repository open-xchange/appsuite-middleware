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

package com.openexchange.contact.storage.rdb.mapping;

import java.util.EnumMap;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.internal.DistListMember;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.groupware.tools.mappings.database.BinaryMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.util.UUIDs;

/**
 * {@link DistListMapper} - Maps distribution list related fields to a corresponding {@link Mapping} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListMapper extends DefaultDbMapper<DistListMember, DistListMemberField> {

	public DistListMapper() {
		super();
	}

	@Override
	public DistListMember newInstance() {
		return new DistListMember();
	}

	@Override
	public DistListMemberField[] newArray(int size) {
		return new DistListMemberField[size];
	}

	@Override
	protected EnumMap<DistListMemberField, DbMapping<? extends Object, DistListMember>> createMappings() {
		final EnumMap<DistListMemberField, DbMapping<? extends Object, DistListMember>> mappings = new
				EnumMap<DistListMemberField, DbMapping<? extends Object, DistListMember>>(DistListMemberField.class);

		mappings.put(DistListMemberField.PARENT_CONTACT_ID, new IntegerMapping<DistListMember>("intfield01", "Parent contact ID") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeParentContactID();
			}
        });

		mappings.put(DistListMemberField.CONTACT_ID, new IntegerMapping<DistListMember>("intfield02", "Contact ID") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeEntryID();
			}
        });

		mappings.put(DistListMemberField.MAIL_FIELD, new IntegerMapping<DistListMember>("intfield03", "Mail Field") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeEmailfield();
			}
        });

		mappings.put(DistListMemberField.CONTACT_FOLDER_ID, new IntegerMapping<DistListMember>("intfield04", "Contact Folder ID") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeFolderld();
			}
        });

		mappings.put(DistListMemberField.DISPLAY_NAME, new VarCharMapping<DistListMember>("field01", "Display Name") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeDisplayname();
			}
        });

		mappings.put(DistListMemberField.LAST_NAME, new VarCharMapping<DistListMember>("field02", "Last Name") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeLastname();
			}
        });

		mappings.put(DistListMemberField.FIRST_NAME, new VarCharMapping<DistListMember>("field03", "First Name") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeFirstname();
			}
        });

		mappings.put(DistListMemberField.MAIL, new VarCharMapping<DistListMember>("field04", "Mail") {

            @Override
            public void set(DistListMember member, String value) throws OXException {
            	member.setEmailaddress(value, false); // don't validate when reading data
            }

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsEmailaddress();
            }

            @Override
            public String get(DistListMember member) {
                return member.getEmailaddress();
            }

			@Override
			public void remove(DistListMember member) {
				member.removeEmailaddress();
			}
        });

		mappings.put(DistListMemberField.CONTEXT_ID, new IntegerMapping<DistListMember>("cid", "Context ID") {

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

			@Override
			public void remove(DistListMember member) {
				member.removeContextID();
			}
        });

		mappings.put(DistListMemberField.UUID, new BinaryMapping<DistListMember>("uuid", "UUID") {

            @Override
            public boolean isSet(DistListMember member) {
                return member.containsUuid();
            }

            @Override
            public void set(DistListMember member, byte[] value) throws OXException {
                member.setUuid(UUIDs.toUUID(value));
            }

            @Override
            public byte[] get(DistListMember member) {
                return UUIDs.toByteArray(member.getUuid());
            }

            @Override
            public void remove(DistListMember member) {
                member.removeUuid();
            }

		});

		return mappings;
	}

}
