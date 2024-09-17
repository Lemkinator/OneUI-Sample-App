rootProject.name = "OneUI-Sample-App"
include(":app")

//To include the design `lib' to your project as a local module,
//you may add the following lines to your project's settings.gradle
//with the Path to the `lib` folder of your local clone of the design-lib
include(":lib")
project(":lib").projectDir = File("../oneui-design-sample-app/lib")