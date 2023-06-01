const assert = require('assert');
const Config = require('../ConfigService');

function sleep(time){
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve();
        }, time);
    })
}


describe('publish one config with different character，wait for 5 second to listen.', function() {
    this.timeout(60000);
    it('expect listen correct config content.', function(done) {
        (async function () {
            const random = `test_${Math.random()}_${Date.now()}`;
            const dataId = 'nacos.test.'+random;
            const group = 'DEFAULT_GROUP';
            const str = Config.getContentFromFile()+ random;

            console.log("dataId => ", dataId, "group => ", group, "str => ", str);
            try {
                let r = await Config.publishSingle(dataId, group, str);
                assert.strictEqual(r, true);

                await sleep(5000);

                let configClient = Config.getClient();
                configClient.subscribe({
                    dataId: dataId,
                    group: group,
                }, content => {
                    console.log('subscribeConfig content => ', content);
                    assert.strictEqual(content, str);
                    configClient.close();
                });


                await Config.removeConfig(dataId, group);
                done();
            } catch (err) {
                done(err);
            }
        })();
    });
});


describe('publish one config with different character，wait for 5 second to get config.', function() {
    this.timeout(60000);
    it('expect get correct config content.', (done) => {
        (async function () {
            const random = `test_${Math.random()}_${Date.now()}`;
            const dataId = 'nacos.test.'+random;
            const group = 'DEFAULT_GROUP';
            const content = Config.getContentFromFile() + random;
            console.log("dataId => ", dataId, "group => ", group, "content => ", content);
            try {
                let r = await Config.publishSingle(dataId, group, content);
                assert.strictEqual(r, true);
                await sleep(5000);
                let g = await Config.getConfig(dataId, group);
                assert.strictEqual(g, content);
                await Config.removeConfig(dataId, group);
                done();
            } catch (err) {
                done(err);
            }
        })();
    });
});