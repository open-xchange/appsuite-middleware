Setting up a kerberized OX prototype
====================================

- setup /etc/krb5.conf so that kinit works
- copy jaas.conf to /opt/open-xchange/etc/jaas.conf
- add the new parameters to /opt/open-xchange/etc/login.conf
  (see kerberos-server.patch)
- install and user open-xchange-authentication-spnego instead of any other
  authbundle
- whatever Kerberos REALM is used, this must be added as a loginmapping
  to the oxcontext that should be used. Case doesn't matter, e.g.:

choeger@oxigian6:~$ grep default_realm /etc/krb5.conf 
        default_realm = OES11QA.LOCAL
choeger@oxigian6:~$ /opt/open-xchange/sbin/listcontext -A oxadminmaster -P secret
cid fid fname         enabled qmax qused name        lmappings                
110   2 110_ctx_store true     500     0 110                                  
111   2 111_ctx_store true     500     0 example.com oes11qa.local,example.com
112   2 112_ctx_store true     500     0 112                                  

- set --imapuser to the kerberos user (without the realm)
- set --imapserver to the correct hostname of the kerberos imapserver

- IP of OX must be in DNS of krb server and ox DNS name must be added as
  HTTP/ox.example.com@KRBREALM
