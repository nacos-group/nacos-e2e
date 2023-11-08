NACOS_SDK_CPP := nacos-sdk-cpp
NACOS_SDK_CPP_VERSION := v1.1.0

install:
	yum -y install libcurl-devel
	git clone -b ${NACOS_SDK_CPP_VERSION} https://github.com/nacos-group/${NACOS_SDK_CPP}.git
	cd /root/code/cpp/nacos-sdk-cpp && cmake . && make && make install
	echo "export LD_LIBRARY_PATH=/usr/local/lib" >> ~/.bashrc  && source ~/.bashrc




