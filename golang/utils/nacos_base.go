package utils

import (
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/config_client"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/naming_client"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/util"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"io/ioutil"
	"math/rand"
	"net/http"
	"net/url"
	"os"
	"strconv"
	"strings"
	"time"
)

var serverList string = "127.0.0.1"
var curServer = ""
var Ns = ""

var Sk = "ak"

var Ak = "LTA"

const DEFAULT_GROUP = "DEFAULT_GROUP"

const TEST_IP_1 = "127.0.0.1"
const TEST_IP_2 = "127.0.0.2"
const TEST_PORT_8080 = 8080
const TEST_PORT_8848 uint64 = 8848

func init() {
	fmt.Println("init env value")

	var ALL_IP = os.Getenv("ALL_IP")
	fmt.Println("ALL_IP:", ALL_IP)

	pairs := strings.Split(ALL_IP, ",")
	addresses := []string{}
	oneValue := ""
	for _, pair := range pairs {
		pair = strings.TrimSpace(pair)
		if strings.HasPrefix(pair, "nacos-") {
			address := strings.Split(pair, ":")[1] + ":8848"
			addresses = append(addresses, address)
			oneValue = strings.Split(pair, ":")[1]
		}
	}

	serverList = strings.Join(addresses, ",")
	fmt.Println("serverList:", serverList)
	fmt.Println("one value:", oneValue)

	Ns = os.Getenv("namespace")
	Ak = os.Getenv("ACCESS_KEY")
	Sk = os.Getenv("SECRET_KEY")

	//If debugging locally, open below and configure env param
	//serverList = "127.0.0.1"
	//Ns = ""
	//Ak = "XXXXX"
	//Sk = "XXXXX"

	curServer = "http://" + oneValue + ":8848"
	fmt.Printf("init: serverList %s, curServer %s, ns %s, Ak %s, Sk %s\n", serverList, curServer, Ns, Ak, Sk)
}

func CreateConfigClient() config_client.IConfigClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithAccessKey(Ak),
		constant.WithSecretKey(Sk),
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	client, err := clients.NewConfigClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfig,
		},
	)

	if err != nil {
		panic("NewConfigClient failed!")
	}
	return client
}

func CreateAuthFailConfigClient() config_client.IConfigClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithAccessKey("abcdefd"),
		constant.WithSecretKey("hijklmn"),
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	client, err := clients.NewConfigClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfig,
		},
	)

	if err != nil {
		panic("NewConfigClient failed!")
	}
	return client
}

func CreateNoAuthConfigClient() config_client.IConfigClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	client, err := clients.NewConfigClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfig,
		},
	)

	if err != nil {
		panic("NewConfigClient failed!")
	}
	return client
}

func CreateNamingClient(updateCacheWhenEmpty bool) naming_client.INamingClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithAccessKey(Ak),
		constant.WithSecretKey(Sk),
		constant.WithUpdateCacheWhenEmpty(updateCacheWhenEmpty), // false protect, true unProtect
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	var clientParam = vo.NacosClientParam{
		ClientConfig:  &clientConfig,
		ServerConfigs: serverConfig,
	}

	client, err := clients.NewNamingClient(clientParam)
	if err != nil {
		panic("NewNamingClient failed!")
	}
	return client
}

func CreateAuthFailNamingClient(updateCacheWhenEmpty bool) naming_client.INamingClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithAccessKey("abcdefd"),
		constant.WithSecretKey("hijklmn"),
		constant.WithUpdateCacheWhenEmpty(updateCacheWhenEmpty), // false protect, true unProtect
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	var clientParam = vo.NacosClientParam{
		ClientConfig:  &clientConfig,
		ServerConfigs: serverConfig,
	}

	client, err := clients.NewNamingClient(clientParam)
	if err != nil {
		panic("NewNamingClient failed!")
	}
	return client
}

func CreateNoAuthNamingClient(updateCacheWhenEmpty bool) naming_client.INamingClient {
	var clientConfig = *constant.NewClientConfig(
		constant.WithNamespaceId(Ns),
		constant.WithUpdateCacheWhenEmpty(updateCacheWhenEmpty), // false protect, true unProtect
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)
	var serverConfig = []constant.ServerConfig{*constant.NewServerConfig(serverList, 8848, constant.WithContextPath("/nacos"))}

	var clientParam = vo.NacosClientParam{
		ClientConfig:  &clientConfig,
		ServerConfigs: serverConfig,
	}

	client, err := clients.NewNamingClient(clientParam)
	if err != nil {
		panic("NewNamingClient failed!")
	}
	return client
}

const letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

func RandStr(n int) string {
	b := make([]byte, n)
	for i := range b {
		b[i] = letters[rand.Intn(len(letters))]
	}
	return string(b)
}

func RandDataId(n int) string {
	return "gotest." + RandStr(n)
}

func RandCipherDataId(n int) string {
	return "cipher-gotest." + RandStr(n)
}

