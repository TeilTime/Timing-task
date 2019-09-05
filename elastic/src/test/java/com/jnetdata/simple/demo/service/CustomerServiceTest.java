package com.jnetdata.simple.demo.service;

import com.jnetdata.simple.BaseElasticSearchJUnit;
import com.jnetdata.simple.demo.model.Customer;
import com.jnetdata.simple.demo.model.CustomerIndex;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thenicesys.data.elasticsearch.config.ElasticsearchConfig;
import org.thenicesys.data.elasticsearch.service.ElasticsearchService;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.thirdparty.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomerServiceTest extends BaseElasticSearchJUnit {

    @Autowired
    private ElasticsearchService elasticsearchService;

    public void clearCustomers() {
        doDataClear(CustomerIndex.index, Customer.class);
    }

    /**
     * 创建索引并建立mapping
     * @throws IOException
     */
    @Ignore
    public void createIndexAndMapping() throws IOException {

        elasticsearchService.deleteIndex(CustomerIndex.index+"");

        String json = FileUtils.readFileToString(new File(getClassPath()+"/docs/mappings.indexcustomer.json"), "utf-8");
        String url = parseEsUrl(elasticsearchConfig, CustomerIndex.index);
        HttpPut httpPut = new HttpPut(url);
        CloseableHttpClient client = HttpClients.createDefault();
        //解决中文乱码问题
        StringEntity entity = new StringEntity(json,"utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPut.setEntity(entity);
        HttpResponse resp = client.execute(httpPut);
        System.out.println(resp);
    }

    /**
     * 新增文档
     */
    @Test
    @SneakyThrows
    public void addDocuments_1() {

        clearCustomers();

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            sourceBuilder.query(QueryBuilders.matchAllQuery());
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(), listPair.getValue());

        List<Customer> customers = createCustomers();
        elasticsearchService.addDocuments(CustomerIndex.index, customers);

        listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            sourceBuilder.query(QueryBuilders.matchAllQuery());
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(1001, 1002), listPair.getValue());

    }

    /**
     * 根据id查询文档 getDocument
     */
    @Test
    @SneakyThrows
    public void getDocument_1() {
        initCustomers();
        Customer customer = elasticsearchService.getDocument(CustomerIndex.index,"1001", Customer.class);
        ReflectionAssert.assertPropertyReflectionEquals("id", 1001, customer);
    }

    /**
     * term查询
     * 查询条件：id=1001
     */
    @Test
    @SneakyThrows
    public void term_1() {
        initCustomers();
        doSearchTermQuery(new Pair<>("id","1001"), createPair("id",1001L));
    }

    /**
     * term查询
     * 查询条件：not in
     */
    @Test
    @SneakyThrows
    public void term_11() {

        initCustomers();

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.boolQuery()
                    .mustNot(
                            QueryBuilders.termsQuery("id", Arrays.asList(1001L)));
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(1002L), listPair.getValue());

        listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.boolQuery()
                    .mustNot(
                            QueryBuilders.termsQuery("id", Arrays.asList(1001L, 1002L)));
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(), listPair.getValue());

    }


    /**
     * term查询
     *  1. name=张大豪哥
     *  2. name=张大豪哥力士
     */
    @Test
    @SneakyThrows
    public void term_2() {
        initCustomers();
        doSearchTermQuery(new Pair<>("name","张大豪哥"), createPair("name","张大豪哥"));
        doSearchTermQuery(new Pair<>("name","张大豪哥力士"), createPair("name","张大豪哥力士"));
    }

    /**
     * 名字mapping为keyword，保存文档时索引部分词
     * name=张大豪查询不到结果
     */
    @Test
    @SneakyThrows
    public void term_3() {
        initCustomers();
        doSearchTermQuery(new Pair<>("name","张大豪"), createEmptyPair());
    }

    /**
     * term查询
     * name=张大豪哥 张大豪哥力士
     */
    @Test
    @SneakyThrows
    public void term_4() {
        initCustomers();
        doSearchTermQuery(new Pair<>("name","张大豪哥 张大豪哥力士"), createEmptyPair());
    }

    /**
     * name=张大豪哥,张大豪哥力士
     */
    @Test
    @SneakyThrows
    public void term_5() {
        initCustomers();
        doSearchTermQuery(new Pair<>("name","张大豪哥,张大豪哥力士"), createEmptyPair());
    }

    /**
     * address mapping为text，保存文档时索引会根据设定的分词器分词
     * address=香港铜锣湾告士打道262号中粮大厦32楼
     * address=香港铜锣湾
     * address=香港 [保存文档时拆词有香港，能查询到]
     */
    @Test
    @SneakyThrows
    public void term_6() {
        initCustomers();
        doSearchTermQuery(new Pair<>("address","香港铜锣湾告士打道262号中粮大厦32楼"), createEmptyPair());
        doSearchTermQuery(new Pair<>("address","香港铜锣湾"), createEmptyPair());
        doSearchTermQuery(new Pair<>("address","香港"), createPair("id",1002));
    }

    /**
     * categorys.tag mapping为自定义按逗号分词，比如id=1001数据customer.setCategorys("大数据开发,UI设计,计算机图形");
     * 保存索引时categorys.tag拆分成3条记录 1）大数据开发 2）UI设计 3）计算机图形
     * categorys.tag=计算机图形 =>1001
     * categorys.tag=UI设计,大数据开发 查询不到结果
     */
    @Test
    @SneakyThrows
    public void term_7() {
        initCustomers();
        doSearchTermQuery(new Pair<>("categorys.tag","计算机图形"), createPair("id",1001L));
        doSearchTermQuery(new Pair<>("categorys.tag","计算机图形 "), createPair("id"));
        doSearchTermQuery(new Pair<>("categorys.tag","可视化"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("categorys.tag","大数据开发"), createPair("id",1001L,1002L));
        doSearchTermQuery(new Pair<>("categorys.tag","UI设计,大数据开发"), createEmptyPair());
        doSearchTermQuery(new Pair<>("categorys.tag","计算机图形,可视化"), createEmptyPair());
        doSearchTermQuery(new Pair<>("categorys.tag",Arrays.asList("UI设计","大数据开发")), createEmptyPair());
    }

    /**
     * term查询
     */
    @Test
    @SneakyThrows
    public void term_8() {
        initCustomers();

        // description mapping为text(ik分词)
        doSearchTermQuery(new Pair<>("description","移动硬盘"), createPair("id",1001L));
        doSearchTermQuery(new Pair<>("description","知识"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("description","外存"), createPair("id",1001L));
        doSearchTermQuery(new Pair<>("description","存储"), createPair("id",1001L));

        // description.st mapping为text(标准分词器[按字拆分])
        doSearchTermQuery(new Pair<>("description.st","知"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("description.st","识"), createPair("id",1002L));

        doSearchTermQuery(new Pair<>("description.ik1","知识"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("description.ik1","社区"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("description.ik1","知识社区"), createEmptyPair());
        doSearchTermQuery(new Pair<>("description.ik1","外存"), createEmptyPair());
    }


    @Test
    @SneakyThrows
    public void term_9() {
        initCustomers();

        // 查询 leader=true
        doSearchTermQuery(new Pair<>("leader","true"), createPair("id",1002L));
        doSearchTermQuery(new Pair<>("leader","false"), createPair("id",1001L));
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void term_9_exception() {
        initCustomers();
        doSearchTermQuery(new Pair<>("leader",""), createPair("id",1002L));
    }

    /**
     * 多term查询
     */
    @Test
    @SneakyThrows
    public void terms_1() {
        initCustomers();

        // 查询 categorys.tag in ("计算机图形", "UI设计", "大数据开发")
        doSearchTermsQuery(new Pair<>("categorys.tag",Arrays.asList("计算机图形","UI设计","大数据开发")), createPair("id",1001L,1002L));
        // 查询 categorys.tag="UI设计"
        doSearchTermsQuery(new Pair<>("categorys.tag",Arrays.asList("UI设计")), createPair("id",1001L,1002L));

        doSearchTermsQuery(new Pair<>("categorys.tag",Arrays.asList("可视化")), createPair("id",1002L));
        doSearchTermsQuery(new Pair<>("categorys.tag",Arrays.asList("可视化","UI设计")), createPair("id",1001L,1002L)); // OR
    }

    @Test
    @SneakyThrows
    public void match_1() {
        initCustomers();

        // 查询 id matchQty "1001"
        doSearchMatchQuery(new Pair<>("id", "1001"), createPair("id",1001L));

        // 查询 name(mapping为keyword) matchQty 张大豪哥
        doSearchMatchQuery(new Pair<>("name", "张大豪哥"), createPair("id",1001L));
        doSearchMatchQuery(new Pair<>("name", "张大豪"), createEmptyPair());

        doSearchMatchQuery(new Pair<>("categorys.tag", "大数据开发"), createPair("id",1001L,1002L));
        doSearchMatchQuery(new Pair<>("categorys.tag", "大数据"), createEmptyPair());
        doSearchMatchQuery(new Pair<>("categorys.tag", "UI设计,计算机图形"), createPair("id",1001L,1002L));
        doSearchMatchQuery(new Pair<>("categorys.ik1", "大数据"), createEmptyPair()); // 查询"大", "数据", 确实一个都匹配不了
        doSearchMatchQuery(new Pair<>("categorys.ik1", "大数据开发"), createEmptyPair()); // 查询"大", "数据", "开发", 确实一个都匹配不了
        doSearchMatchQuery(new Pair<>("categorys", "大数据"), createPair("id",1001L,1002L));
    }

    @Test
    @SneakyThrows
    public void matchPhrase_1() {
        initCustomers();


        doSearchmatchPhraseQuery(new Pair<>("id", "1001"), createPair("id",1001L));
        doSearchmatchPhraseQuery(new Pair<>("name", "张大豪哥"), createPair("id",1001L));
        doSearchmatchPhraseQuery(new Pair<>("categorys.tag", "大数据开发"), createPair("id",1001L,1002L));
        doSearchmatchPhraseQuery(new Pair<>("categorys.tag", "大数据"), createEmptyPair());
        doSearchmatchPhraseQuery(new Pair<>("categorys", "计算机图形"), createEmptyPair());
    }

    @Test
    @SneakyThrows
    public void multiMatchQuery_1() {
        initCustomers();

        doSearchmultiMatchQuery(new Pair<>(new String[]{"id","name"}, "1001"), createPair("id",1001L));
        doSearchmultiMatchQuery(new Pair<>(new String[]{"name","categorys","description","address"}, "张大豪哥"), createPair("id",1002L,1001L));
        doSearchmultiMatchQuery(new Pair<>(new String[]{"name","categorys","description"}, "张大豪哥"), createPair("id",1001L,1002L));
        doSearchmultiMatchQuery(new Pair<>(new String[]{"name","categorys"}, "张大豪哥"), createPair("id",1001L));
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void multiMatchQuery_1_exception() {
        initCustomers();
        try {
            doSearchmultiMatchQuery(new Pair<>(new String[]{"id", "name"}, "中国"), createPair("id",1001L));
            Assert.fail("不能到这里");
        }catch(Exception e){
            throw e;
        }
    }

    @Test
    @SneakyThrows
    public void queryString_1() {
        initCustomers();
        // _all字段: 默认分析器为标准分析器
        doSearchqueryString("1001", createPair("id",1001L));
        doSearchqueryString("光盘", createPair("id",1001L));
        doSearchqueryString("开发", createPair("id",1001L,1002L));
        doSearchqueryString("哥", createPair("id",1002L)); // 注意这个
        doSearchqueryString("张大豪哥", createPair("id",1002L,1001L));
    }

    /**
     *
     * @param strQuery 查询条件
     * @param expect 期望 <属性,属性值List>
     */
    @SneakyThrows
    private void doSearchTermQuery(Pair<String,Object> strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.termQuery(strQuery.getKey(), strQuery.getValue());
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }
    /**
     *
     * @param strQuery 查询条件
     * @param expect 期望 <属性,属性值List>
     */
    @SneakyThrows
    private void doSearchTermsQuery(Pair<String, Collection> strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.termsQuery(strQuery.getKey(), strQuery.getValue());
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }
    /**
     *
     * @param strQuery 查询条件
     * @param expect 期望 <属性,属性值List>
     */
    @SneakyThrows
    private void doSearchMatchQuery(Pair<String,Object> strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.matchQuery(strQuery.getKey(), strQuery.getValue());
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }
    @SneakyThrows
    private void doSearchmatchPhraseQuery(Pair<String,Object> strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.matchPhraseQuery(strQuery.getKey(), strQuery.getValue());
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }
    @SneakyThrows
    private void doSearchmultiMatchQuery(Pair<String[],Object> strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.multiMatchQuery(strQuery.getValue(), strQuery.getKey());
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }
    @SneakyThrows
    private void doSearchqueryString(String strQuery, Pair<String,List> expect) {
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.queryStringQuery(strQuery);
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals(expect.getKey(), expect.getValue(), listPair.getValue());
    }

    /**
     * 前缀查询
     */
    @Test
    @SneakyThrows
    public void prefix_1() {
        initCustomers();
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.prefixQuery("name", "张大豪");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(1001, 1002), listPair.getValue());
    }

    /**
     * 通配符查询
     */
    @Test
    @SneakyThrows
    public void wildcard_1() {
        initCustomers();
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.wildcardQuery("name", "大豪*");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void wildcard_2() {
        initCustomers();
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.wildcardQuery("name", "*大豪");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void wildcard_3() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.wildcardQuery("categorys.ik1", "*数据");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101,104), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void wildcard_4() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.wildcardQuery("categorys.ik1", "*据");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101,104), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void wildcard_5() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.wildcardQuery("categorys.ik1", "?数据");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101,104), listPair.getValue());
    }

    /**
     * 范围查询
     */
    @Test
    @SneakyThrows
    public void rangeQuery_1() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.rangeQuery("age").from(21);
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(102), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void rangeQuery_2() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.rangeQuery("age").from(20);
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101,102), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void rangeQuery_3() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.rangeQuery("age").from(20, false);
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(102), listPair.getValue());
    }
    @Test
    @SneakyThrows
    public void rangeQuery_4() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.rangeQuery("age").to(20, false);
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(103,104), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void rangeQuery_5() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            QueryBuilder query = QueryBuilders.rangeQuery("age");
            sourceBuilder.query(query);
        }));
        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101, 103, 104, 102), listPair.getValue());
    }

    /**
     * 定制评分
     */
    @Test
    @SneakyThrows
    public void functionScore_1_1() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchPhraseQuery("description", "有限公司"));
            sourceBuilder.query(queryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(103, 101, 102), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void functionScore_1_2() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchPhraseQuery("description", "有限公司"));

            FieldValueFactorFunctionBuilder fieldQuery =
                    ScoreFunctionBuilders.fieldValueFactorFunction("workAge").factor(0.1f);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                    .functionScoreQuery(queryBuilder, fieldQuery)
                    .boostMode(CombineFunction.SUM);

            sourceBuilder.query(functionScoreQueryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(103, 102, 101), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void functionScore_2_1() {

        dataSet("CustomerServiceTest.alls.json");

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchPhraseQuery("description", "有限公司"));

            ScriptScoreFunctionBuilder scriptScoreFunctionBuilder = ScoreFunctionBuilders
                    .scriptFunction("doc['workAge'].value*0.1+doc['age'].value*0.2");
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                    .functionScoreQuery(queryBuilder, scriptScoreFunctionBuilder)
                    .boostMode(CombineFunction.SUM);
            sourceBuilder.query(functionScoreQueryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(102, 101, 103), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void functionScore_3_1() {

        dataSet("CustomerServiceTest.alls.json");

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termsQuery("serviceCitys.tag", Arrays.asList("北京","上海")))
                    .should(QueryBuilders.termsQuery("categorys.tag", Arrays.asList("大数据","云计算")));
            sourceBuilder.query(queryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101, 104, 102), listPair.getValue());
    }

    /**
     * 定制排序(默认为根据打分倒叙排列)
     */
    @Test
    @SneakyThrows
    public void sort_1() {
        dataSet("CustomerServiceTest.alls.json");
        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {

            QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
            sourceBuilder.query(queryBuilder);
            sourceBuilder.sorts().clear();
            sourceBuilder.sort("age", SortOrder.DESC).sort("workAge", SortOrder.ASC);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(102, 101, 104, 103), listPair.getValue());
    }

    private String generateScriptOfComputeCount() {
        return "int computeCount(String esString, String queryString) {" +
                "    def esStrings = Arrays.asList(/,/.split(esString));" +
                "    def queryStrings = /,/.split(queryString);" +
                "    int total = 0;" +
                "    for(int i=0; i<queryStrings.length; i++) {" +
                "        if (esStrings.contains(queryStrings[i])) {" +
                "            total += 1;" +
                "        }" +
                "    }" +
                "    return total;" +
                "} ";
    }
    private String generateScriptOfComputeTotalCount() {
        StringBuffer strScript = new StringBuffer("int computeTotalCount(Map doc, Map params) {");
        strScript.append("def totalServiceCitys =  computeCount(doc['serviceCitys.keyword'].value, params.serviceCitys);");
        strScript.append("def totalCategorys =  computeCount(doc['categorys.keyword'].value, params.categorys);");
        strScript.append("def total =  totalServiceCitys + totalCategorys;");
        strScript.append("return total > 10 ? 10 : total; ");
        strScript.append("}");
        return strScript.toString();
    }

    @Test
    @SneakyThrows
    public void functionScore_3_2() {

        dataSet("CustomerServiceTest.alls.json");

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {

            Map<String, Object> params = new HashMap<>(20);
            params.put("serviceCitys", "北京");
            params.put("categorys", "大数据,云计算");

            BoolQueryBuilder     queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termsQuery("serviceCitys.tag", Arrays.asList( ((String)params.get("serviceCitys")).split(","))) )
                    .should(QueryBuilders.termsQuery("categorys.tag", ((String)params.get("categorys")).split(",")) );

            StringBuffer strScript = new StringBuffer(generateScriptOfComputeCount());
            strScript.append(generateScriptOfComputeTotalCount());
            strScript.append("def total = computeTotalCount(doc, params)*1.0; ");
            strScript.append("total += doc['workAge'].value*0.2+doc['age'].value*0.1;");
            strScript.append("return total;");

            Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, strScript.toString(), params);
            ScriptScoreFunctionBuilder scriptScoreFunctionBuilder = ScoreFunctionBuilders.scriptFunction(script);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                    .functionScoreQuery(queryBuilder, scriptScoreFunctionBuilder)
                    .boostMode(CombineFunction.REPLACE);
            sourceBuilder.query(functionScoreQueryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(101, 102, 104), listPair.getValue());
    }

    @Test
    @SneakyThrows
    public void functionScore_3_3() {

        dataSet("CustomerServiceTest.alls.json");

        Pair<Integer, List<Customer>> listPair = elasticsearchService.search(CustomerIndex.index, 0, 100, Customer.class, (sourceBuilder -> {

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termsQuery("serviceCitys.tag", Arrays.asList("北京","上海")))
                    .should(QueryBuilders.termsQuery("categorys.tag", Arrays.asList("大数据","云计算")));

            ScriptScoreFunctionBuilder scriptScoreFunctionBuilder = ScoreFunctionBuilders
                    .scriptFunction("doc['workAge'].value*0.1+doc['age'].value*0.2");
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                    .functionScoreQuery(queryBuilder, scriptScoreFunctionBuilder)
                    .boostMode(CombineFunction.SUM);
            sourceBuilder.query(functionScoreQueryBuilder);
        }));

        ReflectionAssert.assertPropertyReflectionEquals("id", Arrays.asList(102, 101, 104), listPair.getValue());
    }


    @SneakyThrows
    private void initCustomers() {
        clearCustomers();
        elasticsearchService.addDocuments(CustomerIndex.index, createCustomers());
    }
    private List<Customer> createCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.add(createCustomer());
        {
        Customer customer = new Customer();
        customer.setId(1002L);
        customer.setName("张大豪哥力士");
        customer.setDescription("百度宝宝知道是由百度研发,面向备孕、孕期、0-6岁父母的专业母婴知识社区（豪哥）");
        customer.setCategorys("UI设计,可视化,大数据开发");
        customer.setAddress("香港铜锣湾告士打道262号中粮大厦32楼(张大豪哥力士的家)");
        customer.setLeader(true);
        customer.setBirthDate(parseDate("1995/08/15"));
        customer.setWorkAge(18L);
        customers.add(customer);
    }
        return customers;
}
    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setId(1001L);
        customer.setName("张大豪哥");
        customer.setDescription("外存储器包括 硬盘,U盘,软盘,移动硬盘,光盘等等外置存储设备");
        customer.setCategorys("大数据开发,UI设计,计算机图形");
        customer.setAddress("深圳市龙岗区坂田华为总部办公楼");
        customer.setLeader(false);
        customer.setBirthDate(parseDate("2001/01/01"));
        customer.setWorkAge(5L);
        return customer;
    }

    private void dataClear() {
        doDataClear(CustomerIndex.index, Customer.class);
    }

    @SneakyThrows
    private void dataSet(String filename) {
        dataClear();
        List<Customer> customers = loadJsonObjects(getClassPath()+filename, Customer.class);
        doDataSet(CustomerIndex.index, Customer.class, getClassPath(), filename, customers);
    }


    private static String getClassPath() {
        return CustomerServiceTest.class.getResource("").getPath().substring(1);
    }

    private String parseEsUrl(ElasticsearchConfig esconfig, String index) {
        return esconfig.getSchema()+"://"+esconfig.getHost()+":"+esconfig.getPort() + "/"+index;
    }

    private<T> Pair<T,List> createPair(T key, Object...values) {
        Pair<T, List> pair = new Pair<T, List>(key, Arrays.asList(values));
        return pair;
    }
    private Pair<String,List> createEmptyPair() {
        return createPair("id");
    }
}
