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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.exception.interception;


/**
 * Module / action combination to define one responsibility of an {@link OXExceptionInterceptor}. An {@link OXExceptionInterceptor} is able
 * to deal with (almost) unlimited {@link OXExceptionInterceptorResponsibility}s
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class OXExceptionInterceptorResponsibility {

    private final String module;

    private final String action;

    /**
     * Initializes a new {@link OXExceptionInterceptorResponsibility}.
     *
     * @param module - the module the {@link OXExceptionInterceptor} is responsible for
     * @param action - the module the {@link OXExceptionInterceptor} is responsible for.
     */
    public OXExceptionInterceptorResponsibility(String module, String action) {
        if (module == null) {
            throw new IllegalArgumentException("Module might not be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action might not be null");
        }

        this.module = module;
        this.action = action;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the action
     *
     * @return The action
     */
    public String getAction() {
        return action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof OXExceptionInterceptorResponsibility)) {
            return false;
        }
        OXExceptionInterceptorResponsibility pairo = (OXExceptionInterceptorResponsibility) o;
        return this.module.equals(pairo.getModule()) && this.action.equals(pairo.getAction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return module.hashCode() ^ action.hashCode();
    }
}
