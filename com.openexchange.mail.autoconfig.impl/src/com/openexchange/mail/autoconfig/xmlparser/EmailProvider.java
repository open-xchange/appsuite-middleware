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

package com.openexchange.mail.autoconfig.xmlparser;

import java.util.Collection;

/**
 * {@link EmailProvider}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class EmailProvider {

    public static final String DOMAIN = "domain";

    public static final String DISPLAY_NAME = "displayName";

    public static final String DISPLAY_SHORT_NAME = "displayShortName";

    public static final String INCOMING_SERVER = "incomingServer";

    public static final String OUTGOING_SERVER = "outgoingServer";

    public static final String DOCUMENTATION = "documentation";

    public static final String INSTRUCTION = "instruction";

    private String provider;

    private Collection<String> domains;

    private String displayName;

    private String displayShortName;

    private Collection<IncomingServer> incomingServer;

    private Collection<OutgoingServer> outgoingServer;

    private Collection<Documentation> documentations;

    private Collection<Instruction> instructions;

    /**
     * Gets the provider
     *
     * @return The provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider
     *
     * @param provider The provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Gets the domains
     *
     * @return The domains
     */
    public Collection<String> getDomains() {
        return domains;
    }

    /**
     * Sets the domains
     *
     * @param domains The domains to set
     */
    public void setDomains(Collection<String> domains) {
        this.domains = domains;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the displayName
     *
     * @param displayName The displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the displayShortName
     *
     * @return The displayShortName
     */
    public String getDisplayShortName() {
        return displayShortName;
    }

    /**
     * Sets the displayShortName
     *
     * @param displayShortName The displayShortName to set
     */
    public void setDisplayShortName(String displayShortName) {
        this.displayShortName = displayShortName;
    }

    /**
     * Gets the incomingServer
     *
     * @return The incomingServer
     */
    public Collection<IncomingServer> getIncomingServer() {
        return incomingServer;
    }

    /**
     * Sets the incomingServer
     *
     * @param incomingServer The incomingServer to set
     */
    public void setIncomingServer(Collection<IncomingServer> incomingServer) {
        this.incomingServer = incomingServer;
    }

    /**
     * Gets the outgoingServer
     *
     * @return The outgoingServer
     */
    public Collection<OutgoingServer> getOutgoingServer() {
        return outgoingServer;
    }

    /**
     * Sets the outgoingServer
     *
     * @param outgoingServer The outgoingServer to set
     */
    public void setOutgoingServer(Collection<OutgoingServer> outgoingServer) {
        this.outgoingServer = outgoingServer;
    }

    /**
     * Gets the documentations
     *
     * @return The documentations
     */
    public Collection<Documentation> getDocumentations() {
        return documentations;
    }

    /**
     * Sets the documentations
     *
     * @param documentations The documentations to set
     */
    public void setDocumentations(Collection<Documentation> documentations) {
        this.documentations = documentations;
    }

    /**
     * Gets the instructions
     *
     * @return The instructions
     */
    public Collection<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions
     *
     * @param instructions The instructions to set
     */
    public void setInstructions(Collection<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("EmailProvider [");
        if (provider != null) {
            builder.append("provider=").append(provider).append(", ");
        }
        if (domains != null) {
            builder.append("domains=").append(domains).append(", ");
        }
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(", ");
        }
        if (displayShortName != null) {
            builder.append("displayShortName=").append(displayShortName).append(", ");
        }
        if (incomingServer != null) {
            builder.append("incomingServer=").append(incomingServer).append(", ");
        }
        if (outgoingServer != null) {
            builder.append("outgoingServer=").append(outgoingServer).append(", ");
        }
        if (documentations != null) {
            builder.append("documentations=").append(documentations).append(", ");
        }
        if (instructions != null) {
            builder.append("instructions=").append(instructions);
        }
        builder.append("]");
        return builder.toString();
    }

}
