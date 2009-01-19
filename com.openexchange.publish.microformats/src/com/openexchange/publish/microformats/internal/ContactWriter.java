package com.openexchange.publish.microformats.internal;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.publish.microformats.ItemWriter;


public class ContactWriter implements ItemWriter<ContactObject> {

    public String write(ContactObject item) {
        StringBuilder builder = new StringBuilder(1200);
        builder.append("<div class=\"ox-contact\">\n");
        appendAttribute("surname", item.getSurName(), builder);
        appendAttribute("givenName", item.getGivenName(), builder);
        builder.append("</div>\n");
        return builder.toString();
    }

    private void appendAttribute(String attr, String surName, StringBuilder builder) {
        builder.append("<span class=\"").append(attr).append("\">").append(surName).append("</span>\n");
    }

}
