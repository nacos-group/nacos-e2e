'use strict';

const NacosNamingClient = require('nacos').NacosNamingClient;
const logger = console;

const ALL_IP = process.env.ALL_IP || '';
let serverList = '';

if (ALL_IP) {
    const pairs = ALL_IP.split(',');
    for (const pair of pairs) {
        if (pair.startsWith('nacos-')) {
            serverList = pair.split(':')[1];
            break;
        }
    }
}

if (!serverList) {
    serverList = '127.0.0.1:8848';
}

// const serverList = (process.env.serverList == '${serverList}' || process.env.serverList == undefined
//     || process.env.serverList == null || process.env.serverList == '')
//     ? '127.0.0.1:8848' : process.env.serverList;
console.log("get env serverList = ", serverList);

const namingClient = new NacosNamingClient({
    logger,
    serverList: serverList, // replace to real nacos serverList
    namespace: '',
});

exports.getClient = () => {
    return namingClient;
}

// registry instance
exports.registerInstance = async (serviceName, ip, port) => {
    await namingClient.ready();
    // group default is DEFAULT_GROUP
    await namingClient.registerInstance(serviceName, {
        ip: ip,
        port: port,
    });
    namingClient.close();
}

exports.registerInstanceHasGroup = async (serviceName, ip, port, groupName) => {
    await namingClient.ready();
    await client.registerInstance(serviceName, {
        ip: ip,
        port: port
    }, groupName);
    namingClient.close();
}

// get all instance
exports.getAllInstances = async (serviceName) => {
    await namingClient.ready();
    let allInstances = await namingClient.getAllInstances(serviceName, 'DEFAULT_GROUP', 'DEFAULT', false)
    console.log('[Nacos]----allInstances----', allInstances)
    namingClient.close();
    return allInstances;
}

// subscribe instance
exports.subscribe = async (serviceName) => {
    await namingClient.ready();

    namingClient.subscribe(serviceName, hosts => {
        console.log(hosts);
    });
    namingClient.close();

}

// unSubscribe instance
exports.unSubscribe = async (serviceName) => {
    await namingClient.ready();

    namingClient.unSubscribe(serviceName, hosts => {
        console.log(hosts);
    });
    namingClient.close();
}

// deregister instance
exports.deregisterInstance = async (serviceName, ip, port) => {
    await namingClient.deregisterInstance(serviceName, {
        ip: ip,
        port: port,
    });
    namingClient.close();
}