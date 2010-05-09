all: src/net/tevp/JourneyPlannerParser/*.class
	java -ea -cp src net.tevp.JourneyPlannerParser.JourneyPlannerParser

src/net/tevp/JourneyPlannerParser/*.class: src/net/tevp/JourneyPlannerParser/*.java
	javac -d src src/net/tevp/JourneyPlannerParser/*.java -Xlint:unchecked

doc::
	javadoc *.java
