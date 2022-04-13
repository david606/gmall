package org.sirius.gmall.product.feign;

import org.sirius.common.to.SkuReductionTo;
import org.sirius.common.to.SpuBoundTo;
import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/5 下午10:57
 */
@FeignClient(value = "gmall-coupon")
public interface CouponFeignService {
    /*
     * 1、CouponFeignService.saveSpuBounds(spuBoundTo);
     * 1）、@RequestBody将这个对象转为json。
     * 2）、找到gmall-coupon服务，给/coupon/spubounds/save发送请求。
     * 将上一步转的json放在请求体位置，发送请求；
     * 3）、对方服务收到请求。请求体里有json数据。
     * (@RequestBody SpuBoundsEntity spuBounds)；将请求体的json转为SpuBoundsEntity；
     * 只要json数据模型是兼容的。双方服务无需使用同一个to
     */

    /**
     * 积分
     *
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     * 保存商品满减信息
     *
     * @param skuReductionTo
     * @return
     */
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
