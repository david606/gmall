package org.sirius.gmall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.sirius.gmall.product.entity.BrandEntity;
import org.sirius.gmall.product.service.BrandService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;

import javax.annotation.Resource;


/**
 * 品牌
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {

    @Resource
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);
        return R.ok().put("page", page);
    }

    @GetMapping("/infos")
    public R getBrands(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brand =  brandService.getBrandsByIds(brandIds);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);
        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody BrandEntity brand) {
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 更新品牌信息
     * <pre>
     * 更新品牌信息，同时更新品牌与分类关联表冗余字段
     * </pre>
     */
    @RequestMapping("/update")
    public R update(@RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);
        return R.ok();
    }

    /**
     * 批量删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));
        return R.ok();
    }

}
