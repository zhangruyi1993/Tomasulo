
# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default:class

# this target entry builds the Average class
# the Average.class file is dependent on the Average.java file
# and the rule associated with this entry gives the command to create it
#
class:
	javac -d . -classpath . *.java
# To start over from scratch, type 'make clean'.  
# Removes all .class files, so that the next make rebuilds them
#
clean:
	$(RM) *.class

