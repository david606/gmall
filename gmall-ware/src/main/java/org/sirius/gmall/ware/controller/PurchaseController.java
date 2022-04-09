package org.sirius.gmall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sirius.gmall.ware.vo.MergeVo;
import org.sirius.gmall.ware.vo.PurchaseDoneVo;
import org.springframework.web.bind.annotation.*;

import org.sirius.gmall.ware.entity.PurchaseEntity;
import org.sirius.gmall.ware.service.PurchaseService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;

import javax.annotation.Resource;


/**
 * 采购信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {

    @Resource
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 未领取的采购单列表
     *
     * @param params
     * @return
     */
    @GetMapping(value = "/unreceive/list")
    public R unReceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageForUnReceive(params);
        return R.ok().put("page", page);
    }

    /**
     * 合并采购需求
     *
     * @param mergeVo
     * @return
     */
    @PostMapping(value = "/merge")
    public R merge(@RequestBody MergeVo mergeVo) {
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    /**
     * 领取采购单
     *
     * @param ids 采购单列表
     * @return
     */
    @PostMapping(value = "/received")
    public R received(@RequestBody List<Long> ids) {
        purchaseService.received(ids);
        return R.ok();
    }

    /**
     * 完成采购单
     *
     * @param doneVo
     * @return
     */
    @PostMapping(value = "/done")
    public R finish(@RequestBody PurchaseDoneVo doneVo) {
        purchaseService.done(doneVo);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setUpdateTime(new Date());
        purchase.setCreateTime(new Date());
        purchaseService.save(purchase);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
