package owl.groovy.utilities

// We point this to the local maven repository, to which we are publishing the library we're building with gradle.
@GrabResolver(name='localMaven', root='/home/fedderico/.m2/repository/')

@Grab(group='owl-utilities', module='owl-groovy-example', version='0.0.1-SNAPSHOT')

import owl.groovy.utilities.OtherScriptFile

// Script code:

OtherScriptFile.execute()