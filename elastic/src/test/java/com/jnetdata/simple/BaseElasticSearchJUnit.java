package com.jnetdata.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thenicesys.data.api.EntityId;
import org.thenicesys.data.elasticsearch.config.ElasticsearchConfig;
import org.thenicesys.data.elasticsearch.service.ElasticsearchService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ContextConfiguration({
        "classpath:spring/springcontext.xml",
        "classpath:spring/springcontext-elasticsearch.xml",
        "classpath:spring/springcontext-service.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseElasticSearchJUnit {

    @Autowired
    protected ElasticsearchService elasticsearchService;
    @Autowired
    protected ElasticsearchConfig elasticsearchConfig;

    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    protected<T extends EntityId & Serializable> void doDataClear(String index, Class<T> cls) {
        Pair<Integer,List<T>> list = elasticsearchService.search(index, 0, 500, cls, searchSourceBuilder -> {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        });
        elasticsearchService.deleteDocuments(index, list.getValue().stream().map(t -> t.getId()+"").collect(Collectors.toList()));
    }

    @SneakyThrows
    protected<T extends EntityId & Serializable> void doDataSet(String index, Class<T> cls, String classPath, String filename, List<T> datas) {
        doDataClear(index, cls);
        elasticsearchService.bulk(() -> {
            BulkRequest bulkRequest = new BulkRequest();
            for(T data : datas) {
                bulkRequest.add(elasticsearchService.createIndexRequest(index, data));
            }
            return bulkRequest;
        });
    }
    protected <T> List<T> loadJsonObjects(String filename, Class<T> cls) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<T> list = objectMapper.readValue(new File(filename), objectMapper.getTypeFactory().constructCollectionType(List.class, cls));
        return list;
    }

    protected Date parseDate(String str) {
        try {
            return DateUtils.parseDate(str, "yyyy/MM/dd");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    protected Date parseDateTime(String str) {
        try {
            return DateUtils.parseDate(str, "yyyy/MM/dd hh:mm:ss");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
