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

package com.openexchange.hostname.ldap.configuration;

public enum Property implements PropertyInterface {
    ldap_url(String.class, Required.TRUE, "com.openexchange.hostname.ldap.ldap_url"),
    search_base(String.class, Required.TRUE, "com.openexchange.hostname.ldap.search_base"),
    query_filter(String.class, Required.TRUE, "com.openexchange.hostname.ldap.query_filter"),
    result_attribute(String.class, Required.TRUE, "com.openexchange.hostname.ldap.result_attribute"),
    guest_result_attribute(String.class, Required.TRUE, "com.openexchange.hostname.ldap.guest_result_attribute"),
    scope(SearchScope.class, Required.TRUE, "com.openexchange.hostname.ldap.scope"),
    bind(Boolean.class, Required.TRUE, "com.openexchange.hostname.ldap.bind"),
    bind_dn(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(bind, Boolean.TRUE)}), "com.openexchange.hostname.ldap.bind_dn"),
    bind_password(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(bind, Boolean.TRUE)}), "com.openexchange.hostname.ldap.bind_password"),
    cache_config_file(String.class, Required.TRUE, "com.openexchange.hostname.ldap.cache_config_file");


    private final Class<?> clazz;

    private final Required required;

    private final String name;

    /**
     * Properties which must
     */

    private Property(final Class<?> clazz, final Required required, final String name) {
        this.clazz = clazz;
        this.required = required;
        this.name = name;
    }

    @Override
    public Class<? extends Object> getClazz() {
        return clazz;
    }

    @Override
    public Required getRequired() {
        return required;
    }

    @Override
    public String getName() {
        return name;
    }

}
