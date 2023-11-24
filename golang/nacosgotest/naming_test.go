package nacos_go_test

import (
	"encoding/json"
	"fmt"
	"github.com/nacos-group/nacos-sdk-go/v2/model"
	"github.com/nacos-group/nacos-sdk-go/v2/util"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/stretchr/testify/assert"
	"log"
	. "nacos_go_test/utils"
	"strconv"
	"testing"
	"time"
)

func Test_GetService_GetAllServicesInfo_ClusterName(t *testing.T) {
	client := CreateNamingClient(false)
	t.Run("TestWithoutClusterName", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		result, err := client.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})

		assert.NotNil(t, result)
		assert.Nil(t, err)
		assert.Equal(t, serviceName, result.Name)

		values, errs := client.GetAllServicesInfo(vo.GetAllServiceInfoParam{
			NameSpace: Ns,
			GroupName: DEFAULT_GROUP,
			PageNo:    1,
			PageSize:  20,
		})

		assert.NotNil(t, values.Doms)
		assert.Nil(t, errs)
		assert.True(t, Contains(values.Doms, serviceName))
	})
	t.Run("TestWithClusterName", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		var clusterName string = "test"
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			ClusterName: clusterName,
			Ephemeral:   false,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		result, err := client.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			Clusters:    []string{clusterName},
			GroupName:   DEFAULT_GROUP,
		})

		assert.NotNil(t, result)
		assert.Nil(t, err)
		assert.Equal(t, serviceName, result.Name)

		result1, err1 := client.GetService(vo.GetServiceParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})

		assert.NotNil(t, result1)
		assert.Nil(t, err1)
		assert.Equal(t, serviceName, result1.Name)
	})
}

func Test_SelectInstances_SelectAllInstances(t *testing.T) {
	client := CreateNamingClient(false)
	t.Run("TestEphemeralTrue", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		fmt.Println("service name: " + serviceName)
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(10 * time.Second)
		select {
		case <-tempTimer.C:
		}

		results, err := client.SelectInstances(vo.SelectInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			HealthyOnly: true,
		})
		fmt.Println("[SelectInstances results] " + ToJsonString(results))
		assert.NotNil(t, results)
		assert.Nil(t, err)
		assert.True(t, len(results) > 0)
		for i, r := range results {
			fmt.Println("[SelectInstances] i:", i, ": "+ToJsonString(r))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, r.ServiceName)
			assert.Equal(t, TEST_IP_1, r.Ip)
			assert.True(t, TEST_PORT_8848 == r.Port)
		}

		values, errs := client.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		fmt.Println("[SelectAllInstances values] " + ToJsonString(values))
		assert.NotNil(t, values)
		assert.Nil(t, errs)
		assert.True(t, len(values) > 0)
		for i, v := range values {
			fmt.Println("[SelectAllInstances] i:", i, ": "+ToJsonString(v))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, v.ServiceName)
			assert.Equal(t, TEST_IP_1, v.Ip)
			assert.Equal(t, TEST_PORT_8848, v.Port)
		}
	})
	t.Run("TestEphemeralFalse", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   false,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(10 * time.Second)
		select {
		case <-tempTimer.C:
		}
		results, err := client.SelectInstances(vo.SelectInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			HealthyOnly: true,
		})
		fmt.Println("[SelectInstances results] " + ToJsonString(results))
		assert.NotNil(t, results)
		assert.Nil(t, err)
		assert.True(t, len(results) > 0)
		for i, r := range results {
			fmt.Println("[SelectInstances]i:", i, ": "+ToJsonString(r))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, r.ServiceName)
			assert.Equal(t, TEST_IP_1, r.Ip)
			assert.Equal(t, TEST_PORT_8848, r.Port)
		}

		values, errs := client.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		fmt.Println("[SelectAllInstances values] " + ToJsonString(values))
		assert.NotNil(t, values)
		assert.Nil(t, errs)
		assert.True(t, len(values) > 0)
		for i, v := range values {
			fmt.Println("[SelectAllInstances]i:", i, ": "+ToJsonString(v))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, v.ServiceName)
			assert.Equal(t, TEST_IP_1, v.Ip)
			assert.Equal(t, TEST_PORT_8848, v.Port)
		}
	})
}

func Test_RegisterInstance_DeregisterInstance_GroupName(t *testing.T) {
	client := CreateNamingClient(false)
	t.Run("TestWithoutGroupNameAndEphemeralTrue", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR, errR := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   true,
		})
		assert.Equal(t, nil, errR)
		assert.Equal(t, true, successR)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Ephemeral:   true,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)
	})

	t.Run("TestWithGroupNameAndEphemeralFalse", func(t *testing.T) {
		var serviceName string = RandServiceName(10)
		successR, errR := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Enable:      true,
			Healthy:     true,
			GroupName:   "test_group",
			Ephemeral:   false,
		})
		assert.Equal(t, nil, errR)
		assert.Equal(t, true, successR)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			GroupName:   "test_group",
			Ephemeral:   false,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)
	})
}

