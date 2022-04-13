package org.sirius.gmall.search.feign;

import org.sirius.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/12 上午11:37
 */
@FeignClient("gmall-product")
public interface ProductFeignService {

    /**
     * 根据 attrId 远程获取属性信息
     *
     * @param attrId 属性Id
     * @return R
     */
    @RequestMapping("/product/attr/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId);

    /**
     * 根据一批品牌id 远程获取品牌信息列表
     *
     * @param brandIds 品牌ids
     * @return
     */
    @GetMapping("/product/brand/infos")
    public R getBrands(@RequestParam("brandIds") List<Long> brandIds);
}
