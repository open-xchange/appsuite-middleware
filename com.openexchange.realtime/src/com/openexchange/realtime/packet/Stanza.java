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

package com.openexchange.realtime.packet;

/**
 * {@link Stanza} - Abstract information unit that can be send from one entity
 * to another. 
 *
 *  @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *  @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class Stanza<T extends Object> {

	private ID to, from;

	private String namespace = "default";

	private Payload payload;

	/**
	 * Initializes a new {@link Stanza}.
	 */
	protected Stanza() {
	    super();
	}

	/**
	 * Get the {@link ID} describing the stanza's recipient.
	 * @return null or the ID of the stanza's recipient
	 */
	public ID getTo() {
		return to;
	}

	/**
	 * Set the {@link ID} describing the Stanza's recipient.
	 * @param to the ID of the stanza's recipient
	 */
	public void setTo(final ID to) {
		this.to = to;
	}

	/**
	 * Get the {@link ID} describing the Stanza's sender. 
	 * @return the {@link ID} describing the Stanza's sender.
	 */
	public ID getFrom() {
		return from;
	}

	/**
     * Set the {@link ID} describing the Stanza's sender. 
     * @param from the {@link ID} describing the Stanza's sender.
     */
	public void setFrom(final ID from) {
		this.from = from;
	}

	/**
	 * Set the structured information of this Stanza.
	 * @param payload the structured information to transport. 
	 */
	public void setPayload(final Payload payload) {
		this.payload = payload;
	}

	/**
	 * Get the structured information of this Stanza.
	 * @return null or the structured information of this Stanza.
	 */
	public Payload getPayload() {
		return payload;
	}

	/**
	 * Get the namespace of this Stanza.
	 * Namespaces are used to separate data ownership and vocabularies.
	 * A namespace must not be specified for elements of the default namespace e.g. IQ, Presence, Message.
	 * @return the namespace of this Stanza
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Set the namespace of this Stanza.
     * Namespaces are used to separate data ownership and vocabularies.
	 * @param namespace the namespace of this Stanza
	 */
	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

}
