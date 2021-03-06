# MDE4CPPGradlePlugins
This repository is part of the MDE4CPP framework. Further information can be found on [framework website](https://sse.tu-ilmenau.de/mde4cpp) and [framework root repository](https://github.com/MDE4CPP/MDE4CPP)
This repository provides [Gradle](https://gradle.org/) plugins, which are used to build projects with the MDE4CPP framework.
All plugins are published on [https://plugins.gradle.org/](https://plugins.gradle.org/) with tag 'MDE4CPP'.

## MDE4CPPCompile-Plugin
This Gradle plugin provides a task for compiling C++ projects using [CMake](https://cmake.org/) and GNU Compiler ([MinGW]() is used on Windows systems). It can be used on Unix and Windows systems.

### General structure of build.gradle to apply this plugin

```gradle
plugins {
  id "tui.sse.mde4cpp.mde4cpp-compile-plugin" version "0.4"
}

task compileProject(type: tui.sse.mde4cpp.MDE4CPPCompile) {
	projectFolder = file("path/to/project/folder")
}
```
This code fragment presents the minimal content of a *build.gradle* file to use this plugin.
The *plugin* statement must be at the top. It defines, that the plugin can be found in the plugin repository of gradle. Properties *id* and *version* specify the actual plugin and the versin to use. At the moment, a fixed coded version number must be set. This restriction is defined by gradle and will be solved in coming versions.
The plugin will be applied automatically. Thus, it is not necessary to do this here.

Now, an task can be defined, which extend the task MDE4CPPCompile. Only the project folder needs to be configured.
**Attention:** The project folder must contain a *CMakeLists.txt* file, which specify the build instruction.

That's all that needs to be done. The task can be executed by calling *gradle compileProject* now.
The defined project will be compiled with all build modes. The following section describes modification possibilities using properties.


### Properties, which can be used to configure a compile task during compilation
Properties can be used to modify tasks during calling. A property has to specified like -P{property name} or -P{property name}={value}. Alternatively, properties can be speficied in file ${user_home}/.gradle/properties.gradle on windows.


#### Count of parallel jobs:
The property *workerCount* can be used to configure the count of compile jobs, which can be executed in parallel.

Syntax: workerCount={1..n}

*workerCount* has no upper limit, but it is advisable to do not exceed the core limit.
If this property is not set, the system default is used. In general, parallism is not used.

#### Configure build modes
Build modes Debug and Release are provided by the MDE4CPP framework.
The following properties can be used to specify, which build mode is to use:
* **compile with debug options** - *D* or *DEBUG*
* **compile with release options** - *R* or *RELEASE*

Options:
 * If *D* or *DEBUG* is defined, Debug mode is performed.
 * If *R* or *RELEASE* are defined, Release mode is performed.
 * If none of these properties is set, both build modes are performed.
 * DEBUG and RELEASE can be used in the same build. Both modes are executed one after the other.
 * Properties with assigned value *0* are ignored.
 
#### Configure builds to be compile (only projects generated with fUML4CPP)
The generator fUML4CPP creates two projects - one for the structural part and one for the execution. Path to structure project is be set automatically in a Gradle task generated by fUML4CPP. The path to execution project will be calculated based on folder structure and name convention. The execution project is placed in same parent folder and named identically to structure project extended by 'Exec'.

Same as the build mode, properties are used to enable or disable the compilation of structure or execution project.
* **structure project** - *S* or *STRUCTURE*
* **execution project** - *E* or *EXECUTION*

Options:
 * If *S* or *STRUCTURE* is defined, structure project will be compiled.
 * If *E* or *EXECUTION* are defined, structure execution will be compiled.
 * If none of these properties is set, both build modes are performed.
 * Execution and structure project can be used in the same build. Both modes are executed one after the other.
 * Properties with assigned value *0* are ignored.

## MDE4CPPGenerate-Plugin
This Gradle plugin provides a task for generate C++ projects for Ecore and UML models using [MDE4CPP generators](https://sse.tu-ilmenau.de/mde4cpp).


#### Simple configuration of MDE4CPPGenerate task inside build.gradle

```gradle
plugins {
  id "tui.sse.mde4cpp.mde4cpp-generate-plugin" version "0.1"
}

task generateProject(type: tui.sse.mde4cpp.MDE4CPPGenerate) {
	modelFilePath = file("path/to/model file") // essentially
    structureOnly = true // only necessary if UML4CPP should be used
}
```

This is the simple configuration of the MDE4CPPGenerate task.
**Essential properties**
 * *modelFilePath* - specifies the path of the model file
 * *structureOnly* - indicated, that the generator UML4CPP should be used

**Automatically defined properties**
 * Used generator and its path:
 	* ecore4CPP for .ecore models
 	* fUML4CPP for .uml models, if property *structureOnly* is undefined or *0*
 	* UML4CPP for .uml models, if property *structureOnly* is defined and not *0*
 * Generator path:
 	* Environment variable **MDE4CPP_HOME** has to be set to calculate generator path.
 	* Generators have to be located at: `$MDE4CPP_HOME/application/generator`
 	* The project folder structure should be defined as follows:
 		* project root folder
 			* folder `model` including model file
 			* folder `src_gen` including the generated C++ code
 * Target folder:
 	* The code will be generated into folder {model folder}/../src_gen




#### Extended configuration of MDE4CPPGenerate task inside build.gradle
```gradle 
plugins {
  id "tui.sse.mde4cpp.mde4cpp-generate-plugin" version "0.1"
}

task generateProject(type: tui.sse.mde4cpp.MDE4CPPGenerate) {
	modelFilePath = file("path/to/model file") // essentially
    structureOnly = true // only necessary if UML4CPP should be used 
    generatorPath = file("path/to/model file") // specify the path of generator which should be used
    targetFolder = "path/to/source gen folder" // target folder for generated source code
}
```

#### Parameters of MDE4CPPGenerate task

```gradle
gradle taskName -PModel=*path_to_model_file* // for ecore4CPP and fUML4CPP
gradle taskName --model=*path_to_model_file* // alternative for ecore4CPP and fUML4CPP
gradle taskName -PModel=*path_to_model_file* -PStructureOnly // for UML4CPP
gradle taskName --model=*path_to_model_file* --structureOnly // alternative for UML4CPP

```
Parameter *Model* will only be considered, if property *modelFilePath* is not specified in *taskName*.

#### Experimental mode ####
To use the experimental mode, property *experimentalMode* with value *true* has to be defined. It is recommended to do this inside the file *gradle.properties*.

Experimental mode enabled the following feature(s):
 * add related models to up-to-date check
 	* configure *relatedModels* inside a MDE4CPPGenerate task using the model name (without extension)
 	* Only models will be considered, which can be found inside current folder or subfolders. 
 	* *.uml* and *.ecore* are supported

```gradle 
plugins {
  id "tui.sse.mde4cpp.mde4cpp-generate-plugin" version "0.1"
}

task generateProject(type: tui.sse.mde4cpp.MDE4CPPGenerate) {
	...
    relatedModels = ['modelName1', 'modelName2']
}
```