all: JourneyPlannerParser.class
	java -ea JourneyPlannerParser

JourneyPlannerParser.class: JourneyPlannerParser.java
	javac $^ -Xlint:unchecked
