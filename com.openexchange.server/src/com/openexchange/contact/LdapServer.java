/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contact;

public class LdapServer {

    private String server_ip;

    private String ldap_name;

    private String password;

    private String base_dn;

    private String bind_dn;

    private String port;

    private String addressbook_dn;

    private int folder_id;

    private String list_filter;

    private String modified_filter;

    private String contacts_filter;

    private String search_filter;

    private String birthday_timeformat;

    private String date_timeformat;

    private String bind_user;

    public static final int AUTH_ANONYMOUS = 1;

    public static final int AUTH_USER = 2;

    public static final int AUTH_ADMIN = 4;

    private int context;

    private int auth_type;

    private boolean subfolder = false;

    public String[][] field_mapping;

    public LdapServer() {

    }

    public void setLdapName(final String ldap_name) {
        this.ldap_name = ldap_name;
    }

    public String getServerName() {
        return ldap_name;
    }

    public void setServerIP(final String server_ip) {
        this.server_ip = server_ip;
    }

    public String getServerIP() {
        return server_ip;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setBaseDN(final String base_dn) {
        this.base_dn = base_dn;
    }

    public String getBaseDN() {
        return base_dn;
    }

    public void setBindDN(final String bind_dn) {
        this.bind_dn = bind_dn;
    }

    public String getBindDN() {
        return bind_dn;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setAddressbookDN(final String addressbook_dn) {
        this.addressbook_dn = addressbook_dn;
    }

    public String getAddressbookDN() {
        return addressbook_dn;
    }

    public void setFolderId(final int folder_id) {
        this.folder_id = folder_id;
    }

    public int getFolderId() {
        return folder_id;
    }

    public void setListFilter(final String list_filter) {
        this.list_filter = list_filter;
    }

    public String getListFilter() {
        return list_filter;
    }

    public void setContactsFilter(final String contacts_filter) {
        this.contacts_filter = contacts_filter;
    }

    public String getContactsFilter() {
        return contacts_filter;
    }

    public void setBirthdayTimeformat(final String birthday_timeformat) {
        this.birthday_timeformat = birthday_timeformat;
    }

    public String getBirthdayTimeformat() {
        return birthday_timeformat;
    }

    public void setDateTimeformat(final String date_timeformat) {
        this.date_timeformat = date_timeformat;
    }

    public String getDateTimeformat() {
        return date_timeformat;
    }

    public void setAuthType(final int auth_type) {
        this.auth_type = auth_type;
    }

    public int getAuthType() {
        return auth_type;
    }

    public void setFieldMapping(final String[][] mapping) {
        field_mapping = mapping;
    }

    public String[][] getFieldMapping() {
        return field_mapping;
    }

    public String[] getField(final int field) {
        return field_mapping[field];
    }

    public void activateSubfolderList(final boolean subfolder) {
        this.subfolder = subfolder;
    }

    public boolean isSubfolderListActive() {
        return subfolder;
    }

    public void setBindUser(final String bind_user) {
        this.bind_user = bind_user;
    }

    public String getBindUser() {
        return bind_user;
    }

    public void setModifiedListFilter(final String modified_filter) {
        this.modified_filter = modified_filter;
    }

    public String getModifiedListFilter() {
        return modified_filter;
    }

    public void setContext(final String context) {
        this.context = Integer.parseInt(context);
    }

    public int getContext() {
        return context;
    }

    public void setSearchFilter(final String search_filter) {
        this.search_filter = search_filter;
    }

    public String getSearchFilter() {
        return search_filter;
    }
}
