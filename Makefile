JAVAFILES=$(wildcard src/net/tevp/JourneyPlannerParser/*.java)
CLASSFILES=$(patsubst %.java,%.class,$(JAVAFILES))

all: $(CLASSFILES)
	java -ea -cp src net.tevp.JourneyPlannerParser.JourneyPlannerParser

$(CLASSFILES): $(JAVAFILES)
	javac -d src $(JAVAFILES) -Xlint:unchecked

doc::
	javadoc -d doc $(JAVAFILES)
