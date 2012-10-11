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

package com.openexchange.user.json.writer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.json.Utility;
import com.openexchange.user.json.field.DistributionListField;
import com.openexchange.user.json.field.UserField;

/**
 * {@link UserWriter} - The user writer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserWriter {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(UserWriter.class));

    private static final boolean WARN = LOG.isWarnEnabled();

    /**
     * Initializes a new {@link UserWriter}.
     */
    private UserWriter() {
        super();
    }

    private static interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;
    }

    private static final class JSONArrayPutter implements JSONValuePutter {

        private JSONArray jsonArray;

        public JSONArrayPutter() {
            super();
        }

        public JSONArrayPutter(final JSONArray jsonArray) {
            this();
            this.jsonArray = jsonArray;
        }

        public void setJSONArray(final JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            jsonArray.put(value);
        }

    }

    private static final class JSONObjectPutter implements JSONValuePutter {

        private JSONObject jsonObject;

        public JSONObjectPutter() {
            super();
        }

        public JSONObjectPutter(final JSONObject jsonObject) {
            this();
            this.jsonObject = jsonObject;
        }

        public void setJSONObject(final JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            if (null == value || JSONObject.NULL.equals(value)) {
                // Don't write NULL value
                return;
            }
            jsonObject.put(key, value);
        }

    }

    private static interface UserFieldWriter {

        void writeField(JSONValuePutter jsonValue, User user, Contact contact) throws JSONException;
    }

    private static final class AttributeUserFieldWriter implements UserFieldWriter {

        private final String attributeName;

        AttributeUserFieldWriter(final String attributeName) {
            super();
            this.attributeName = attributeName;
        }

        @Override
        public void writeField(final JSONValuePutter jsonValue, final User user, final Contact contact) throws JSONException {
            jsonValue.put(attributeName, toJSONValue(user.getAttributes().get(attributeName)));
        }

        private static Object toJSONValue(final Set<String> values) {
            if (null == values || values.isEmpty()) {
                return JSONObject.NULL;
            }
            if (values.size() > 1) {
                final JSONArray ja = new JSONArray();
                for (final String value : values) {
                    ja.put(value);
                }
                return ja;
            }
            return values.iterator().next();
        }

    }

    private static final class WildcardAttributeUserFieldWriter implements UserFieldWriter {

        private final String attributePrefix;

        WildcardAttributeUserFieldWriter(final String attributePrefix) {
            super();
            this.attributePrefix = attributePrefix;
        }

        @Override
        public void writeField(final JSONValuePutter jsonValue, final User user, final Contact contact) throws JSONException {
            jsonValue.put(attributePrefix, toJSONValue(user.getAttributes()));
        }

        private Object toJSONValue(final Map<String, Set<String>> attributes) throws JSONException {
            if (null == attributes || attributes.isEmpty()) {
                return JSONObject.NULL;
            }
            final JSONObject jo = new JSONObject();
            final int size = attributes.size();
            final Iterator<Entry<String, Set<String>>> iter = attributes.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                final Entry<String, Set<String>> entry = iter.next();
                final String key = entry.getKey();
                if (key.startsWith(attributePrefix)) {
                    jo.put(key, set2JSONValue(entry.getValue()));
                }
            }
            return jo;
        }

        private static Object set2JSONValue(final Set<String> values) {
            if (null == values || values.isEmpty()) {
                return JSONObject.NULL;
            }
            if (values.size() > 1) {
                final JSONArray ja = new JSONArray();
                for (final String value : values) {
                    ja.put(value);
                }
                return ja;
            }
            return values.iterator().next();
        }

    }

    private static final UserFieldWriter UNKNOWN_FIELD_FFW = new UserFieldWriter() {

        @Override
        public void writeField(final JSONValuePutter jsonValue, final User user, final Contact contact) throws JSONException {
            jsonValue.put("unknown_field", JSONObject.NULL);
        }
    };

    private static final TIntObjectMap<UserFieldWriter> STATIC_WRITERS_MAP;

    private static final int[] ALL_FIELDS;

    static {
        final TIntObjectMap<UserFieldWriter> m = new TIntObjectHashMap<UserFieldWriter>();
        m.put(UserField.ID.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.ID.getName(), Integer.valueOf(user.getId()));
            }
        });
        m.put(UserField.CREATED_BY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final int createdBy = contact.getCreatedBy();
                jsonPutter.put(UserField.CREATED_BY.getName(), -1 == createdBy ? JSONObject.NULL : Integer.valueOf(createdBy));
            }
        });
        m.put(UserField.MODIFIED_BY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final int modifiedBy = contact.getModifiedBy();
                jsonPutter.put(UserField.MODIFIED_BY.getName(), -1 == modifiedBy ? JSONObject.NULL : Integer.valueOf(modifiedBy));
            }
        });
        m.put(UserField.CREATION_DATE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Date d = contact.getCreationDate();
                jsonPutter.put(UserField.CREATION_DATE.getName(), null == d ? JSONObject.NULL : Long.valueOf(Utility.addTimeZoneOffset(d.getTime(), user.getTimeZone())));
            }
        });
        m.put(UserField.LAST_MODIFIED.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Date d = contact.getLastModified();
                jsonPutter.put(UserField.LAST_MODIFIED.getName(), null == d ? JSONObject.NULL : Long.valueOf(Utility.addTimeZoneOffset(d.getTime(), user.getTimeZone())));
            }
        });
        m.put(UserField.LAST_MODIFIED_UTC.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Date d = contact.getLastModified();
                jsonPutter.put(UserField.LAST_MODIFIED_UTC.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(UserField.FOLDER_ID.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final int pid = contact.getParentFolderID();
                jsonPutter.put(UserField.FOLDER_ID.getName(), pid <= 0 ? JSONObject.NULL : Integer.valueOf(pid));
            }
        });

        // ######################### COMMON ATTRIBUTES ###########################################

        m.put(UserField.CATEGORIES.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCategories();
                jsonPutter.put(UserField.CATEGORIES.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.PRIVATE_FLAG.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.PRIVATE_FLAG.getName(), Boolean.valueOf(contact.getPrivateFlag()));
            }
        });
        m.put(UserField.COLOR_LABEL.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.COLOR_LABEL.getName(), Integer.valueOf(contact.getLabel()));
            }
        });
        m.put(UserField.NUMBER_OF_ATTACHMENTS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.NUMBER_OF_ATTACHMENTS.getName(), Integer.valueOf(contact.getNumberOfAttachments()));
            }
        });

        // ######################### CONTACT ATTRIBUTES ###########################################

        m.put(UserField.DISPLAY_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String name = contact.getDisplayName();
                jsonPutter.put(UserField.DISPLAY_NAME.getName(), name);
            }
        });
        m.put(UserField.FIRST_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getGivenName();
                jsonPutter.put(UserField.FIRST_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.LAST_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getSurName();
                jsonPutter.put(UserField.LAST_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.SECOND_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getMiddleName();
                jsonPutter.put(UserField.SECOND_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.SUFFIX.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getSuffix();
                jsonPutter.put(UserField.SUFFIX.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TITLE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTitle();
                jsonPutter.put(UserField.TITLE.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.STREET_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStreetHome();
                jsonPutter.put(UserField.STREET_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.POSTAL_CODE_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getPostalCodeHome();
                jsonPutter.put(UserField.POSTAL_CODE_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.CITY_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCityHome();
                jsonPutter.put(UserField.CITY_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.STATE_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStateHome();
                jsonPutter.put(UserField.STATE_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.COUNTRY_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCountryHome();
                jsonPutter.put(UserField.COUNTRY_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.BIRTHDAY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Date d = contact.getBirthday();
                jsonPutter.put(UserField.BIRTHDAY.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(UserField.MARITAL_STATUS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getMaritalStatus();
                jsonPutter.put(UserField.MARITAL_STATUS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.NUMBER_OF_CHILDREN.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getNumberOfChildren();
                jsonPutter.put(UserField.NUMBER_OF_CHILDREN.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.PROFESSION.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getProfession();
                jsonPutter.put(UserField.PROFESSION.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.NICKNAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getNickname();
                jsonPutter.put(UserField.NICKNAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.SPOUSE_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getSpouseName();
                jsonPutter.put(UserField.SPOUSE_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.ANNIVERSARY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Date d = contact.getAnniversary();
                jsonPutter.put(UserField.ANNIVERSARY.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(UserField.NOTE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getNote();
                jsonPutter.put(UserField.NOTE.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.DEFAULT_ADDRESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final int i = contact.getDefaultAddress();
                jsonPutter.put(UserField.DEFAULT_ADDRESS.getName(), Integer.valueOf(i));
            }
        });
        m.put(UserField.DEPARTMENT.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getDepartment();
                jsonPutter.put(UserField.DEPARTMENT.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.POSITION.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getPosition();
                jsonPutter.put(UserField.POSITION.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.EMPLOYEE_TYPE.getColumn(), new UserFieldWriter() {;

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getEmployeeType();
                jsonPutter.put(UserField.EMPLOYEE_TYPE.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.ROOM_NUMBER.getColumn(), new UserFieldWriter() {;

        @Override
        public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
            final String s = contact.getRoomNumber();
            jsonPutter.put(UserField.ROOM_NUMBER.getName(), null == s ? JSONObject.NULL : s);
        }
    });
        m.put(UserField.STREET_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStreetBusiness();
                jsonPutter.put(UserField.STREET_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.POSTAL_CODE_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getPostalCodeBusiness();
                jsonPutter.put(UserField.POSTAL_CODE_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.CITY_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCityBusiness();
                jsonPutter.put(UserField.CITY_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.INTERNAL_USERID.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.INTERNAL_USERID.getName(), Integer.valueOf(user.getId()));
            }
        });
        m.put(UserField.STATE_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStateBusiness();
                jsonPutter.put(UserField.STATE_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.COUNTRY_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCountryBusiness();
                jsonPutter.put(UserField.COUNTRY_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.NUMBER_OF_EMPLOYEE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getNumberOfEmployee();
                jsonPutter.put(UserField.NUMBER_OF_EMPLOYEE.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.SALES_VOLUME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getSalesVolume();
                jsonPutter.put(UserField.SALES_VOLUME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TAX_ID.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTaxID();
                jsonPutter.put(UserField.TAX_ID.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.COMMERCIAL_REGISTER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCommercialRegister();
                jsonPutter.put(UserField.COMMERCIAL_REGISTER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.BRANCHES.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getBranches();
                jsonPutter.put(UserField.BRANCHES.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.BUSINESS_CATEGORY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getBusinessCategory();
                jsonPutter.put(UserField.BUSINESS_CATEGORY.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.INFO.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getInfo();
                jsonPutter.put(UserField.INFO.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.MANAGER_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getManagerName();
                jsonPutter.put(UserField.MANAGER_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.ASSISTANT_NAME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getAssistantName();
                jsonPutter.put(UserField.ASSISTANT_NAME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.STREET_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStreetOther();
                jsonPutter.put(UserField.STREET_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.CITY_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCityOther();
                jsonPutter.put(UserField.CITY_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.STATE_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getStateOther();
                jsonPutter.put(UserField.STATE_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.POSTAL_CODE_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getPostalCodeOther();
                jsonPutter.put(UserField.POSTAL_CODE_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.COUNTRY_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCountryOther();
                jsonPutter.put(UserField.COUNTRY_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_BUSINESS1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneBusiness1();
                jsonPutter.put(UserField.TELEPHONE_BUSINESS1.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_BUSINESS2.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneBusiness2();
                jsonPutter.put(UserField.TELEPHONE_BUSINESS2.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.FAX_BUSINESS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getFaxBusiness();
                jsonPutter.put(UserField.FAX_BUSINESS.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_CALLBACK.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneCallback();
                jsonPutter.put(UserField.TELEPHONE_CALLBACK.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_CAR.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneCar();
                jsonPutter.put(UserField.TELEPHONE_CAR.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_COMPANY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneCompany();
                jsonPutter.put(UserField.TELEPHONE_COMPANY.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_HOME1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneHome1();
                jsonPutter.put(UserField.TELEPHONE_HOME1.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_HOME2.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneHome2();
                jsonPutter.put(UserField.TELEPHONE_HOME2.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.FAX_HOME.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getFaxHome();
                jsonPutter.put(UserField.FAX_HOME.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.CELLULAR_TELEPHONE1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCellularTelephone1();
                jsonPutter.put(UserField.CELLULAR_TELEPHONE1.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.CELLULAR_TELEPHONE2.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCellularTelephone2();
                jsonPutter.put(UserField.CELLULAR_TELEPHONE2.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneOther();
                jsonPutter.put(UserField.TELEPHONE_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.FAX_OTHER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getFaxOther();
                jsonPutter.put(UserField.FAX_OTHER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.EMAIL1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getEmail1();
                jsonPutter.put(UserField.EMAIL1.getName(), null == s ? JSONObject.NULL : Tools.toIDN(s));
            }
        });
        m.put(UserField.EMAIL2.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getEmail2();
                jsonPutter.put(UserField.EMAIL2.getName(), null == s ? JSONObject.NULL : Tools.toIDN(s));
            }
        });
        m.put(UserField.EMAIL3.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getEmail3();
                jsonPutter.put(UserField.EMAIL3.getName(), null == s ? JSONObject.NULL : Tools.toIDN(s));
            }
        });
        m.put(UserField.TELEPHONE_ISDN.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneISDN();
                jsonPutter.put(UserField.TELEPHONE_ISDN.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_PAGER.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephonePager();
                jsonPutter.put(UserField.TELEPHONE_PAGER.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_PRIMARY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephonePrimary();
                jsonPutter.put(UserField.TELEPHONE_PRIMARY.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_TELEX.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneTelex();
                jsonPutter.put(UserField.TELEPHONE_TELEX.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_RADIO.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneRadio();
                jsonPutter.put(UserField.TELEPHONE_RADIO.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_TTYTDD.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneTTYTTD();
                jsonPutter.put(UserField.TELEPHONE_TTYTDD.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.INSTANT_MESSENGER1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getInstantMessenger1();
                jsonPutter.put(UserField.INSTANT_MESSENGER1.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.INSTANT_MESSENGER2.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getInstantMessenger2();
                jsonPutter.put(UserField.INSTANT_MESSENGER2.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_IP.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneIP();
                jsonPutter.put(UserField.TELEPHONE_IP.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.TELEPHONE_ASSISTANT.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getTelephoneAssistant();
                jsonPutter.put(UserField.TELEPHONE_ASSISTANT.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.COMPANY.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getCompany();
                jsonPutter.put(UserField.COMPANY.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.IMAGE1.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                // final byte[] s = contact.getImage1();
                jsonPutter.put(UserField.IMAGE1.getName(), JSONObject.NULL);
            }
        });
        m.put(UserField.USERFIELD01.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField01();
                jsonPutter.put(UserField.USERFIELD01.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD02.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField02();
                jsonPutter.put(UserField.USERFIELD02.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD03.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField03();
                jsonPutter.put(UserField.USERFIELD03.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD04.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField04();
                jsonPutter.put(UserField.USERFIELD04.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD05.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField05();
                jsonPutter.put(UserField.USERFIELD05.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD06.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField06();
                jsonPutter.put(UserField.USERFIELD06.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD07.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField07();
                jsonPutter.put(UserField.USERFIELD07.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD08.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField08();
                jsonPutter.put(UserField.USERFIELD08.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD09.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField09();
                jsonPutter.put(UserField.USERFIELD09.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD10.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField10();
                jsonPutter.put(UserField.USERFIELD10.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD11.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField11();
                jsonPutter.put(UserField.USERFIELD11.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD12.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField12();
                jsonPutter.put(UserField.USERFIELD12.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD13.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField13();
                jsonPutter.put(UserField.USERFIELD13.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD14.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField14();
                jsonPutter.put(UserField.USERFIELD14.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD15.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField15();
                jsonPutter.put(UserField.USERFIELD15.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD16.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField16();
                jsonPutter.put(UserField.USERFIELD16.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD17.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField17();
                jsonPutter.put(UserField.USERFIELD17.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD18.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField18();
                jsonPutter.put(UserField.USERFIELD18.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD19.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField19();
                jsonPutter.put(UserField.USERFIELD19.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USERFIELD20.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getUserField20();
                jsonPutter.put(UserField.USERFIELD20.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.LINKS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final LinkEntryObject[] links = contact.getLinks();
                if (null == links || 0 == links.length) {
                    jsonPutter.put(UserField.LINKS.getName(), JSONObject.NULL);
                } else {
                    final JSONArray jsonArray = new JSONArray();

                    for (final LinkEntryObject link : links) {
                        final JSONObject jsonLinkObject = new JSONObject();
                        if (link.containsLinkID()) {
                            jsonLinkObject.put(UserField.ID.getName(), link.getLinkID());
                        }
                        jsonLinkObject.put(UserField.DISPLAY_NAME.getName(), link.getLinkDisplayname());
                        jsonArray.put(jsonLinkObject);
                    }
                    jsonPutter.put(UserField.LINKS.getName(), jsonArray);
                }
            }
        });
        m.put(UserField.DISTRIBUTIONLIST.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final DistributionListEntryObject[] distributionList = contact.getDistributionList();
                if (null == distributionList || 0 == distributionList.length) {
                    jsonPutter.put(UserField.DISTRIBUTIONLIST.getName(), JSONObject.NULL);
                } else {
                    final JSONArray jsonArray = new JSONArray();
                    for (final DistributionListEntryObject listEntry : distributionList) {
                        final JSONObject jsonDListObj = new JSONObject();
                        /*
                         * Write entry to new JSON object
                         */
                        final int emailField = listEntry.getEmailfield();
                        if (emailField != DistributionListEntryObject.INDEPENDENT) {
                            jsonDListObj.put(UserField.ID.getName(), listEntry.getEntryID());
                        }

                        String s = listEntry.getEmailaddress();
                        if (null != s && s.length() > 0) {
                            jsonDListObj.put(DistributionListField.MAIL.getName(), s);
                        }
                        s = listEntry.getDisplayname();
                        if (null != s && s.length() > 0) {
                            jsonDListObj.put(UserField.DISPLAY_NAME.getName(), s);
                        }
                        jsonDListObj.put(DistributionListField.MAIL_FIELD.getName(), emailField);

                        jsonArray.put(jsonDListObj);
                    }
                    jsonPutter.put(UserField.DISTRIBUTIONLIST.getName(), jsonArray);
                }
            }
        });
        m.put(UserField.MARK_AS_DISTRIBUTIONLIST.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                if (contact.containsMarkAsDistributionlist()) {
                    jsonPutter.put(UserField.MARK_AS_DISTRIBUTIONLIST.getName(), Boolean.valueOf(contact.getMarkAsDistribtuionlist()));
                } else {
                    jsonPutter.put(UserField.MARK_AS_DISTRIBUTIONLIST.getName(), JSONObject.NULL);
                }
            }
        });
        m.put(UserField.URL.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = contact.getURL();
                jsonPutter.put(UserField.URL.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.USE_COUNT.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.USE_COUNT.getName(), Integer.valueOf(contact.getUseCount()));
            }
        });
        m.put(UserField.NUMBER_OF_LINKS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.NUMBER_OF_LINKS.getName(), Integer.valueOf(contact.getNumberOfLinks()));
            }
        });
        m.put(UserField.NUMBER_OF_DISTRIBUTIONLIST.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.NUMBER_OF_DISTRIBUTIONLIST.getName(), Integer.valueOf(contact.getNumberOfDistributionLists()));
            }
        });

        // ######################### USER ATTRIBUTES ###########################################

        m.put(UserField.ALIASES.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String[] aliases = user.getAliases();
                if (null == aliases || 0 == aliases.length) {
                    jsonPutter.put(UserField.ALIASES.getName(), JSONObject.NULL);
                } else {
                    final JSONArray aliasesArray = new JSONArray();
                    for (final String alias : aliases) {
                        aliasesArray.put(alias);
                    }
                    jsonPutter.put(UserField.ALIASES.getName(), aliasesArray);
                }
            }
        });
        m.put(UserField.TIME_ZONE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = user.getTimeZone();
                jsonPutter.put(UserField.TIME_ZONE.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        m.put(UserField.LOCALE.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final Locale l = user.getLocale();
                jsonPutter.put(UserField.LOCALE.getName(), null == l ? JSONObject.NULL : l.toString());
            }
        });
        m.put(UserField.GROUPS.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final int[] groups = user.getGroups();
                if (null == groups || 0 == groups.length) {
                    jsonPutter.put(UserField.GROUPS.getName(), JSONObject.NULL);
                } else {
                    final JSONArray groupsArray = new JSONArray();
                    for (final int group : groups) {
                        groupsArray.put(group);
                    }
                    jsonPutter.put(UserField.GROUPS.getName(), groupsArray);
                }
            }
        });
        m.put(UserField.CONTACT_ID.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                jsonPutter.put(UserField.CONTACT_ID.getName(), Integer.valueOf(user.getContactId()));
            }
        });
        m.put(UserField.LOGIN_INFO.getColumn(), new UserFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                final String s = user.getLoginInfo();
                jsonPutter.put(UserField.LOGIN_INFO.getName(), null == s ? JSONObject.NULL : s);
            }
        });
        STATIC_WRITERS_MAP = m;

        ALL_FIELDS = new int[UserField.ALL_FIELDS.length];
        int i = 0;
        for (final UserField userField : UserField.ALL_FIELDS) {
            ALL_FIELDS[i++] = userField.getColumn();
        }
    }

    private static UserFieldWriter getUserFieldWriter(final int field, final String timeZoneId) {
        if (null == timeZoneId) {
            return STATIC_WRITERS_MAP.get(field);
        }
        /*
         * Check for time zone sensitive fields
         */
        if (UserField.CREATION_DATE.getColumn() == field) {
            return new UserFieldWriter() {

                @Override
                public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                    final Date d = contact.getCreationDate();
                    jsonPutter.put(UserField.CREATION_DATE.getName(), null == d ? JSONObject.NULL : Long.valueOf(Utility.addTimeZoneOffset(d.getTime(), timeZoneId)));
                }
            };
        }
        if (UserField.LAST_MODIFIED.getColumn() == field) {
            return new UserFieldWriter() {

                @Override
                public void writeField(final JSONValuePutter jsonPutter, final User user, final Contact contact) throws JSONException {
                    final Date d = contact.getCreationDate();
                    jsonPutter.put(UserField.LAST_MODIFIED.getName(), null == d ? JSONObject.NULL : Long.valueOf(Utility.addTimeZoneOffset(d.getTime(), timeZoneId)));
                }
            };
        }
        /*
         * Return static instance
         */
        return STATIC_WRITERS_MAP.get(field);
    }

    private static final String ALL = "*";

    private static void addAttributeWriters(final Map<String, List<String>> attributeParameters, final List<UserFieldWriter> ufws) {
        if (null == attributeParameters || attributeParameters.isEmpty()) {
            return;
        }
        final int size = attributeParameters.size();
        final Iterator<Entry<String, List<String>>> iter = attributeParameters.entrySet().iterator();
        final StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < size; i++) {
            final Entry<String, List<String>> entry = iter.next();
            final List<String> list = entry.getValue();
            if (!list.isEmpty()) {
                if (1 == list.size() && ALL.equals(list.get(0))) {
                    /*
                     * Wildcard
                     */
                    ufws.add(new WildcardAttributeUserFieldWriter(entry.getKey()));
                } else {
                    /*
                     * Non wildcard
                     */
                    final String prefix = entry.getKey();
                    final int pLen = prefix.length() + 1;
                    sb.setLength(0);
                    sb.append(prefix).append('/');
                    for (final String appendix : list) {
                        sb.setLength(pLen);
                        sb.append(appendix);
                        ufws.add(new AttributeUserFieldWriter(sb.toString()));
                    }
                }
            }
        }
    }

    private static final int AVERAGE_ATTR_PARAMS_SIZE = 4;

    /**
     * Writes requested fields of given user into a JSON array.
     *
     * @param fields The fields to write or <code>null</code> to write all
     * @param attributeParameters The user attributes to write
     * @param user The user
     * @param contact The user's contact
     * @param timeZoneId The optional time zone ID
     * @return The JSON array carrying requested fields of given user
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeSingle2Array(final int[] fields, final Map<String, List<String>> attributeParameters, final User user, final Contact contact, final String timeZoneId) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final List<UserFieldWriter> ufws = new ArrayList<UserFieldWriter>(cols.length + AVERAGE_ATTR_PARAMS_SIZE);
        for (final int col : cols) {
            UserFieldWriter ufw = getUserFieldWriter(col, timeZoneId);
            if (null == ufw) {
                if (WARN) {
                    LOG.warn("Unknown field: " + col, new Throwable());
                }
                ufw = UNKNOWN_FIELD_FFW;
            }
            ufws.add(ufw);
        }
        addAttributeWriters(attributeParameters, ufws);
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONValuePutter jsonPutter = new JSONArrayPutter(jsonArray);
            for (final UserFieldWriter ufw : ufws) {
                ufw.writeField(jsonPutter, user, contact);
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON arrays.
     *
     * @param fields The fields to write to each JSON array or <code>null</code> to write all
     * @param attributeParameters The user attributes to write
     * @param users The users
     * @param contacts The users' contacts
     * @param timeZoneId The optional time zone ID
     * @return The JSON array carrying JSON arrays of given users
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Array(final int[] fields, final Map<String, List<String>> attributeParameters, final User[] users, final Contact[] contacts, final String timeZoneId) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final List<UserFieldWriter> ufws = new ArrayList<UserFieldWriter>(cols.length + AVERAGE_ATTR_PARAMS_SIZE);
        for (final int col : cols) {
            UserFieldWriter ufw = getUserFieldWriter(col, timeZoneId);
            if (null == ufw) {
                if (WARN) {
                    LOG.warn("Unknown field: " + col, new Throwable());
                }
                ufw = UNKNOWN_FIELD_FFW;
            }
            ufws.add(ufw);
        }
        addAttributeWriters(attributeParameters, ufws);
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONArrayPutter jsonPutter = new JSONArrayPutter();
            for (int i = 0; i < users.length; i++) {
                final JSONArray folderArray = new JSONArray();
                jsonPutter.setJSONArray(folderArray);
                for (final UserFieldWriter ufw : ufws) {
                    ufw.writeField(jsonPutter, users[i], contacts[i]);
                }
                jsonArray.put(folderArray);
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given user into a JSON object.
     *
     * @param fields The fields to write or <code>null</code> to write all
     * @param attributeParameters The user attributes to write
     * @param user The user
     * @param contact The user's contact
     * @param timeZoneId The optional time zone ID
     * @return The JSON object carrying requested fields of given user
     * @throws OXException If writing JSON object fails
     */
    public static JSONObject writeSingle2Object(final int[] fields, final Map<String, List<String>> attributeParameters, final User user, final Contact contact, final String timeZoneId) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final List<UserFieldWriter> ufws = new ArrayList<UserFieldWriter>(cols.length + AVERAGE_ATTR_PARAMS_SIZE);
        for (final int col : cols) {
            UserFieldWriter ufw = getUserFieldWriter(col, timeZoneId);
            if (null == ufw) {
                if (WARN) {
                    LOG.warn("Unknown field: " + col, new Throwable());
                }
                ufw = UNKNOWN_FIELD_FFW;
            }
            ufws.add(ufw);
        }
        addAttributeWriters(attributeParameters, ufws);
        try {
            final JSONObject jsonObject = new JSONObject();
            final JSONValuePutter jsonPutter = new JSONObjectPutter(jsonObject);
            for (final UserFieldWriter ufw : ufws) {
                ufw.writeField(jsonPutter, user, contact);
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON objects.
     *
     * @param fields The fields to write to each JSON object or <code>null</code> to write all
     * @param attributeParameters The user attributes to write
     * @param users The users
     * @param contacts The users' contacts
     * @param timeZoneId The optional time zone ID
     * @return The JSON array carrying JSON objects of given folders
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Object(final int[] fields, final Map<String, List<String>> attributeParameters, final User[] users, final Contact[] contacts, final String timeZoneId) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final List<UserFieldWriter> ufws = new ArrayList<UserFieldWriter>(cols.length + AVERAGE_ATTR_PARAMS_SIZE);
        for (final int col : cols) {
            UserFieldWriter ufw = getUserFieldWriter(col, timeZoneId);
            if (null == ufw) {
                if (WARN) {
                    LOG.warn("Unknown field: " + col, new Throwable());
                }
                ufw = UNKNOWN_FIELD_FFW;
            }
            ufws.add(ufw);
        }
        addAttributeWriters(attributeParameters, ufws);
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONObjectPutter jsonPutter = new JSONObjectPutter();
            for (int i = 0; i < users.length; i++) {
                final JSONObject folderObject = new JSONObject();
                jsonPutter.setJSONObject(folderObject);
                for (final UserFieldWriter ufw : ufws) {
                    ufw.writeField(jsonPutter, users[i], contacts[i]);
                }
                jsonArray.put(folderObject);
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

}
