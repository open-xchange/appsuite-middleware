/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.multifactor.storage.hazelcast.impl;

import static com.openexchange.java.Autoboxing.L;
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

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        long lifeTime = this.lifeTime.map( l -> L(l.toMillis())).orElse(L(0L)).longValue();
        writer.writeLong(FIELD_NAME_LIFE_TIME, lifeTime);

        try(ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(output)) {

            objectOutputStream.writeObject(writingTokenValue);
            writer.writeByteArray(FIELD_NAME_VALUE, output.toByteArray());
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        long lifeTimeValue = reader.readLong(FIELD_NAME_LIFE_TIME);
        this.lifeTime = lifeTimeValue > 0 ? Optional.of(Duration.ofMillis(lifeTimeValue)) : Optional.empty();

        ByteArrayInputStream input = new ByteArrayInputStream(reader.readByteArray(FIELD_NAME_VALUE));
        this.tokenValueData = new ObjectInputStream(input);
    }

}
