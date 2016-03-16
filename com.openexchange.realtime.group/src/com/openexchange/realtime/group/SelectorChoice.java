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

package com.openexchange.realtime.group;

import org.apache.commons.lang.Validate;
import com.openexchange.realtime.packet.ID;

/**
 * {@link SelectorChoice} - When joining a {@link GroupDispatcher} clients have to choose a selector to associate with the joined
 * {@link GroupDispatcher} so they can correlate {@link Stanza}s they receive from ClientX via {@link GroupDispatcher}Y.
 * <p>
 * <img src="doc-files/SelectorChoice_interaction.png"/>
 * </p>
 * This class simply represents the combination of client, selector and {@link GroupDispatcher}. This can be useful for sending a
 * {@link Stanza} in the name of the {@link GroupDispatcher} to one of the clients when e.g the {@link GroupDispatcher} was already
 * destroyed.
 * <p>
 * <img src="doc-files/SelectorChoice_object" />
 * </p>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
/*
 * @startuml doc-files/SelectorChoice_object
 * object selectorChoice1 {
 * client = "client1@context1/resourceIdentifier1"
 * group="groupDispatcher1@synthetic/reourceIdentifier2"
 * selector="s1"
 * }
 * @enduml
 * 
 * @startuml doc-files/SelectorChoice_interaction.png
 * actor Client1
 * actor Client2
 * actor Client3
 * entity GroupDispatcher1
 * Client1 -> GroupDispatcher1: Join
 * note right: Join with selector <b>s1</b>
 * Client2 -> GroupDispatcher1: Join
 * note right: Join with selector <b>s2</b>
 * Client3 -> GroupDispatcher1: Join
 * note right: Join with selector <b>s3</b>
 * Client1 -> GroupDispatcher1: "Hello all"
 * GroupDispatcher1 -> Client2: "Hello all"
 * note right
 * client receives Stanza
 * <i> {from: "client1", selector: "<b>s2</b>", payload: "Hello all"}</i>
 * end note
 * GroupDispatcher1 -> Client3: "Hello all"
 * note right
 * client receives Stanza
 * <i> {from: "client1", selector: "<b>s3</b>", payload: "Hello all"}</i>
 * end note
 * @enduml
 */
public class SelectorChoice {

    protected ID client;

    protected ID group;

    protected String selector;

    public SelectorChoice() {
        super();
    }

    /**
     * Initializes a new {@link SelectorChoice}.
     * 
     * @param client The {@link ID} of the client
     * @param group The {@link ID} of the group
     * @param selector The selector the client has chosen for this group
     * @throws IllegalStateException when any of client, group or selector are missing
     */
    public SelectorChoice(ID client, ID group, String selector) {
        super();
        Validate.notNull(client, "Client must not be null");
        Validate.notNull(group, "Group must not be null");
        Validate.notEmpty(selector, "Selector must not be null");
        this.client = client;
        this.group = group;
        this.selector = selector;
    }

    /**
     * Initializes a new {@link SelectorChoice} based on an existing instance.
     * 
     * @param selectorChoice the instance to copy
     * @throws IllegalStateException when any of client, group or selector are missing in the selectorChoice
     */
    public SelectorChoice(SelectorChoice selectorChoice) {
        this(selectorChoice.client, selectorChoice.group, selectorChoice.selector);
    }

    /**
     * Gets the client
     * 
     * @return The client
     */
    public ID getClient() {
        return client;
    }

    /**
     * Sets the client
     * 
     * @param client The client to set
     */
    public void setClient(ID client) {
        this.client = client;
    }

    /**
     * Gets the group
     * 
     * @return The group
     */
    public ID getGroup() {
        return group;
    }

    /**
     * Sets the group
     * 
     * @param group The group to set
     */
    public void setGroup(ID group) {
        this.group = group;
    }

    /**
     * Gets the selector
     * 
     * @return The selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Sets the selector
     * 
     * @param selector The selector to set
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((selector == null) ? 0 : selector.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SelectorChoice))
            return false;
        SelectorChoice other = (SelectorChoice) obj;
        if (client == null) {
            if (other.client != null)
                return false;
        } else if (!client.equals(other.client))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (selector == null) {
            if (other.selector != null)
                return false;
        } else if (!selector.equals(other.selector))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SelectorChoice [client=" + client + ", group=" + group + ", selector=" + selector + "]";
    }

}
