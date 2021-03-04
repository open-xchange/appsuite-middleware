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

package com.openexchange.test.common.test;

import com.openexchange.ajax.framework.AJAXClient;

/**
 * 
 * {@link TestClassConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class TestClassConfig {

    final int numberOfContexts;
    final int numberOfusersPerContext;
    final boolean createAjaxClients;
    final boolean createApiClients;
    final boolean useEnhancedApiClients;

    /**
     * Initializes a new {@link TestClassConfig}.
     *
     * @param numberOfContexts The amount of context to prepare for the test
     * @param numberOfusersPerContext The amount of users to prepare for each context
     * @param createAjaxClients <code>true</code> to generate an initialized {@link AJAXClient} for each user
     * @param createApiClients <code>true</code> to generate an initialized {@link AJAXClient} for each user
     * @param useEnhancedApiClients <code>true</code> to use com.openexchange.ajax.chronos.EnhancedApiClient
     */
    public TestClassConfig(int numberOfContexts, int numberOfusersPerContext, boolean createAjaxClients, boolean createApiClients, boolean useEnhancedApiClients) {
        super();
        this.numberOfContexts = numberOfContexts;
        this.numberOfusersPerContext = 2 > numberOfusersPerContext ? 2 : numberOfusersPerContext;
        this.createAjaxClients = createAjaxClients;
        this.createApiClients = createApiClients;
        this.useEnhancedApiClients = useEnhancedApiClients;
    }

    /**
     * The amount of context to prepare for the test
     *
     * @return the amount
     */
    public int getNumberOfContexts() {
        return numberOfContexts;
    }

    /**
     * The amount of users to prepare for each context
     *
     * @return the amount
     */
    public int getNumberOfusersPerContext() {
        return numberOfusersPerContext;
    }

    /**
     * Whether or not to create AJAX clients for each user
     *
     * @return <code>true</code> if client shall be generated
     */
    public boolean createAjaxClients() {
        return createAjaxClients;
    }

    /**
     * Whether or not to create API clients for each user
     *
     * @return <code>true</code> if client shall be generated
     */
    public boolean createApiClients() {
        return createApiClients;
    }

    /**
     * Whether or not to use the com.openexchange.ajax.chronos.EnhancedApiClient class
     * instead of the normal API client
     *
     * @return <code>true</code> if the enhanced client shalll be used
     */
    public boolean useEnhancedApiClients() {
        return useEnhancedApiClients;
    }

    /**
     * Builder for a {@link TestClassConfig}
     * 
     * @return The builder
     */
    public static TestConfigBuilder builder() {
        return new TestConfigBuilder();
    }

    /**
     *
     * {@link TestConfigBuilder}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.5
     */
    public static class TestConfigBuilder {

        private int numberOfContexts = 1;
        private int numberOfusersPerContext = 1;
        private boolean createAjaxClients = false;
        private boolean createApiClients = false;
        boolean useEnhancedApiClients = false;

        /**
         * Initializes a new {@link TestClassConfig}.
         */
        TestConfigBuilder() {}

        /**
         * The amount of context to prepare for the test
         *
         * @param x The amoutn
         * @return This builder for chaining
         */
        public TestConfigBuilder withContexts(int x) {
            numberOfContexts = x;
            return this;
        }

        /**
         * The amount of users to prepare for each context
         *
         * @param x The amount
         * @return This builder for chaining
         */
        public TestConfigBuilder withUserPerContext(int x) {
            numberOfusersPerContext = x;
            return this;
        }

        /**
         * Enables the generation of an AJAX client per user
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder createAjaxClient() {
            createAjaxClients = true;
            return this;
        }

        /**
         * Enables the generation of an API client per user
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder createApiClient() {
            createApiClients = true;
            return this;
        }

        /**
         * Will use the com.openexchange.ajax.chronos.EnhancedApiClient class
         * instead of the normal API class
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder useEnhancedApiClients() {
            useEnhancedApiClients = true;
            return this;
        }

        /**
         * Build the configuration
         *
         * @return The {@link TestClassConfig}
         */
        public TestClassConfig build() {
            return new TestClassConfig(numberOfContexts, numberOfusersPerContext, createAjaxClients, createApiClients, useEnhancedApiClients);
        }

    }

}
