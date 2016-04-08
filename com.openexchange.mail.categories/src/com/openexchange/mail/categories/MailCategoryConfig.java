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

package com.openexchange.mail.categories;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link MailCategoryConfig}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoryConfig {

    /**
     * Builder for {@link MailCategoryConfig} instances;
     */
    public static class Builder {

        private String category;
        private String flag;
        private boolean force;
        private boolean enabled;
        private String name;
        private Map<Locale, String> names;
        private boolean isSystemCategory = false;

        /**
         * Initializes a new {@link Builder}.
         */
        public Builder() {
            super();
        }

        /**
         * Sets the category identifier.
         *
         * @param category The category identifier
         * @return This builder
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * Sets the flag name.
         *
         * @param flag The flag name
         * @return This builder
         */
        public Builder flag(String flag) {
            this.flag = flag;
            return this;
        }


        /**
         * Sets the <code>force</code> flag.
         *
         * @param force The <code>force</code> flag
         * @return This builder
         */
        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * Sets the <code>enabled</code> flag.
         *
         * @param active The <code>enabled</code> flag
         * @return This builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the <code>isSystemCategory</code> flag.
         *
         * @param isSystemCategory The <code>isSystemCategory</code> flag
         * @return This builder
         */
        public Builder isSystemCategory(boolean isSystemCategory) {
            this.isSystemCategory = isSystemCategory;
            return this;
        }

        /**
         * Sets the name.
         *
         * @param enabled The name
         * @return This builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds specified localized name.
         *
         * @param locale The locale
         * @param name The localized name
         * @return This builder
         */
        public Builder addLocalizedName(Locale locale, String name) {
            Map<Locale, String> ns = this.names;
            if (null == ns) {
                ns = new LinkedHashMap<Locale, String>(16, 0.9F);
                this.names = ns;
            }
            ns.put(locale, name);
            return this;
        }

        /**
         * Adds specified localized names.
         *
         * @param names The localized names to add
         * @return This builder
         */
        public Builder addLocalizedNames(Map<Locale, String> names) {
            if (null == names || names.isEmpty()) {
                return this;
            }

            Map<Locale, String> ns = this.names;
            if (null == ns) {
                ns = new LinkedHashMap<Locale, String>(16, 0.9F);
                this.names = ns;
            }
            ns.putAll(names);
            return this;
        }

        /**
         * Builds the {@link MailCategoryConfig} instance according to this builder's settings.
         *
         * @return The new {@link MailCategoryConfig} instance
         */
        public MailCategoryConfig build() {
            return new MailCategoryConfig(category, flag, force, enabled, name, names, isSystemCategory);
        }

    }// End of class Builder

    /**
     * Creates a copy of specified instance with given active flag applied.
     *
     * @param categoryConfig The instance to copy from
     * @param active The <code>active</code> flag
     * @return The copied instance
     */
    public static MailCategoryConfig copyOf(MailCategoryConfig categoryConfig, boolean active) {
        if (null == categoryConfig) {
            return null;
        }

        return new MailCategoryConfig(categoryConfig.category, categoryConfig.flag, categoryConfig.force, active, categoryConfig.name, categoryConfig.names, categoryConfig.isSystemCategory);
    }

    /**
     * Creates a copy of specified instance..
     *
     * @param categoryConfig The instance to copy from
     * @return The copied instance
     */
    public static MailCategoryConfig copyOf(MailCategoryConfig categoryConfig) {
        return copyOf(categoryConfig, categoryConfig.enabled);
    }

    // ----------------------------------------------------------------------------------------------------------

    private final String category;
    private final String flag;
    private final boolean force;
    private final boolean enabled;
    private final String name;
    private final boolean isSystemCategory;
    private final Map<Locale, String> names;

    /**
     * Initializes a new {@link MailCategoryConfig}.
     */
    MailCategoryConfig(String category, String flag, boolean force, boolean enabled, String name, Map<Locale, String> names, boolean isSystemCategory) {
        super();
        this.category = category;
        this.flag = flag;
        this.force = force;
        this.enabled = enabled;
        this.isSystemCategory = isSystemCategory;
        this.name = name;
        this.names = null == names ? Collections.<Locale, String> emptyMap() : names;

    }

    /**
     * Gets the category identifier
     *
     * @return The category name
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the flag name (to be used for search/filter expressions).
     *
     * @return The flag name
     */
    public String getFlag() {
        return flag;
    }

    /**
     * Checks if the associated mail category is forced to be active.
     *
     * @return <code>true</code> if forcefully active; otherwise <code>false</code>
     */
    public boolean isForced() {
        return force;
    }

    /**
     * Checks if the associated mail category is active.
     *
     * @return <code>true</code> if active; otherwise <code>false</code>
     */
    public boolean isActive() {
        return force || enabled;
    }

    /**
     * Checks if the associated mail category is enabled.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the associated mail category is a system category.
     *
     * @return <code>true</code> if it is a system category; otherwise <code>false</code>
     */
    public boolean isSystemCategory() {
        return isSystemCategory;
    }

    /**
     * Gets the en_US name.
     *
     * @return The en_US name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the localized names.
     *
     * @return The localized names
     */
    public Map<Locale, String> getNames() {
        return names;
    }

    @Override
    public String toString(){
        return getCategory();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MailCategoryConfig) {
            MailCategoryConfig other = (MailCategoryConfig) o;
            if (this.getCategory().equals(other.getCategory())) {
                return true;
            }
        }
        return false;
    }

}
