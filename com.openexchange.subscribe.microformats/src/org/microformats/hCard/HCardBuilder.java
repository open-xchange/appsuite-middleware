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
import java.util.List;
import java.util.TimeZone;

/**
 * Standard builder pattern to create {@link HCard}s.
 *
 * @version 0.2
 * @author Reinier Zwitserloot
 * @author Carlton Northern
 */
public class HCardBuilder {
	/**
	 * To create a <code>NameBuilder</code>, call {@link HCard.Name#build()}.
	 */
	public static class NameBuilder {
		private String familyName;
		private String givenName;
		private final List<String> additionalNames = new ArrayList<String>();
		private final List<String> honorificPrefixes = new ArrayList<String>();
		private final List<String> honorificSuffixes = new ArrayList<String>();

		NameBuilder() {}

		public NameBuilder familyName(String familyName) {
			this.familyName = familyName;
			return this;
		}

		public NameBuilder givenName(String givenName) {
			this.givenName = givenName;
			return this;
		}

		public NameBuilder addAdditionalName(String additionalName) {
			additionalNames.add(additionalName);
			return this;
		}

		public NameBuilder addHonorificPrefix(String honorificPrefix) {
			honorificPrefixes.add(honorificPrefix);
			return this;
		}

		public NameBuilder addHonorificSuffix(String honorificSuffix) {
			honorificSuffixes.add(honorificSuffix);
			return this;
		}

		public HCard.Name done() {
			return new HCard.Name(familyName, givenName, additionalNames, honorificPrefixes, honorificSuffixes);
		}
	}

	public static class XFNURLBuilder {
		private URI url;
		private final List<XFNRelationship> rels = new ArrayList<XFNRelationship>();

		XFNURLBuilder() {}

		public XFNURLBuilder url(URI url) {
			this.url = url;
			return this;
		}

		public XFNURLBuilder addXFNRelationship(XFNRelationship rel) {
			if ( !rels.contains(rel) ) {
                rels.add(rel);
            }
			return this;
		}

		public XFNURLBuilder addXFNRelationship(String rel) {
			return addXFNRelationship(XFNRelationship.create(rel));
		}

		public HCard.XFNURL done() {
			return new HCard.XFNURL(url, rels);
		}
	}

	/**
	 * To create an <code>AddressBuilder</code>, call {@link HCard.Address#build()}.
	 */
	public static class AddressBuilder {
		private final List<String> types = new ArrayList<String>();
		private String postOfficeBox;
		private String streetAddress;
		private String extendedAddress;
		private String region;
		private String locality;
		private String postalCode;
		private String countryName;

		AddressBuilder() {}

		public AddressBuilder streetAddress(String streetAddress) {
			this.streetAddress = streetAddress;
			return this;
		}

		public AddressBuilder extendedAddress(String extendedAddress) {
			this.extendedAddress = extendedAddress;
			return this;
		}

		public AddressBuilder region(String region) {
			this.region = region;
			return this;
		}

		public AddressBuilder postOfficeBox(String postOfficeBox) {
			this.postOfficeBox = postOfficeBox;
			return this;
		}

		public AddressBuilder locality(String locality) {
			this.locality = locality;
			return this;
		}

		public AddressBuilder postalCode(String postalCode) {
			this.postalCode = postalCode;
			return this;
		}

		public AddressBuilder countryName(String countryName) {
			this.countryName = countryName;
			return this;
		}

		public AddressBuilder addType(String type) {
			type = type.toLowerCase();
			if ( !types.contains(type) ) {
                types.add(type);
            }
			return this;
		}

		public HCard.Address done() {
			if ( types.size() == 0 ) {
                types.addAll(HCard.Address.DEFAULT_TYPE_LIST);
            }
			return new HCard.Address(types, streetAddress, extendedAddress, locality, postOfficeBox, region, postalCode, countryName);
		}
	}

