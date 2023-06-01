## Nacos E2E
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Nacos E2E Test
### Test Case Coverage
* Java
    * Naming 
      * registerInstance
      * deregisterInstance
      * getAllInstances
      * selectInstances
      * selectOneHealthyInstance
      * subscribe
      * unsubscribe
      * getServicesOfServer
    * Config
      * publishConfig
      * getConfig
      * removeConfig
      * addListener
      * removeListener
* Golang
    * Naming
      * RegisterInstance
      * DeregisterInstance
      * GetAllServicesInfo
    * Config
      * PublishConfig
      * GetConfig
      * SearchConfig
      * DeleteConfig
      * ListenConfig
      * CancelListenConfig
* Python
    * Naming
      * add_naming_instance
      * remove_naming_instance
      * list_naming_instance
      * subscribe
      * unsubscribe
    * Config
      * publish_config
      * get_config
      * remove_config
      * add_config_watcher
* Cpp
    * Naming
      * registerInstance
      * getAllInstances
      * deregisterInstance
      * subscribe
    * Config
      * publishConfig
      * getConfig
      * addListener
* Csharp
    * Naming
      * RegisterInstance
      * GetAllInstances
      * Subscribe
    * Config
      * PublishConfig
      * GetConfig
      * RemoveConfig
      * AddListener


#### How to start use Java 
```angular2html
mvn clean test -B -DserverList=127.0.0.1 -Dnacos.client.version=2.2.1
```
##### Options
* `serverList` : required, default is 127.0.0.1, it will get from environment variables
* `nacos.client.version`: not required, default is find in pom.xml

