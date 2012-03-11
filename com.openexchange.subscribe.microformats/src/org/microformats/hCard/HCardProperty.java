/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit http://creativecommons.org/licenses/publicdomain/ or
 * send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * Check HCard.java's top-level comment for more information.
 */
package org.microformats.hCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This is the hCard 'profile'. While lots of hCard specific implied 'rules' and other logic resides in the HCardParser class directly,
 * this is the basic list of legal tags. double underscore implies parenthood; N__FAMILY_NAME finds class="family-name" as a child of a tag
 * with class="n", for example. If a property suggests it's a URL, src/href (for img/a/area tags) is preferred over content.
 */
enum HCardProperty {
	FN(null, true, false),
	N(null, true, false),
		N__FAMILY_NAME(N, true, false),
		N__GIVEN_NAME(N, true, false),
		N__ADDITIONAL_NAME(N, true, false),
		N__HONORIFIC_PREFIX(N, true, false),
		N__HONORIFIC_SUFFIX(N, true, false),
	NICKNAME(null, false, false),
	SORT_STRING(null, true, false),
	URL(null, false, true),
	EMAIL(null, false, true),
		EMAIL__TYPE(EMAIL, false, false),
		EMAIL__VALUE(EMAIL, false, true),
	TEL(null, false, true),
		TEL__TYPE(TEL, false, false),
		TEL__VALUE(TEL, false, true),
	ADR(null, false, false),
		ADR__POST_OFFICE_BOX(ADR, false, false),
		ADR__EXTENDED_ADDRESS(ADR, false, false),
		ADR__STREET_ADDRESS(ADR, false, false),
		ADR__LOCALITY(ADR, false, false),
		ADR__REGION(ADR, false, false),
		ADR__POSTAL_CODE(ADR, false, false),
		ADR__COUNTRY_NAME(ADR, false, false),
		ADR__TYPE(ADR, false, false),
	LABEL(null, false, false),
	GEO(null, true, false),
		GEO__LATITUDE(GEO, true, false),
		GEO__LONGITUDE(GEO, true, false),
	TZ(null, true, false),
	PHOTO(null, false, true),
	LOGO(null, false, true),
	AGENT(null, false, true),
	SOUND(null, false, true),
	BDAY(null, true, false),
	TITLE(null, false, false),
	ROLE(null, false, false),
	ORG(null, false, false),
		ORG__ORGANIZATION_NAME(ORG, false, false),
		ORG__ORGANIZATION_UNIT(ORG, false, false),
	CATEGORY(null, false, false),
	NOTE(null, false, false),
	CLASS(null, true, false),
	KEY(null, false, false),
	MAILER(null, false, false),
	UID(null, true, false),
	REV(null, false, false);

	private final HCardProperty parent;
	private final boolean singular;
	private final boolean isUrl;

	HCardProperty(HCardProperty parent, boolean singular, boolean isUrl) {
		this.parent = parent;
		this.singular = singular;
		this.isUrl = isUrl;
	}

	static HCardProperty fromClassAttribute(Collection<HCardProperty> contexts, String hClass) {
		if ( hClass == null ) {
            return null;
        }
		List<String> enumNames = new ArrayList<String>(contexts == null ? 1 : contexts.size() + 1);
		String baseName = hClass.toUpperCase(Locale.ENGLISH).replaceAll("-", "_");
		enumNames.add(baseName);
		if ( contexts != null ) {
            for ( HCardProperty context : contexts ) {
                enumNames.add(String.format("%s__%s", context.name(), baseName));
            }
        }

		Collections.reverse(enumNames);
		for ( String enumName : enumNames ) {
            try {
            	HCardProperty p = HCardProperty.valueOf(enumName);
            	if ( p != null ) {
                    return p;
                }
            } catch ( Exception ignore ) {}
        }

		return null;
	}

	boolean isSingular() {
		return singular;
	}

	boolean isUrl() {
		return isUrl;
	}

	boolean isTopLevel() {
		return parent == null;
	}

	HCardProperty parent() {
		return parent;
	}
}
