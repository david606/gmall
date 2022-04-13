package org.sirius.gmall.search.client;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 上午10:31
 */
@Slf4j
@SpringBootTest
public class EsLowLevelClientTest {
    @Resource
    private RestClient lowLevelClient;

    final String INDEX = "/users";
    final String POST = "POST";
    final String PUT = "PUT";
    final String GET = "GET";
    final String DELETE = "DELETE";

    @Test
    void postSyncTest() throws IOException {
        Request request = new Request(POST, INDEX + "/_doc/1");

        request.setEntity(new NStringEntity(JSON.toJSONString(new User(1L, "Lisa"))));
        request.setJsonEntity(JSON.toJSONString(new User(2L, "David")));

        /*
         * 向客户端指向的 Elasticsearch 集群发送请求。阻塞直到请求完成并返回其响应或通过抛出异常而失败。
         * 以循环方式从提供的主机中选择主机。失败的主机被标记为死亡并在一定时间后重试（最少 1 分钟，最多 30 分钟），
         * 具体取决于它们之前失败的次数（失败次数越多，重试时间越晚）。
         * 如果发生故障，所有活动节点（或值得重试的死节点）都会重试，
         * 直到有一个响应或没有一个响应，在这种情况下将抛出IOException 。
         * 此方法通过执行异步调用并等待结果来工作。
         * 如果异步调用抛出异常，我们将其包装并重新抛出，以便附加到异常的堆栈跟踪包含调用站点。
         * 虽然我们试图保留原始异常，但这并不总是可能的，并且可能没有涵盖所有情况。
         * 您可以从Exception.getCause()获取原始异常。
         */
        Response response = lowLevelClient.performRequest(request);
        parseResponse(response);

        /*
         * java.io.Closeable 关闭此流并释放与其关联的任何系统资源。如果流已经关闭，则调用此方法无效。
         * 如AutoCloseable.close()中所述，关闭可能失败的情况需要仔细注意。
         * 强烈建议在抛出IOException之前放释放底层资源并在内部将Closeable标记为已关闭。
         */
        lowLevelClient.close();
    }

    @Test
    public void postAsyncTest() throws IOException {
        Request request = new Request(POST, INDEX + "/_doc/1");

        request.setEntity(new NStringEntity(JSON.toJSONString(new User(1L, "Lisa"))));
        request.setJsonEntity(JSON.toJSONString(new User(2L, "David")));

        /*
         * 向客户端指向的 Elasticsearch 集群发送请求。
         * 请求异步执行，并在请求完成或失败时通知提供的ResponseListener 。
         * 以循环方式从提供的主机中选择主机。失败的主机被标记为死亡并在一定时间后重试（最少 1 分钟，最多 30 分钟），
         * 具体取决于它们之前失败的次数（失败次数越多，重试时间越晚）。
         * 如果发生故障，所有活动节点（或值得重试的死节点）都会重试，直到有一个响应或没有一个响应，在这种情况下将抛出IOException 。
         */
        lowLevelClient.performRequestAsync(request, new ResponseListener() {
            @SneakyThrows
            @Override
            public void onSuccess(Response response) {
                parseResponse(response);
            }

            @Override
            public void onFailure(Exception exception) {
                exception.printStackTrace();
            }
        });

        /*
         * java.io.Closeable 关闭此流并释放与其关联的任何系统资源。如果流已经关闭，则调用此方法无效。
         * 如AutoCloseable.close()中所述，关闭可能失败的情况需要仔细注意。
         * 强烈建议在抛出IOException之前放释放底层资源并在内部将Closeable标记为已关闭。
         */
        lowLevelClient.close();
    }

    @Test
    void getTest() throws IOException {
        Request request = new Request(GET, INDEX + "/_doc/1");
        Response response = lowLevelClient.performRequest(request);
        parseResponse(response);
    }

    @Test
    void getAsyncTest() throws IOException {
        Request request = new Request(GET, INDEX + "/_doc/1");
        lowLevelClient.performRequestAsync(request, new ResponseListener() {
            @SneakyThrows
            @Override
            public void onSuccess(Response response) {
                parseResponse(response);
            }

            @Override
            public void onFailure(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Test
    void deleteTest() throws IOException {
        Request request = new Request(DELETE, INDEX);
        Response response = lowLevelClient.performRequest(request);
    }

    private void parseResponse(Response response) throws IOException {
        HttpHost host = response.getHost();
        log.info("host = {}", host);

        int statusCode = response.getStatusLine().getStatusCode();
        log.info("statsCode ={}", statusCode);

        RequestLine requestLine = response.getRequestLine();
        log.info("requestLine ={} ", requestLine);

        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        log.info("responseBody = {}", responseBody);
    }
}
