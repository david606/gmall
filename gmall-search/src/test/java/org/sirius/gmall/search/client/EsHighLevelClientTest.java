package org.sirius.gmall.search.client;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/9 下午5:11
 */
@Slf4j
@SpringBootTest
public class EsHighLevelClientTest {

    @Resource
    private RestHighLevelClient highLevelClient;
    final String INDEX_NAME = "/users";
    final String _DOC = "_doc";

    @Test
    public void buildIndexRequestWithString() throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id("1");

        // 设置超时时间
        indexRequest.timeout(TimeValue.timeValueSeconds(10));
        // indexRequest.timeout("10s");

        // 设置超时策略
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        // indexRequest.setRefreshPolicy("wait_for");

        // 设置版本
        indexRequest.version(2);
        // 设置版本类型
        indexRequest.versionType(VersionType.EXTERNAL);

        // 设置操作类型
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
        // indexRequest.opType("create");

        String jsonString = JSON.toJSONString(new User(1L, "Cecilia"));
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse response = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void buildIndexRequestWithMap() throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id("2");

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", 2L);
        map.put("name", "David");
        indexRequest.source(map);

        IndexResponse response = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void buildIndexRequestWithContentBuilder() throws IOException {

        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id("3");

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("id", 3L);
            builder.field("name", "Lucy");
            builder.timeField("createTime", new Date());
        }
        builder.endObject();

        indexRequest.source(builder);
    }

    private void parseResponse(IndexResponse indexResponse) {

        String index = indexResponse.getIndex();
        log.info("index :{}", index);

        String docId = indexResponse.getId();
        log.info("documentId :{}", docId);

        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("document is created!");
        } else {
            log.info("document is updated!");
        }

        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            log.info("Succeed shard are not enough!");
        }

        if (shardInfo.getFailed() > 0) {
            ReplicationResponse.ShardInfo.Failure[] failures = shardInfo.getFailures();
            Arrays.stream(failures).forEach(failure -> {
                String reason = failure.reason();
                log.info("Fail reason is :{}", reason);
            });
        }
    }
}
