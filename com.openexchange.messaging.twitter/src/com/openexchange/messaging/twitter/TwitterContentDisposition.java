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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.twitter;

import java.util.Collections;
import java.util.Iterator;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.MessagingPart;

/**
 * {@link TwitterContentDisposition}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterContentDisposition implements ContentDisposition {

    private static final TwitterContentDisposition instance = new TwitterContentDisposition();

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static TwitterContentDisposition getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link TwitterContentDisposition}.
     */
    private TwitterContentDisposition() {
        super();
    }

    public boolean containsFilenameParameter() {
        return false;
    }

    public String getDisposition() {
        return MessagingPart.INLINE;
    }

    public String getFilenameParameter() {
        return null;
    }

    public boolean isAttachment() {
        return false;
    }

    public boolean isInline() {
        return true;
    }

    public void setContentDispositio(final ContentDisposition contentDisp) {
        throw new UnsupportedOperationException("TwitterContentDisposition.setContentDispositio()");
    }

    public void setContentDisposition(final String contentDisp) {
        throw new UnsupportedOperationException("TwitterContentDisposition.setContentDisposition()");
    }

    public void setDisposition(final String disposition) {
        throw new UnsupportedOperationException("TwitterContentDisposition.setDisposition()");
    }

    public void setFilenameParameter(final String filename) {
        throw new UnsupportedOperationException("TwitterContentDisposition.setFilenameParameter()");
    }

    public void addParameter(final String key, final String value) {
        throw new UnsupportedOperationException("TwitterContentDisposition.addParameter()");
    }

    public boolean containsParameter(final String key) {
        return false;
    }

    public String getParameter(final String key) {
        return null;
    }

    public Iterator<String> getParameterNames() {
        return Collections.EMPTY_LIST.iterator();
    }

    public String removeParameter(final String key) {
        throw new UnsupportedOperationException("TwitterContentDisposition.removeParameter()");
    }

    public void setParameter(final String key, final String value) {
        throw new UnsupportedOperationException("TwitterContentDisposition.setParameter()");
    }

    public String getName() {
        return "Content-Disposition";
    }

    public String getValue() {
        return MessagingPart.INLINE;
    }

}
