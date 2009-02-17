package com.openexchange.ajax.kata;

import com.openexchange.groupware.container.ContactObject;

/**
 * {@link ContactRunner}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class ContactRunner extends AbstractDirectoryRunner {
    public ContactRunner(String name) {
        super(name, "contactKatas" , ContactObject.class);
    }
}
