# Compiling - JDA

## Subrepositories

You will need to clone the subrepositories in the `libs` folder.
```
git submodule init
git submodule clone
```

## maple-ir

This project now depends on [Maple-IR](https://github.com/LLVM-but-worse/maple-ir). To install it:
```
git clone https://github.com/LLVM-but-worse/maple-ir
cd maple-ir
mvn clean compile test install
```

## Manual dependencies
 - [CFR](http://www.benf.org/other/cfr/)

You will need to acquire jars of these to proceed. Then, you must add these to a mvn local repository.
So (in the project root directory):
```
mvn install:install-file -Dfile=path-to\cfr.jar -DgroupId=org.benf -DartifactId=cfr -Dversion=<version> -Dpackaging=jar -DlocalRepositoryPath=libs
```
For example, the version of CFR might be 0.121, depending on `pom.xml`. Note that you will need to replace <version> with whatever version is specified in `pom.xml`.
Optionally, you can use `-DlocalRepositoryPath=path-to-specific-local-repo` to specify a specific location to store the local repository.

Then, `mvn clean compile test package`.

Two jars are produced: one with dependencies, which should be used for running JDA standalone, and one without, used by plugins for linking against JDA.

# MapleIR plugin

To compile the MapleIR plugin, JDA must be installed to the local Maven repository first. Hence, in the root project (JDA) directory:
```
mvn install
```

Next, build MapleIR:
```
cd mapleir
mvn clean compile test package
```

