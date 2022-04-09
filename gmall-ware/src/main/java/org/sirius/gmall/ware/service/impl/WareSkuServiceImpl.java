package org.sirius.gmall.ware.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sirius.common.utils.R;
import org.sirius.gmall.ware.feign.ProductFeignService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.ware.dao.WareSkuDao;
import org.sirius.gmall.ware.entity.WareSkuEntity;
import org.sirius.gmall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    WareSkuDao wareSkuDao;
    @Resource
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //1. 查询商品库存记录是否存在
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId)
                .eq("ware_id", wareId));

        // 2.这个商品在此仓库已有库存记录：更新数量
        if (wareSkuEntities != null && wareSkuEntities.size() != 0) {
            wareSkuDao.addStock(skuId, wareId, skuNum);
            return;
        }

        // 3．这个商品在此仓库没有库存记录：新建记录
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        wareSkuEntity.setSkuId(skuId);
        wareSkuEntity.setStock(skuNum);
        wareSkuEntity.setWareId(wareId);
        wareSkuEntity.setStockLocked(0);

        // 远程查询sku的名字，如果失败整个事务无需回滚
        try {
            R info = productFeignService.info(skuId);
            if (info.getCode() == 0) {
                Map data = (Map) info.get("skuInfo");
                wareSkuEntity.setSkuName((String) data.get("skuName"));
            }
        } catch (Exception e) {
            log.info("远程获取sku失败,自行捕捉异常，不做处理，异常：{}", e.getMessage());
        }

        // 插入库存信息
        wareSkuDao.insert(wareSkuEntity);
    }

}