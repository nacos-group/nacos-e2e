const NacosConfigClient = require('nacos').NacosConfigClient; // js
const fs = require('mz/fs');

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

// for direct mode
const configClient = new NacosConfigClient({
    serverAddr: serverList,
    namespace: '',
    requestTimeout: 10000,
});


exports.getClient = () => {
    return configClient;
}

// get config once
exports.getConfig = async (dataId, group) => {
    await configClient.ready();

    const content= await configClient.getConfig(dataId, group);
    console.log('getConfig content => ',content);
    configClient.close();
    return content;
}

// listen data changed
exports.subscribeConfig = async (dataId, group) => {
    await configClient.ready();

    configClient.subscribe({
        dataId: dataId,
        group: group,
    }, content => {
        console.log('subscribeConfig content => ', content);
        configClient.close();
        return content;
    });
}


// publish config
exports.publishSingle = async(dataId, group, content) => {
    await configClient.ready();

    const result= await configClient.publishSingle(dataId, group, content);
    console.log('publishSingle result => ',result);
    configClient.close();
    return result;
};


// remove config
exports.removeConfig = async (dataId, group) => {
    await configClient.ready();

    const result  = await configClient.remove(dataId, group);
    console.log('removeConfig result => ', result);
    configClient.close();
    return result;
}


// get content from file
exports.getContentFromFile = () => {
    let content = fs.readFileSync('./data.txt').toString();
    return content;
}