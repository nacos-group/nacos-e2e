package nacos_go_test

import (
	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/config_client"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
	"time"
	"math/rand"
)

const letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
func randStr(n int) string {
	b := make([]byte, n)
	for i := range b {
	  b[i] = letters[rand.Intn(len(letters))]
	}
	return string(b)
}

func createConfigClientTest()  config_client.IConfigClient {

	//create ServerConfig
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
	// create config client
	client, err := clients.NewConfigClient(
		vo.NacosClientParam{
			ClientConfig:  &cc,
			ServerConfigs: sc,
		},
	)

	if err != nil {
		panic("NewConfigClient failed!")
	}
	return client
}

var configParam = vo.ConfigParam{
	DataId:  "gotest" + randStr(10),
	Group:   "DEFAULT_GROUP",
	Content: "hello world",
}


func Test_PublishConfig_And_GetConfig(t *testing.T) {
	client := createConfigClientTest()
	var dataId string = configParam.DataId
	var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   configParam.Group,
		Content: content})

	assert.Nil(t, err)
	assert.True(t, success)

	time.Sleep(5 * time.Second)

	value, err := client.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  configParam.Group})

	assert.Nil(t, err)
	assert.Equal(t, content, value)
}

func Test_PublishConfig_And_SearchConfig(t *testing.T) {
	client := createConfigClientTest()
	var dataId string = configParam.DataId
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   configParam.Group,
		Content: configParam.Content})

	assert.Nil(t, err)
	assert.True(t, success)

	configPage, err := client.SearchConfig(vo.SearchConfigParam{
		Search:   "accurate",
		DataId:   dataId,
		Group:    configParam.Group,
		PageNo:   1,
		PageSize: 10,
	})
	assert.Nil(t, err)
	assert.NotEmpty(t, configPage)
}


func Test_PublishConfigWithoutDataId(t *testing.T) {
	client := createConfigClientTest()
	_, err := client.PublishConfig(vo.ConfigParam{
		DataId:  "",
		Group:   configParam.Group,
		Content: configParam.Content,
	})
	assert.NotNil(t, err)
}

func Test_PublishConfigWithoutContent(t *testing.T) {
	client := createConfigClientTest()
	_, err := client.PublishConfig(vo.ConfigParam{
		DataId:  configParam.DataId,
		Group:   configParam.Group,
		Content: "",
	})
	assert.NotNil(t, err)
}


func Test_PublishConfig_And_DeleteConfig(t *testing.T) {

	client := createConfigClientTest()
	var dataId string = configParam.DataId
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   configParam.Group,
		Content: configParam.Content})

	assert.Nil(t, err)
	assert.True(t, success)

	success, err = client.DeleteConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  configParam.Group})

	assert.Nil(t, err)
	assert.True(t, success)
}

func Test_DeleteConfigWithoutDataId(t *testing.T) {
	client := createConfigClientTest()
	success, err := client.DeleteConfig(vo.ConfigParam{
		DataId: "",
		Group:  configParam.Group,
	})
	assert.NotNil(t, err)
	assert.Equal(t, false, success)
}

func TestListen(t *testing.T) {
	t.Run("TestListenConfig", func(t *testing.T) {
		client := createConfigClientTest()
		err := client.ListenConfig(vo.ConfigParam{
			DataId: configParam.DataId,
			Group:  configParam.Group,
			OnChange: func(namespace, group, dataId, data string) {
			},
		})
		assert.Nil(t, err)
	})
	// ListenConfig no dataId
	t.Run("TestListenConfigNoDataId", func(t *testing.T) {
		listenConfigParam := vo.ConfigParam{
			Group: configParam.Group,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}
		client := createConfigClientTest()
		err := client.ListenConfig(listenConfigParam)
		assert.Error(t, err)
	})
}

// CancelListenConfig
func TestCancelListenConfig(t *testing.T) {
	//Multiple listeners listen for different configurations, cancel one
	t.Run("TestMultipleListenersCancelOne", func(t *testing.T) {
		client := createConfigClientTest()
		var err error
		listenConfigParam := vo.ConfigParam{
			DataId: configParam.DataId,
			Group:  configParam.Group,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}

		listenConfigParam1 := vo.ConfigParam{
			DataId: configParam.DataId + "1",
			Group:  configParam.Group,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}
		_ = client.ListenConfig(listenConfigParam)

		_ = client.ListenConfig(listenConfigParam1)

		err = client.CancelListenConfig(listenConfigParam)
		assert.Nil(t, err)
	})
}
