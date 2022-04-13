package org.sirius.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;
import org.sirius.gmall.product.dao.SkuInfoDao;
import org.sirius.gmall.product.entity.SkuImagesEntity;
import org.sirius.gmall.product.entity.SkuInfoEntity;
import org.sirius.gmall.product.entity.SpuInfoDescEntity;
import org.sirius.gmall.product.feign.SeckillFeignService;
import org.sirius.gmall.product.service.*;
import org.sirius.gmall.product.vo.SkuItemSaleAttrVo;
import org.sirius.gmall.product.vo.SkuItemVo;
import org.sirius.gmall.product.vo.SpuItemAttrGroupVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Sku信息
 *
 * @author david
 */
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private SeckillFeignService secKillFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), new QueryWrapper<SkuInfoEntity>());
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        final String categoryRootId = "0";

        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /*
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !categoryRootId.equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !categoryRootId.equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                // max price 值，需要大于0才有意义，才能被做为实际条件
                if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    /*
     * ======================= 为其它逻辑提供方法 & 商城业务逻辑 ===============================
     */


    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息的获取  pms_sku_info
            SkuInfoEntity info = this.getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);


        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、获取spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);


        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、获取spu的介绍    pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDescription(spuInfoDescEntity);
        }, executor);


        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);


        //2、sku的图片信息(pms_sku_images)
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        // TODO 秒杀还没有完成，暂不开放
//        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
//            // 3、远程调用查询当前sku是否参与秒杀优惠活动
//            R skuSeckillInfo = secKillFeignService.getSkuSeckillInfo(skuId);
//
//            if (skuSeckillInfo.getCode() == 0) {
//                // 查询成功
//                SeckillSkuVo seckillInfoData = skuSeckillInfo.getData("data", new TypeReference<SeckillSkuVo>() {
//                });
//
//                skuItemVo.setSeckillSkuVo(seckillInfoData);
//
//                if (seckillInfoData != null) {
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime > seckillInfoData.getEndTime()) {
//                        skuItemVo.setSeckillSkuVo(null);
//                    }
//                }
//            }
//        }, executor);

        // 等到所有任务都完成
//        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, seckillFuture).get();
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();

        return skuItemVo;
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }


}