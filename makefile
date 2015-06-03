JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Map.java \
	State.java \
	Search.java \
	Agent.java \
	Bounty.java \
	Move.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class