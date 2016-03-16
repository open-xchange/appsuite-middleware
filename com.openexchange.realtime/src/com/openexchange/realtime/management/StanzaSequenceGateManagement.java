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

package com.openexchange.realtime.management;

import java.util.List;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementObject;


/**
 * {@link StanzaSequenceGateManagement}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaSequenceGateManagement extends ManagementObject<StanzaSequenceGateMBean> implements StanzaSequenceGateMBean {

    private ObjectName objectName = null;
    private final String name;
    private int bufferSize;
    private long numberOfInboxes;
    private Map<String, List<Long>> inboxes;
    private Map<String, Long> sequenceNumbers;
    
    /**
     * Initializes a new {@link RealtimeConfigManagement}.
     * @param mbeanInterface
     * @param isMxBean
     * @throws IllegalArgumentException if the String is missing
     */
    public StanzaSequenceGateManagement(String name) {
        super(StanzaSequenceGateMBean.class);
        if(Strings.isEmpty(name)) {
            throw new IllegalArgumentException("Parameter name must not be empty");
        }
        this.name  = name;
    }

    @Override
    public ObjectName getObjectName() {
        if (objectName == null) {
            String gateName = "StanzaSequenceGate-" + name;
            try {
                objectName = new ObjectName("com.openexchange.realtime", "name", gateName);
            } catch (MalformedObjectNameException e) {
                // can't happen: valid domain and no missing parameters
            } catch (NullPointerException e) {
                // can't happen: valid domain and no missing parameters
            }
        }
        return objectName;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Set the buffersize configured for this {@ link StanzaSequenceGate}
     * @param bufferSize The buffersize configured for this the number of inboxes handled by this {@ link StanzaSequenceGate}
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize=bufferSize;
        
    }

    @Override
    public long getNumberOfInboxes() {
        return numberOfInboxes;
    }

    /**
     * Set the number of inboxes handled by this {@ link StanzaSequenceGate}
     * @param numberOfInboxes The number of inboxes handled by this {@ link StanzaSequenceGate}
     */
    public void setNumberOfInboxes(long numberOfInboxes) {
        this.numberOfInboxes = numberOfInboxes;
    }

    @Override
    public Map<String, List<Long>> getInboxes() {
        return inboxes;
    }

    /**
     * @param unmodifiableMap
     */
    public void setInboxes(Map<String, List<Long>> inboxes) {
        this.inboxes = inboxes;
    }

    @Override
    public Map<String, Long> getSequenceNumbers() {
        return sequenceNumbers;
    }

    /**
     * @param sequenceNumbers
     */
    public void setSequenceNumbers(Map<String, Long> sequenceNumbers) {
        this.sequenceNumbers = sequenceNumbers;
    }

}
