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

package com.openexchange.mobile.configuration.generator.configuration;

public enum Property implements PropertyInterface {
    iPhoneRegex(String.class, Required.TRUE, "com.openexchange.mobile.configuration.generator.iPhoneRegex"),
    WinMobRegex(String.class, Required.TRUE, "com.openexchange.mobile.configuration.generator.WinMobRegex"),
    OnlySecureConnect(Boolean.class, Required.TRUE, "com.openexchange.mobile.configuration.generator.OnlySecureConnect"),
    DomainUser(String.class, Required.TRUE, "com.openexchange.usm.eas.login_pattern.domain_user"),
    SignConfig(Boolean.class, Required.TRUE, "com.openexchange.mobile.configuration.generator.SignConfig"),
    OpensslBinary(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(SignConfig, Boolean.TRUE)}), "com.openexchange.mobile.configuration.generator.OpensslBinary"),
    OpensslTimeout(Integer.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(SignConfig, Boolean.TRUE)}), "com.openexchange.mobile.configuration.generator.OpensslTimeout"),
    CertFile(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(SignConfig, Boolean.TRUE)}), "com.openexchange.mobile.configuration.generator.CertFile"),
    KeyFile(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(SignConfig, Boolean.TRUE)}), "com.openexchange.mobile.configuration.generator.KeyFile"),
    PemFile(String.class, new Required(Required.Value.CONDITION, new Condition[]{new Condition(SignConfig, Boolean.TRUE)}), "com.openexchange.mobile.configuration.generator.PemFile"),
    ;

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
