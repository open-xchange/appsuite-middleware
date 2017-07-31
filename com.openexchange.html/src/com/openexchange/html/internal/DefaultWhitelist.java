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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.html.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.html.whitelist.Attribute;
import com.openexchange.html.whitelist.Element;
import com.openexchange.html.whitelist.Tag;
import com.openexchange.html.whitelist.Whitelist;


/**
 * {@link DefaultWhitelist} - The default white-list implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultWhitelist implements Whitelist {

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds an instance of <code>DefaultWhitelist</code> */
    public static final class Builder {

        private final Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist;
        private final Map<Element, Set<String>> styleWhitelist;

        /**
         * Initializes a new {@link DefaultWhitelist.Builder}.
         */
        Builder() {
            super();
            htmlWhitelist = new LinkedHashMap<>();
            styleWhitelist = new LinkedHashMap<>();
        }

        /**
         * Sets specified HTML white-list map.
         * <p>
         * Existing entries are replaced.
         *
         * @param htmlWhitelist The white-list to add
         * @return This builder
         */
        public Builder setHtmlWhitelistMap(Map<String, Map<String, Set<String>>> htmlWhitelist) {
            if (null != htmlWhitelist) {
                for (Map.Entry<String, Map<String, Set<String>>> e : htmlWhitelist.entrySet()) {
                    Map<String, Set<String>> sAttrs = e.getValue();
                    if (null == sAttrs) {
                        this.htmlWhitelist.put(Tag.valueOf(e.getKey()), null);
                    } else {
                        Map<Attribute, Set<String>> attrs = new LinkedHashMap<>(sAttrs.size());
                        for (Entry<String, Set<String>> en : sAttrs.entrySet()) {
                            attrs.put(Attribute.valueOf(en.getKey()), en.getValue());
                        }

                        this.htmlWhitelist.put(Tag.valueOf(e.getKey()), attrs);
                    }
                }
            }
            return this;
        }

        /**
         * Sets specified HTML white-list.
         * <p>
         * Existing entries are replaced.
         *
         * @param htmlWhitelist The white-list to add
         * @return This builder
         */
        public Builder setHtmlWhitelist(Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist) {
            if (null != htmlWhitelist) {
                for (Map.Entry<Tag, Map<Attribute, Set<String>>> e : htmlWhitelist.entrySet()) {
                    this.htmlWhitelist.put(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        /**
         * Sets specified HTML tag.
         * <p>
         * Existing entry is replaced.
         *
         * @param tag The tag to add
         * @param attributes The allowed attributes or <code>null</code> if no restrictions
         * @return This builder
         */
        public Builder setHtmlTag(Tag tag, Map<Attribute, Set<String>> attributes) {
            if (null != tag) {
                this.htmlWhitelist.put(tag, attributes);
            }
            return this;
        }

        /**
         * Sets specified CSS white-list map.
         * <p>
         * Existing entries are replaced.
         *
         * @param htmlWhitelist The white-list to add
         * @return This builder
         */
        public Builder setStyleWhitelistMap( Map<String, Set<String>> styleWhitelist) {
            if (null != styleWhitelist) {
                for (Map.Entry<String, Set<String>> e : styleWhitelist.entrySet()) {
                    this.styleWhitelist.put(Element.valueOf(e.getKey()), e.getValue());
                }
            }
            return this;
        }

        /**
         * Sets specified CSS white-list.
         *
         * @param styleWhitelist The white-list to add
         * @return This builder
         */
        public Builder setStyleWhitelist(Map<Element, Set<String>> styleWhitelist) {
            if (null != styleWhitelist) {
                for (Map.Entry<Element, Set<String>> e : styleWhitelist.entrySet()) {
                    this.styleWhitelist.put(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        /**
         * Sets specified CSS element.
         * <p>
         * Existing entry is replaced.
         *
         * @param element The element to add
         * @param values The allowed values or <code>null</code> if no restrictions
         * @return This builder
         */
        public Builder setStyleElement(Element element, Set<String> values) {
            if (null != element) {
                this.styleWhitelist.put(element, values);
            }
            return this;
        }

        /**
         * Builds the instance of <code>DefaultWhitelist</code> from this builder's arguments
         *
         * @return The resulting <code>DefaultWhitelist</code> instance
         */
        public DefaultWhitelist build() {
            return new DefaultWhitelist(htmlWhitelist, styleWhitelist);
        }
    }

    // --------------------------------------------------------------------------------------------

    private final Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist;
    private final Map<Element, Set<String>> styleWhitelist;

    /**
     * Initializes a new {@link DefaultWhitelist}.
     */
    DefaultWhitelist(Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist, Map<Element, Set<String>> styleWhitelist) {
        super();
        this.styleWhitelist = styleWhitelist;
        this.htmlWhitelist = htmlWhitelist;
    }

    @Override
    public Map<Tag, Map<Attribute, Set<String>>> getHtmlWhitelist() {
        return htmlWhitelist;
    }

    @Override
    public Map<Element, Set<String>> getStyleWhitelist() {
        return styleWhitelist;
    }

}
