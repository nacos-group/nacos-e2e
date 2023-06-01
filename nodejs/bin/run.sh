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
echo '====start test===='
cd ./nodejs/nacosnodejstest &&\
npm install &&\
mocha test