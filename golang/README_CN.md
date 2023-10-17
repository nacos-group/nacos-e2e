## golang
Nacos go 客户端测试用例，包含对Nacos go 客户端的接口、场景、接口鉴权测试用例

## 使用限制

支持Go>=v1.15版本

支持Nacos>2.x版本

## 安装 和 执行

通过以下命令安装SDK，并执行所有用例 或 执行指定用例：

```sh
cd ./golang

go mod init nacos_go_test

go get github.com/nacos-group/nacos-sdk-go/v2/clients@v2.2.2

cat go.mod

go mod tidy

gotestsum --junitfile ./TEST-report.xml

# gotestsum --junitfile ./TEST-report.xml ./nacosgotest/config_test.go  ./nacosgotest/naming_test.go
```

## 本地调试 

打开 ./golang/util 目录下的 nacos_base.go 文件中 init() 函数的注释，并设置测试的Nacos服务端参数

```sh
//  If debugging locally, open below and configure env param
// 	serverList = "127.0.0.1"
// 	Ns = ""
// 	Ak = "XXXXX"
// 	Sk = "XXXXX"
```

通过Golang IDE或者其他go编译器执行单个或多个用例