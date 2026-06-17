.PHONY: all clean build install uninstall lint test

APP_ID ?= com.chimera.pro.zpqmxr

all: clean build

clean:
	./gradlew clean

build:
	./gradlew assembleDebug

release:
	./gradlew assembleRelease

install:
	adb install -r app/build/outputs/apk/debug/app-debug.apk

uninstall:
	adb uninstall $(APP_ID)

lint:
	./gradlew lintDebug

test:
	./gradlew testDebugUnitTest