func RandServiceName(n int) string {
	return "gotest." + RandStr(n)
}

func HttpGet(path string, params map[string]string) string {
	client := &http.Client{}

	reqUrl := curServer + path
	fmt.Println("HttpGet reqUrl:", reqUrl)

	timestamp := fmt.Sprint(time.Now().UnixNano() / 1e6)
	signature := signWithhmacSHA1Encrypt(timestamp, Sk)
	params["Timestamp"] = timestamp
	params["Spas-AccessKey"] = Ak
	params["Spas-Signature"] = signature

	reqUrl += "?" + encodeParams(params)
	fmt.Println("HttpGet newReqUrl:", reqUrl)

	req, err := http.NewRequest("GET", reqUrl, nil)
	if err != nil {
		fmt.Println("Error creating HTTP request:", err)
		return ""
	}

	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("Error sending HTTP request:", err)
		return ""
	}

	if resp.StatusCode != 200 {
		fmt.Println("Error response:", resp.Status)
		return ""
	}

	responseBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading HTTP response:", err)
		return ""
	}
	defer resp.Body.Close()

	fmt.Println("HttpGet body:", string(responseBody))
	return string(responseBody)
}

func HttpPost(path string, params map[string]string) string {
	client := &http.Client{}

	reqUrl := curServer + path
	fmt.Println("HttpPost reqUrl:", reqUrl)

	signData := getNamingSignData(params)
	params["signature"] = signWithhmacSHA1Encrypt(signData, Sk)
	params["ak"] = Ak
	params["data"] = signData

	reqUrl += "?" + encodeParams(params)
	fmt.Println("HttpPost newReqUrl:", reqUrl)

	body := util.GetUrlFormedMap(params)
	req, err := http.NewRequest("POST", reqUrl, strings.NewReader(body))
	if err != nil {
		fmt.Println("Error creating HTTP request:", err)
		return ""
	}

	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("Error sending HTTP request:", err)
		return ""
	}

	if resp.StatusCode != 200 {
		fmt.Println("Error response:", resp.Status)
		return ""
	}

	responseBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading HTTP response:", err)
		return ""
	}
	defer resp.Body.Close()

	fmt.Println("HttpPost body:", string(responseBody))
	return string(responseBody)
}

func addSignParam(signType string, params map[string]string) map[string]string {
	if "config" == signType {
		timestamp := fmt.Sprint(time.Now().UnixNano() / 1e6)

		if group, hasGroup := params["group"]; hasGroup {
			signData := getConfigSignData(timestamp, Ns, group)
			params["Spas-Signature"] = signWithhmacSHA1Encrypt(signData, Sk)
			params["Timestamp"] = timestamp
			params["Spas-AccessKey"] = Ak
		} else {
			signature := signWithhmacSHA1Encrypt(timestamp, Sk)
			params["Timestamp"] = timestamp
			params["Spas-AccessKey"] = Ak
			params["Spas-Signature"] = signature
		}
	}

	if "naming" == signType {
		signData := getNamingSignData(params)
		params["signature"] = signWithhmacSHA1Encrypt(signData, Sk)
		params["ak"] = Ak
		params["data"] = signData
	}
	return params
}

func getNamingSignData(param map[string]string) string {
	var signData string
	timeStamp := strconv.FormatInt(time.Now().UnixNano()/1e6, 10)
	if serviceName, hasServiceName := param["serviceName"]; hasServiceName {
		if groupName, hasGroup := param["groupName"]; strings.Contains(serviceName, constant.SERVICE_INFO_SPLITER) || !hasGroup || groupName == "" {
			signData = timeStamp + constant.SERVICE_INFO_SPLITER + serviceName
		} else {
			signData = timeStamp + constant.SERVICE_INFO_SPLITER + util.GetGroupName(serviceName, groupName)
		}
	} else {
		signData = timeStamp
	}
	return signData
}

func getConfigSignData(timeStamp string, ns string, group string) string {
	var signData string

	resource := ""
	if len(ns) != 0 {
		resource = ns + group
	} else {
		resource = group
	}

	if resource == "" {
		signData = timeStamp
	} else {
		signData = resource + "+" + timeStamp
	}
	return signData
}

func signWithhmacSHA1Encrypt(encryptText, encryptKey string) string {
	//hmac ,use sha1
	key := []byte(encryptKey)
	mac := hmac.New(sha1.New, key)
	mac.Write([]byte(encryptText))

	return base64.StdEncoding.EncodeToString(mac.Sum(nil))
}

func encodeParams(params map[string]string) string {
	reqParams := url.Values{}
	for key, val := range params {
		reqParams.Set(key, val)
	}
	return reqParams.Encode()
}

func Contains(arr []string, str string) bool {
	for _, v := range arr {
		if strings.Contains(v, str) {
			return true
		}
	}
	return false
}

func ToJsonString(object interface{}) string {
	js, _ := json.Marshal(object)
	return string(js)
}
