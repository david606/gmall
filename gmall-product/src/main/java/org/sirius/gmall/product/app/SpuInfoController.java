package org.sirius.gmall.product.app;

import java.util.Arrays;
import java.util.Map;

import org.sirius.gmall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.sirius.gmall.product.entity.SpuInfoEntity;
import org.sirius.gmall.product.service.SpuInfoService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;

import javax.annotation.Resource;


/**
 * spu信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Resource
    private SpuInfoService spuInfoService;

    /**
     * 根据 sku 获取 spu
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skuId/{id}")
    public R getSpuInfoBySkuId(@PathVariable("id") Long skuId) {
        SpuInfoEntity entity = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(entity);
    }

    /**
     * 商品上架
     * /product/spuinfo/{spuId}/up
     *
     * @param spuId 上架商品
     * @return
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        spuInfoService.spuUp(spuId);
        return R.ok();
    }

    /**
     * 查询分页列表
     *
     * @param params 查询过滤关键字
     * @return 分页列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = spuInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存 spu
     *
     * @param vo 保存的传到对象
     * @return R
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVo vo) {
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
