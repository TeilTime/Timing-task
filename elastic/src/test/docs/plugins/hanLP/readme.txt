拷贝插件(analysis-hanlp-6.4.2.zip)和数据()文件到共享目录/storage/datas/elasticsearch/data

在docker-compose.yml中配置环境变量
      - HANLP_ROOT=/sharedir/hanlp-data/
      - TZ=Asia/Shanghai

进入es容器
$ docker exec -it es-node2 /bin/bash
$ cd /usr/share/elasticsearch/plugins

一、插件文件
$ mkdir analysis-hanlp
$ cd analysis-hanlp
$ unzip /sharedir/analysis-hanlp-6.4.2.zip

a) 修改版本号
修改 plugin-descriptor.properties 文件中的
$ vi plugin-descriptor.properties
elasticsearch.version=6.5.0

b) vi /usr/share/elasticsearch/config/jvm.options
-Djava.security.policy=file:///usr/share/elasticsearch/plugins/analysis-hanlp/plugin-security.policy

二、数据
在数据中
$ cd /storage/datas/elasticsearch/data
$ mkdir hanlp-data
$ cd hanlp-data
$ unzip 数据文件.zip


三、重新启动
docker-compose restart

附：HanLP-停用词表的使用示例
https://yq.aliyun.com/articles/703281?utm_content=g_1000059151
