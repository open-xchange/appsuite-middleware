/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit http://creativecommons.org/licenses/publicdomain/ or
 * send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * Check HCard.java's top-level comment for more information.
 */
package org.microformats.hCard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.microformats.hCard.HCardParser.Result;

class HCardCreator {
	static HCard.Name createName(Result result) {
		if ( result.property != HCardProperty.N ) {
            throw new IllegalArgumentException();
        }

		String familyName = null;
		String givenName = null;

		List<String> additionalNames = new ArrayList<String>();
		List<String> honorificPrefixes = new ArrayList<String>();
		List<String> honorificSuffixes = new ArrayList<String>();

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case N__FAMILY_NAME:
            		if ( familyName == null ) {
                        familyName = sub.value;
                    }
            		break;
            	case N__GIVEN_NAME:
            		if ( givenName == null ) {
                        givenName = sub.value;
                    }
            		break;
            	case N__ADDITIONAL_NAME: additionalNames.add(sub.value); break;
            	case N__HONORIFIC_PREFIX: honorificPrefixes.add(sub.value); break;
            	case N__HONORIFIC_SUFFIX: honorificSuffixes.add(sub.value); break;
            	}
            }
        }

		return new HCard.Name(familyName, givenName, additionalNames, honorificPrefixes, honorificSuffixes);
	}

	static HCard.Address createAddress(Result result) {
		if ( result.property != HCardProperty.ADR ) {
            throw new IllegalArgumentException();
        }

		List<String> types = new ArrayList<String>();
		String postOfficeBox = null;
		String streetAddress = null;
		String extendedAddress = null;
		String region = null;
		String locality = null;
		String postalCode = null;
		String countryName = null;

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case ADR__COUNTRY_NAME:
            		if ( countryName == null ) {
                        countryName = sub.value;
                    }
            		break;
            	case ADR__EXTENDED_ADDRESS:
            		if ( extendedAddress == null ) {
                        extendedAddress = sub.value;
                    }
            		break;
            	case ADR__LOCALITY:
            		if ( locality == null ) {
                        locality = sub.value;
                    }
            		break;
            	case ADR__POST_OFFICE_BOX:
            		if ( postalCode == null ) {
                        postOfficeBox = sub.value;
                    }
            		break;
            	case ADR__POSTAL_CODE:
            		if ( postalCode == null ) {
                        postalCode = sub.value;
                    }
            		break;
            	case ADR__REGION:
            		if ( region == null ) {
                        region = sub.value;
                    }
            		break;
            	case ADR__STREET_ADDRESS:
            		if ( streetAddress == null ) {
                        streetAddress = sub.value;
                    }
            		break;
            	case ADR__TYPE: types.add(sub.value.toLowerCase(Locale.ENGLISH)); break;
            	}
            }
        }

		if ( types.isEmpty() ) {
            types = HCard.Address.DEFAULT_TYPE_LIST;
        }

		return new HCard.Address(types, streetAddress, extendedAddress, locality, postOfficeBox, region, postalCode, countryName);
	}

	private static final List<String> TEL_URL_SCHEMES = Collections.unmodifiableList(Arrays.asList("tel", "fax", "modem"));
	private static String getTelValueFrom(Result result) {
		String value;

		if ( result.uri != null && TEL_URL_SCHEMES.contains(result.uri.getScheme()) ) {
			value = result.uri.getSchemeSpecificPart();
			int idx = value.indexOf(';');
			if ( idx != -1 ) {
                value = value.substring(0, idx);
            }
		} else {
            value = result.value;
        }

		if ( value != null ) {
            value = value.trim();
        }
		return value;
	}

	static HCard.Tel createTel(Result result) {
		if ( result.property != HCardProperty.TEL ) {
            throw new IllegalArgumentException();
        }

		List<String> types = new ArrayList<String>();
		String value = null;

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case TEL__TYPE: types.add(sub.value.toLowerCase(Locale.ENGLISH)); break;
            	case TEL__VALUE:
            		if ( value == null ) {
                        value = getTelValueFrom(sub);
                    }
            		break;
            	}
            }
        }

		if ( types.isEmpty() ) {
            types = HCard.Tel.DEFAULT_TYPE_LIST;
        }
		if ( value == null ) {
            value = getTelValueFrom(result);
        }

		if ( value == null || value.length() == 0 ) {
            return null;
        } else {
            return new HCard.Tel(value, types.toArray(new String[0]));
        }
	}

	private static String getEmailValueFrom(Result result) {
		String value;

		if ( result.uri != null && "mailto".equals(result.uri.getScheme()) ) {
			value = result.uri.getSchemeSpecificPart();
			final int pos = value.indexOf('?');
            if (pos > -1 ) {
                value = value.substring(0, pos);
            }
		} else {
            value = result.value;
        }

		if ( value != null ) {
            value = value.trim();
        }
		return value;
	}

	static HCard.Email createEmail(Result result) {
		if ( result.property != HCardProperty.EMAIL ) {
            throw new IllegalArgumentException();
        }

		List<String> types = new ArrayList<String>();
		String value = null;

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case EMAIL__TYPE: types.add(sub.value.toLowerCase(Locale.ENGLISH)); break;
            	case EMAIL__VALUE:
            		if ( value == null ) {
                        value = getEmailValueFrom(sub);
                    }
            		break;
            	}
            }
        }

		if ( types.isEmpty() ) {
            types = HCard.Email.DEFAULT_TYPE_LIST;
        }
		if ( value == null ) {
            value = getEmailValueFrom(result);
        }

		if ( value == null || value.length() == 0 ) {
            return null;
        } else {
            return new HCard.Email(value, types.toArray(new String[0]));
        }
	}

	static HCard.Geolocation createGeolocation(Result result) {
		if ( result.property != HCardProperty.GEO ) {
            throw new IllegalArgumentException();
        }

		String latitude = null;
		String longitude = null;

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case GEO__LATITUDE:
            		if ( latitude == null ) {
                        latitude = sub.value;
                    }
            		break;
            	case GEO__LONGITUDE:
            		if ( longitude == null ) {
                        longitude = sub.value;
                    }
            		break;
            	}
            }
        }

		if ( latitude == null && longitude == null && result.value != null ) {
			String[] elems = result.value.split(";");
			if ( elems.length == 2 ) {
				latitude = elems[0];
				longitude = elems[1];
			}
		}

		if (null == longitude || null == latitude) {
		    return null;
		}
		try {
			double lat = Double.parseDouble(latitude.trim());
			double lon = Double.parseDouble(longitude.trim());
			return new HCard.Geolocation(lat, lon);
		} catch ( Exception e ) {
			return null;
		}
	}

	static HCard.Organization createOrganization(Result result) {
		if ( result.property != HCardProperty.ORG ) {
            throw new IllegalArgumentException();
        }

		String unit = null;
		String name = null;

		if ( result.subResults != null ) {
            for ( Result sub : result.subResults ) {
            	switch ( sub.property ) {
            	case ORG__ORGANIZATION_NAME:
            		if ( name == null ) {
                        name = sub.value;
                    }
            		break;
            	case ORG__ORGANIZATION_UNIT:
            		if ( unit == null ) {
                        unit = sub.value;
                    }
            		break;
            	}
            }
        }

		if ( unit == null && name == null ) {
			name = result.value;
			if ( name == null ) {
                name = "";
            }
			name = name.trim();
			if ( name.length() == 0 ) {
                return null;
            }
			unit = "";
		}

		return new HCard.Organization(name, unit);
	}

	static HCard.XFNURL createXFNURL(Result result) {
		if ( result.property != HCardProperty.URL ) {
            throw new IllegalArgumentException();
        }

		URI url = result.uri;
		if ( result.rel == null || result.rel.length() == 0 ) {
            return new HCard.XFNURL(url, null);
        }
		List<XFNRelationship> list = new ArrayList<XFNRelationship>();
		for ( String rel : result.rel.split("\\s+") ) {
            list.add(XFNRelationship.create(rel));
        }
		return new HCard.XFNURL(url, list);
	}

	static HCard createHCard(List<Result> results) {
		String fn = null;
		HCard.Name n = null;
		List<String> nicknames = new ArrayList<String>();
		List<URI> photos = new ArrayList<URI>();
		Long bday = null;
		List<HCard.Address> adrs = new ArrayList<HCard.Address>();
		List<String> labels = new ArrayList<String>();
		List<HCard.Tel> tels = new ArrayList<HCard.Tel>();
		List<HCard.Email> emails = new ArrayList<HCard.Email>();
		List<String> mailers = new ArrayList<String>();
		Long tz = null;
		HCard.Geolocation geo = null;
		List<String> titles = new ArrayList<String>();
		List<String> roles = new ArrayList<String>();
		List<URI> logos = new ArrayList<URI>();
		List<URI> agents = new ArrayList<URI>();
		List<HCard.Organization> orgs = new ArrayList<HCard.Organization>();
		List<String> categories = new ArrayList<String>();
		List<String> notes = new ArrayList<String>();
		Long rev = null;
		String sortString = null;
		List<URI> sounds = new ArrayList<URI>();
		String uid = null;
		List<HCard.XFNURL> urls = new ArrayList<HCard.XFNURL>();
		String accessClass = null;
		List<String> keys = new ArrayList<String>();

		for ( Result result : results ) {
			switch ( result.property ) {
			case ADR:
				HCard.Address adr = createAddress(result);
				if ( adr != null ) {
                    adrs.add(adr);
                }
				break;
			case AGENT:
				URI agent = result.uri;
				if ( agent != null ) {
                    agents.add(agent);
                }
				break;
			case BDAY:
				if ( bday == null ) {
                    bday = parseDate(result.value);
                }
				break;
			case CATEGORY:
				String category = result.value;
				if ( category != null ) {
                    categories.add(category);
                }
				break;
			case CLASS:
				if ( accessClass == null ) {
                    accessClass = result.value;
                }
				break;
			case EMAIL:
				HCard.Email email = createEmail(result);
				if ( email != null ) {
                    emails.add(email);
                }
				break;
			case FN:
				if ( fn == null ) {
                    fn = result.value;
                }
				break;
			case GEO:
				if ( geo == null ) {
                    geo = createGeolocation(result);
                }
				break;
			case KEY:
				String key = result.value;
				if ( key != null ) {
                    keys.add(key);
                }
				break;
			case LABEL:
				String label = result.value;
				if ( label != null ) {
                    labels.add(label);
                }
				break;
			case LOGO:
				URI logo = result.uri;
				if ( logo != null ) {
                    logos.add(logo);
                }
				break;
			case MAILER:
				String mailer = result.value;
				if ( mailer != null ) {
                    mailers.add(mailer);
                }
				break;
			case N:
				if ( n == null ) {
                    n = createName(result);
                }
				break;
			case NICKNAME:
				String nickname = result.value;
				if ( nickname != null ) {
                    nicknames.add(nickname);
                }
				break;
			case NOTE:
				String note = result.value;
				if ( note != null ) {
                    notes.add(note);
                }
				break;
			case ORG:
				HCard.Organization org = createOrganization(result);
				if ( org != null ) {
                    orgs.add(org);
                }
				break;
			case PHOTO:
				URI photo = result.uri;
				if ( photo != null ) {
                    photos.add(photo);
                }
				break;
			case REV:
				if ( rev == null ) {
                    rev = parseDate(result.value);
                }
				break;
			case ROLE:
				String role = result.value;
				if ( role != null ) {
                    roles.add(role);
                }
				break;
			case SORT_STRING:
				if ( sortString == null ) {
                    sortString = result.value;
                }
				break;
			case SOUND:
				URI sound = result.uri;
				if ( sound != null ) {
                    sounds.add(sound);
                }
				break;
			case TEL:
				HCard.Tel tel = createTel(result);
				if ( tel != null ) {
                    tels.add(tel);
                }
				break;
			case TITLE:
				String title = result.value;
				if ( title != null ) {
                    titles.add(title);
                }
				break;
			case TZ:
				if ( tz == null ) {
                    tz = parseTimezone(result.value);
                }
				break;
			case UID:
				if ( uid == null ) {
                    uid = result.value;
                }
				break;
			case URL:
				HCard.XFNURL url = createXFNURL(result);
				if ( url != null ) {
                    urls.add(url);
                }
				break;
			}
		}

		//No fn means this is not a (valid) hCard.
		if ( fn == null ) {
            return null;
        }

		boolean isOrg = orgs.size() == 1 && fn.equals(orgs.get(0).name);

		// ---- Implied "nickname" optimization
		if ( !isOrg && (n == null || n.isEmpty()) && fn.matches("^\\w+$") ) {
			List<String> newNicks = new ArrayList<String>();
			newNicks.add(fn);
			newNicks.addAll(nicknames);
			nicknames = newNicks;
			n = new HCard.Name(null, null, null, null, null);
		}

		// ---- Organization Contact Info - always has an empty 'N' value.
		if ( isOrg ) {
            n = new HCard.Name(null, null, null, null, null);
        }

		// ---- Implied "n" optimization
		if ( n == null && !isOrg ) {
            n = impliedFN2N(fn);
        }

		return new HCard(fn, n, nicknames, photos, bday, adrs, labels, tels, emails, mailers, tz, geo, titles, roles, logos,
				agents, orgs, categories, notes, rev, sortString, sounds, uid, urls, accessClass, keys);
	}

	static HCard.Name impliedFN2N(String fn) {
		String[] elems = fn.trim().split("\\s+");
		if ( elems.length == 2 ) {
			String given, family;

			if ( elems[0].endsWith(",") ) {
				family = elems[0].substring(0, elems[0].length() -1);
				given = elems[1];
			} else if ( elems[1].length() < 2 || (elems[1].length() == 2 && elems[1].endsWith(".")) ) {
				family = elems[0];
				given = elems[1];
			} else {
				family = elems[1];
				given = elems[0];
			}

			return new HCard.Name(family, given, null, null, null);
		} else {
            return null;
        }
	}

	private static Long parseDate(String value) {
		try {
			return ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC).parseMillis(value);
		} catch ( Exception e ) {
			return null;
		}
	}

	private static final Pattern TIMEZONE_REGEX = Pattern.compile("^([\\-+])(\\d\\d?)(?::?(\\d\\d))$");

	private static Long parseTimezone(String value) {
		try {
			Matcher matcher = TIMEZONE_REGEX.matcher(value);
			if ( !matcher.matches() ) {
                return null;
            }
			boolean negative = matcher.group(1).charAt(0) == '-';
			int hours = Integer.parseInt(matcher.group(2));
			int minutes = matcher.groupCount() > 2 ? Integer.parseInt(matcher.group(3)) : 0;

			long millis = ((hours * 60L) + minutes) * 60L * 1000L;
			if ( negative ) {
                millis = -millis;
            }
			return millis;
		} catch ( Exception e ) {
			return null;
		}
	}
}
