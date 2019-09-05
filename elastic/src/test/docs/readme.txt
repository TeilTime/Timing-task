https://www.elastic.co/guide/cn/elasticsearch/guide/current/root-object.html#all-field

为避免ES启动报错，需要调整一下系统设置。首先打开/etc/security/limits.conf，添加如表 2‑7所示的内容
表 2‑7修改limits.conf文件
* soft nofile 65536
* hard nofile 131072
* soft nproc 2048
* hard nproc 4096
打开/etc/security/limits.d/90-nproc.conf，将“* soft nproc 1024”修改为“* soft nproc 4096”。除了这两个文件，还需要修改/etc/sysctl.conf文件，在文件末尾添加“vm.max_map_count=655360”。接着执行命令“sysctl -p”。


1. es分词器 配置
1.0 查看elasticsearch已经安装了什么插件
http://es的ip地址/_cat/plugins

1.1 ik分词器 IKAnalyzer.cfg.xml
1.2 hanlp分词器


2. es docker创建文件(docker-compose)
docker-compose.yml
// 创建并且启动容器
docker up -d

3. docker-compose命令
restart             Restart services
exec                Execute a command in a running container
logs                View output from containers
ps                  List containers

build               Build or rebuild services
bundle              Generate a Docker bundle from the Compose file
config              Validate and view the Compose file
create              Create services
down                Stop and remove containers, networks, images, and volumes
events              Receive real time events from containers
help                Get help on a command
images              List images
kill                Kill containers
pause               Pause services
port                Print the public port for a port binding
pull                Pull service images
push                Push service images
rm                  Remove stopped containers
run                 Run a one-off command
scale               Set number of containers for a service
start               Start services
stop                Stop services
top                 Display the running processes
unpause             Unpause services
up                  Create and start containers
version             Show the Docker-Compose version information

1. 进入容器
docker exec -it 容器名 /bin/bash
2. 查看容器日志
docker logs -f 容器名
3. 拷贝容器文件
docker cp 容器名:/路径 主机路径  (容器->主机)
docker cp 主机路径 容器名:/路径 (主机->容器)


=============== ES分词器 ==========================
Occasionally, it makes sense to use a different analyzer at index and search time. For instance, at index time we may want to index synonyms, eg for every occurrence of quick we also index fast, rapid and speedy. But at search time, we don’t need to search for all of these synonyms. Instead we can just look up the single word that the user has entered, be it quick, fast, rapid or speedy.

To enable this distinction, Elasticsearch also supports the index_analyzer and search_analyzer parameters, and analyzers named default_index and default_search.

Taking these extra parameters into account, the full sequence at index time really looks like this:

the index_analyzer defined in the field mapping, else
the analyzer defined in the field mapping, else
the analyzer defined in the _analyzer field of the document, else
the default index_analyzer for the type, which defaults to
the default analyzer for the type, which defaults to
the analyzer named default_index in the index settings, which defaults to
the analyzer named default in the index settings, which defaults to
the analyzer named default_index at node level, which defaults to
the analyzer named default at node level, which defaults to
the standard analyzer
And at search time:

the analyzer defined in the query itself, else
the search_analyzer defined in the field mapping, else
the analyzer defined in the field mapping, else
the default search_analyzer for the type, which defaults to
the default analyzer for the type, which defaults to
the analyzer named default_search in the index settings, which defaults to
the analyzer named default in the index settings, which defaults to
the analyzer named default_search at node level, which defaults to
the analyzer named default at node level, which defaults to
the standard analyzer
