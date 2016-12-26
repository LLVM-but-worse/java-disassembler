# Compiling

## Manual dependencies
 - [byte-engineer](https://bitbucket.org/0xJAVA/byte-engineer)
 - [Procyon](https://bitbucket.org/mstrobel/procyon)
 - [CFR](http://www.benf.org/other/cfr/)
 - [Fernflower](https://github.com/fesh0r/fernflower.git)

You will need to acquire jars of these to proceed. Then, you must add these to a mvn local repository.
So:
```
mkdir libs
mvn install:install-file -Dfile=path-to\fernflower.jar -DgroupId=org.jetbrains.java.decompiler -DartifactId=fernflower -Dversion=<version> -Dpackaging=jar
mvn install:install-file -Dfile=path-to\cfr.jar -DgroupId=org.benf -DartifactId=cfr -Dversion=<version> -Dpackaging=jar
mvn install:install-file -Dfile=path-to\byteanalysis-1.0.jar -DgroupId=eu.bibl -DartifactId=byteanalysis -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=path-to\procyon.jar -DgroupId=com.strobel.decompiler -DartifactId=procyon -Dversion=<version> -Dpackaging=jar
```
Note that you will need to replace <version> with whatever version is specified in `pom.xml`.
Optionally, you can use `-DlocalRepositoryPath=path-to-specific-local-repo` to specify a specific location to store the local repository.

Then, `mvn compile package`.