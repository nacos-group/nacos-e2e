# get run params
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
echo '====start test===='
cd ./python/nacospythontest &&\
python run_all.py; echo "done"
#pytest test/*_test.py --log-cli-level=DEBUG