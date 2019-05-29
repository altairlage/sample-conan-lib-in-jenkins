# Build-Library

## Description
This is a group of classes and objects that allow us to reuse code inside of our Jenkins files. There are two options setup in Jenkins that allow us to use the master branch and the dev branch. Please make all code changes in here as modular as possible.

## Usage
Inside the Jenkins file include one of the following options:

### For the Dev Version

```groovy
@Library('Build-Library-Dev')
```

### For the Master version

```groovy
@Library('Build-Library')
```

### Import the Classes
```groovy
import org.altabuild.conan.ConanBuilder
...
```

### Initialize the objects
```groovy
def conanBuilder = new ConanBuilder(this, os) // org.altabuild.conan.ConanBuilder
```