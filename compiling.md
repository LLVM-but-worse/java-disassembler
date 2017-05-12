# Compiling

## Manual dependencies
 - [CFR](http://www.benf.org/other/cfr/)

You will need to acquire jars of these to proceed. Then, you must add these to a mvn local repository.
So:
```
mkdir libs
mvn install:install-file -Dfile=path-to\cfr.jar -DgroupId=org.benf -DartifactId=cfr -Dversion=<version> -Dpackaging=jar
```
For example, the version of CFR might be 0.121, depending on `pom.xml`. Note that you will need to replace <version> with whatever version is specified in `pom.xml`.
Optionally, you can use `-DlocalRepositoryPath=path-to-specific-local-repo` to specify a specific location to store the local repository.

Then, `mvn compile package`.