func Test_RegisterInstance_DeregisterInstance_OpenProtectOrNOT(t *testing.T) {
	t.Run("TestOpenProtect", func(t *testing.T) {
		client := CreateNamingClient(false)
		var serviceName string = RandServiceName(10)
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Healthy:     true,
			Enable:      true,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		values, errs := client.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, values)
		assert.Nil(t, errs)
		assert.True(t, len(values) > 0)
		for i, v := range values {
			fmt.Println("i:", i, ": "+ToJsonString(v))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, v.ServiceName)
			assert.Equal(t, TEST_IP_1, v.Ip)
			assert.Equal(t, TEST_PORT_8848, v.Port)
		}

		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)

		timeout := 120
		start := time.Now()
		for {
			values, errs = client.SelectAllInstances(vo.SelectAllInstancesParam{
				ServiceName: serviceName,
				GroupName:   DEFAULT_GROUP,
			})
			if len(values) == 0 {
				fmt.Printf("Current SelectInstances is empty\n")
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			fmt.Println("Wait 5s……")
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(values) != 0)
	})

	t.Run("TestCloseProtect", func(t *testing.T) {
		client := CreateNamingClient(true)
		var serviceName string = RandServiceName(10)
		fmt.Println("service name: " + serviceName)
		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Healthy:     true,
			Enable:      true,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		values, errs := client.SelectAllInstances(vo.SelectAllInstancesParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
		})
		assert.NotNil(t, values)
		assert.Nil(t, errs)
		assert.True(t, len(values) > 0)
		for i, v := range values {
			fmt.Println("i:", i, ": "+ToJsonString(v))
			assert.Equal(t, DEFAULT_GROUP+"@@"+serviceName, v.ServiceName)
			assert.Equal(t, TEST_IP_1, v.Ip)
			assert.Equal(t, TEST_PORT_8848, v.Port)
		}

		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			GroupName:   DEFAULT_GROUP,
			Ephemeral:   true,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)

		timeout := 240
		start := time.Now()
		for {
			values, errs = client.SelectAllInstances(vo.SelectAllInstancesParam{
				ServiceName: serviceName,
				GroupName:   DEFAULT_GROUP,
			})
			fmt.Println("CurrentService:" + ToJsonString(values))
			if len(values) == 0 {
				fmt.Printf("Current SelectInstances is empty\n")
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			fmt.Println("Wait 5s……")
			//time.Sleep(5 * time.Second)
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.Nil(t, err)
		assert.True(t, len(values) == 0)
	})
}

func Test_RegisterInstance_EphemeralTrueAndFalse_AtTheSameTime(t *testing.T) {
	client := CreateNamingClient(false)
	var serviceName string = RandServiceName(10)

	params := map[string]string{}
	params["namespaceId"] = Ns
	params["serviceName"] = util.GetGroupName(serviceName, DEFAULT_GROUP)
	params["groupName"] = DEFAULT_GROUP
	params["ip"] = TEST_IP_1
	params["port"] = strconv.Itoa(int(TEST_PORT_8848))
	params["weight"] = strconv.FormatFloat(1, 'f', -1, 64)
	params["enable"] = strconv.FormatBool(true)
	params["healthy"] = strconv.FormatBool(true)
	metadata := map[string]string{"key": RandStr(100)}
	params["metadata"] = util.ToJsonString(metadata)
	params["ephemeral"] = strconv.FormatBool(false)

	body := HttpPost("/nacos/v1/ns/instance", params)
	assert.Equal(t, "ok", body)

	tempTimer := time.NewTimer(5 * time.Second)
	select {
	case <-tempTimer.C:
	}
	success, err := client.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.False(t, success)
	assert.NotNil(t, err)

	successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Ephemeral:   true,
	})
	assert.Equal(t, nil, errD)
	assert.Equal(t, true, successD)

	successD, errD = client.DeregisterInstance(vo.DeregisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Ephemeral:   false,
	})
	assert.Equal(t, nil, errD)
	assert.Equal(t, true, successD)
}