	private final String fn;
	private HCard.Name n;
	private final List<String> nicknames = new ArrayList<String>();
	private final List<URI> photos = new ArrayList<URI>();
	private Long bday;
	private final List<HCard.Address> adrs = new ArrayList<HCard.Address>();
	private final List<String> labels = new ArrayList<String>();
	private final List<HCard.Tel> tels = new ArrayList<HCard.Tel>();
	private final List<HCard.Email> emails = new ArrayList<HCard.Email>();
	private final List<String> mailers = new ArrayList<String>();
	private long tz;
	private HCard.Geolocation geo;
	private final List<String> titles = new ArrayList<String>();
	private final List<String> roles = new ArrayList<String>();
	private final List<URI> logos = new ArrayList<URI>();
	private final List<URI> agents = new ArrayList<URI>();
	private final List<HCard.Organization> orgs = new ArrayList<HCard.Organization>();
	private final List<String> categories = new ArrayList<String>();
	private final List<String> notes = new ArrayList<String>();
	private Long rev;
	private String sortString;
	private final List<URI> sounds = new ArrayList<URI>();
	private String uid;
	private final List<HCard.XFNURL> urls = new ArrayList<HCard.XFNURL>();
	private String accessClass;
	private final List<String> keys = new ArrayList<String>();

	HCardBuilder(String fn) {
		this.fn = fn;
	}

	public HCard done() {
		boolean isOrg = orgs.size() == 1 && fn.equals(orgs.get(0).name);
		if ( n == null && isOrg ) {
            n = new HCard.Name(null, null, null, null, null);
        }

		if ( !isOrg && (n == null || n.isEmpty()) && fn.matches("^\\w+$") ) {
			nicknames.add(0, fn);
			if ( n == null ) {
                n = new HCard.Name(null, null, null, null, null);
            }
		}

		if ( n == null ) {
            n = HCardCreator.impliedFN2N(fn);
        }

		return new HCard(fn, n, nicknames, photos, bday, adrs, labels, tels,
				emails, mailers, tz, geo, titles, roles, logos, agents, orgs, categories, notes, rev, sortString, sounds, uid, urls, accessClass, keys);
	}

	public HCardBuilder setN(HCard.Name n) {
		this.n = n;
		return this;
	}

	public HCardBuilder addNickname(String nickname) {
		if ( nickname == null ) {
            throw new NullPointerException();
        }
		this.nicknames.add(nickname);
		return this;
	}

	public HCardBuilder addPhoto(URI photo) {
		if ( photo == null ) {
            throw new NullPointerException();
        }
		this.photos.add(photo);
		return this;
	}

	public HCardBuilder setBday(long millis) {
		this.bday = millis;
		return this;
	}

	public HCardBuilder addAdr(HCard.Address adr) {
		if ( adr == null ) {
            throw new NullPointerException();
        }
		this.adrs.add(adr);
		return this;
	}

	public HCardBuilder addLabel(String label) {
		if ( label == null ) {
            throw new NullPointerException();
        }
		this.labels.add(label);
		return this;
	}

	public HCardBuilder addTel(HCard.Tel tel) {
		if ( tel == null ) {
            throw new NullPointerException();
        }
		this.tels.add(tel);
		return this;
	}

	/**
	 * Adds a telephone number with multiple types (labels)
	 */
	public HCardBuilder addTel(String value, String... types) {
		if ( value == null ) {
            throw new NullPointerException();
        }

		String[] t;

		if ( types == null || types.length == 0 ) {
            t = HCard.Tel.DEFAULT_TYPE_LIST.toArray(new String[0]);
        } else {
            t = types;
        }

		addTel(new HCard.Tel(value, t));
		return this;
	}

	public HCardBuilder addEmail(HCard.Email email) {
		if ( email == null ) {
            throw new NullPointerException();
        }
		this.emails.add(email);
		return this;
	}

