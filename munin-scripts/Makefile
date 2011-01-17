#
VERSION		= 0.1
ARCHNAME	= open-xchange-munin-scripts
DISTARCH	= ../$(ARCHNAME)-$(VERSION)
DESTDIR		= /

dist:
	mkdir -p $(DISTARCH)
	cp -a * $(DISTARCH)
	find $(DISTARCH) -name .svn | xargs rm -rf
	tar zcvpf ../$(ARCHNAME)-$(VERSION).tar.gz $(DISTARCH)
	rm -rf $(DISTARCH)

clean:

