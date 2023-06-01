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
cd ./python/nacospythontest &&\
python run_all.py; echo "done"
#pytest test/*_test.py --log-cli-level=DEBUG