	public HCardBuilder addEmail(String value, String... types) {
		if ( value == null ) {
            throw new NullPointerException();
        }

		String[] t;

		if ( types == null || types.length == 0 ) {
            t = HCard.Email.DEFAULT_TYPE_LIST.toArray(new String[0]);
        } else {
            t = types;
        }

		addEmail(new HCard.Email(value, t));
		return this;
	}

	public HCardBuilder addMailer(String mailer) {
		if ( mailer == null ) {
            throw new NullPointerException();
        }
		this.mailers.add(mailer);
		return this;
	}

	public HCardBuilder setTimeZone(TimeZone tz) {
		this.tz = tz.getRawOffset();
		return this;
	}

	public HCardBuilder setTimeZone(long tz) {
		this.tz = tz;
		return this;
	}

	public HCardBuilder setGeo(HCard.Geolocation geo) {
		this.geo = geo;
		return this;
	}

	public HCardBuilder setGeo(double latitude, double longitude) {
		this.geo = new HCard.Geolocation(latitude, longitude);
		return this;
	}

	public HCardBuilder addTitle(String title) {
		if ( title == null ) {
            throw new NullPointerException();
        }
		this.titles.add(title);
		return this;
	}

	public HCardBuilder addRole(String role) {
		if ( role == null ) {
            throw new NullPointerException();
        }
		this.roles.add(role);
		return this;
	}

	public HCardBuilder addLogo(URI logo) {
		if ( logo == null ) {
            throw new NullPointerException();
        }
		this.logos.add(logo);
		return this;
	}

	public HCardBuilder addAgent(URI agent) {
		if ( agent == null ) {
            throw new NullPointerException();
        }
		this.agents.add(agent);
		return this;
	}

	public HCardBuilder addOrganization(HCard.Organization org) {
		if ( org == null ) {
            throw new NullPointerException();
        }
		this.orgs.add(org);
		return this;
	}

	public HCardBuilder addOrganization(String orgName) {
		if ( orgName == null ) {
            throw new NullPointerException();
        }
		this.orgs.add(new HCard.Organization(orgName, ""));
		return this;
	}

	public HCardBuilder addOrganization(String orgName, String orgUnit) {
		if ( orgName == null ) {
            throw new NullPointerException();
        }
		if ( orgUnit == null ) {
            orgUnit = "";
        }
		this.orgs.add(new HCard.Organization(orgName, orgUnit));
		return this;
	}

	public HCardBuilder addCategory(String category) {
		if ( category == null ) {
            throw new NullPointerException();
        }
		this.categories.add(category);
		return this;
	}

	public HCardBuilder addNote(String note) {
		if ( note == null ) {
            throw new NullPointerException();
        }
		this.notes.add(note);
		return this;
	}

	public HCardBuilder addKey(String key) {
		if ( key == null ) {
            throw new NullPointerException();
        }
		this.keys.add(key);
		return this;
	}

	public HCardBuilder setRev(long millis) {
		this.rev = millis;
		return this;
	}

	public HCardBuilder setSortString(String sortString) {
		this.sortString = sortString;
		return this;
	}

	public HCardBuilder setUID(String uid) {
		this.uid = uid;
		return this;
	}

	public HCardBuilder setClass(String accessClass) {
		this.accessClass = accessClass;
		return this;
	}

	/**
	 * Adds a URL to the hCard, with no relationship information.
	 */
	public HCardBuilder addURL(URI url) {
		if ( url == null ) {
            throw new NullPointerException();
        }
		this.urls.add(new HCard.XFNURL(url, null));
		return this;
	}

	/**
	 * Adds a URL with XFN Relationship data to the hCard.
	 */
	public HCardBuilder addURL(HCard.XFNURL url) {
		if ( url == null ) {
            throw new NullPointerException();
        }
		this.urls.add(url);
		return this;
	}

	public HCardBuilder addSound(URI sound) {
		if ( sound == null ) {
            throw new NullPointerException();
        }
		this.sounds.add(sound);
		return this;
	}
}
