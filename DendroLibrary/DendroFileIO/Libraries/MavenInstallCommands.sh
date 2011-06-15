# The Jars within this folder are those not found in standard Maven repositories.  They have been
# placed in the maven.tridas.org repository but are included here in case this repository becomes
# unavailable.  The jars can be installed with the following commands:

mvn install:install-file -DgroupId=com.jhlabs    -DartifactId=jmapprojlib   -Dversion=1.2.0    -Dpackaging=jar -Dfile=jmapprojlib-1.2.0-SNAPSHOT.jar
mvn install:install-file -DgroupId=gov.nist.math -DartifactId=jama          -Dversion=1.0.2    -Dpackaging=jar -Dfile=jama-1.0.2.jar
