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

package com.openexchange.templating;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
@SingletonService
public interface TemplateService {

    /**
     * Loads the denoted template from default template path w/o an exception handler.
     *
     * @param templateName The template name
     * @return The loaded template
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName) throws OXException;

    /**
     * Loads the denoted template from default template path using given optional exception handler.
     *
     * @param templateName The template name
     * @param exceptionHandler The exception handler or <code>null</code>
     * @return The loaded template
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, OXTemplateExceptionHandler exceptionHandler) throws OXException;

    /**
     * Loads the denoted template w/o an exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The name of default template
     * @param session The associated session
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session) throws OXException;

    /**
     * Loads the denoted template using optional exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The name of default template
     * @param session The associated session
     * @param exceptionHandler The exception handler or <code>null</code>
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session, OXTemplateExceptionHandler exceptionHandler) throws OXException;

    /**
     * Loads the denoted template w/o an exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The template path
     * @param session The associated session
     * @param createCopy <code>true</code> to return a copy; otherwise <code>false</code> for a reference
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session, boolean createCopy) throws OXException;

    /**
     * Gets the names of basic templates that match given filters
     *
     * @param filter The optional filter tags
     * @return The matching names
     * @throws OXException If operation fails
     */
    List<String> getBasicTemplateNames(String... filter) throws OXException;

    /**
     * Gets the names of the user templates that match given filters
     *
     * @param session The associated session
     * @param filter The optional filter tags
     * @return The matching names
     * @throws OXException If operation fails
     */
    List<String> getTemplateNames(Session session, String... filter) throws OXException;

    /**
     * Creates a templating helper.
     *
     * @param rootObject The root object
     * @param session The associated session
     * @param createCopy Whether to create a copy
     * @return The templating helper
     */
    TemplatingHelper createHelper(Object rootObject, Session session, boolean createCopy);
    
    /**
     * Loads an image from the configured template path, encodes the image to base64 and returns a {@link Pair} consisting of 
     * (contentType, base64Representation) of the image for use within data-urls in templates
     * 
     * @param imageName The name of the image within the templates path
     * @return a {@link Pair} consisting of (contentType, base64Representation) of the image at footerImagePath
     * @throws OXException if loading/encoding fails
     */
    public Pair<String, String> encodeTemplateImage(String imageName) throws OXException;

}
