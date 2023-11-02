using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Nacos.V2;
using Nacos.V2.DependencyInjection;
using Nacos.V2.Utils;
using System;
using System.Threading.Tasks;
using Xunit;
using Xunit.Abstractions;
using System.Text.RegularExpressions;

namespace nacos.tests
{
	public class NacosMainTest
	{
        public ITestOutputHelper output;
        public NacosMainTest(ITestOutputHelper output)
        {
            this.output = output;
        }

        #region 初始化
        static IServiceProvider InitServiceProvider()
        {
            IServiceCollection services = new ServiceCollection();
            TextWriter output = Console.Out;

            string input = Environment.GetEnvironmentVariable("ALL_IP");
            string nacos_server_address = "";
            string[] tokens = input.Split(',');
            foreach (string token in tokens)
            {
                if (token.StartsWith("nacos-"))
                {
                    string[] parts = token.Split(':');
                    if (parts.Length > 1)
                    {
                        string ipAddress = parts[1];
                        output.WriteLine($"ip {ipAddress}");
                        nacos_server_address = ipAddress;
                    }
                }
            }

            // string nacos_server_address = Environment.GetEnvironmentVariable("serverList");
            // output.WriteLine($"InitServiceProvider, nacos_server_address {nacos_server_address} ");
            string url = "http://"+nacos_server_address+":8848";
            // string namespaceId = Environment.GetEnvironmentVariable("namespaceId");
            // string accessKey = Environment.GetEnvironmentVariable("accessKey");
            // string secretKey = Environment.GetEnvironmentVariable("secretKey");

            services.AddNacosV2Config(x =>
            {
                x.ServerAddresses = new System.Collections.Generic.List<string> { url };
                x.EndPoint = "";
                // x.Namespace = namespaceId;
                // x.AccessKey = accessKey;
                // x.SecretKey = secretKey;

                // swich to use http or rpc
                x.ConfigUseRpc = true;
            });

            services.AddNacosV2Naming(x =>
            {
                x.ServerAddresses = new System.Collections.Generic.List<string> { url };
                x.EndPoint = "";
                // x.Namespace = namespaceId;
                // x.AccessKey = accessKey;
                // x.SecretKey = secretKey;

                // swich to use http or rpc
                x.NamingUseRpc = true;
            });

            services.AddLogging(builder => { builder.AddConsole(); });

            IServiceProvider serviceProvider = services.BuildServiceProvider();

            return serviceProvider;
        }
        #endregion

        #region Config测试开始
        [Fact]
        public virtual async Task PublishConfig_Should_Succeed()
        {
            var dataId = $"pub-{Guid.NewGuid().ToString()}";
            var group = Nacos.V2.Common.Constants.DEFAULT_GROUP;
            var val = "test-value";

            var serviceProvider = InitServiceProvider();

            INacosConfigService configSvc = serviceProvider.GetService<INacosConfigService>();

            var pubFlag = await configSvc.PublishConfig(dataId, group, val).ConfigureAwait(false);
            output.WriteLine($"PublishConfig_Should_Succeed, PublishConfig {dataId} return {pubFlag}");
            Assert.True(pubFlag);
        }

        [Fact]
        public virtual async Task Iss116_Should_Succeed()
        {
            var dataId = $"pub-{Guid.NewGuid().ToString()}";
            var group = Nacos.V2.Common.Constants.DEFAULT_GROUP;
            var val = @"{
    ""NacosConfig"": {
        ""ConfigFilterExtInfo"": ""{\""JsonPaths\"":[\""ConnectionStrings.Default\""],\""Other\"":\""xxxxxx\""}""
    }
}";
            var serviceProvider = InitServiceProvider();
            INacosConfigService configSvc = serviceProvider.GetService<INacosConfigService>();
            var pubFlag = await configSvc.PublishConfig(dataId, group, val).ConfigureAwait(false);
            output.WriteLine($"Iss116_Should_Succeed, PublishConfig {dataId} return {pubFlag}");
            Assert.True(pubFlag);
        }

        [Fact]
        public virtual async Task GetConfig_Should_Succeed()
        {
            var dataId = $"get-{Guid.NewGuid().ToString()}";
            var group = Nacos.V2.Common.Constants.DEFAULT_GROUP;
            var val = "test-value";

            var serviceProvider = InitServiceProvider();
            INacosConfigService configSvc = serviceProvider.GetService<INacosConfigService>();

            var pubFlag = await configSvc.PublishConfig(dataId, group, val).ConfigureAwait(false);
            output.WriteLine($"GetConfig_Should_Succeed, PublishConfig {dataId} return {pubFlag}");
            Assert.True(pubFlag);

            await Task.Delay(500).ConfigureAwait(false);

            var config = await configSvc.GetConfig(dataId, group, 10000L).ConfigureAwait(false);
            output.WriteLine($"GetConfig_Should_Succeed, GetConfig {dataId} return {pubFlag}");
            Assert.Equal(val, config);
        }

