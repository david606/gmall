package org.sirius.gmall.product.feign;

import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 秒杀远程服务
 *
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/5 下午10:10
 */
@FeignClient(value = "gmall-seckill")
public interface SeckillFeignService {
    /**
     * 根据skuId查询商品是否参加秒杀活动
     *
     * @param skuId
     * @return
     */
    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
