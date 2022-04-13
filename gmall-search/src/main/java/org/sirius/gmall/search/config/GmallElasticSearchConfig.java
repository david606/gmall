package org.sirius.gmall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/2 下午9:12
 */
@Configuration
public class GmallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
                HttpHost.create("http://172.18.0.5:9200")
        );
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
            }
        });
        builder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        return new RestHighLevelClient(builder);
    }

    @Bean
    public RestClient restClient() {
        RestClientBuilder builder = RestClient.builder(
                HttpHost.create("http://172.18.0.5:9200")
        );
        return builder.build();
    }
}