func Test_Subscribe(t *testing.T) {
	var serviceName string = RandServiceName(10)
	var listenService []model.Instance
	client := CreateNamingClient(false)

	subscribeParam := &vo.SubscribeParam{
		ServiceName: serviceName,
		GroupName:   DEFAULT_GROUP,
		SubscribeCallback: func(services []model.Instance, err error) {
			if err != nil {
				log.Printf("subscribe error:%+v", err)
				return
			}
			assert.Nil(t, err)
			listenService = services
			for _, service := range services {
				log.Printf("subscribe service:%+v", service)
			}
			js, _ := json.Marshal(services)
			fmt.Printf("\n\n callback return services:%s \n\n", string(js))
		},
	}
	err := client.Subscribe(subscribeParam)
	assert.Nil(t, err)

	success, err := client.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Healthy:     true,
		Enable:      true,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.Equal(t, nil, err)
	assert.Equal(t, true, success)

	timeout := 60
	start := time.Now()
	for {
		if len(listenService) != 0 {
			js, _ := json.Marshal(listenService)
			fmt.Printf("Current service: %s\n", string(js))
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}
	}
	assert.True(t, len(listenService) != 0)
}

func Test_Subscribe_DeregisterInstance_OpenProtectOrNOT(t *testing.T) {
	t.Run("TestOpenProtect", func(t *testing.T) {
		client := CreateNamingClient(false)
		var serviceName string = RandServiceName(10)
		var listenService []model.Instance

		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Healthy:     true,
			Enable:      true,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		subscribeParam := &vo.SubscribeParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			SubscribeCallback: func(services []model.Instance, err error) {
				if err != nil {
					log.Printf("subscribe error:%+v", err)
					return
				}
				assert.Nil(t, err)
				listenService = services
				for _, service := range services {
					log.Printf("subscribe service:%+v", service)
				}
				js, _ := json.Marshal(services)
				fmt.Printf("\n\n callback return services:%s \n\n", string(js))
			},
		}
		errS := client.Subscribe(subscribeParam)
		assert.Nil(t, errS)

		// receive the register instance
		timeout := 30
		start := time.Now()
		for {
			if len(listenService) != 0 {
				js, _ := json.Marshal(listenService)
				fmt.Printf("Current service: %s\n", string(js))
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(listenService) != 0)

		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)

		//expect not receive the deregister instance
		timeout = 30
		start = time.Now()
		for {
			if len(listenService) == 0 {
				js, _ := json.Marshal(listenService)
				fmt.Printf("Current service: %s\n", string(js))
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(listenService) != 0)
	})

	t.Run("TestCloseProtect", func(t *testing.T) {
		client := CreateNamingClient(true)
		var serviceName string = RandServiceName(10)
		var listenService []model.Instance

		success, err := client.RegisterInstance(vo.RegisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Healthy:     true,
			Enable:      true,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}

		subscribeParam := &vo.SubscribeParam{
			ServiceName: serviceName,
			GroupName:   DEFAULT_GROUP,
			SubscribeCallback: func(services []model.Instance, err error) {
				if err != nil {
					log.Printf("subscribe error:%+v", err)
					return
				}
				assert.Nil(t, err)
				listenService = services
				for _, service := range services {
					log.Printf("subscribe service:%+v", service)
				}
				js, _ := json.Marshal(services)
				fmt.Printf("\n\n callback return services:%s \n\n", string(js))
			},
		}
		errS := client.Subscribe(subscribeParam)
		assert.Nil(t, errS)

		// receive the register instance
		timeout := 30
		start := time.Now()
		for {
			if len(listenService) != 0 {
				js, _ := json.Marshal(listenService)
				fmt.Printf("Current service: %s\n", string(js))
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(listenService) != 0)

		//reset and deregister
		listenService = []model.Instance{}
		successD, errD := client.DeregisterInstance(vo.DeregisterInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
		})
		assert.Equal(t, nil, errD)
		assert.Equal(t, true, successD)

		//expect receive the deregister instance
		timeout = 30
		start = time.Now()
		for {
			if len(listenService) == 0 {
				js, _ := json.Marshal(listenService)
				fmt.Printf("Current service: %s\n", string(js))
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(listenService) == 0)
	})
}

func Test_Subscribe_And_Unsubscribe(t *testing.T) {
	var serviceName string = RandServiceName(10)
	var listenService []model.Instance
	client := CreateNamingClient(false)

	subscribeParam := &vo.SubscribeParam{
		ServiceName: serviceName,
		GroupName:   DEFAULT_GROUP,
		SubscribeCallback: func(services []model.Instance, err error) {
			if err != nil {
				log.Printf("subscribe error:%+v", err)
				return
			}
			assert.Nil(t, err)
			listenService = services
			for _, service := range services {
				log.Printf("subscribe service:%+v", service)
			}
			js, _ := json.Marshal(services)
			fmt.Printf("\n\n callback return services:%s \n\n", string(js))
		},
	}
	err := client.Subscribe(subscribeParam)
	assert.Nil(t, err)

	success, err := client.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Healthy:     true,
		Enable:      true,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.Equal(t, nil, err)
	assert.Equal(t, true, success)

	timeout := 30
	start := time.Now()
	for {
		if len(listenService) != 0 {
			js, _ := json.Marshal(listenService)
			fmt.Printf("Current service: %s\n", string(js))
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}
	}
	assert.True(t, len(listenService) != 0)

	listenService = []model.Instance{}
	errU := client.Unsubscribe(subscribeParam)
	assert.Nil(t, errU)

	// expect unsubscribe after update
	success, err = client.UpdateInstance(vo.UpdateInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8080,
		Healthy:     true,
		Enable:      true,
		Weight:      2,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.Equal(t, nil, err)
	assert.Equal(t, true, success)

	timeout = 30
	start = time.Now()
	for {
		if len(listenService) != 0 {
			js, _ := json.Marshal(listenService)
			fmt.Printf("Current service: %s\n", string(js))
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}
	}
	assert.True(t, len(listenService) == 0)

	listenService = []model.Instance{}
	errS := client.Subscribe(subscribeParam)
	assert.Nil(t, errS)

	// expect subscribe after update
	success, err = client.UpdateInstance(vo.UpdateInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Healthy:     true,
		Enable:      true,
		Weight:      100,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.Equal(t, nil, err)
	assert.Equal(t, true, success)

	timeout = 30
	start = time.Now()
	for {
		if len(listenService) != 0 {
			js, _ := json.Marshal(listenService)
			fmt.Printf("Current service: %s\n", string(js))
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}
	}
	assert.True(t, len(listenService) != 0)

}

func Test_ReloadCurrent_And_Subscribe(t *testing.T) {
	var serviceName string = RandServiceName(10)
	var listenService []model.Instance
	client := CreateNamingClient(false)

	subscribeParam := &vo.SubscribeParam{
		ServiceName: serviceName,
		GroupName:   DEFAULT_GROUP,
		SubscribeCallback: func(services []model.Instance, err error) {
			if err != nil {
				log.Printf("subscribe error:%+v", err)
				return
			}
			assert.Nil(t, err)
			listenService = services
			for _, service := range services {
				log.Printf("subscribe service:%+v", service)
			}
			js, _ := json.Marshal(services)
			fmt.Printf("\n\n callback return services:%s \n\n", string(js))
		},
	}
	err := client.Subscribe(subscribeParam)
	assert.Nil(t, err)

	success, err := client.RegisterInstance(vo.RegisterInstanceParam{
		ServiceName: serviceName,
		Ip:          TEST_IP_1,
		Port:        TEST_PORT_8848,
		Healthy:     true,
		Enable:      true,
		Ephemeral:   true,
		GroupName:   DEFAULT_GROUP,
	})
	assert.Equal(t, nil, err)
	assert.Equal(t, true, success)

	timeout := 30
	start := time.Now()
	for {
		if len(listenService) != 0 {
			js, _ := json.Marshal(listenService)
			fmt.Printf("Current service: %s\n", string(js))
			break
		}
		if time.Since(start).Seconds() > float64(timeout) {
			fmt.Println("Timeout exceeded. Exiting loop.")
			break
		}
		tempTimer := time.NewTimer(5 * time.Second)
		select {
		case <-tempTimer.C:
		}
	}
	assert.True(t, len(listenService) != 0)

	listenService = []model.Instance{}
	params := make(map[string]string)
	params["count"] = "0"
	// make server break connect, it should be effect in singleton, but may not be effect in cluster
	body := HttpGet("/nacos/v2/core/loader/reloadCurrent", params)
	if body == "" {
		assert.True(t, false)
	} else {
		assert.Equal(t, "success", body)
		// expect subscribe after update
		success, err = client.UpdateInstance(vo.UpdateInstanceParam{
			ServiceName: serviceName,
			Ip:          TEST_IP_1,
			Port:        TEST_PORT_8848,
			Healthy:     true,
			Enable:      true,
			Weight:      100,
			Ephemeral:   true,
			GroupName:   DEFAULT_GROUP,
		})
		assert.Equal(t, nil, err)
		assert.Equal(t, true, success)

		timeout = 30
		start = time.Now()
		for {
			if len(listenService) != 0 {
				js, _ := json.Marshal(listenService)
				fmt.Printf("Current service: %s\n", string(js))
				break
			}
			if time.Since(start).Seconds() > float64(timeout) {
				fmt.Println("Timeout exceeded. Exiting loop.")
				break
			}
			tempTimer := time.NewTimer(5 * time.Second)
			select {
			case <-tempTimer.C:
			}
		}
		assert.True(t, len(listenService) != 0)
	}
}
