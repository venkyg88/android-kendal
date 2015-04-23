# Makefile to generate graphics

INKSCAPE='/Applications/Inkscape.app/Contents/Resources/bin/inkscape'
TEMPFILE='../../temp.svg'
ICON='../../app/src/main/res/drawable-hdpi/ic_app.png'

all: $(ICON) splash

$(ICON): icons.txt
	./convert

splash:
	sed -e "s/<path /<path fill=\"#cc0000\" /g" -e "s/<circle /<circle fill=\"#cc0000\" /g" ../svg/staples/splash.svg > ${TEMPFILE}
	$(INKSCAPE) -f $(TEMPFILE) -w 1024 -e ../../app/src/main/res/drawable/splash.png
