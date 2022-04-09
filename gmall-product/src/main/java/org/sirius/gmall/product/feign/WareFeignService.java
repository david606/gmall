package org.sirius.gmall.product.feign;

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
@FeignClient(value = "gmall-ware")
public interface WareFeignService {

    /**
     * 远程查询查询这批商品是否有库存
     * @param skuIds 一批商品
     * @return
     */
    @PostMapping(value = "/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
