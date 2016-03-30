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

package com.openexchange.snippet;


/**
 * {@link GetSwitch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetSwitch implements PropertySwitch {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetSwitch.class);

    /**
     * The snippet.
     */
    protected final Snippet snippet;

    /**
     * Initializes a new {@link GetSwitch}.
     *
     * @param snippet The snippet to get properties from
     */
    public GetSwitch(final Snippet snippet) {
        super();
        if (null == snippet) {
            throw new IllegalArgumentException("Snippet must not be null.");
        }
        this.snippet = snippet;
    }

    @Override
    public Object id() {
        return Integer.valueOf(snippet.getId());
    }

    @Override
    public Object properties() {
        return snippet.getUnnamedProperties();
    }

    @Override
    public Object content() {
        return snippet.getContent();
    }

    @Override
    public Object attachments() {
        return snippet.getAttachments();
    }

    @Override
    public Object accountId() {
        return Integer.valueOf(snippet.getAccountId());
    }

    @Override
    public Object type() {
        return snippet.getType();
    }

    @Override
    public Object displayName() {
        return snippet.getDisplayName();
    }

    @Override
    public Object module() {
        return snippet.getModule();
    }

    @Override
    public Object createdBy() {
        return Integer.valueOf(snippet.getCreatedBy());
    }

    @Override
    public Object shared() {
        return Boolean.valueOf(snippet.isShared());
    }

    @Override
    public Object misc() {
        return snippet.getMisc();
    }

}
