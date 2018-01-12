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

package com.openexchange.find.basic.conf;

import com.openexchange.config.lean.Property;

/**
 * {@link FindBasicProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum FindBasicProperty implements Property {

    /**
     * Some mail backends provide a virtual folder that contains all messages of
     * a user to enable cross-folder mail search. Open-Xchange can make use of
     * this feature to improve the search experience.
     * 
     * Set the value to the name of the virtual mail folder containing all messages.
     * Leave blank if no such folder exists.
     */
    allMessageFolder,

    /**
     * Denotes if mail search queries should be matched against mail bodies.
     * This improves the search experience within the mail module, if your mail
     * backend supports fast full text search. Otherwise it can slow down the
     * search requests significantly.
     * 
     * Change the value to 'true', if fast full text search is supported. Default
     * is 'false'.
     */
    searchmailbody(false);

    private final Object defaultValue;

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.find.basic.mail.";

    /**
     * Initialises a new {@link MailFilterProperty}.
     */
    private FindBasicProperty() {
        this(EMPTY);
    }

    /**
     * Initializes a new {@link FindBasicProperty}.
     */
    private FindBasicProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getFQPropertyName()
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
