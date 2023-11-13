#include <gtest/gtest.h>
#include <iostream>
#include "Nacos.h"
#include <unistd.h>
#include <sys/timeb.h>
#include <stdlib.h>
#include <string>
using namespace testing;
using namespace std;
using namespace nacos;

std::string extractNacosIPs(std::string& inputString) {
    std::vector<std::string> nacosIPs;
    std::string delimiter = ","; // split by ,
    size_t pos = 0;
    std::string token;

    while ((pos = inputString.find(delimiter)) != std::string::npos) {
        token = inputString.substr(0, pos);
        inputString.erase(0, pos + delimiter.length());

        // check start with "nacos-"
        if (token.find("nacos-") == 0) {
            size_t colonPos = token.find(":");
            if (colonPos != std::string::npos) {
                std::string ip = token.substr(colonPos + 1);
                nacosIPs.push_back(ip);
            }
        }
    }

    if (inputString.find("nacos-") == 0) {
        size_t colonPos = inputString.find(":");
        if (colonPos != std::string::npos) {
            std::string ip = inputString.substr(colonPos + 1);
            nacosIPs.push_back(ip);
        }
    }

    std::string nacos_server_address;
    for (const auto& ip : nacosIPs) {
        nacos_server_address = ip + ":8848";
        break;
    }
//    if (!nacos_server_address.empty()) {
//        nacos_server_address.pop_back();
//    }
    return nacos_server_address;
}

TEST(ConfigTestSuit,CASE1_publishAndGetConfig) {
    cout << "====Begin to test====" << endl;
    char *ALL_IP;
    ALL_IP = getenv("ALL_IP");
    string inputString = ALL_IP;
    std::cout << "ALL_IP is:" << inputString << std::endl;
    std::string nacos_server_address = extractNacosIPs(inputString);
    std::cout << "SERVER_ADDR is:" << std::endl;

    Properties props;
    props[PropertyKeyConst::SERVER_ADDR] = nacos_server_address ;//server address
    INacosServiceFactory *factory = NacosFactoryFactory::getNacosFactory(props);
    ResourceGuard <INacosServiceFactory> _guardFactory(factory);
    ConfigService *n = factory->CreateConfigService();
    ResourceGuard <ConfigService> _serviceFactory(n);
    timeb t;
    ftime(&t);
    NacosString dataId = "nacos.test." + to_string(t.time * 1000 + t.millitm);
    NacosString group = NULLSTR;
    NacosString expectContent = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」";
    NacosString actualContent = "";

    cout << "publishConfig Key:" << dataId << " with value:"<< expectContent << endl;

    bool bSucc = false;
    try {
        bSucc = n->publishConfig(dataId, group, expectContent);
    }
    catch (NacosException &e) {
        cout <<
             "Request failed with curl code:" << e.errorcode() << endl <<
             "Reason:" << e.what() << endl;
    }
    cout << "publishConfig  result:" << bSucc << endl;
    ASSERT_EQ(bSucc,true);

    cout << "sleep 5s ..." << endl;
    sleep(5);

    try {
        actualContent = n->getConfig(dataId, group, 1000);
    }
    catch (NacosException &e) {
        cout <<
             "Request failed with curl code:" << e.errorcode() << endl <<
             "Reason:" << e.what() << endl;
    }
    cout << "getConfig  actualContent:" << actualContent << endl;
    ASSERT_EQ(actualContent,expectContent);

    cout << "====End to test====" << endl;
}

class MyListener : public Listener {
private:
    int num;
    string expectContent;
public:
    MyListener(int num, string expectContent) {
        this->num = num;
        this->expectContent = expectContent;
    }

    void receiveConfigInfo(const NacosString &configInfo) {
        cout << "===================================" << endl;
        cout << "Watcher" << num << endl;
        cout << "Watched Key UPDATED:" << configInfo << endl;
        ASSERT_EQ(configInfo, expectContent);
        cout << "===================================" << endl;
    }
};

TEST(ConfigTestSuit,CASE2_ListenAndPublisConfig) {
    cout << "====Begin to test====" << endl;
    char *ALL_IP;
    ALL_IP = getenv("ALL_IP");
    string inputString = ALL_IP;
    std::cout << "ALL_IP is:" << inputString << std::endl;
    std::string nacos_server_address = extractNacosIPs(inputString);
    std::cout << "SERVER_ADDR is:" << std::endl;

    Properties props;
    props[PropertyKeyConst::SERVER_ADDR] = nacos_server_address;
    INacosServiceFactory *factory = NacosFactoryFactory::getNacosFactory(props);
    ResourceGuard <INacosServiceFactory> _guardFactory(factory);
    ConfigService *n = factory->CreateConfigService();
    ResourceGuard <ConfigService> _serviceFactory(n);

    timeb t;
    ftime(&t);
    NacosString dataId = "nacos.test." + to_string(t.time * 1000 + t.millitm);
    NacosString group = NULLSTR;
    NacosString expectContent = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」";

    cout << "Start to Listen Key:" << dataId  << endl;
    MyListener *theListener = new MyListener(1, expectContent);
    n->addListener(dataId, group, theListener);

    cout << "publishConfig Key:" << dataId << " with value:"<< expectContent << endl;
    bool bSucc = false;
    try {
        bSucc = n->publishConfig(dataId, group, expectContent);
    }
    catch (NacosException &e) {
        cout <<
             "Request failed with curl code:" << e.errorcode() << endl <<
             "Reason:" << e.what() << endl;
    }
    cout << "publishConfig  result:" << bSucc << endl;
    ASSERT_EQ(bSucc,true);

    cout << "sleep 5s ..." << endl;
    sleep(5);

    cout << "====End to test====" << endl;
}

