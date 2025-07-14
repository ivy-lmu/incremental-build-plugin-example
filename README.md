~~This repo is used to reproduce an incremental m2e build problem in VS Code using the Extension Pack for Java.
Reported with https://github.com/redhat-developer/vscode-java/issues/3632~~ fixed with https://github.com/ivy-lmu/incremental-build-plugin-example/pull/1

# Goal
With the help of our maven plugin (see `/build-plugin`) we want to incrementally convert `*.ivyClass` files into `*.java` files. The resulting java files will be written into `src_dataClasses` folder. So if an `*.ivyClass` file is created or changed, the plugin is expected to write/update the corresponding `*.java` file.

# Setup
## build-plugin
`/build-plugin` is the maven plugin.

- `src/../BuildDataClassMojo.java` MOJO that is used to transform `*.ivyClass` files to `*.java` files.
- `src/../components.xml` defines the custom `iar` maven packaing type, that is used in `/sample/pom.xml`.

## sample
`/sample` project where the maven plugin is used.

- `dataclasses/NotSrcDirExample.ivyClass` file that should be transformed by the MOJO located in a folder that is not a src folder but a resource folder.

# Reproduce
You can run this project using codespaces.

- install the build-plugin (not needed if devcontainer/codespaces is used) `mvn -f build-plugin install`
- wait until "Java: Ready" status message appears in VS Code
- press "Realod All Maven Projects" button under maven view
- edit `dataclasses/NotSrcDirExample.ivyClass`, verify that `src_dataClasses/NotSrcDirExample.java`gets immediately created
- verify that `target/classes/NotSrcDirExample.class` gets created
- in case the Java Projects are corrup, the command `Java: Clean Java Language Server Workspace` can help