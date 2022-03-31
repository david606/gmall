package org.sirius.gmall.member.feign;

import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 远程调用优惠服务客户端
 *
 * @author david
 */
@FeignClient("gmall-coupon")
public interface CouponFeignService {

    /**
     * 远程调用测试： 获取会员下优惠券列表
     *
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
