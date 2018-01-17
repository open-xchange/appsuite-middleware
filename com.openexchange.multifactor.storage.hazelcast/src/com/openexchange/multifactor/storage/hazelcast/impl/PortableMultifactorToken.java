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

package com.openexchange.multifactor.storage.hazelcast.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Optional;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.multifactor.MultifactorToken;

/**
 *
 * {@link PortableMultifactorToken} - The portable representation of {@link MultifactorToken}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class PortableMultifactorToken extends AbstractCustomPortable {

    private static final int CLASS_ID = 800;
    private static final String FIELD_NAME_LIFE_TIME = "lifeTime";
    private static final String FIELD_NAME_VALUE = "value";
    private Optional<Duration> lifeTime;
    private Object writingTokenValue;
    private ObjectInputStream tokenValueData;

    public PortableMultifactorToken(){ }

    public PortableMultifactorToken(Duration lifeTime, Object tokenValue){
        this.lifeTime = Optional.ofNullable(lifeTime);
        this.writingTokenValue = tokenValue;
    }

    public ObjectInputStream getTokenValueData() {
       return tokenValueData;
    }

    public Optional<Duration> getLifeTime(){
        return lifeTime;
    }

    /* (non-Javadoc)
     * @see com.openexchange.hazelcast.serialization.CustomPortable#getClassId()
     */
    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /* (non-Javadoc)
     * @see com.hazelcast.nio.serialization.Portable#writePortable(com.hazelcast.nio.serialization.PortableWriter)
     */
    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        long lifeTime = this.lifeTime.map( l -> l.toMillis()).orElse(0L);
        writer.writeLong(FIELD_NAME_LIFE_TIME, lifeTime);

        try(ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(output)) {

            objectOutputStream.writeObject(writingTokenValue);
            writer.writeByteArray(FIELD_NAME_VALUE, output.toByteArray());
        }
    }

    /* (non-Javadoc)
     * @see com.hazelcast.nio.serialization.Portable#readPortable(com.hazelcast.nio.serialization.PortableReader)
     */
    @Override
    public void readPortable(PortableReader reader) throws IOException {
        long lifeTimeValue = reader.readLong(FIELD_NAME_LIFE_TIME);
        this.lifeTime = lifeTimeValue > 0 ? Optional.of(Duration.ofMillis(lifeTimeValue)) : Optional.empty();

        ByteArrayInputStream input = new ByteArrayInputStream(reader.readByteArray(FIELD_NAME_VALUE));
        this.tokenValueData = new ObjectInputStream(input);;
    }
}