TEST(NamingTestSuit,CASE3_registerAndGetAllInstances) {
    cout << "====Begin to test====" << endl;
    char *ALL_IP;
    ALL_IP = getenv("ALL_IP");
    string inputString = ALL_IP;
    std::cout << "ALL_IP is:" << inputString << std::endl;
    std::string nacos_server_address = extractNacosIPs(inputString);
    std::cout << "SERVER_ADDR is:" << std::endl;

    Properties props;
    props[PropertyKeyConst::SERVER_ADDR] = nacos_server_address;
    INacosServiceFactory *factory = NacosFactoryFactory::getNacosFactory(props);
    ResourceGuard <INacosServiceFactory> _guardFactory(factory);
    NamingService *n = factory->CreateNamingService();
    ResourceGuard <NamingService> _serviceFactory(n);
    timeb t;
    ftime(&t);
    NacosString serviceName = "TestNamingService.1." + to_string(t.time * 1000 + t.millitm);
    Instance instance;
    instance.clusterName = "DefaultCluster";
    instance.ip = "127.0.0.1";
    instance.port = 2333;
    instance.instanceId = "1";
    instance.ephemeral = true;

    //Registers services
    cout << "Register Sevice:" << serviceName  << endl;
    try {
        n->registerInstance(serviceName, instance);
    }
    catch (NacosException &e) {
        cout << "encounter exception while registering service instance, raison:" << e.what() << endl;
    }
    cout << "sleep 15s ..." << endl;
    sleep(15);

    list <Instance> instances = n->getAllInstances(serviceName);
    cout << "getAllInstances from server check:" << endl;
    for (list<Instance>::iterator it = instances.begin();
         it != instances.end(); it++) {
        cout << "Instance:" << it->toString() << endl;
        ASSERT_EQ((*it).ip,instance.ip);
        ASSERT_EQ((*it).port,instance.port);
        ASSERT_EQ((*it).clusterName,instance.clusterName);
    }

    try {
        n->deregisterInstance(serviceName, instance.ip, instance.port);
        sleep(1);
    }
    catch (NacosException &e) {
        cout << "encounter exception while registering service instance, raison:" << e.what() << endl;
    }
    cout << "====End to test====" << endl;
}

class MyServiceListener : public EventListener {
private:
    int num;
    string serviceName;
    Instance instance;
public:
    MyServiceListener(int num, string serviceName, Instance instance) {
        this->num = num;
        this->serviceName = serviceName;
        this->instance = instance;
    }

    void receiveNamingInfo(const ServiceInfo &serviceInfo){
        cout << "===================================" << endl;
        cout << "Watcher: " << num << endl;
        NacosString instanceString = serviceInfo.toInstanceString();
        cout << "Watched service UPDATED: " << instanceString << endl;
        NacosString expectString = instance.ip + "#" +  to_string(instance.port) + "#" + instance.clusterName + "#DEFAULT_GROUP" + "@@" + serviceName;
        cout << "expectString: " << expectString << endl;
        string::size_type idx = instanceString.find( expectString );
        EXPECT_TRUE(idx != string::npos);
        // list <Instance> instances = serviceInfo.getHosts();
        // cout << "ListenHost from server check:" << endl;
        // for (list<Instance>::iterator it = instances.begin();
        //     it != instances.end(); it++) {
        //     cout << "Instance:" << it->toString() << endl;
        //     ASSERT_EQ((*it).ip,instance.ip);
        //     ASSERT_EQ((*it).port,instance.port);
        //     ASSERT_EQ((*it).clusterName,instance.clusterName);
        // }
        cout << "===================================" << endl;

    }
};

TEST(NamingTestSuit,CASE4_ListenAndRegisterInstance) {
    cout << "====Begin to test====" << endl;
    char *ALL_IP;
    ALL_IP = getenv("ALL_IP");
    string inputString = ALL_IP;
    std::cout << "ALL_IP is:" << inputString << std::endl;
    std::string nacos_server_address = extractNacosIPs(inputString);
    std::cout << "SERVER_ADDR is:" << std::endl;

    Properties props;
    props[PropertyKeyConst::SERVER_ADDR] = nacos_server_address;
    INacosServiceFactory *factory = NacosFactoryFactory::getNacosFactory(props);
    ResourceGuard <INacosServiceFactory> _guardFactory(factory);
    NamingService *n = factory->CreateNamingService();
    ResourceGuard <NamingService> _serviceFactory(n);
    timeb t;
    ftime(&t);
    NacosString serviceName = "TestNamingService.2." + to_string(t.time * 1000 + t.millitm);
    Instance instance;
    instance.clusterName = "DefaultCluster";
    instance.ip = "127.0.0.1";
    instance.port = 2333;
    instance.instanceId = "1";
    instance.ephemeral = true;

    cout << "Start to Listen Sevice:" << serviceName  << endl;
    n->subscribe(serviceName, new MyServiceListener(1, serviceName, instance));


    //Registers services
    cout << "Register Sevice:" << serviceName  << endl;
    try {
        n->registerInstance(serviceName, instance);
    }
    catch (NacosException &e) {
        cout << "encounter exception while registering service instance, raison:" << e.what() << endl;
    }

    cout << "sleep 30s ..." << endl;
    sleep(30);

    try {
        n->deregisterInstance(serviceName, instance.ip, instance.port);
        sleep(1);
    }
    catch (NacosException &e) {
        cout << "encounter exception while registering service instance, raison:" << e.what() << endl;
    }
    cout << "====End to test====" << endl;
}

int main(int argc, char *argv[]) {
    InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
