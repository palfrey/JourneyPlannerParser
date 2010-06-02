SOURCE_FILES=$(wildcard src/net/tevp/JourneyPlannerParser/*.java)

# This should be the Android SDK root directory
ANDROID ?= ../../../android-sdk-linux_86

# SDK version
ANDROID_VERSION=1.5

.PHONY: first
first: bin-stamp
	java -ea -cp bin net.tevp.JourneyPlannerParser.JourneyPlannerParser

doc::
	javadoc -d doc $(JAVAFILES)

include Makefile.common
