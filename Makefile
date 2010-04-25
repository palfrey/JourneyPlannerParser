all: JourneyPlannerParser.class
	java JourneyPlannerParser jp-complete.html

JourneyPlannerParser.class: JourneyPlannerParser.java
	javac $^

