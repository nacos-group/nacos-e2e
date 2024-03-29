cd ./cpp/nacoscpptest
mkdir ~/build
cd ~/build

echo "install googletest"
#git clone https://github.com/google/googletest.git
wget http://sarcheckin-test.oss-cn-hangzhou-zmf.aliyuncs.com/nacos-cplus-test/googletest.zip
unzip googletest.zip
cd googletest
cmake ../googletest
make
make install

echo "install curl"
yum -y -b current install libcurl-devel

echo "install nacos"
git clone -b v1.1.0 https://github.com/nacos-group/nacos-sdk-cpp.git
cd nacos-sdk-cpp
cmake .
make
make install

echo "export LD_LIBRARY_PATH=/usr/local/lib" >> ~/.bashrc
source ~/.bashrc

echo 'get ALL_IP'
echo $ALL_IP
if [[ "$ALL_IP" == "" ]]
then
ALL_IP=nacos-0:127.0.0.1
export ALL_IP
else
ALL_IP=$ALL_IP
export ALL_IP
fi
echo 'ALL_IP is ' $ALL_IP

echo "start to run"
cd ./cpp/nacoscpptest
g++ nacos_test.cpp -o nacos_test -lgtest -lpthread -I/usr/local/include/nacos/ -L/usr/local/lib/  -lnacos-cli
./nacos_test
