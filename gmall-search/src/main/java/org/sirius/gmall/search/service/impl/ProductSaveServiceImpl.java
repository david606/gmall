package org.sirius.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.sirius.common.es.SkuEsModel;
import org.sirius.gmall.search.config.GmallElasticSearchConfig;
import org.sirius.gmall.search.constant.ElasticSearchConstant;
import org.sirius.gmall.search.service.ProductSaveService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 下午8:41
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean saveSpuEsModel(List<SkuEsModel> skuEsModels) throws IOException {

        if (skuEsModels == null || skuEsModels.size() == 0) {
            return false;
        }

        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(skuEsModel -> {

            // 将键入的 JSON 文档索引到特定索引并使其可搜索的索引请求
            IndexRequest indexRequest = new IndexRequest(ElasticSearchConstant.INDEX_NAME);
            // 设置索引文档的 id 为 skuId
            indexRequest.id(skuEsModel.getSkuId().toString());

            // 将数据转成Json
            String modelJson = JSON.toJSONString(skuEsModel);
            indexRequest.source(modelJson, XContentType.JSON);

            // 将IndexRequest添加到要执行的操作列表中
            bulkRequest.add(indexRequest);
        });

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GmallElasticSearchConfig.COMMON_OPTIONS);

        boolean hasFailures = bulkResponse.hasFailures();

        if (hasFailures) {
            log.info(bulkResponse.buildFailureMessage());
            return false;
        }

        BulkItemResponse[] items = bulkResponse.getItems();
        List<String> ids = Arrays.stream(items).map(BulkItemResponse::getId).collect(Collectors.toList());

        log.info("商品上架完成：{}", ids);

        return true;
    }
}
