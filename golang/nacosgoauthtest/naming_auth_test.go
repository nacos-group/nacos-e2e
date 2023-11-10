package nacos_go_test

import (
	"fmt"
	. "github.com/nacos-group/nacos-e2e/golang/util"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	"strconv"
	"strings"
	"testing"
)

var AuthTrueNamingClient = CreateNamingClient(false)
var AuthFailNamingClient = CreateAuthFailNamingClient(false)
var NoAuthNamingClient = CreateNoAuthNamingClient(false)

func Test_RegisterInstance_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		success, err := AuthFailNamingClient.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))
		assert.Equal(t, false, success)
	})
	t.Run("TestNoAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		success, err := NoAuthNamingClient.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))
		assert.Equal(t, false, success)
	})
}

func Test_BatchRegisterInstance_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		var instances []vo.RegisterInstanceParam
		instance1 := vo.RegisterInstanceParam{
			Ip:        "127.0.0.1",
			Port:      8080,
			Weight:    1,
			Enable:    true,
			Healthy:   true,
			Ephemeral: true,
		}
		instance2 := vo.RegisterInstanceParam{
			Ip:        "127.0.0.1",
			Port:      8081,
			Weight:    1,
			Enable:    true,
			Healthy:   true,
			Ephemeral: true,
		}
		instances = append(instances, instance1)
		instances = append(instances, instance2)

		success, err := AuthFailNamingClient.BatchRegisterInstance(vo.BatchRegisterInstanceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			Instances:   instances,
		})
		assert.Equal(t, false, success)
		if !success {
			assert.True(t, err != nil)
			assert.True(t, strings.Contains(string(err.Error()), "403"))
		}
	})

	t.Run("TestNoAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		var instances []vo.RegisterInstanceParam
		instance1 := vo.RegisterInstanceParam{
			Ip:        "127.0.0.1",
			Port:      8080,
			Weight:    1,
			Enable:    true,
			Healthy:   true,
			Ephemeral: true,
		}
		instance2 := vo.RegisterInstanceParam{
			Ip:        "127.0.0.1",
			Port:      8081,
			Weight:    1,
			Enable:    true,
			Healthy:   true,
			Ephemeral: true,
		}
		instances = append(instances, instance1)
		instances = append(instances, instance2)

		success, err := NoAuthNamingClient.BatchRegisterInstance(vo.BatchRegisterInstanceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			Instances:   instances,
		})

		assert.Equal(t, false, success)
		if !success {
			assert.True(t, err != nil)
			assert.True(t, strings.Contains(string(err.Error()), "403"))
		}
	})
}

func registerInstance(serviceName string, ip string, port uint64) bool {
	success, err := AuthTrueNamingClient.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          ip,
		Port:        port,
		Ephemeral:   true,
	})
	fmt.Printf("RegisterInstance: success %s, err %s\n", strconv.FormatBool(success), err)
	return success
}

func registerInstance1(serviceName string, ip string, port uint64, ephemeral bool) bool {
	success, err := AuthTrueNamingClient.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          ip,
		Port:        port,
		Ephemeral:   ephemeral,
	})
	fmt.Printf("RegisterInstance: success %s, err %s\n", strconv.FormatBool(success), err)
	return success
}

func deregisterInstance(serviceName string, ip string, port uint64, groupName string) bool {
	success, err := AuthTrueNamingClient.DeregisterInstance(vo.DeregisterInstanceParam{
		ServiceName: serviceName,
		Ip:          ip,
		Port:        port,
		Ephemeral:   true,
		GroupName:   groupName,
	})
	fmt.Printf("DeregisterInstance: success %s, err %s\n", strconv.FormatBool(success), err)
	return success
}

