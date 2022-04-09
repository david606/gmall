package org.sirius.gmall.ware.feign;

import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/8 下午9:02
 */
@FeignClient("gmall-product")
public interface ProductFeignService {

    /**
     * 获取skuId获取商品信息
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
