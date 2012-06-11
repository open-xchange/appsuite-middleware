package com.openexchange.groupware.contact.helpers;

import java.util.Comparator;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ContactFieldDocumentor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Formatter formatter = new Formatter();
		ContactField[] fields = ContactField.values();
		Arrays.sort(fields, new Comparator<ContactField>() {
			@Override
			public int compare(ContactField o1, ContactField o2) {
				return o1.getNumber() - o2.getNumber();
			}
		});
		System.out.println(formatter.format("%3s %38s %38s\n", "#", "Ajax name", "OXMF name"));
		for(ContactField field: fields){
			System.out.println(formatter.format("%3s %38s %38s\n", field.getNumber(), field.getAjaxName(), oxmf(field.getAjaxName())));
			
		}

	}

	private static String oxmf(String ajaxName) {
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)_(\\w)([a-zA-Z0-9]+)");
		Matcher matcher = p.matcher(ajaxName);
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		while(matcher.find()){
			found = true;
			sb.append(matcher.group(1));
			sb.append(matcher.group(2).toUpperCase());
			sb.append(matcher.group(3));
		}
		if(!found) {
            return ajaxName;
        }
		return sb.toString();
	}

}
