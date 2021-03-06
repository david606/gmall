package org.sirius.gmall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.sirius.gmall.coupon.entity.CouponEntity;
import org.sirius.gmall.coupon.service.CouponService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;


/**
 * 优惠券信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:17:32
 */
//@RefreshScope
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 测试 Nacos 配置中心：配置在配置中心的 gmall-coupon.properties
     * coupon.user.name=David
     * coupon.user.salary=50
     */
//    @Value("${coupon.user.name}")
//    private String name;
//    @Value("${coupon.user.salary}")
//    private Double age;
//
//    @RequestMapping("/test/nacosconfig")
//    public R testNacosConfig() {
//        return R.ok().put("name", name).put("age", age);
//    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CouponEntity coupon) {
        couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CouponEntity coupon) {
        couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 远程调用测试： 获取会员下优惠券列表
     *
     * @return R 全系统的所有返回都返回R
     */
    @RequestMapping("/member/list")
    public R memberCoupons() {
        // 应该去数据库查用户对于的优惠券
        // 但这个我们简化了，不去数据库查，构造了一个优惠券给他返回
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100-10");

        return R.ok().put("coupons", List.of(couponEntity));
    }

}
