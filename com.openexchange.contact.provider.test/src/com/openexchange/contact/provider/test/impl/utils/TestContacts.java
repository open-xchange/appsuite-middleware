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

package com.openexchange.contact.provider.test.impl.utils;

import java.util.Collections;
import java.util.List;
import com.openexchange.groupware.container.Contact;

/**
 * {@link TestContacts} holds a simple set of test contacts
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContacts {

    /**
     * The folder ID to use for testing purpose
     */
    public static final String TEST_FOLDER_ID = "0";

    /**
     * A simple list of test contacts
     */
    //https://dummyimage.com/
    public static final List<Contact> TEST_DATA = Collections.unmodifiableList(
        new TestContactsBuilder()
            .add(
                new TestContactBuilder()
                    .setFolderId(TEST_FOLDER_ID)
                    .setGivenName("Max")
                    .setSurname("Mustermann")
                    .setEmail("max.mustermann@example.org")
            )
            .add(
                new TestContactBuilder()
                    .setFolderId(TEST_FOLDER_ID)
                    .setGivenName("Lieschen")
                    .setSurname("Müller")
                    .setEmail("lieschen.mueller@example.org")
                    .setImageString("iVBORw0KGgoAAAANSUhEUgAAAUAAAAFABAMAAAA/vriZAAAAG1BMVEUZnCAWH5wXbU4XXV4YfD8XTW0WLowWPn0YjC/dm1nEAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAEVklEQVR4nO3YzVPbRhzGcSELo2N/UNs54uJOcsR5IVcbezIcY1qYHFFTQo5VOy09msYN/NmR9kXSru0S23Kn0/l+DuDVSqtH2pW0UhAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP7znv66zla3v9SdQwtfDPxF/YN1GpJvNs6y0K4c+4sIuBICbmpBwNHLdRr6FwOuh4CbIuCmCLjYw/isWozHbjk6HU/NTx1wXJT1+jO3HAQzt4G1ApazmehERDqHxe7yonTOp0755mURsJGKtN+VLY2SrN62dp/9iC+zBb95mVcOWDxJ4kkeQNo2YV+0jlMtMjABw0QV39qG/tbVP+nSsGW32K8rYFdavYfZiZjyvbSfPzycvhYxe5T2+Xh8dCmHJuBE3ve+m0jLbB6KnJ89ZGfx2AZ8I62z2fey0rD/h4ANaU11Tr1K2h6o/9Frs39T/jTQAffUuYr7tsW+vNPrHZiAkW7wXr6tJ2BXn5og0g2G8tFZ7a7sSx0wbZnjemIOwHTlUI+BYcv8CCadWgLGxVgZtvO/e6Z5K205RTmwgdN9cwCHuhzJlWqlk/wcmJppHQF3izOko+20nbVCvdsyYDH2uvoETYoDSFUPDMUe4Ur33uUBy+OMVNQ7t2Oa9gQVAe1b0466iGLT04G6PFRA2yOROzjWDdgtAyX5TfjOPYNDt5gFnBbR819hEThboAPaBbF38tcMOClvV5O8j3Zk0VplwKJH9YjYLc/wrlowLMdwssJjcXnApOiioLuv9uusmXr3ivLeoaM1yyshVAsqp7yegJV+UIModG9f5RDzyw2Vp3JN6TE3LIdM6m27VsC4HEPm+kirq86No3I201DrVa4p3VTdAaO5gHsiPxaLqtULAw63HTCU6wvL3PqzyULHvvE9GrDbLja/2FLACjOe8ulVaxDo6scCVre/2krAm9vC76bqU2pnX48HbJeb327jIllyux+l0pkGXzMG3Uf1di+SqmzS+fGrAvpTlm3eZhxR0glWu81sJ+Dy1zQ9j/Kr/YDek3ELAZNlrTTU6Ez8R50XsOlP+moPOFn2bqN7d+JPFryAlcnClgJ2l07MVZauP93yAs7dh2oP2PSm+JUsVwuq/YBzY6D2gI1ll7Hu4oY/5fcD9r0eqD1gnCz5FKwvEr96LuBnr+XaA2ZTYKcqLvZkXiPd6rmAkbhHUH9A++KePYLzP00z1wrFvv92dHU8WBjQvrhnKxxvFLAyJ/jTCZjtofUs+zc7Uffcplz3sn9/pXZs9qWTVcej9O3igA2R/DtTPNIz/LUDVjxxA0aJyIfb/IuQ2l/2o/0hKd9/82pJ7Nei+YDBm2yDmz+yFbYVMAhTtfj6mQ50qUpmQlhWv58uCRg81ZNJ/b1uzYCPOH118bz84jg7evWi51cP/G0qoqOLH3rT2tIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwP/AF6DF0PdgQtjlAAAAAElFTkSuQmCC")
                    .setImageContentType("image/png")
            )
            .add(
                new TestContactBuilder()
                    .setFolderId(TEST_FOLDER_ID)
                    .setGivenName("John")
                    .setSurname("Doe")
                    .setEmail("john.doe@example.org")
            )
            .add(
                new TestContactBuilder()
                    .setFolderId(TEST_FOLDER_ID)
                    .setGivenName("Jane")
                    .setSurname("Doe")
                    .setEmail("jane.doe@example.org")
                    .setImageString("iVBORw0KGgoAAAANSUhEUgAAAUAAAAFABAMAAAA/vriZAAAAG1BMVEX3NjYWH5y+ME9OJIKiLVwyIY+GKmlqJ3XaM0JwO+D0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAFk0lEQVR4nO2ay1fbRhSHbUnGWlYhDl3aJAWWVTlxssThtM0yKk3J0iJQukQHON5KBKj/7GhmbGse14/xyIVz+vtWII1G3zzu1WisRgMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8TXh8e/vzUDgvpRdEPT+2wEAi6AkFXIOgKBF2BoCsQdAWCrkDQFQi6AkFXIOgKLTguWXbleJwvOBsuPr06puDd5dskYpycz7/F/S0rcXo2x+63t7yCsxocdcE9XvWEzj9K2SCKXoi/vk5L/EUpPMbT09tHNQvex5HKL5RgmFYFdkzDfbmJzoaK4HFkcEUI/ioX+EOv8V/l+k63RkHP9Iu2c0Pwm5ihh59FiSO1wkAcPRmNxGR5Vb/g6fluaRXeXSbsP2nvSwiG7PBpl5XnU/GlWmHGjt3wZnkX+hjUINi56c7OhexmO7pgeUX05+QIH87qgobo3mrivVYrqEGwr9yNG1ZHAtZdnjJsP5X/fZKvSNTAYNN1WJ+gllbEhPok//eSXdDJK6FYHWO/vOC9JvyiNsGwa5xO5fqZoKfNqofSV/o31ofUV9rjKEjgyzdkgg9aXHpKHAdGULAudAqTJYLs/opgrM+pTA70osxL5g1+3KBgI5GipBTsGImtkAUy02bLMY6XCaZSj4kcPFQL+FKUhGbe5k3MNyjYk6YQF9SHUO6hthoxgtQt0SwTLKQ8wwW/aAU8yblpPFe0GjYg2JLOB8ZzgyGF0YByabtFia2gOeHjao4ZIc7w3BYMywR9qf1M0OyMrOrViIwHc95uUtBMuukscj0qRli/UkfrEmxLz7qAmoJs4g3FX3NSXuqUZ2wFyRom3epTQcxbcPSfCRKzqZgJtuh4LZwSoaUgMYTF7GHcpOtqOi0XLAWJhNGcCfaUF4QZLfqwk2D45vLw8+SlyEqQ6qqW06OEEry/Vl7sVhYc0JPNd9r+MQXFu9g6gnOWBTULPib6q/HKghmdT9wexrrg1lTr5N2IcWsn2P/b5KJOwUD0X/9DPjlgE8WZ3vcz6hPk9+jn1YFnJsj3BZSXYxvBeOOCfF9gqJx/XoL6voClYDnEZ7sk3fUFB7JgZhpYCh6tb7KCIFtO6Xd4VoItQsB3f5I4C86e5Cmx7HhywVQSjIi1uY3gwHk7lSCreo1cj7bc14NuxFWlbWqvsWkh6LiHQJNUw9KilkWFhWDTcaeNJKomdkGN0MBC0Hfd7iWQN8zIFXtmIdh2/1XEIJAil1yx2yxY6ddSN8pGz97FqTTm2QiGVJ5ypCU1mhL0bQTp3S03CumelGDPSjCtP8/Iv4NQD4LYSrCoP4wTqc3Eg4Dvqa4u6NceJYGUBln79UT9YCfoEes1N1py3DXNAYqjbRtBNiPqnYSZPCbmAJVJqG8l2HP+9VVFyXLEBmrp/2gl2I7qTTQsiVSBy/KsEiXs0bVlJcheC9d/2uX6AS9RU3+mDhD7LfjKTpA3ebiu4OFH7UCZBJVN5UKtfcDmpKUgmybydxicpR8xTYiVPQ3xc77SXLZxVHUh+97gi60gb7RaKDymdtZpQfmTiZBvA+7oJabfS4THoi9sBXlqf5VXBx5jcut/jmDEPzoZj8dvfk8ivQMnefn0Y94I93jhq4a1oBiXzuSrrXDvNqJ/m5gvqKB9NhQmylnWmdaCobhN52Q0up58B7aqoL61S3x49c04ay043WKUWHkBsa9dSnwYlupn7QUbgTZUN+Zt5uFdyopqTAvCC+3sGoLqNrz6qdBSwr1r4djpH9Al9mN+Vs+Zdtx/FXd5d56vcXU43j1YdN14d9XMuojx3UEd1QAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAm+c7qPkfluoHxeMAAAAASUVORK5CYII=")
                    .setImageContentType("image/png")
            )
            .add(
                new TestContactBuilder()
                    .setFolderId(TEST_FOLDER_ID)
                    .setGivenName("Gustav")
                    .setSurname("Gast")
                    .setEmail("gustav.gast@example.org")
                    .setImageString("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")
                    .setImageContentType("image/png")
    ).getContacts());
}
