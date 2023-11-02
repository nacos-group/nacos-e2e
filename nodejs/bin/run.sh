# get run params
echo 'get serverList'
echo ALL_IP
if [[ "ALL_IP" == "" ]]
then
serverList=nacos-0:127.0.0.1
export serverList
else
serverList=ALL_IP
export serverList
fi
echo 'serverList is ' ALL_IP
echo '====start test===='
cd ./nodejs/nacosnodejstest &&\
npm install &&\
mocha test