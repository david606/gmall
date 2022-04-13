package org.sirius.gmall.search.service;

import org.sirius.common.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 下午8:07
 */
public interface ProductSaveService {

    /**
     * 保存 sku 到 ElasticSearch
     *
     * @param skuEsModels
     * @return
     */
    boolean saveSpuEsModel(List<SkuEsModel> skuEsModels) throws IOException;
}
