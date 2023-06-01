echo 'modify version'
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.AspNetCore --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.Extensions.Configuration --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.YamlParser --version 1.3.4
dotnet add csharp/nacos-csharp-sdk-test/nacos-csharp-sdk-test.csproj package nacos-sdk-csharp.IniParser --version 1.3.4

cd csharp/nacos-csharp-sdk-test/
dotnet list package

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

#run case
dotnet test