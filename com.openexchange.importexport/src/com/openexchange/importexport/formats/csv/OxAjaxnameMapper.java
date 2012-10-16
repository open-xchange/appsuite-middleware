package com.openexchange.importexport.formats.csv;

import com.openexchange.groupware.contact.helpers.ContactField;

public class OxAjaxnameMapper extends AbstractOutlookMapper {
	public OxAjaxnameMapper() {
		ContactField[] fields = ContactField.values();
		
		for(ContactField field: fields) {
			store(field, field.getAjaxName());
		}
	}
}
