package nacos_go_test

import (
	"fmt"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	. "nacos_go_test/utils"
	"strconv"
	"testing"
	"time"
)

func Test_PublishConfig_And_GetConfig(t *testing.T) {
	client := CreateConfigClient()
	var dataId string = RandDataId(10)
	var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   DEFAULT_GROUP,
		Content: content})

	assert.Nil(t, err)
	assert.True(t, success)

	time.Sleep(5 * time.Second)

	value, err := client.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  DEFAULT_GROUP})

	assert.Nil(t, err)
	assert.Equal(t, content, value)

	success, err = client.DeleteConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  DEFAULT_GROUP})

	assert.Nil(t, err)
	assert.True(t, success)
}

func Test_PublishConfig_And_SearchConfig(t *testing.T) {
	client := CreateConfigClient()
	var dataId string = RandDataId(10)
	var content string = RandStr(10)
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   DEFAULT_GROUP,
		Content: content})

	assert.Nil(t, err)
	assert.True(t, success)

	time.Sleep(5 * time.Second)

	configPage, err := client.SearchConfig(vo.SearchConfigParam{
		Search:   "accurate",
		DataId:   dataId,
		Group:    DEFAULT_GROUP,
		PageNo:   1,
		PageSize: 10,
	})
	assert.Nil(t, err)
	assert.NotEmpty(t, configPage)

	success, err = client.DeleteConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  DEFAULT_GROUP})

	assert.Nil(t, err)
	assert.True(t, success)
}

func Test_PublishConfigWithoutDataId(t *testing.T) {
	client := CreateConfigClient()
	t.Run("TestPublishConfigDataIdIsEmptyString", func(t *testing.T) {
		_, err := client.PublishConfig(vo.ConfigParam{
			DataId:  "",
			Group:   DEFAULT_GROUP,
			Content: RandStr(10),
		})
		assert.NotNil(t, err)
	})
	t.Run("TestPublishConfigDataIdIsNil", func(t *testing.T) {
		_, err := client.PublishConfig(vo.ConfigParam{
			Group:   DEFAULT_GROUP,
			Content: RandStr(10),
		})
		assert.NotNil(t, err)
	})
}

func Test_PublishConfigWithoutGroup(t *testing.T) {
	client := CreateConfigClient()
	t.Run("TestPublishConfigGroupIsEmptyString", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = RandStr(10)
		success, err := client.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Group:   "",
			Content: content,
		})
		assert.Nil(t, err)
		assert.True(t, success)

		time.Sleep(5 * time.Second)
		value, err := client.GetConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  ""})

		assert.Nil(t, err)
		assert.Equal(t, content, value)

		success, err = client.DeleteConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  ""})

		assert.Nil(t, err)
		assert.True(t, success)
	})
	t.Run("TestPublishConfigGroupIsNil", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = RandStr(10)
		success, err := client.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Content: content,
		})
		assert.Nil(t, err)
		assert.True(t, success)

		time.Sleep(5 * time.Second)
		value, err := client.GetConfig(vo.ConfigParam{
			DataId: dataId})

		assert.Nil(t, err)
		assert.Equal(t, content, value)

		success, err = client.DeleteConfig(vo.ConfigParam{
			DataId: dataId})

		assert.Nil(t, err)
		assert.True(t, success)
	})
}

func Test_PublishConfigWithoutContent(t *testing.T) {
	client := CreateConfigClient()
	t.Run("TestPublishConfigContentIsEmptyString", func(t *testing.T) {
		_, err := client.PublishConfig(vo.ConfigParam{
			DataId:  RandDataId(10),
			Group:   DEFAULT_GROUP,
			Content: "",
		})
		assert.NotNil(t, err)
	})
	t.Run("TestPublishConfigContentIsNil", func(t *testing.T) {
		_, err := client.PublishConfig(vo.ConfigParam{
			DataId: RandDataId(10),
			Group:  DEFAULT_GROUP,
		})
		assert.NotNil(t, err)
	})
}

func Test_PublishConfig_And_DeleteConfig(t *testing.T) {

	client := CreateConfigClient()
	var dataId string = RandDataId(10)
	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   DEFAULT_GROUP,
		Content: RandStr(10)})

	assert.Nil(t, err)
	assert.True(t, success)

	success, err = client.DeleteConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  DEFAULT_GROUP})

	assert.Nil(t, err)
	assert.True(t, success)
}

func Test_DeleteConfigWithoutDataId(t *testing.T) {
	client := CreateConfigClient()
	success, err := client.DeleteConfig(vo.ConfigParam{
		DataId: "",
		Group:  DEFAULT_GROUP,
	})
	assert.NotNil(t, err)
	assert.Equal(t, false, success)
}

