/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit http://creativecommons.org/licenses/publicdomain/ or
 * send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * Created in 2007 by Reinier Zwitserloot
 * XFN support includes contributions by Carlton Northern.
 *
 * Change log:
 *  October 2007: First release (Reinier Zwitserloot)
 *  Januari 2009: Added XFN support (Carlton Northern, Reinier Zwitserloot)
 */
package org.microformats.hCard;

import java.io.Serializable;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Immutable representation of all data that can legally show up in an HCard.
 * <p>
 * General rule: Values in 'subtypes' such as Name, Address, and Tel are NEVER null - they are either empty lists or empty strings if there is no value.
 * Lists are NEVER null - they are always the empty list if there are no values.
 * In other circumstances, 'null' indicates complete absence of that information in the hCard.
 * <p>
 * To create an HCard object, parse one using the {@link org.microformats.hCard.HCardParser}, or call {@link #build(String)}
 * <p>
 * Output methods:<br>
 * The {@link #toString()} method produces a human readable version of the HCard.<br>
 * The {@link #toJSON()} method produces a <a href="http://www.json.org/">JSON</a> formatted version of the HCard.<br>
 * The {@link #toHTML()} method produces a minimal HTML version that should parse out with any hCard parser, though it won't look like much.
 *
 * @version 0.5
 * @author Reinier Zwitserloot
 * @author Carlton Northern
 * @see <a href="http://microformats.org/wiki/hcard">hCard specification on microformats.org</a>
 */
public final class HCard implements Serializable {
	private static final long serialVersionUID = -6684491319880806937L;

	public static HCardBuilder build(String fn) {
		return new HCardBuilder(fn);
	}

	private static <T> List<T> immutableList(List<T> list) {
		if ( list == null ) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
	}

	public static final class Name implements Serializable {
		private static final long serialVersionUID = 2946216905219526596L;

		public final String familyName;
		public final String givenName;
		public final List<String> additionalNames;
		public final List<String> honorificPrefixes;
		public final List<String> honorificSuffixes;

		public static HCardBuilder.NameBuilder build() {
			return new HCardBuilder.NameBuilder();
		}

		public Name(String familyName, String givenName, List<String> additionalNames, List<String> honorificPrefixes, List<String> honorificSuffixes) {
			this.familyName = familyName == null ? "" : familyName;
			this.givenName = givenName == null ? "" : givenName;
			this.additionalNames = immutableList(additionalNames);
			this.honorificPrefixes = immutableList(honorificPrefixes);
			this.honorificSuffixes = immutableList(honorificSuffixes);
		}

		public boolean isEmpty() {
			return familyName.length() + givenName.length() + additionalNames.size() + honorificPrefixes.size() + honorificSuffixes.size() == 0;
		}

		public @Override int hashCode() {
			final int prime = 97;
			int result = 1;
			result = prime * result + additionalNames.hashCode();
			result = prime * result + familyName.hashCode();
			result = prime * result + givenName.hashCode();
			result = prime * result + honorificPrefixes.hashCode();
			result = prime * result + honorificSuffixes.hashCode();
			return result;
		}

		public @Override boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( !(obj instanceof Name) ) {
                return false;
            }
			final Name other = (Name)obj;
			if ( !familyName.equals(other.familyName) ) {
                return false;
            }
			if ( !givenName.equals(other.givenName) ) {
                return false;
            }
			if ( !additionalNames.equals(other.additionalNames) ) {
                return false;
            }
			if ( !honorificPrefixes.equals(other.honorificPrefixes) ) {
                return false;
            }
			if ( !honorificSuffixes.equals(other.honorificSuffixes) ) {
                return false;
            }
			return true;
		}

		public @Override String toString() {
			StringBuilder sb = new StringBuilder();
			for ( String honorific : honorificPrefixes ) {
                sb.append(honorific + " ");
            }
			if ( givenName.length() > 0 ) {
                sb.append(givenName + " ");
            }
			for ( String name : additionalNames ) {
                sb.append(name + " ");
            }
			if ( familyName.length() > 0 ) {
                sb.append(familyName + " ");
            }
			for ( String honorific : honorificSuffixes ) {
                sb.append(honorific + " ");
            }

			return sb.toString();
		}

		public String toJSON() {
			return String.format("{\"family-name\":%s,\"given-name\":%s,\"additional-names\":%s,\"honorific-prefixes\":%s,\"honorific-suffixes\":%s}",
					s2j(familyName), s2j(givenName), sl2j(additionalNames), sl2j(honorificPrefixes), sl2j(honorificSuffixes));
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class=\"n\">");
			for ( String honorific : honorificPrefixes ) {
                sb.append("<span class=\"honorific-prefix\">").append(s2h(honorific)).append("</span> ");
            }
			if ( givenName.length() > 0 ) {
                sb.append("<span class=\"given-name\">").append(s2h(givenName)).append("</span> ");
            }
			for ( String additional : additionalNames ) {
                sb.append("<span class=\"additional-name\">").append(s2h(additional)).append("</span> ");
            }
			if ( familyName.length() > 0 ) {
                sb.append("<span class=\"family-name\">").append(s2h(familyName)).append("</span> ");
            }
			for ( String honorific : honorificSuffixes ) {
                sb.append("<span class=\"honorific-suffix\">").append(s2h(honorific)).append("</span> ");
            }
			sb.append("</div>");

			return sb.toString();
		}
	}

	public static final class Address implements Serializable {
		private static final long serialVersionUID = 7525396887713460884L;

		public static final String INTL = "intl";
		public static final String POSTAL = "postal";
		public static final String PARCEL = "parcel";
		public static final String WORK = "work";
		public static final String DOM = "dom";
		public static final String HOME = "home";
		public static final String PREF = "pref";
		public static final List<String> DEFAULT_TYPE_LIST = Collections.unmodifiableList(Arrays.asList(
				INTL, POSTAL, PARCEL, WORK));

		/** Always lower case */
		public final List<String> types;
		public final String postOfficeBox;
		public final String streetAddress;
		public final String extendedAddress;
		public final String region;
		public final String locality;
		public final String postalCode;
		public final String countryName;

		public static HCardBuilder.AddressBuilder build() {
			return new HCardBuilder.AddressBuilder();
		}

		public Address(List<String> types, String streetAddress, String extendedAddress, String locality,
				String postOfficeBox, String region, String postalCode, String countryName) {
			this.types = immutableList(types);
			this.streetAddress = streetAddress == null ? "" : streetAddress;
			this.extendedAddress = extendedAddress == null ? "" : extendedAddress;
			this.locality = locality == null ? "" : locality;
			this.postOfficeBox = postOfficeBox == null ? "" : postOfficeBox;
			this.region = region == null ? "" : region;
			this.postalCode = postalCode == null ? "" : postalCode;
			this.countryName = countryName == null ? "" : countryName;
		}

		public @Override int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + countryName.hashCode();
			result = prime * result + extendedAddress.hashCode();
			result = prime * result + locality.hashCode();
			result = prime * result + postOfficeBox.hashCode();
			result = prime * result + postalCode.hashCode();
			result = prime * result + region.hashCode();
			result = prime * result + streetAddress.hashCode();
			return result;
		}

		public @Override boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( !(obj instanceof Address) ) {
                return false;
            }
			final Address other = (Address)obj;
			if ( !countryName.equals(other.countryName) ) {
                return false;
            }
			if ( !extendedAddress.equals(other.extendedAddress) ) {
                return false;
            }
			if ( !locality.equals(other.locality) ) {
                return false;
            }
			if ( !postOfficeBox.equals(other.postOfficeBox) ) {
                return false;
            }
			if ( !postalCode.equals(other.postalCode) ) {
                return false;
            }
			if ( !region.equals(other.region) ) {
                return false;
            }
			if ( !streetAddress.equals(other.streetAddress) ) {
                return false;
            }
			return true;
		}

		public @Override String toString() {
			StringBuilder sb = new StringBuilder();
			if ( streetAddress.length() > 0 ) {
                sb.append(streetAddress + "\n");
            }
			if ( extendedAddress.length() > 0 ) {
                sb.append(extendedAddress + "\n");
            }
			boolean space = false;

			if ( locality.length() > 0 ) {
				sb.append(locality);
				space = true;
			}

			if ( postOfficeBox.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append(postOfficeBox);
				space = true;
			}

			if ( region.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append(region);
				space = true;
			}

			if ( postalCode.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append(postalCode);
				space = true;
			}

			if ( space ) {
                sb.append('\n');
            }
			if ( countryName.length() > 0 ) {
                sb.append(countryName + "\n");
            }

			return sb.toString();
		}

		public String toJSON() {
			String pob = postOfficeBox.length() > 0 ? String.format("\"post-office-box\":%s,", s2j(postOfficeBox)) : "";
			String sa = streetAddress.length() > 0 ? String.format("\"street-address\":%s,", s2j(streetAddress)) : "";
			String ea = extendedAddress.length() > 0 ? String.format("\"extended-address\":%s,", s2j(extendedAddress)) : "";
			String r = region.length() > 0 ? String.format("\"region\":%s,", s2j(region)) : "";
			String l = locality.length() > 0 ? String.format("\"locality\":%s,", s2j(locality)) : "";
			String pc = postalCode.length() > 0 ? String.format("\"postal-code\":%s,", s2j(postalCode)) : "";
			String cn = countryName.length() > 0 ? String.format("\"country-name\":%s,", s2j(countryName)) : "";

			StringBuilder sb = new StringBuilder();
			sb.append("{\"types\":").append(sl2j(types)).append(',').append(sa).append(ea).append(pob).append(r).append(l).append(pc).append(cn);
			sb.setLength(sb.length() -1);
			sb.append('}');
			return sb.toString();
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class=\"adr\"> Address");

			if ( types.size() > 0 && !types.equals(DEFAULT_TYPE_LIST) ) {
				sb.append(" [");
				boolean first = true;
				for ( String type : types ) {
					if ( first ) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
					sb.append("<span class=\"type\">").append(s2h(type)).append("</span>");
				}
				sb.append(']');
			}
			sb.append(":<br />");
			if ( streetAddress.length() > 0 ) {
                sb.append("<span class=\"street-address\">").append(s2h(streetAddress)).append("</span><br />");
            }
			if ( extendedAddress.length() > 0 ) {
                sb.append("<span class=\"extended-address\">").append(s2h(extendedAddress)).append("</span><br />");
            }

			boolean space = false;
			if ( locality.length() > 0 ) {
				sb.append("<span class=\"locality\">").append(s2h(locality)).append("</span>");
				space = true;
			}

			if ( postOfficeBox.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append("<span class=\"post-office-box\">").append(s2h(postOfficeBox)).append("</span>");
				space = true;
			}

			if ( region.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append("<span class=\"region\">").append(s2h(region)).append("</span>");
				space = true;
			}

			if ( postalCode.length() > 0 ) {
				if ( space ) {
                    sb.append(", ");
                }
				sb.append("<span class=\"postal-code\">").append(s2h(postalCode)).append("</span>");
				space = true;
			}
			if ( countryName.length() > 0 ) {
				if ( space ) {
                    sb.append("<br />");
                }
				sb.append("<span class=\"country-name\">").append(s2h(countryName)).append("</span>");
			}

			sb.append("</div>");
			return sb.toString();
		}
	}

	public static final class Tel implements Serializable {
		private static final long serialVersionUID = 6527163115412154386L;

		public static final String VOICE = "voice";
		public static final String HOME = "home";
		public static final String MSG = "msg";
		public static final String WORK = "work";
		public static final String PREF = "pref";
		public static final String FAX = "fax";
		public static final String CELL = "cell";
		public static final String VIDEO = "video";
		public static final String PAGER = "pager";
		public static final String BBS = "bbs";
		public static final String MODEM = "modem";
		public static final String CAR = "car";
		public static final String ISDN = "isdn";
		public static final String PCS = "pcs";
		public static final List<String> DEFAULT_TYPE_LIST = Collections.unmodifiableList(Arrays.asList(
				VOICE));

		public final List<String> types;
		public final String value;

		public Tel(String value, String... types) {
			this.types = immutableList(Arrays.asList(types));
			this.value = value == null ? "" : value;
		}

		public @Override int hashCode() {
			return 421 + value.hashCode();
		}

		public @Override boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( !(obj instanceof Tel) ) {
                return false;
            }
			return value.equals(((Tel)obj).value);
		}

		public @Override String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(types.toString().substring(1));
			sb.setLength(sb.length() -1);
			if ( sb.length() > 0 ) {
                sb.append(": ");
            }
			sb.append(value);
			return sb.toString();
		}

		public String toJSON() {
			return String.format("{\"types\":%s,\"value\":%s}", sl2j(types), s2j(value));
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();

			sb.append("<div class=\"tel\"> Tel");
			if ( types.size() > 0 && !types.equals(DEFAULT_TYPE_LIST) ) {
				sb.append(" [");
				boolean first = true;
				for ( String type : types ) {
					if ( first ) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
					sb.append("<span class=\"type\">").append(s2h(type)).append("</span>");
				}
				sb.append(']');
			}
			sb.append(": ");
			sb.append("<span class=\"value\">").append(s2h(value)).append("</span></div>");

			return sb.toString();
		}
	}

	public static final class Email implements Serializable {
		private static final long serialVersionUID = -8224878605667906296L;

		public static final String INTERNET = "internet";
		public static final String X400 = "x400";
		public static final String PREF = "pref";
		public static final List<String> DEFAULT_TYPE_LIST = Collections.unmodifiableList(Arrays.asList(
				INTERNET));

		public final List<String> types;
		public final String value;

		public Email(String value, String... types) {
			this.types = immutableList(Arrays.asList(types));
			this.value = value == null ? "" : value;
		}

		public @Override int hashCode() {
			return 1123 + value.hashCode();
		}

		public @Override boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( !(obj instanceof Email) ) {
                return false;
            }
			return value.equals(((Email)obj).value);
		}

		public @Override String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(types.toString().substring(1));
			sb.setLength(sb.length() -1);
			if ( sb.length() > 0 ) {
                sb.append(": ");
            }
			sb.append(value);
			return sb.toString();
		}

		public String toJSON() {
			return String.format("{\"types\":%s,\"value\":%s}", sl2j(types), s2j(value));
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();

			sb.append("<div class=\"email\"> Email");
			if ( types.size() > 0 && !types.equals(DEFAULT_TYPE_LIST) ) {
				sb.append(" [");
				boolean first = true;
				for ( String type : types ) {
					if ( first ) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
					sb.append("<span class=\"type\">").append(s2h(type)).append("</span>");
				}
				sb.append(']');
			}
			sb.append(": ");
			sb.append("<a href=\"mailto:").append(s2a(value)).append("\" class=\"value\">").append(s2h(value)).append("</a></div>");

			return sb.toString();
		}
	}

	public static final class Geolocation implements Serializable {
		private static final long serialVersionUID = -2046683495038969122L;

		public final double latitude, longitude;

		public Geolocation(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public @Override int hashCode() {
			Long a1 = Double.doubleToLongBits(latitude);
			Long a2 = Double.doubleToLongBits(longitude);

			final int prime = 1567;
			int result = 1;
			result = prime * result + a1.hashCode();
			result = prime * result + a2.hashCode();
			return result;
		}

		public @Override boolean equals(Object obj) {
			if ( obj == this ) {
                return true;
            }
			if ( !(obj instanceof Geolocation) ) {
                return false;
            }
			final Geolocation other = (Geolocation)obj;
			return latitude == other.latitude && longitude == other.longitude;
		}

		public @Override String toString() {
			return String.format("Latitude: %.6f Longitude: %.6f", latitude, longitude);
		}

		public String toJSON() {
			return String.format("{\"latitude\":%.6f,\"longitude\":%.6f}", latitude, longitude);
		}

		public String toHTML() {
			return String.format("<div class=\"geo\">Location: <span class=\"latitude\">%.6f</span> " +
					"Longitude: <span class=\"longitude\">%.6f</span></div>", latitude, longitude);
		}
	}

	public static final class Organization implements Serializable {
		private static final long serialVersionUID = -6247900991038736546L;

		public final String name, unit;

		public Organization(String name, String unit) {
			this.name = name == null ? "" : name;
			this.unit = unit == null ? "" : unit;
		}

		public @Override int hashCode() {
			return (7 + name.hashCode()) * 7 + unit.hashCode();
		}

		public @Override boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( !(obj instanceof Organization) ) {
                return false;
            }
			final Organization other = (Organization)obj;
			return (name.equals(other.name) && unit.equals(other.unit));
		}

		public @Override String toString() {
			if ( unit.equals("") ) {
                return name;
            } else {
                return String.format("%s (%s)", name, unit);
            }
		}

		public String toJSON() {
			return String.format("{\"organization-name\":%s,\"organization-unit\":%s}", s2j(name), s2j(unit));
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class=\"org\">Organization: ");
			sb.append("<span class=\"organization-name\">").append(s2h(name)).append("</span>");
			if ( unit.length() > 0 ) {
                sb.append(" [<span class=\"organization-unit\">").append(s2h(unit)).append("</span>]");
            }
			sb.append("</div>");
			return sb.toString();
		}
	}

	public static final class XFNURL implements Serializable {
		private static final long serialVersionUID = -704459786744404792L;

		public final URI url;
		public final List<XFNRelationship> rels;

		public static HCardBuilder.XFNURLBuilder build() {
			return new HCardBuilder.XFNURLBuilder();
		}

		public XFNURL(URI url, List<XFNRelationship> rels) {
			if ( url == null ) {
                throw new NullPointerException();
            }
			this.url = url;
			if ( rels == null || rels.isEmpty() ) {
                this.rels = Collections.emptyList();
            } else {
                this.rels = Collections.unmodifiableList(new ArrayList<XFNRelationship>(rels));
            }
		}

		@Override public int hashCode() {
			return (53 + url.hashCode()) * 53 + rels.hashCode();
		}

		@Override public boolean equals(Object obj) {
			if ( this == obj ) {
                return true;
            }
			if ( ! (obj instanceof XFNURL) ) {
                return false;
            }
			final XFNURL other = (XFNURL)obj;
			if ( !url.equals(other.url) || rels.size() != other.rels.size() ) {
                return false;
            }

			return equalRels(rels, other.rels);
		}

		static boolean equalRels(List<XFNRelationship> one, List<XFNRelationship> two) {
			//for equality, we'll consider order irrelevant, so we dupe the list and remove one at a time.
			List<XFNRelationship> list = new ArrayList<XFNRelationship>(one);
			for ( XFNRelationship rel : two ) {
                if ( !list.remove(rel) ) {
                    return false;
                }
            }
			return true;
		}

		@Override public String toString() {
			if ( rels.isEmpty() ) {
                return url.toString();
            } else {
                return String.format("%s  rel: %s", url.toString(), xfnRelsToString());
            }
		}

		private String xfnRelsToString() {
			StringBuilder out = new StringBuilder();
			for ( XFNRelationship rel : rels ) {
                out.append(rel).append(' ');
            }
			out.setLength(out.length() -1);
			return out.toString();
		}

		public String toJSON() {
			if ( rels.isEmpty() ) {
                return String.format("{\"url\":%s}", s2j(url.toString()));
            } else {
                return String.format("{\"url\":%s,\"rels\":%s", s2j(url.toString()), sl2j(rels));
            }
		}

		public String toHTML() {
			StringBuilder sb = new StringBuilder();
			if ( rels.isEmpty() ) {
                sb.append("<a class=\"url\" href=\"");
            } else {
				for ( XFNRelationship rel : rels ) {
                    sb.append(rel).append(' ');
                }
				sb.setCharAt(sb.length() -1, ':');
				sb.append(" <a class=\"url\" rel=\"");
				for ( XFNRelationship rel : rels ) {
                    sb.append(s2a(rel.toString())).append(' ');
                }
				sb.setCharAt(sb.length() -1, '"');
			}
			sb.append(' ').append(url.toASCIIString()).append("\">").append(s2h(url.toString())).append("</a><br />");
			return sb.toString();
		}
	}

	public final String fn;
	public final Name n;
	public final List<String> nicknames;
	public final List<URI> photos;
	public final Long bday;
	public final List<Address> adrs;
	public final List<String> labels;
	public final List<Tel> tels;
	public final List<Email> emails;
	public final List<String> mailers;
	public final Long tz;
	public final Geolocation geo;
	public final List<String> titles;
	public final List<String> roles;
	public final List<URI> logos;
	public final List<URI> agents;
	public final List<Organization> orgs;
	public final List<String> categories;
	public final List<String> notes;
	public final Long rev;
	public final String sortString;
	public final List<URI> sounds;
	public final String uid;

	/** Any urls in the hCard are represented as both a url and an xfnUrl - pick whichever one you like. */
	public final List<URI> urls;

	/** Any urls in the hCard are represented as both a url and an xfnUrl - pick whichever one you like. */
	public final List<XFNURL> xfnUrls;

	public final String accessClass;
	public final List<String> keys;

	HCard(String fn, Name n, List<String> nicknames, List<URI> photos, Long bday, List<Address> adrs, List<String> labels, List<Tel> tels,
			List<Email> emails, List<String> mailers, Long tz, Geolocation geo, List<String> titles, List<String> roles,
			List<URI> logos, List<URI> agents, List<Organization> orgs, List<String> categories, List<String> notes, Long rev,
			String sortString, List<URI> sounds, String uid, List<XFNURL> xfns, String accessClass, List<String> keys) {
		if ( fn == null ) {
            throw new IllegalArgumentException("fn must be specified.");
        }
		this.fn = fn;
		this.n = n == null ? new Name(null, null, null, null, null) : n;
		this.nicknames = immutableList(nicknames);
		this.photos = immutableList(photos);
		this.bday = bday;
		this.adrs = immutableList(adrs);
		this.labels = immutableList(labels);
		this.tels = immutableList(tels);
		this.emails = immutableList(emails);
		this.mailers = immutableList(mailers);
		this.tz = tz;
		this.geo = geo;
		this.titles = immutableList(titles);
		this.roles = immutableList(roles);
		this.logos = immutableList(logos);
		this.agents = immutableList(agents);
		this.orgs = immutableList(orgs);
		this.categories = immutableList(categories);
		this.notes = immutableList(notes);
		this.rev = rev;
		this.sortString = sortString;
		this.sounds = immutableList(sounds);
		this.uid = uid;
		this.xfnUrls = immutableList(xfns);
		List<URI> urls = new ArrayList<URI>();
		for ( XFNURL xfn : xfnUrls ) {
            urls.add(xfn.url);
        }
		this.urls = immutableList(urls);
		this.accessClass = accessClass;
		this.keys = immutableList(keys);
	}

	public @Override String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("hCard for ").append(fn).append('\n');
		if ( !n.isEmpty() ) {
            sb.append("  Name: ").append(n).append('\n');
        }
		if ( nicknames.size() > 0 ) {
            sb.append("  Nickname: ").append(printCommaList(nicknames)).append('\n');
        }
		if ( bday != null ) {
            sb.append("  Birth day: ").append(printDate(bday.longValue())).append('\n');
        }
		if ( tels.size() > 0 ) {
            sb.append("  Tel Nr: ").append(printCommaList(tels)).append('\n');
        }
		if ( emails.size() > 0 ) {
            sb.append("  Email: ").append(printCommaList(emails)).append('\n');
        }
		if ( geo != null ) {
            sb.append("  Geolocation: ").append(geo).append('\n');
        }
		if ( tz != null ) {
            sb.append("  Timezone: ").append(tz).append('\n');
        }
		if ( adrs.size() > 0 ) {
            sb.append("  Address:\n").append(printBlockList(adrs));
        }
		if ( labels.size() > 0 ) {
            sb.append("  Label:\n").append(printBlockList(labels));
        }
		if ( photos.size() > 0 ) {
            sb.append("  Photo: ").append(printLineList(photos)).append('\n');
        }
		if ( sounds.size() > 0 ) {
            sb.append("  Sounds: ").append(printLineList(sounds)).append('\n');
        }
		if ( mailers.size() > 0 ) {
            sb.append(" Mailer:").append(printLineList(mailers)).append('\n');
        }
		if ( titles.size() > 0 ) {
            sb.append("  Title:").append(printCommaList(titles)).append('\n');
        }
		if ( orgs.size() > 0 ) {
            sb.append("  Organization: ").append(printLineList(orgs)).append('\n');
        }
		if ( roles.size() > 0 ) {
            sb.append("  Roles: ").append(printCommaList(roles)).append('\n');
        }
		if ( logos.size() > 0 ) {
            sb.append("  Logo: ").append(printLineList(logos)).append('\n');
        }
		if ( agents.size() > 0 ) {
            sb.append("  Agent: ").append(printLineList(agents)).append('\n');
        }
		if ( categories.size() > 0 ) {
            sb.append("  Category: ").append(printCommaList(categories)).append('\n');
        }
		if ( xfnUrls.size() > 0 ) {
            sb.append("  Url: ").append(printLineList(urls)).append('\n');
        }
		if ( keys.size() > 0 ) {
            sb.append("  Key: ").append(printBlockList(keys));
        }
		if ( notes.size() > 0 ) {
            sb.append("  Note: ").append(printBlockList(notes));
        }
		if ( rev != null ) {
            sb.append("  Rev: ").append(printDate(rev.longValue())).append('\n');
        }
		if ( sortString != null ) {
            sb.append("  SortString: ").append(sortString).append('\n');
        }
		if ( uid != null ) {
            sb.append("  UID: " ).append(uid).append('\n');
        }
		if ( accessClass != null ) {
            sb.append(" Class: ").append(accessClass).append('\n');
        }

		return sb.toString();
	}

	public String toHTML() {
		StringBuilder sb = new StringBuilder();

		sb.append("<div class=\"vcard\">");

		boolean orgCard = orgs.size() == 1 && orgs.get(0).name.equals(fn);
		sb.append("Contact information for <span class=\"fn").append(orgCard ? " org" : "").append("\">").append(s2h(fn)).append("</span><br />");

		if ( !orgCard ) {
			if ( n.isEmpty() ) {
                sb.append("<span class=\"n\"></span>");
            } else {
                sb.append("Proper Name: ").append(n.toHTML());
            }
		}

		sb.append(printStrings2HTMLLine(nicknames, "nickname", "Nickname", "Nicknames"));

		if ( bday != null ) {
            sb.append("Birth Date: <span class=\"bday\">").append(date2s(bday)).append("</span><br />");
        }

		for ( Tel tel : tels ) {
            sb.append(tel.toHTML());
        }
		for ( Email email : emails ) {
            sb.append(email.toHTML());
        }
		if ( geo != null ) {
            sb.append(geo.toHTML());
        }
		if ( tz != null ) {
            sb.append("Timezone: <span class=\"tz\">").append(tz2s(tz)).append("</span><br />");
        }
		for ( Address adr : adrs ) {
            sb.append(adr.toHTML());
        }
		for ( String label : labels ) {
            sb.append("Shipping Label: <div class=\"label\">").append(label).append("</div>");
        }
		for ( URI photo : photos ) {
            sb.append("<img class=\"photo\" src=\"").append(photo.toASCIIString()).append("\" /><br />");
        }
		for ( XFNURL url : xfnUrls ) {
            sb.append(url.toHTML());
        }

		if ( !orgCard ) {
            for ( Organization org : orgs ) {
                sb.append(org.toHTML());
            }
        }
		for ( URI logo : logos ) {
            sb.append("<img class=\"logo\" src=\"").append(logo.toASCIIString()).append("\" /><br />");
        }
		sb.append(printStrings2HTMLLine(roles, "role", "Role", "Roles"));

		sb.append(printStrings2HTMLLine(titles, "title", "Title", "Titles"));
		sb.append(printStrings2HTMLLine(categories, "category", "Category", "Categories"));
		sb.append(printStrings2HTMLLine(mailers, "mailer", "Mailer", "Mailers"));

		sb.append(printURIs2HTMLBlock(sounds, "sound", "Sound", "Sounds"));
		sb.append(printURIs2HTMLBlock(agents, "agent", "Agent", "Agents"));

		if ( keys.size() > 0 ) {
			sb.append("Key").append(keys.size() == 1 ? "" : "s").append(" :<br />");
			for ( String key : keys ) {
                sb.append("<div class=\"key\">").append(s2h(key)).append("</div>");
            }
		}

		if ( notes.size() > 0 ) {
			sb.append("Note").append(notes.size() == 1 ? "" : "s").append(" :<br />");
			for ( String note : notes ) {
                sb.append("<div class=\"note\">").append(s2h(note)).append("</div>");
            }
		}

		if ( rev != null ) {
            sb.append("Last Updated: <span class=\"rev\">").append(date2s(rev)).append("</span><br />");
        }
		if ( sortString != null ) {
            sb.append("Sort String: <span class=\"sort-string\">").append(s2h(sortString)).append("</span><br />");
        }
		if ( accessClass != null ) {
            sb.append("Access class: <span class=\"class\">").append(s2h(accessClass)).append("</span><br />");
        }
		if ( uid != null ) {
            sb.append("UID: <span class=\"uid\">").append(s2h(uid)).append("</span><br />");
        }

		sb.append("</div>");

		return sb.toString();
	}

	private static String printStrings2HTMLLine(List<String> items, String classValue, String singular, String plural) {
		if ( items.size() == 0 ) {
            return "";
        }
		String t = items.size() == 1 ? singular : plural;
		return String.format("%s: %s<br />", t, sl2h(items, classValue));
	}

	private static String printURIs2HTMLBlock(List<URI> items, String classValue, String singular, String plural) {
		switch ( items.size() ) {
		case 0: return "";
		case 1: return singular + ": " + uri2ahref(items.get(0), classValue) + "<br />";
		default:
			StringBuilder sb = new StringBuilder();
			sb.append(plural).append(": <br /><ul>");
			for ( URI item : items ) {
                sb.append("<li>").append(uri2ahref(item, classValue)).append("</li>");
            }
			return sb.append("</ul>").toString();
		}
	}

	private static String uri2ahref(URI item, String classValue) {
		return String.format("<a class=\"%s\" href=\"%s\">%s</a>", s2a(classValue), item.toASCIIString(), s2h(item.toString()));
	}

	public String toJSON() {
		StringBuilder sb = new StringBuilder();

		sb.append('{');
		appendStringValue(sb, "fn", s2j(fn));
		appendStringValue(sb, "n", n.toJSON());
		if ( nicknames.size() > 0 ) {
            appendStringValue(sb, "nicknames", sl2j(nicknames));
        }
		if ( bday != null ) {
            appendStringValue(sb, "bday", "" + bday);
        }

		if ( tels.size() > 0 ) {
			sb.append("\"tels\":[");
			boolean first = true;
			for ( Tel tel : tels ) {
				if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
				sb.append(tel.toJSON());
			}
			sb.append(']');
		}

		if ( emails.size() > 0 ) {
			sb.append("\"emails\":[");
			boolean first = true;
			for ( Email email : emails ) {
				if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
				sb.append(email.toJSON());
			}
			sb.append(']');
		}

		if ( geo != null ) {
            appendStringValue(sb, "geo", geo.toJSON());
        }
		if ( tz != null ) {
            appendStringValue(sb, "tz", tz2s(tz));
        }

		if ( adrs.size() > 0 ) {
			sb.append("\"adrs\":[");
			boolean first = true;
			for ( Address adr : adrs ) {
				if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
				sb.append(adr.toJSON());
			}
			sb.append(']');
		}

		if ( labels.size() > 0 ) {
            appendStringValue(sb, "labels", sl2j(nicknames));
        }
		if ( photos.size() > 0 ) {
            appendStringValue(sb, "photos", sl2j(photos));
        }
		if ( sounds.size() > 0 ) {
            appendStringValue(sb, "sounds", sl2j(sounds));
        }
		if ( mailers.size() > 0 ) {
            appendStringValue(sb, "mailers", sl2j(mailers));
        }
		if ( titles.size() > 0 ) {
            appendStringValue(sb, "titles", sl2j(titles));
        }

		if ( orgs.size() > 0 ) {
			sb.append("\"orgs\":[");
			boolean first = true;
			for ( Organization org : orgs ) {
				if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
				sb.append(org.toJSON());
			}
			sb.append(']');
		}
		if ( roles.size() > 0 ) {
            appendStringValue(sb, "roles", sl2j(roles));
        }
		if ( logos.size() > 0 ) {
            appendStringValue(sb, "logos", sl2j(logos));
        }
		if ( agents.size() > 0 ) {
            appendStringValue(sb, "agents", sl2j(agents));
        }
		if ( categories.size() > 0 ) {
            appendStringValue(sb, "categories", sl2j(categories));
        }
		if ( urls.size() > 0 ) {
            appendStringValue(sb, "urls", sl2j(urls));
        }
		if ( xfnUrls.size() > 0 ) {
			sb.append("\"xfnUrls\":[");
			boolean first = true;
			for ( XFNURL url : xfnUrls ) {
				if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
				sb.append(url.toJSON());
			}
			sb.append(']');
		}
		if ( keys.size() > 0 ) {
            appendStringValue(sb, "keys", sl2j(keys));
        }
		if ( notes.size() > 0 ) {
            appendStringValue(sb, "notes", sl2j(notes));
        }
		if ( rev != null ) {
            appendStringValue(sb, "rev", "" + rev);
        }
		if ( sortString != null ) {
            appendStringValue(sb, "sort-string", s2j(sortString));
        }
		if ( uid != null ) {
            appendStringValue(sb, "uid", s2j(uid));
        }
		if ( accessClass != null ) {
            appendStringValue(sb, "class", s2j(accessClass));
        }

		sb.setLength(sb.length() -1);
		sb.append('}');

		return sb.toString();
	}

	private static StringBuilder appendStringValue(StringBuilder sb, String name, String value) {
		return sb.append(s2j(name)).append(':').append(value).append(',');
	}

	private static final Map<Character, String> JSON_STRING_REPLACEMENTS;
	static {
		Map<Character, String> m = new HashMap<Character, String>();
		m.put('"', "\\\"");
		m.put('\\', "\\\\");
		m.put('\b', "\\b");
		m.put('\f', "\\f");
		m.put('\n', "\\n");
		m.put('\r', "\\r");
		m.put('\t', "\\t");
		JSON_STRING_REPLACEMENTS = Collections.unmodifiableMap(m);
	}

	private static String s2h(String x) {
		return x.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	private static String sl2h(List<?> x, String classValue) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for ( Object s : x ) {
			if ( first ) {
                first = false;
            } else {
                sb.append(", ");
            }
			sb.append("<span class=\"").append(classValue).append("\">").append(s2h(s.toString())).append("</span>");
		}

		return sb.toString();
	}

	private static String date2s(long millis) {
		DateTime dt = new DateTime(millis, DateTimeZone.UTC);
		DateTimeFormatter formatter = (dt.getSecondOfDay() == 0) ? ISODateTimeFormat.date() : ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(millis);
	}

	private static String tz2s(Long tz) {
		int rawOffset = (int)(tz / 60000);
		return String.format("%+04d", rawOffset);
	}

	private static String s2a(String x) {
		return s2h(x).replaceAll("\"", "&quot;");
	}

	private static String s2j(String x) {
		if ( x == null ) {
            return "null";
        }
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for ( int i = 0 ; i < x.length() ; i++ ) {
			char c = x.charAt(i);
			String replacement = JSON_STRING_REPLACEMENTS.get(c);
			if ( replacement != null ) {
                sb.append(replacement);
            } else if ( c >= 32 ) {
                sb.append(c);
            } else {
                sb.append(String.format("\\u%04x", (int)c));
            }
		}
		sb.append('"');

		return sb.toString();
	}

	private static String sl2j(List<?> x) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		boolean first = true;
		for ( Object s : x ) {
			if ( first ) {
                first = false;
            } else {
                sb.append(',');
            }
			sb.append(s2j(s.toString()));
		}
		sb.append(']');
		return sb.toString();
	}

	private static String printDate(long millis) {
		return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).format(new Date(millis));
	}

	private static String printLineList(List<?> list) {
		if ( list.size() == 1 ) {
            return list.get(0).toString();
        }

		StringBuilder sb = new StringBuilder();
		for ( Object o : list ) {
            sb.append("\n    ").append(o);
        }
		return sb.toString();
	}

	private static String printBlockList(List<?> list) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for ( Object o : list ) {
			if ( first ) {
                first = false;
            } else {
                sb.append(" ---- \n");
            }
			String item = o.toString();
			if ( item.endsWith("\n") ) {
                item = item.substring(0, item.length() -1);
            }
			sb.append("    ");
			sb.append(item.replaceAll("\n", "\n    "));
			sb.append('\n');
		}

		return sb.toString();
	}

	private static String printCommaList(List<?> list) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for ( Object o : list ) {
			if ( first ) {
                first = false;
            } else {
                sb.append(", ");
            }
			sb.append(o);
		}

		return sb.toString();
	}

	public @Override int hashCode() {
		final int prime = 97;
		int result = 1;
		result = prime * result + adrs.hashCode();
		result = prime * result + ((bday == null) ? 0 : bday.hashCode());
		result = prime * result + categories.hashCode();
		result = prime * result + emails.hashCode();
		result = prime * result + ((fn == null) ? 0 : fn.hashCode());
		result = prime * result + ((geo == null) ? 0 : geo.hashCode());
		result = prime * result + keys.hashCode();
		result = prime * result + labels.hashCode();
		result = prime * result + logos.hashCode();
		result = prime * result + mailers.hashCode();
		result = prime * result + n.hashCode();
		result = prime * result + nicknames.hashCode();
		result = prime * result + notes.hashCode();
		result = prime * result + orgs.hashCode();
		result = prime * result + photos.hashCode();
		result = prime * result + ((rev == null) ? 0 : rev.hashCode());
		result = prime * result + roles.hashCode();
		result = prime * result + tels.hashCode();
		result = prime * result + titles.hashCode();
		result = prime * result + xfnUrls.hashCode();
		return result;
	}

	public @Override boolean equals(Object obj) {
		if ( this == obj ) {
            return true;
        }
		if ( !(obj instanceof HCard) ) {
            return false;
        }
		final HCard other = (HCard)obj;

		if ( !fn.equals(other.fn) ) {
            return false;
        }
		if ( !adrs.equals(other.adrs) ) {
            return false;
        }
		if ( !categories.equals(other.categories) ) {
            return false;
        }
		if ( !emails.equals(other.emails) ) {
            return false;
        }
		if ( !keys.equals(other.keys) ) {
            return false;
        }
		if ( !labels.equals(other.labels) ) {
            return false;
        }
		if ( !logos.equals(other.logos) ) {
            return false;
        }
		if ( !mailers.equals(other.mailers) ) {
            return false;
        }
		if ( !nicknames.equals(other.nicknames) ) {
            return false;
        }
		if ( !notes.equals(other.notes) ) {
            return false;
        }
		if ( !orgs.equals(other.orgs) ) {
            return false;
        }
		if ( !photos.equals(other.photos) ) {
            return false;
        }
		if ( !roles.equals(other.roles) ) {
            return false;
        }
		if ( !tels.equals(other.tels) ) {
            return false;
        }
		if ( !titles.equals(other.titles) ) {
            return false;
        }
		if ( !xfnUrls.equals(other.xfnUrls) ) {
            return false;
        }

		if ( bday == null ) {
			if ( other.bday != null ) {
                return false;
            }
		} else if ( !bday.equals(other.bday) ) {
            return false;
        }

		if ( geo == null ) {
			if ( other.geo != null ) {
                return false;
            }
		} else if ( !geo.equals(other.geo) ) {
            return false;
        }

		if ( !n.equals(other.n) ) {
            return false;
        }

		if ( rev == null ) {
			if ( other.rev != null ) {
                return false;
            }
		} else if ( !rev.equals(other.rev) ) {
            return false;
        }

		return true;
	}

	/**
	 * Checks if the relationship attributes of the various URLs in this hCard match up, and returns them.
	 * URLs without any relationship info are ignored.
	 *
	 * @throws IllegalStateException If the urls have conflicting relationship attributes.
	 */
	public List<XFNRelationship> getXFNRelationships() {
		if ( xfnUrls.isEmpty() ) {
            return Collections.emptyList();
        }
		List<XFNRelationship> rels = new ArrayList<XFNRelationship>();
		for ( XFNURL xfn : xfnUrls ) {
			if ( xfn.rels.isEmpty() ) {
                continue;
            }
			if ( rels.isEmpty() ) {
                rels.addAll(xfn.rels);
            } else if ( !XFNURL.equalRels(rels, xfn.rels) ) {
                throw new IllegalStateException("Conflicting XFN relations - check each url individually.");
            }
		}
		return Collections.unmodifiableList(rels);
	}

	public String getDefaultTelValue() {
		if ( tels.isEmpty() ) {
            return null;
        }

		String value = getTelValue(Tel.VOICE);
		if ( value == null ) {
            value = tels.get(0).value;
        }

		return value;
	}

	public String getTelValue(String type) {
		if ( type == null ) {
            throw new NullPointerException();
        }

		type = type.toLowerCase();
		for ( Tel tel : tels ) {
            if ( tel.types.contains(type) ) {
                return tel.value;
            }
        }
		return null;
	}

	public Address getDefaultAddress() {
		if ( adrs.isEmpty() ) {
            return null;
        }

		Address address = getAddress(Address.POSTAL);
		if ( address == null ) {
            address = adrs.get(0);
        }

		return address;
	}

	public Address getAddress(String type) {
		if ( type == null ) {
            throw new NullPointerException();
        }

		type = type.toLowerCase();
		for ( Address adr : adrs ) {
            if ( adr.types.contains(type) ) {
                return adr;
            }
        }
		return null;
	}

	public String getDefaultEmailValue() {
		if ( emails.isEmpty() ) {
            return null;
        }

		String email = getEmailValue(Email.INTERNET);
		if ( email == null ) {
            email = emails.get(0).value;
        }

		return email;
	}

	public String getEmailValue(String type) {
		if ( type == null ) {
            throw new NullPointerException();
        }

		type = type.toLowerCase();
		for ( Email email : emails ) {
            if ( email.types.contains(type) ) {
                return email.value;
            }
        }
		return null;
	}
}
