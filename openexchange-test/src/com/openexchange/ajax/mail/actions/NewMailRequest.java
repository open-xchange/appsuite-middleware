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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.mail.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link NewMailRequest} - The request for <code>/ajax/mail?action=new</code>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NewMailRequest extends AbstractMailRequest<NewMailResponse> {

    private String rfc822;

    private String folder;

    private int flags;

    private boolean failOnError;

    public boolean doesFailOnError() {
        return failOnError;
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Gets the flags.
     * 
     * @return The flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Sets the flags.
     * 
     * @param flags The flags to set
     */
    public void setFlags(final int flags) {
        this.flags = flags;
    }

    /**
     * Gets the RFC822 data.
     * 
     * @return The RFC822 data
     */
    public String getRfc822() {
        return rfc822;
    }

    /**
     * Sets the RFC822 data.
     * 
     * @param rfc822 The RFC822 data to set
     */
    public void setRfc822(final String rfc822) {
        this.rfc822 = rfc822;
    }

    /**
     * Gets the folder to append the message to.
     * 
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the folder to append the message to
     * 
     * @param folder The folder to set
     */
    public void setFolder(final String folder) {
        this.folder = folder;
    }

    /**
     * Initializes a new {@link NewMailRequest}.
     * 
     * @param rfc822 The RFC822 data to set
     */
    public NewMailRequest(final String rfc822) {
        super();
        this.rfc822 = rfc822;
        this.flags = -1;
        this.folder = null;
    }

    public Object getBody() throws JSONException {
        return rfc822;
    }

    public Method getMethod() {
        return Method.PUT;
    }

    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, Mail.ACTION_NEW));
        list.add(new Parameter(Mail.PARAMETER_SRC, "1"));
        if (folder != null) {
            list.add(new Parameter(Mail.PARAMETER_FOLDERID, folder));
        }
        if (flags >= 0) {
            list.add(new Parameter(Mail.PARAMETER_FLAGS, flags));
        }

        return list.toArray(new Parameter[list.size()]);
    }

    public AbstractAJAXParser<? extends NewMailResponse> getParser() {
        return new AbstractAJAXParser<NewMailResponse>(failOnError) {

            @Override
            protected NewMailResponse createResponse(final Response response) throws JSONException {
                return new NewMailResponse(response);
            }
        };
    }

}
