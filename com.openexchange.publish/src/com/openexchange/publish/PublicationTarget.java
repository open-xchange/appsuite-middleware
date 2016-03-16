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

package com.openexchange.publish;

import com.openexchange.datatypes.genericonf.DynamicFormDescription;

/**
 * {@link PublicationTarget} - A publication target.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class PublicationTarget {

    private String id;
    private String displayName;
    private String icon;
    private String module;

    private DynamicFormDescription description;
    private PublicationService publicationService;

    /**
     * Initializes a new {@link PublicationTarget}.
     */
    public PublicationTarget() {
        super();
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the display name
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the icon
     *
     * @return The icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon
     *
     * @param icon The icon to set
     */
    public void setIcon(final String icon) {
        this.icon = icon;
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
     * Sets the module
     *
     * @param module The module to set
     */
    public void setModule(final String module) {
        this.module = module;
    }

    /**
     * Gets the form description
     *
     * @return The form description
     */
    public DynamicFormDescription getFormDescription() {
        return description;
    }

    /**
     * Sets the form description
     *
     * @param description The form description to set
     */
    public void setFormDescription(final DynamicFormDescription description) {
        this.description = description;
    }

    /**
     * Gets the publication service
     *
     * @return The publication service
     */
    public PublicationService getPublicationService() {
        return publicationService;
    }

    /**
     * Sets the publication service
     *
     * @param publicationService The publication service to set
     */
    public void setPublicationService(final PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Checks if this publication target is responsible for given module.
     *
     * @param module The module to check
     * @return <code>true</code> if responsible for given module; otherwise <code>false</code>
     */
    public boolean isResponsibleFor(final String module) {
        return this.module.equals(module);
    }

}
