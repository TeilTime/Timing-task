1. 创建索引映射

PUT http://localhost:9200/indexcustomer
内容: mappings.indexcustomer.json

2. 查看索引情况
GET http://localhost:9200/indexcustomer
查看索引映射
GET http://localhost:9200/indexcustomer/_mapping

获取某个字段索引分词情况
GET http://localhost:9200/myindex/mytype/1/_termvectors?fields=title

试验分词器方法
https://blog.csdn.net/wangzhuo0978/article/details/79914849
#
GET http://localhost:9200/myindex/_analyze
{
  "tokenizer" : "standard",
  "filter": [{"type": "length", "min":1, "max":3 }],
  "text" : "this is a test"
}
