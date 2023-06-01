const assert = require('assert');
const Service = require('../ServiceDiscovery');

const {
    address,
} = require('ip');
const { Console } = require('console');

function sleep(time){
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve();
        }, time);
    })
}


describe('register one instance and wait for 10 second to get all instance.', function() {
    this.timeout(60000);
    it('expect instance size is one, and check instance detail.', function(done) {
        (async function () {
            const random = `test_${Math.random()}_${Date.now()}`;
            // generate local ip
            const ip = address();
            const port = 3001
            const serviceName = 'nacos.nodejs.test.'+random;
            console.log("serviceName => ", serviceName, "ip => ", ip, "port => ", port);
            try {
                await Service.registerInstance(serviceName, ip, port);
                console.log(`[Nacos] register info: ${serviceName}:${ip}:${port}`);

                await sleep(10000);

                let g = await Service.getAllInstances(serviceName);
                console.log(`[Nacos] get all instance result: ` + JSON.stringify(g));
                assert.strictEqual(JSON.stringify(g) != '[]', true, "instance can't be[]");
                let instances = JSON.parse(JSON.stringify(g));
                instances.forEach((instance, index) => {
                    assert.strictEqual(instance.serviceName, 'DEFAULT_GROUP@@' + serviceName, "serviceName check not match");
                    assert.strictEqual(instance.ip, ip, "ip check not match");
                    assert.strictEqual(instance.port, port, "port check not match");
                    assert.strictEqual(instance.clusterName, "DEFAULT", "clusterName check not match");

                });

                await Service.deregisterInstance(serviceName, ip, port);

                done();
            } catch (err) {
                console.log('[Nacos] test fail: ' + err.toString());
                done(err);
            }
        })();
    });
});



describe('register one service and subscribe the service, after register instance wait for 10 second to listen the instance.', function() {
    this.timeout(60000);
    it('expect listen instance the same as register one.', function(done) {
        (async function () {
            const random = `test_${Math.random()}_${Date.now()}`;
            const ip = address();
            const port = 3001
            const serviceName = 'nacos.nodejs.test.'+random;
            console.log("serviceName => ", serviceName, "ip => ", ip, "port => ", port);
            try {

                let instances = '[]';
                let namingClient = Service.getClient();
                namingClient.subscribe({
                    serviceName: serviceName
                }, hosts => {
                    console.log(`[Nacos] listen info:` + JSON.stringify(hosts));
                    instances = JSON.stringify(hosts);
                });

                await Service.registerInstance(serviceName, ip, port);
                console.log(`[Nacos] register info: ${serviceName}:${ip}:${port}`);

                let i = 0;
                while (instances == '[]') {
                    console.log("wait to subscribe instance...i="+i );
                    await sleep(1000);
                    if (i++ > 10) {
                        break;
                    }
                }
                console.log(`[Nacos] listen result:` + JSON.stringify(instances));
                namingClient.unSubscribe(serviceName, hosts => {
                    console.log(hosts);
                });
                namingClient.close();
                assert.strictEqual(instances != '[]', true, "instance can't be[]");
                let result = JSON.parse(instances);
                result.forEach((instance, index) => {
                    assert.strictEqual(instance.serviceName, 'DEFAULT_GROUP@@' + serviceName, "serviceName check not match");
                    assert.strictEqual(instance.ip, ip, "ip check not match");
                    assert.strictEqual(instance.port, port, "port check not match");
                    assert.strictEqual(instance.clusterName, "DEFAULT", "clusterName check not match");

                });

                await Service.deregisterInstance(serviceName, ip, port);

                done();
            } catch (err) {
                console.log('[Nacos] test fail: ' + err.toString());
                done(err);
            }
        })();
    });
});
