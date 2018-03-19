---
title: Postfix and Dovecot
---

This article contains exemplary configuration for Dovecot and Postfix. Dovecot will provide the SASL mechanisms [OAUTHBEARER](https://tools.ietf.org/html/rfc7628) and [XOAUTH2](https://developers.google.com/gmail/xoauth2_protocol) for IMAP and ManageSieve. It will also provide an Unix socket that is used by Postfix for SMTP authentication via SASL.

# Mail Backend Configuration

## Dovecot

Dovecot is required in version 2.2.28 or higher. The official configuration guide for the SASL mechanisms can be found [here](http://wiki2.dovecot.org/PasswordDatabase/oauth2). The actual authentication happens via *OAuth 2.0 Token Introspection* as described in [RFC 7662](https://tools.ietf.org/html/rfc7662).

*Note:* The configuration of ManageSieve via Dovecot Pigeonhole is out of scope for this guide. Pigeonhole offers all configured SASL mechanisms out of the box after it has been enabled.

*/etc/dovecot/dovecot.conf:*

	...
	
    auth_mechanisms = plain xoauth2 oauthbearer

    passdb {
      driver = oauth2
      mechanisms = xoauth2 oauthbearer
      args = /etc/dovecot/dovecot-oauth2.conf.ext
    }

    userdb {
      driver = static
      args = uid=vmail gid=vmail home=/var/vmail/%u
    }

    # authentication debug logging
    auth_debug = yes
    auth_verbose = yes

    # provide SASL via unix socket to postfix
    service auth {
      unix_listener /var/spool/postfix/private/auth {
        mode = 0660
        # Assuming the default Postfix user and group
        user = postfix
        group = postfix
      }
    }
    
    ...

*/etc/dovecot/dovecot-oauth2.conf.ext:*

    introspection_mode = post
    introspection_url = https://<usr>:<pw>@idp.example.com/oauth2/introspect
    username_attribute = username
    tls_ca_cert_file = /etc/ssl/certs/ca-certificates.crt
    active_attribute = active
    active_value = true

## Postfix

Configure Postfix to require SMTP authentication via SASL. A guide on using Dovecot SASL by Postfix can be found [here](http://wiki2.dovecot.org/HowTo/PostfixAndDovecotSASL).

*main.cf:*

    smtpd_relay_restrictions = permit_sasl_authenticated, reject
    smtpd_sasl_type = dovecot
    smtpd_sasl_path = private/auth
    smtpd_sasl_auth_enable = yes
    
# Examples

If everything was configured correctly you should be able to login to IMAP, SMTP and ManageSieve with both SASL mechanisms. Both have different ways of encoding the SASL initial client response. For a user `user@example.com` and access token `1234567890` you can encode it like so:

*OAUTHBEARER:*

	$ echo -en 'n,a=user@example.com,\001host=localhost\001port=143\001auth=Bearer 1234567890\001\001' | base64 -w0; echo
	bixhPXVzZXJAZXhhbXBsZS5jb20sAWhvc3Q9bG9jYWxob3N0AXBvcnQ9MTQzAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==
	
*XOAUTH2:*

	$ echo -en 'user=user@example.com\001auth=Bearer 1234567890\001\001' | base64 -w0; echo
	dXNlcj11c2VyQGV4YW1wbGUuY29tAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==

All below examples can be verified via telnet.

## IMAP

*OAUTHBEARER:*

	S: * OK [CAPABILITY SASL-IR AUTH=PLAIN AUTH=XOAUTH2 AUTH=OAUTHBEARER ...] Dovecot ready.
	C: 01 AUTHENTICATE OAUTHBEARER bixhPXVzZXJAZXhhbXBsZS5jb20sAWhvc3Q9bG9jYWxob3N0AXBvcnQ9MTQzAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==
	S: 01 OK [CAPABILITY IMAP4rev1 ...] Logged in

*XOAUTH2:*

	S: * OK [CAPABILITY SASL-IRAUTH=PLAIN AUTH=XOAUTH2 AUTH=OAUTHBEARER ...] Dovecot ready.
	C: 01 AUTHENTICATE XOAUTH2 dXNlcj11c2VyQGV4YW1wbGUuY29tAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==
	S: 01 OK [CAPABILITY IMAP4rev1 ...] Logged in

## ManageSieve

*OAUTHBEARER:*

	S: "IMPLEMENTATION" "Dovecot Pigeonhole"
	S: "SIEVE" "fileinto reject envelope encoded-character vacation subaddress comparator-i;ascii-numeric relational regex imap4flags copy include variables body enotify environment mailbox date index ihave duplicate mime foreverypart extracttext"
	S: "NOTIFY" "mailto"
	S: "SASL" "PLAIN XOAUTH2 OAUTHBEARER"
	S: "VERSION" "1.0"
	S: OK "Dovecot ready."
	C: AUTHENTICATE "OAUTHBEARER" "bixhPXVzZXJAZXhhbXBsZS5jb20sAWhvc3Q9bG9jYWxob3N0AXBvcnQ9MTQzAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ=="
	S: OK "Logged in."
	
*XOAUTH2:*

	S: "IMPLEMENTATION" "Dovecot Pigeonhole"
	S: "SIEVE" "fileinto reject envelope encoded-character vacation subaddress comparator-i;ascii-numeric relational regex imap4flags copy include variables body enotify environment mailbox date index ihave duplicate mime foreverypart extracttext"
	S: "NOTIFY" "mailto"
	S: "SASL" "PLAIN XOAUTH2 OAUTHBEARER"
	S: "VERSION" "1.0"
	S: OK "Dovecot ready."
	C: AUTHENTICATE "XOAUTH2" "dXNlcj11c2VyQGV4YW1wbGUuY29tAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ=="
	S: OK "Logged in."
	
## SMTP

*OAUTHBEARER:*

	S: 220 debian-jessie.vagrantup.com ESMTP Postfix (Debian/GNU)
	C: EHLO localhost
	S: 250-debian-jessie.vagrantup.com
	S: 250-AUTH PLAIN XOAUTH2 OAUTHBEARER
	C: AUTH OAUTHBEARER bixhPXVzZXJAZXhhbXBsZS5jb20sAWhvc3Q9bG9jYWxob3N0AXBvcnQ9MTQzAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==
	S: 235 2.7.0 Authentication successful

*XOAUTH2:*

	S: 220 debian-jessie.vagrantup.com ESMTP Postfix (Debian/GNU)
	C: EHLO localhost
	S: 250-debian-jessie.vagrantup.com
	S: 250-AUTH PLAIN XOAUTH2 OAUTHBEARER
	C: AUTH XOAUTH2 dXNlcj11c2VyQGV4YW1wbGUuY29tAWF1dGg9QmVhcmVyIDEyMzQ1Njc4OTABAQ==
	S: 235 2.7.0 Authentication successful