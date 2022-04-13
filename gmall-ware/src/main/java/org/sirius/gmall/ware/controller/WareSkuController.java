package org.sirius.gmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sirius.gmall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.sirius.gmall.ware.entity.WareSkuEntity;
import org.sirius.gmall.ware.service.WareSkuService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品库存
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {

    @Resource
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);
        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    /**
     * 查询查询这批商品是否有库存
     * @param skuIds 一批商品
     * @return
     */
    @PostMapping(value = "/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds){
        //skuId stock
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(vos);
    }
}
