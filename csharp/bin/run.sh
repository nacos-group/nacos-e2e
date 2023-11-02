echo 'modify version'
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.AspNetCore --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.Extensions.Configuration --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.YamlParser --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.IniParser --version 1.3.4

cd csharp/nacos-csharp-sdk-test/
dotnet list package

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

#run case
dotnet test