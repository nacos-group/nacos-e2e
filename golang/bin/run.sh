# download go
wget -O go1.18.linux-amd64.tar.gz -q https://dl.google.com/go/go1.18.linux-amd64.tar.gz
tar -C /usr/local -xzf go1.18.linux-amd64.tar.gz
rm go1.18.linux-amd64.tar.gz

# set environment
echo "export GOROOT=/usr/local/go" >> ~/.bashrc
echo "export PATH=\$GOROOT/bin:\$PATH" >> ~/.bashrc
echo "export GOPATH=/home/tops/gopath" >> ~/.bashrc
echo "export GOPROXY=https://proxy.golang.com.cn,direct" >> ~/.bashrc
echo "export GO111MODULE=on" >> ~/.bashrc
echo "export GOSUMDB=off" >> ~/.bashrc
echo "export GONOSUMDB=*.corp.example.com,rsc.io/private" >> ~/.bashrc
source ~/.bashrc

# make gopath workspace
workspace=/home/tops/gopath/nacos-e2e
mkdir -p $workspace
mv * $workspace
cd $workspace/golang/nacosgotest

# get run params
echo 'get serverList'
echo $serverList
if [[ "$serverList" == "" ]]
then
serverList=127.0.0.1
export serverList
else
serverList=$serverList
export serverList
fi
echo 'serverList is ' $serverList

# get nacos-sdk-go in set version and run
cd ./golang/nacosgotest
go mod init nacos_go_test
go get github.com/nacos-group/nacos-sdk-go/v2/clients@v2.2.1
go mod tidy
go test . -timeout 2m  -v
ret=$?; if [[ $ret -ne 0 && $ret -ne 1 ]]; then exit $ret; fi
exit 0
