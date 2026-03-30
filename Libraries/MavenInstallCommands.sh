# This folder is retained as an archival fallback for legacy jars that were once
# installed manually. The current Maven build no longer depends on these files as
# part of the normal workflow.
#
# Current state:
# - TridasJLib is resolved from GitHub Packages
# - DendroFileIO is published to GitHub Packages
# - The active POM uses com.jhlabs:javaproj:1.0, not jmapprojlib
# - charset is commented out in pom.xml
# - jama is not declared in the current pom.xml
#
# Keep these jars only for historical reference or manual recovery.
#
# If you ever need to reinstall them into a local Maven repository manually, the
# legacy commands are below:

mvn install:install-file -DgroupId=com.jhlabs    -DartifactId=jmapprojlib   -Dversion=1.2.0    -Dpackaging=jar -Dfile=jmapprojlib-1.2.0-SNAPSHOT.jar
mvn install:install-file -DgroupId=gov.nist.math -DartifactId=jama          -Dversion=1.0.2    -Dpackaging=jar -Dfile=jama-1.0.2.jar
mvn install:install-file -DgroupId=charset       -DartifactId=charset       -Dversion=1.2.1    -Dpackaging=jar -Dfile=charset-1.2.1.jar
