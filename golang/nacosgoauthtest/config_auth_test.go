package nacos_go_test

import (
	"fmt"
	. "github.com/nacos-group/nacos-e2e/golang/util"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	"strconv"
	"testing"
	"time"
)

var AuthTrueConfigClient = CreateConfigClient()
var AuthFailConfigClient = CreateAuthFailConfigClient()
var NoAuthConfigClient = CreateNoAuthConfigClient()

func Test_PublishConfig_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		success, err := AuthFailConfigClient.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Group:   DEFAULT_GROUP,
			Content: content})

		assert.False(t, success)
		assert.Nil(t, err)
	})
	t.Run("TestNOAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		success, err := AuthFailConfigClient.PublishConfig(vo.ConfigParam{
			DataId:  dataId,
			Group:   DEFAULT_GROUP,
			Content: content})

		assert.False(t, success)
		assert.Nil(t, err)
	})
}

func publishConfig(dataId string, group string, content string) {
	success, err := AuthTrueConfigClient.PublishConfig(vo.ConfigParam{
		DataId:  dataId,
		Group:   group,
		Content: content})
	fmt.Printf("PublishConfig: success %s, err %s\n", strconv.FormatBool(success), err)
	time.Sleep(5 * time.Second)
}

func getConfig(dataId string, group string) string {
	value, err := AuthTrueConfigClient.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  group})
	fmt.Printf("GetConfig: value %s, err %s\n", value, err)
	return value
}

func deleteConfig(dataId string, group string) {
	success, err := AuthTrueConfigClient.DeleteConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  group})
	fmt.Printf("DeleteConfig: success %s, err %s\n", strconv.FormatBool(success), err)
}

func Test_ConfigSearchAPI_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		publishConfig(dataId, DEFAULT_GROUP, content)
		time.Sleep(5 * time.Second)
		assert.Equal(t, content, getConfig(dataId, DEFAULT_GROUP))

		value, err := AuthFailConfigClient.GetConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  DEFAULT_GROUP})
		assert.Equal(t, "", value)
		assert.Nil(t, err)

		configPage, err := AuthFailConfigClient.SearchConfig(vo.SearchConfigParam{
			Search:   "accurate",
			DataId:   dataId,
			Group:    DEFAULT_GROUP,
			PageNo:   1,
			PageSize: 10,
		})
		assert.Empty(t, configPage)
		assert.NotNil(t, err)

		deleteConfig(dataId, DEFAULT_GROUP)
	})
	t.Run("TestNOAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		publishConfig(dataId, DEFAULT_GROUP, content)
		time.Sleep(5 * time.Second)
		assert.Equal(t, content, getConfig(dataId, DEFAULT_GROUP))

		value, err := AuthFailConfigClient.GetConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  DEFAULT_GROUP})
		assert.Equal(t, "", value)
		assert.Nil(t, err)

		configPage, err := AuthFailConfigClient.SearchConfig(vo.SearchConfigParam{
			Search:   "accurate",
			DataId:   dataId,
			Group:    DEFAULT_GROUP,
			PageNo:   1,
			PageSize: 10,
		})
		assert.Empty(t, configPage)
		assert.NotNil(t, err)

		deleteConfig(dataId, DEFAULT_GROUP)
	})
}

func Test_DeleteConfig_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		publishConfig(dataId, DEFAULT_GROUP, content)
		time.Sleep(5 * time.Second)

		success, err := AuthFailConfigClient.DeleteConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  DEFAULT_GROUP})
		assert.False(t, success)
		assert.Nil(t, err)

		value := getConfig(dataId, DEFAULT_GROUP)
		assert.Equal(t, content, value)

		deleteConfig(dataId, DEFAULT_GROUP)
		time.Sleep(5 * time.Second)
		value = getConfig(dataId, DEFAULT_GROUP)
		assert.Equal(t, "", value)
	})

	t.Run("TestNOAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var content string = "112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
		publishConfig(dataId, DEFAULT_GROUP, content)
		time.Sleep(5 * time.Second)

		success, err := NoAuthConfigClient.DeleteConfig(vo.ConfigParam{
			DataId: dataId,
			Group:  DEFAULT_GROUP})
		assert.False(t, success)
		assert.Nil(t, err)

		value := getConfig(dataId, DEFAULT_GROUP)
		assert.Equal(t, content, value)

		deleteConfig(dataId, DEFAULT_GROUP)
		time.Sleep(5 * time.Second)
		value = getConfig(dataId, DEFAULT_GROUP)
		assert.Equal(t, "", value)
	})
}

func Test_ListenConfig_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var group = DEFAULT_GROUP
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
		err1 := AuthFailConfigClient.ListenConfig(listenConfigParam)
		assert.Nil(t, err1)

		success, err := AuthTrueConfigClient.PublishConfig(vo.ConfigParam{
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
		// first listen is error ak, expect ""
		assert.Equal(t, "", listenContent)
		// second listen is success ak, expect not ""
		listenContent = ""
		err = AuthTrueConfigClient.ListenConfig(listenConfigParam)
		assert.Nil(t, err)
		timeout = 30
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
		assert.Equal(t, pubContent, listenContent)
	})

	t.Run("TestNoAKSK", func(t *testing.T) {
		var dataId string = RandDataId(10)
		var group = DEFAULT_GROUP
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
		err1 := NoAuthConfigClient.ListenConfig(listenConfigParam)
		assert.Nil(t, err1)

		success, err := AuthTrueConfigClient.PublishConfig(vo.ConfigParam{
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
		// first listen is error ak, expect ""
		assert.Equal(t, "", listenContent)
		// second listen is success ak, expect not ""
		listenContent = ""
		err = AuthTrueConfigClient.ListenConfig(listenConfigParam)
		assert.Nil(t, err)
		timeout = 30
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
		assert.Equal(t, pubContent, listenContent)
	})
}
