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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.smslmms.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.messaging.CaptchaParams;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DefaultSMSMessage} - The default implementation of a SMS/MMS message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSMSMessage implements SMSMessage {

    private static final AtomicReference<ServiceLookup> SERVICE_LOOKUP = new AtomicReference<ServiceLookup>();

    /**
     * Sets the service look-up.
     * 
     * @param serviceLookup The service look-up
     */
    public static void setServiceLookup(final ServiceLookup serviceLookup) {
        SERVICE_LOOKUP.set(serviceLookup);
    }

    /**
     * Gets the service look-up.
     * 
     * @return The service look-up
     */
    public static ServiceLookup getServiceLookup() {
        return SERVICE_LOOKUP.get();
    }

    private String sender;

    private Set<String> recipients;

    private String message;

    private String id;

    private String fullName;

    private long size;

    private CaptchaParams captchaParams;

    private final Map<String, Object> parameters;

    private final List<ManagedFile> files;

    /**
     * Initializes a new {@link DefaultSMSMessage}.
     */
    public DefaultSMSMessage() {
        super();
        files = new LinkedList<ManagedFile>();
        parameters = new HashMap<String, Object>(4);
        fullName = MessagingFolder.ROOT_FULLNAME;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message
     * 
     * @param message The message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String getSender() {
        return sender;
    }

    /**
     * Sets the sender
     * 
     * @param sender The sender to set
     */
    public void setSender(final String sender) {
        this.sender = sender;
    }

    @Override
    public Set<String> getRecipients() {
        return null == recipients ? Collections.<String> emptySet() : new LinkedHashSet<String>(recipients);
    }

    /**
     * Sets the recipients
     * 
     * @param recipients The recipients to set
     */
    public void setRecipients(final Set<String> recipients) {
        if (null == recipients || recipients.isEmpty()) {
            this.recipients = Collections.<String> emptySet();
        } else {
            this.recipients = new LinkedHashSet<String>(recipients);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     * 
     * @param id The id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getFolder() {
        return fullName;
    }

    /**
     * Sets the full name
     * 
     * @param fullName The full name to set
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    @Override
    public long getSize() throws OXException {
        return size;
    }

    /**
     * Sets the size
     * 
     * @param size The size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public void putParameter(final String name, final Object value) {
        parameters.put(name, value);
    }

    @Override
    public boolean putParameterIfAbsent(final String name, final Object value) {
        if (parameters.containsKey(name)) {
            return false;
        }
        parameters.put(name, value);
        return true;
    }

    @Override
    public void clearParameters() {
        parameters.clear();
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

    /**
     * Sets the captcha parameters
     * 
     * @param params The captcha parameters
     */
    @Override
    public void setCaptchaParameters(final CaptchaParams captchaParams) {
        this.captchaParams = captchaParams;
        parameters.put(PARAM_CAPTCHA_PARAMS, captchaParams);
    }

    /**
     * Gets the captcha parameters
     * 
     * @return The captcha parameters
     */
    @Override
    public CaptchaParams getCaptchaParams() {
        if (null == captchaParams) {
            captchaParams = (CaptchaParams) parameters.get(PARAM_CAPTCHA_PARAMS);
        }
        return captchaParams;
    }

    @Override
    public void addAttachment(final ManagedFile managedFile) throws OXException {
        /*
         * Add to list
         */
        files.add(managedFile);
    }

    @Override
    public void addAttachment(final String attachmentId) throws OXException {
        /*
         * Ensure presence of needed service
         */
        final ManagedFileManagement managedFileManagement = SERVICE_LOOKUP.get().getService(ManagedFileManagement.class);
        if (null == managedFileManagement) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ManagedFileManagement.class.getName());
        }
        addAttachment(managedFileManagement.getByID(attachmentId));
    }

    /**
     * Gets the associated files.
     * 
     * @return The files.
     */
    @Override
    public List<ManagedFile> getFiles() {
        return new ArrayList<ManagedFile>(files);
    }

}
