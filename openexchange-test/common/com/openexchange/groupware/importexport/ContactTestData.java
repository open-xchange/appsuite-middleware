package com.openexchange.groupware.importexport;

import com.openexchange.groupware.contact.helpers.ContactField;

public class ContactTestData {
    public static String DISPLAY_NAME1 = "Tobias Prinz";
	public static String NAME1 = "Prinz";
	public static String EMAIL1 = "tobias.prinz@open-xchange.com";
	public static String DISPLAY_NAME2 = "Francisco Laguna";
	public static String NAME2 = "Laguna";
	public static String EMAIL2 = "francisco.laguna@open-xchange.com";
    public static String IMPORT_HEADERS = ContactField.GIVEN_NAME.getReadableName()+","+ContactField.EMAIL1.getReadableName()+", "+ContactField.DISPLAY_NAME.getReadableName()+"\n";
    public static String IMPORT_ONE = IMPORT_HEADERS+NAME1+", "+EMAIL1+", "+DISPLAY_NAME1;
	public static String IMPORT_MULTIPLE = IMPORT_ONE + "\n"+NAME2+", "+EMAIL2+", "+DISPLAY_NAME2+"\n";
}
