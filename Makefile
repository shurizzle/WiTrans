ADB	=	$(shell which adb)
ANT = $(shell which ant)

build:
	$(ANT) debug

all: build

install: build
	$(ANT) installd

run: install
	$(ADB) shell 'am start -n shurizzle.witrans/.MainActivity'

log:
	$(ADB) logcat ActivityManager:I WiTrans:\* \*:S

clean:
	$(ANT) clean

.PHONY: all build install run log clean
