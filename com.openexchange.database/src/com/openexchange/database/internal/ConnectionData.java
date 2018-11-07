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

package com.openexchange.database.internal;

import java.util.Properties;

/**
 * Data to create connections to some specific database.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConnectionData {

    /**
     * Creates a new builder for an instance of <code>ConnectionData</code>
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for an instance of <code>ConnectionData</code> */
    public static class Builder {

        private String url;
        private String driverClass;
        private Properties props;
        private boolean block;
        private int max;
        private int min;

        /**
         * Initializes a new {@link ConnectionData.Builder}.
         */
        Builder() {
            super();
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder withDriverClass(String driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public Builder withProps(Properties props) {
            this.props = props;
            return this;
        }

        public Builder withBlock(boolean block) {
            this.block = block;
            return this;
        }

        public Builder withMax(int max) {
            this.max = max;
            return this;
        }

        public Builder withMin(int min) {
            this.min = min;
            return this;
        }

        /**
         * Creates the <code>ConnectionData</code> instance from this builder's arguments.
         *
         * @return The <code>ConnectionData</code> instance
         */
        public ConnectionData build() {
            return new ConnectionData(url, driverClass, props, block, max, min);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * The ASCII-only URL to database.
     */
    final String url;

    /**
     * The driver class name.
     */
    final String driverClass;

    /**
     * The properties.
     */
    final Properties props;

    /**
     * The block flag.
     */
    final boolean block;

    /**
     * The max. limit.
     */
    final int max;

    /**
     * The min. limit
     */
    final int min;

    /**
     * Initializes a new {@link ConnectionData}.
     */
    ConnectionData(String url, String driverClass, Properties props, boolean block, int max, int min) {
        super();
        this.url = url;
        this.driverClass = driverClass;
        this.props = props;
        this.block = block;
        this.max = max;
        this.min = min;
    }

}