func Test_ListenConfig(t *testing.T) {
	t.Run("TestListenConfigAndCancelListen", func(t *testing.T) {
		client := CreateConfigClient()
		var dataId string = RandDataId(10)
		var group string = DEFAULT_GROUP
		var pubContent string = RandStr(10)
		var listenContent string

		listenConfigParam := vo.ConfigParam{
			DataId: dataId,
			Group:  group,
			OnChange: func(namespace, group, dataId, data string) {
				fmt.Printf("Config changed: ns %s, dataId %s, group %s, data %s\n", namespace, dataId, group, data)
				listenContent = data
			},
		}
		err := client.ListenConfig(listenConfigParam)
		assert.Nil(t, err)

		success, err := client.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Group:   group,
			Content: pubContent})
		fmt.Printf("Publish Config: dataId %s, group %s, content %s, result %s\n", dataId, group, pubContent, strconv.FormatBool(success))
		assert.Nil(t, err)
		assert.True(t, success)
		// wait for config change in timeout(second)
		timeout := 30
		start := time.Now()
		for {
			if listenContent != "" {
				fmt.Printf("Current config: %s\n", listenContent)
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			time.Sleep(5 * time.Second)
		}
		assert.Equal(t, pubContent, listenContent)

		err = client.CancelListenConfig(listenConfigParam)
		assert.Nil(t, err)

		pubContent += "change"
		listenContent = ""
		success, err = client.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Group:   group,
			Content: pubContent})
		fmt.Printf("Publish Config: dataId %s, group %s, content %s, result %s\n", dataId, group, pubContent, strconv.FormatBool(success))
		assert.Nil(t, err)
		assert.True(t, success)

		start = time.Now()
		for {
			if listenContent != "" {
				fmt.Printf("Current config: %s\n", listenContent)
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			time.Sleep(5 * time.Second)
		}
		assert.Equal(t, "", listenContent)

		value, err := client.GetConfig(vo.ConfigParam{
			DataId: dataId})

		assert.Nil(t, err)
		assert.Equal(t, pubContent, value)

		success, err = client.DeleteConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  ""})

		assert.Nil(t, err)
		assert.True(t, success)
	})
	// ListenConfig no dataId
	t.Run("TestListenConfigNoDataId", func(t *testing.T) {
		listenConfigParam := vo.ConfigParam{
			Group: DEFAULT_GROUP,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}
		client := CreateConfigClient()
		err := client.ListenConfig(listenConfigParam)
		assert.Error(t, err)
	})
}

// Multiple listeners listen for different configurations, cancel one
func Test_CancelListenConfig(t *testing.T) {
	t.Run("TestMultipleListenersCancelOne", func(t *testing.T) {
		client := CreateConfigClient()
		var dataId string = RandDataId(10)
		var err error
		listenConfigParam := vo.ConfigParam{
			DataId: dataId,
			Group:  DEFAULT_GROUP,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}

		listenConfigParam1 := vo.ConfigParam{
			DataId: dataId + "1",
			Group:  DEFAULT_GROUP,
			OnChange: func(namespace, group, dataId, data string) {
			},
		}
		_ = client.ListenConfig(listenConfigParam)

		_ = client.ListenConfig(listenConfigParam1)

		err = client.CancelListenConfig(listenConfigParam)
		assert.Nil(t, err)
	})
}

func Test_ReloadCurrentAndListenConfig(t *testing.T) {
	client := CreateConfigClient()
	var dataId string = RandDataId(10)
	var group string = DEFAULT_GROUP
	var pubContent string = RandStr(10)
	var listenContent string

	listenConfigParam := vo.ConfigParam{
		DataId: dataId,
		Group:  group,
		OnChange: func(namespace, group, dataId, data string) {
			fmt.Printf("Config changed: ns %s, dataId %s, group %s, data %s\n", namespace, dataId, group, data)
			listenContent = data
		},
	}
	err := client.ListenConfig(listenConfigParam)
	assert.Nil(t, err)

	success, err := client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   group,
		Content: pubContent})
	fmt.Printf("Publish Config: dataId %s, group %s, content %s, result %s\n", dataId, group, pubContent, strconv.FormatBool(success))
	assert.Nil(t, err)
	assert.True(t, success)
	// wait for config change in timeout(second)
	timeout := 30
	start := time.Now()
	for {
		if listenContent != "" {
			fmt.Printf("Current config: %s\n", listenContent)
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		time.Sleep(5 * time.Second)
	}
	assert.Equal(t, pubContent, listenContent)

	pubContent += "change"
	listenContent = ""
	success, err = client.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   group,
		Content: pubContent})
	fmt.Printf("Publish Config: dataId %s, group %s, content %s, result %s\n", dataId, group, pubContent, strconv.FormatBool(success))
	assert.Nil(t, err)
	assert.True(t, success)

	params := make(map[string]string)
	params["count"] = "0"
	// make server break connect, it should be effect in singleton, but may not be effect in cluster
	body := HttpGet("/nacos/v2/core/loader/reloadCurrent", params)
	if body == "" {
		assert.True(t, false)
	} else {
		assert.Equal(t, "success", body)
		err := client.ListenConfig(listenConfigParam)
		assert.Nil(t, err)

		start := time.Now()
		for {
			if listenContent != "" {
				fmt.Printf("Current config: %s\n", listenContent)
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			time.Sleep(5 * time.Second)
		}
		assert.Equal(t, pubContent, listenContent)

		success, err = client.DeleteConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  ""})

	 cipher	assert.Nil(t, err)
		assert.True(t, success)
	}
}