        [Fact]
        public virtual async Task DeleteConfig_Should_Succeed()
        {
            var dataId = $"del-{Guid.NewGuid().ToString()}";
            var group = Nacos.V2.Common.Constants.DEFAULT_GROUP;
            var val = "test-value";

            var serviceProvider = InitServiceProvider();
            INacosConfigService configSvc = serviceProvider.GetService<INacosConfigService>();


            var pubFlag = await configSvc.PublishConfig(dataId, group, val).ConfigureAwait(false);
            output.WriteLine($"DeleteConfig_Should_Succeed, PublishConfig {dataId} return {pubFlag}");
            Assert.True(pubFlag);

            await Task.Delay(500).ConfigureAwait(false);

            var config1 = await configSvc.GetConfig(dataId, group, 10000L).ConfigureAwait(false);
            output.WriteLine($"DeleteConfig_Should_Succeed, GetConfig1 {dataId} return {config1}");
            Assert.Equal(val, config1);

            var remFlag = await configSvc.RemoveConfig(dataId, group).ConfigureAwait(false);
            output.WriteLine($"DeleteConfig_Should_Succeed, RemoveConfig {dataId} return {remFlag}");
            Assert.True(remFlag);

            await Task.Delay(500).ConfigureAwait(false);

            var config2 = await configSvc.GetConfig(dataId, group, 10000L).ConfigureAwait(false);
            output.WriteLine($"DeleteConfig_Should_Succeed, GetConfig2 {dataId} return {config2}");
            Assert.Null(config2);
        }

        [Fact]
        public virtual async Task ListenConfig_Should_Succeed()
        {
            var dataId = $"lis-{Guid.NewGuid().ToString()}";
            var group = Nacos.V2.Common.Constants.DEFAULT_GROUP;
            var val = "test-value";

            var serviceProvider = InitServiceProvider();
            INacosConfigService configSvc = serviceProvider.GetService<INacosConfigService>();

            var pubFlag = await configSvc.PublishConfig(dataId, group, val).ConfigureAwait(false);
            output.WriteLine($"ListenConfig_Should_Succeed, PublishConfig1 {dataId} return {pubFlag}");
            Assert.True(pubFlag);

            await Task.Delay(1000).ConfigureAwait(false);

            var listener = new TestListener();
            await configSvc.AddListener(dataId, group, listener).ConfigureAwait(false);


            var pubFlag2 = await configSvc.PublishConfig(dataId, group, "123").ConfigureAwait(false);
            output.WriteLine($"ListenConfig_Should_Succeed, PublishConfig2 {dataId} return {pubFlag}");
            Assert.True(pubFlag2);
        }

        public class TestListener : Nacos.V2.IListener
        {
            public void ReceiveConfigInfo(string configInfo)
            {
                Assert.Equal("123", configInfo);
            }
        }
        #endregion

        #region Naming测试开始
        [Fact]
        protected virtual async Task RegisterInstance_Should_Succeed()
        {
            var serviceName = $"reg-{Guid.NewGuid().ToString()}";
            var ip = "127.0.0.1";
            var port = 9999;

            await AssertRegisterSingleInstance(serviceName, ip, port, nameof(RegisterInstance_Should_Succeed)).ConfigureAwait(false);
        }

        // [Fact]
        // protected virtual async Task DeregisterInstance_Should_Succeed()
        // {
        //     var serviceName = $"dereg-{Guid.NewGuid().ToString()}";
        //     var ip = "127.0.0.2";
        //     var port = 9999;
        //     var serviceProvider = InitServiceProvider();
        //     INacosNamingService namingSvc = serviceProvider.GetService<INacosNamingService>();


        //     await AssertRegisterSingleInstance(serviceName, ip, port, nameof(DeregisterInstance_Should_Succeed)).ConfigureAwait(false);

        //     await namingSvc.DeregisterInstance(serviceName, ip, port).ConfigureAwait(false);
        //     await Task.Delay(1000).ConfigureAwait(false);
        //     var instances = await namingSvc.GetAllInstances(serviceName, false).ConfigureAwait(false);
        //     output.WriteLine($"DeregisterInstance_Should_Succeed, GetAllInstances After Deregister, {serviceName}, {instances?.ToJsonString()}");
        //     Assert.Empty(instances);
        // }

        [Fact]
        protected virtual async Task Subscribe_Should_Succeed()
        {
            var serviceName = $"sub-{Guid.NewGuid().ToString()}";
            var ip = "127.0.0.3";
            var port = 9999;
            var serviceProvider = InitServiceProvider();
            INacosNamingService namingSvc = serviceProvider.GetService<INacosNamingService>();


            await AssertRegisterSingleInstance(serviceName, ip, port, nameof(Subscribe_Should_Succeed)).ConfigureAwait(false);

            var listerner = new NamingListerner(output);

            await namingSvc.Subscribe(serviceName, listerner).ConfigureAwait(false);

            await namingSvc.RegisterInstance(serviceName, "127.0.0.4", 9999).ConfigureAwait(false);

            await Task.Delay(500).ConfigureAwait(false);
        }

        private async Task AssertRegisterSingleInstance(string serviceName, string ip, int port, string testName)
        {
            var serviceProvider = InitServiceProvider();
            INacosNamingService namingSvc = serviceProvider.GetService<INacosNamingService>();

            output.WriteLine($"{testName}, register instance, {serviceName} ,{ip} , {port}");
            await namingSvc.RegisterInstance(serviceName, ip, port).ConfigureAwait(false);
            await Task.Delay(500).ConfigureAwait(false);
            var instances = await namingSvc.GetAllInstances(serviceName, false).ConfigureAwait(false);
            output.WriteLine($"{testName}, GetAllInstances, {serviceName}, {instances?.ToJsonString()}");
            Assert.Single(instances);

            var first = instances[0];
            Assert.Equal(ip, first.Ip);
            Assert.Equal(port, first.Port);
        }

        public class NamingListerner : Nacos.V2.IEventListener
        {
            private ITestOutputHelper _output;

            public NamingListerner(ITestOutputHelper output) => _output = output;

            public Task OnEvent(IEvent @event)
            {
                _output.WriteLine($"NamingListerner, {@event.ToJsonString()}");

                Assert.IsType<Nacos.V2.Naming.Event.InstancesChangeEvent>(@event);

                return Task.CompletedTask;
            }
        }
        #endregion
    }
}