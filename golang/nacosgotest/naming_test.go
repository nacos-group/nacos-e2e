package nacos_go_test

import (
	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/nacos_client"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/naming_client"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/common/http_agent"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
	"time"
    "math/rand"
)

var clientConfigTest = *constant.NewClientConfig(
	constant.WithTimeoutMs(10*1000),
	constant.WithBeatInterval(5*1000),
	constant.WithNotLoadCacheAtStart(true),
)


const strs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
func rands(n int) string {
    b := make([]byte, n)
    for i := range b {
      b[i] = strs[rand.Intn(len(strs))]
    }
    return string(b)
}

var serverConfigTest = *constant.NewServerConfig("127.0.0.1", 8848, constant.WithContextPath("/nacos"))

func NewTestNamingClient() naming_client.INamingClient {
	nc := nacos_client.NacosClient{}
	sc := []constant.ServerConfig{
		*constant.NewServerConfig(os.Getenv("serverList"), 8848, constant.WithContextPath("/nacos")),
	}

	//create ClientConfig
	cc := *constant.NewClientConfig(
		constant.WithNamespaceId(""),
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)

	var clientParam = vo.NacosClientParam{
		ClientConfig:  &cc,
		ServerConfigs: sc,
	}
	_ = nc.SetServerConfig([]constant.ServerConfig{serverConfigTest})
	_ = nc.SetClientConfig(clientConfigTest)
	_ = nc.SetHttpAgent(&http_agent.HttpAgent{})
	client, err := clients.NewNamingClient(clientParam)
	if err != nil {
        panic("NewNamingClient failed!")
    }
	return client
}

func Test_RegisterServiceInstance__DeregisterService_withoutGroupName(t *testing.T) {
    //create ServerConfig
    var serviceName string = "DEMO" + rands(10)
    successR, errR := NewTestNamingClient().RegisterInstance(vo.RegisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        Ephemeral:   false,
    })
    assert.Equal(t, nil, errR)
    assert.Equal(t, true, successR)

    time.Sleep(5 * time.Second)

    successD, errD := NewTestNamingClient().DeregisterInstance(vo.DeregisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        Ephemeral:   false,
    })
    assert.Equal(t, nil, errD)
    assert.Equal(t, true, successD)
}

func Test_RegisterServiceInstance__DeregisterService_withGroupName(t *testing.T) {
    var serviceName string = "DEMO" + rands(10)
    successR, errR := NewTestNamingClient().RegisterInstance(vo.RegisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        GroupName:   "test_group",
        Ephemeral:   false,
    })
    assert.Equal(t, nil, errR)
    assert.Equal(t, true, successR)

    time.Sleep(5 * time.Second)


    successD, errD := NewTestNamingClient().DeregisterInstance(vo.DeregisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        GroupName:   "test_group",
        Ephemeral:   false,
    })
    assert.Equal(t, nil, errD)
    assert.Equal(t, true, successD)
}

func Test_RegisterServiceInstance_withCluster(t *testing.T) {
    var serviceName string = "DEMO" + rands(10)
    success, err := NewTestNamingClient().RegisterInstance(vo.RegisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        GroupName:   "test_group",
        ClusterName: "test",
        Ephemeral:   false,
    })
    assert.Equal(t, nil, err)
    assert.Equal(t, true, success)
}


func TestNamingClient_GetAllServicesInfo(t *testing.T) {
    //create ServerConfig
    var serviceName string = "testGet" + rands(10)
    success, err := NewTestNamingClient().RegisterInstance(vo.RegisterInstanceParam{
        ServiceName: serviceName,
        Ip:          "10.0.0.10",
        Port:        80,
        Ephemeral:   false,
        GroupName: "DEFAULT_GROUP",
    })
    assert.Equal(t, nil, err)
    assert.Equal(t, true, success)

    time.Sleep(5 * time.Second)

    result, err := NewTestNamingClient().GetAllServicesInfo(vo.GetAllServiceInfoParam{
        GroupName: "DEFAULT_GROUP",
        PageNo:    1,
        PageSize:  20,
    })

    assert.NotNil(t, result.Doms)
    assert.Nil(t, err)
}

// func TestSubscribe(t *testing.T) {
// 	err := NewTestNamingClient().Subscribe(vo.SubscribeParam{
//     ServiceName: "DEMO6",
//     GroupName:   "test_group",
// 		Clusters:    []string{"DEFAULT"},
//     SubscribeCallback: func(services []model.SubscribeService, err error) {
// 				assert.Nil(t, err)
//
// 				js, _ := json.Marshal(services)
//         fmt.Printf("\n\n callback return services:%s \n\n", string(js))
//     },
// 	})
//
// 	assert.Nil(t, err)
//
// }