func Test_DeregisterInstance_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance(serviceName, TEST_IP_1, TEST_PORT_8848)
		assert.True(t, successR)
		success, err := AuthFailNamingClient.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, false, success)
		if !success && nil != err {
			assert.NotNil(t, err)
			assert.True(t, strings.Contains(string(err.Error()), "403"))
		}
		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)

	})
	t.Run("TestNoAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance(serviceName, TEST_IP_1, TEST_PORT_8848)
		assert.True(t, successR)
		success, err := NoAuthNamingClient.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, false, success)
		if !success && nil != err {
			assert.NotNil(t, err)
			assert.True(t, strings.Contains(string(err.Error()), "403"))
		}
		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)
	})
}

func Test_UpdateInstance_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance(serviceName, TEST_IP_1, TEST_PORT_8848)
		assert.True(t, successR)
		success, err := AuthFailNamingClient.UpdateInstance(vo.UpdateInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))
		assert.Equal(t, false, success)
		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)
	})
	t.Run("TestNoAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance(serviceName, TEST_IP_1, TEST_PORT_8848)
		assert.True(t, successR)
		success, err := NoAuthNamingClient.UpdateInstance(vo.UpdateInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))
		assert.Equal(t, false, success)
		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)
	})
}

func Test_SearchInterface_AuthFail(t *testing.T) {
	t.Run("TestErrorAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance1(serviceName, TEST_IP_1, TEST_PORT_8848, false)
		assert.True(t, successR)

		result, err := AuthTrueNamingClient.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.True(t, result.Name == serviceName)
		assert.True(t, result.GroupName == DEFAULT_GROUP)

		result, err = AuthFailNamingClient.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.True(t, result.Name == "")
		assert.True(t, result.GroupName == "")
		//assert.Equal(t, nil, result)
		//assert.NotNil(t, err)
		//assert.True(t, strings.Contains(string(err.Error()), "403"))

		values, errs := AuthTrueNamingClient.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})

		values, errs = AuthFailNamingClient.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, values)
		assert.NotNil(t, errs)
		assert.True(t, strings.Contains(string(errs.Error()), "403"))

		results, err := AuthFailNamingClient.SelectInstances(vo.SelectInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.True(t, len(results) == 0)
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))

		res, err := AuthFailNamingClient.SelectOneHealthyInstance(vo.SelectOneHealthInstanceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Nil(t, res)
		assert.True(t, strings.Contains(string(err.Error()), "403"))

		allServices, errs := AuthFailNamingClient.GetAllServicesInfo(vo.GetAllServiceInfoParam{
			NameSpace: Ns,
			GroupName: DEFAULT_GROUP,
			PageNo:    1,
			PageSize:  100,
		})
		assert.Nil(t, allServices.Doms)
		assert.True(t, allServices.Count == 0)
		assert.True(t, strings.Contains(string(errs.Error()), "403"))

		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)
	})
	t.Run("TestNoAKSK", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR := registerInstance(serviceName, TEST_IP_1, TEST_PORT_8848)
		assert.True(t, successR)
		result, err := NoAuthNamingClient.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, result)
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))

		values, errs := NoAuthNamingClient.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, values)
		assert.NotNil(t, errs)
		assert.True(t, strings.Contains(string(errs.Error()), "403"))

		results, err := NoAuthNamingClient.SelectInstances(vo.SelectInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.True(t, len(results) == 0)
		assert.NotNil(t, err)
		assert.True(t, strings.Contains(string(err.Error()), "403"))

		res, err := NoAuthNamingClient.SelectOneHealthyInstance(vo.SelectOneHealthInstanceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Nil(t, res)
		assert.True(t, strings.Contains(string(err.Error()), "403"))

		allServices, errs := NoAuthNamingClient.GetAllServicesInfo(vo.GetAllServiceInfoParam{
			NameSpace: Ns,
			GroupName: DEFAULT_GROUP,
			PageNo:    1,
			PageSize:  100,
		})
		assert.Nil(t, allServices.Doms)
		assert.True(t, allServices.Count == 0)
		assert.True(t, strings.Contains(string(errs.Error()), "403"))

		successD := deregisterInstance(serviceName, TEST_IP_1, TEST_PORT_8848, DEFAULT_GROUP)
		assert.True(t, successD)
	})
}
