package org.sirius.gmall.product.feign;

import org.sirius.common.es.SkuEsModel;
import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/5 下午10:58
 */
@FeignClient(value = "gmall-search")
public interface SearchFeignService {

    /**
     * 搜索服务保存商品搜索模型
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping(value = "/search/save/product")
    public R saveSpuEsModel(@RequestBody List<SkuEsModel> skuEsModels);
}
