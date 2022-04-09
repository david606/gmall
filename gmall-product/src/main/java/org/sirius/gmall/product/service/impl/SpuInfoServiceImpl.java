package org.sirius.gmall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.sirius.common.constant.ProductConstant.ProductStatusEnum;
import org.sirius.common.es.SkuEsModel;
import org.sirius.common.to.SkuHasStockVo;
import org.sirius.common.to.SkuReductionTo;
import org.sirius.common.to.SpuBoundTo;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;
import org.sirius.common.utils.R;
import org.sirius.gmall.product.dao.SpuInfoDao;
import org.sirius.gmall.product.entity.*;
import org.sirius.gmall.product.feign.CouponFeignService;
import org.sirius.gmall.product.feign.SearchFeignService;
import org.sirius.gmall.product.feign.WareFeignService;
import org.sirius.gmall.product.service.*;
import org.sirius.gmall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author david
 */
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private AttrService attrService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private CouponFeignService couponFeignService;

    @Resource
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private WareFeignService wareFeignService;

    @Resource
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        // 1.保存spu基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = privateSaveBaseInfo(vo);
        Long spuId = spuInfoEntity.getId();

        // 2.保存spu的描述图片：pms_spu_info_desc
        privateSaveSpuDesc(vo, spuId);

        // 3.保存spu的图片集：pms_spu_images
        privateSaveBatchSpuImages(vo, spuId);

        // 4.保存spu的规格参数：pms_product_attr_value
        privateSaveProductAttr(vo, spuId);

        // 5.保存spu的积分信息：gulimall_sms--->sms_spu_bounds
        privateCouponRemoteSaveSpuBounds(vo, spuId);

        // 6.保存当前spu对应的所有sku信息：pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus == null || skus.size() <= 0) {
            return;
        }

        skus.forEach(item -> {

            // 6.1 保存 sku 的基本信息:pms_sku_info
            SkuInfoEntity skuInfoEntity = privateSaveSkuInfo(spuInfoEntity, item);
            Long skuId = skuInfoEntity.getSkuId();

            // 6.2 保存 sku 的图片信息：pms_sku_images
            privateSaveBatchSkuImages(item, skuId);

            // 6.3 保存 sku的销售属性：pms_sku_sale_attr_value
            privateSaveBatchSkuSaleAttrValues(item, skuId);

            // 6.4 保存 sku的优惠、满减等信息：gulimall_sms--->sms_sku_ladder、sms_sku_full_reduction、sms_member_price
            privateCouponRemoteSaveSkuReduction(item, skuId);
        });

    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        // 分类的根节点是 0
        final String rootCategoryId = "0";

        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !rootCategoryId.equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !rootCategoryId.equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    //    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void spuUp(Long spuId) {

        // 1.查出当前sku的所有可以被用来检索的规格(基本)属性
        List<SkuEsModel.Attrs> searchableAttrList = privateGetSearchableAttrs(spuId);

        // 查出当前spuId对应的所有sku信息
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.getSkusBySpuId(spuId);

        // 2.获取当前spu的每个 sku 是否有库存
        Map<Long, Boolean> hasStockMap = privateGetPerSkuHasStock(skuInfoEntityList);

        // 3. 封装每个sku的信息到 SkuEsModel
        List<SkuEsModel> skuEsModelList = privateEncapsulateSkuEsModels(searchableAttrList, skuInfoEntityList, hasStockMap);

        // 4、将数据发给es进行保存：gmall-search
        R r = searchFeignService.saveSpuEsModel(skuEsModelList);

        // 5. es 数据保存成功后，更新产品发布状态
        if (r.getCode() == 0) {
            this.baseMapper.updateSpuPublishStatus(spuId, ProductStatusEnum.SPU_UP.getCode());
        }

        /*
        TODO 6、远程调用失败：重复调用？接口幂等性？重试机制?
        Feign调用流程
          1、构造请求数据，将对象转为json；
               RequestTemplate template = buildTemplateFromArgs.create(argv);
          2、发送请求进行执行（执行成功会解码响应数据）：
               executeAndDecode(template);
          3、执行请求会有重试机制
               while(true){
                   try{
                     executeAndDecode(template);
                   }catch(){
                       try{retryer.continueOrPropagate(e);}catch(){throw ex;}
                       continue;
                   }

               }
         */
    }

    /**
     * 商品上架：封装 Elastic Search Model
     *
     * @param searchableAttrList 可检索属性列表
     * @param skuInfoEntities    spu 下所有sku列表
     * @param hasStockMap        是否有库存：key(skuId); value(true|false)
     * @return List<SkuEsModel>
     */
    private List<SkuEsModel> privateEncapsulateSkuEsModels(
            List<SkuEsModel.Attrs> searchableAttrList,
            List<SkuInfoEntity> skuInfoEntities,
            Map<Long, Boolean> hasStockMap) {

        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {

            SkuEsModel esModel = new SkuEsModel();

            // 1.命名不一致属性手动设置
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            // 2.设置库存信息
            if (hasStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(hasStockMap.get(sku.getSkuId()));
            }

            // 3.热度评分:0
            esModel.setHotScore(0L);

            // 4.查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandId(brandEntity.getBrandId());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogId(categoryEntity.getCatId());
            esModel.setCatalogName(categoryEntity.getName());

            // 5.设置检索属性
            esModel.setAttrs(searchableAttrList);

            // 6.命名一致属性直接拷贝
            BeanUtils.copyProperties(sku, esModel);

            return esModel;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 商品上架：获取每个sku是否有库存
     *
     * @param skuInfoEntities
     * @return
     */
    private Map<Long, Boolean> privateGetPerSkuHasStock(List<SkuInfoEntity> skuInfoEntities) {
        // 2.1 过滤出 skuId 列表
        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 2.2 发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }
        return stockMap;
    }

    /**
     * 商品上架：获取spu下所有可检索属性
     *
     * @param spuId
     * @return
     */
    private List<SkuEsModel.Attrs> privateGetSearchableAttrs(Long spuId) {
        // 1.1 查询对应商品（spu）下所有规格（基本）属性列表
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        // 1.2 过滤出所有属性Id
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 1.3 挑出所有可被检索的属性Id列表&转换成 Set 去重
        List<Long> searchAttrIds = attrService.selectSearchableAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);

        // 1.4 根据挑出来的可被检索规格（基本）属性的Id列表，找出[可被检索基本属性列表]
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());
        return attrsList;
    }

    /**
     * 保存spu:基本信息：pms_spu_info
     *
     * @param vo
     * @return
     */
    private SpuInfoEntity privateSaveBaseInfo(SpuSaveVo vo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        return spuInfoEntity;
    }

    /**
     * 保存spu：描述图片：pms_spu_info_desc
     *
     * @param vo
     * @param spuId
     */
    private void privateSaveSpuDesc(SpuSaveVo vo, Long spuId) {
        List<String> desc = vo.getDesc();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDesc(String.join(",", desc));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
    }

    /**
     * 保存spu：图片集：pms_spu_images
     *
     * @param vo
     * @param spuId
     */
    private void privateSaveBatchSpuImages(SpuSaveVo vo, Long spuId) {
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuId, images);
    }

    /**
     * 保存spu：规格参数：pms_product_attr_value
     *
     * @param vo
     * @param spuId
     */
    private void privateSaveProductAttr(SpuSaveVo vo, Long spuId) {
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());

            // 查询attr属性名
            AttrEntity byId = attrService.getById(attr.getAttrId());

            valueEntity.setAttrName(byId.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuId);
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);
    }

    /**
     * 保存spu：积分信息：gulimall_sms--->sms_spu_bounds
     *
     * @param vo
     * @param spuId
     */
    private void privateCouponRemoteSaveSpuBounds(SpuSaveVo vo, Long spuId) {
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundTo);

        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }
    }

    /**
     * 保存 spu: 保存其下的sku的基本信息:pms_sku_info
     *
     * @param spuInfoEntity
     * @param item
     * @return
     */
    private SkuInfoEntity privateSaveSkuInfo(SpuInfoEntity spuInfoEntity, Skus item) {
        String defaultImg = "";
        for (Images image : item.getImages()) {
            if (image.getDefaultImg() == 1) {
                defaultImg = image.getImgUrl();
            }
        }

        SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
        BeanUtils.copyProperties(item, skuInfoEntity);
        skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
        skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
        skuInfoEntity.setSaleCount(0L);
        skuInfoEntity.setSpuId(spuInfoEntity.getId());
        skuInfoEntity.setSkuDefaultImg(defaultImg);
        skuInfoService.saveSkuInfo(skuInfoEntity);
        return skuInfoEntity;
    }

    /**
     * 保存 spu: sku的图片信息：pms_sku_images
     *
     * @param item
     * @param skuId
     */
    private void privateSaveBatchSkuImages(Skus item, Long skuId) {
        List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
            skuImagesEntity.setSkuId(skuId);
            skuImagesEntity.setImgUrl(img.getImgUrl());
            skuImagesEntity.setDefaultImg(img.getDefaultImg());
            return skuImagesEntity;
        }).filter(entity -> {
            // 返回true就是需要，false就是剔除
            return !StringUtils.isEmpty(entity.getImgUrl());
        }).collect(Collectors.toList());

        skuImagesService.saveBatch(imagesEntities);
    }

    /**
     * 保存 spu: sku的销售属性：pms_sku_sale_attr_value
     *
     * @param item
     * @param skuId
     */
    private void privateSaveBatchSkuSaleAttrValues(Skus item, Long skuId) {
        List<Attr> attr = item.getAttr();
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
            BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
            skuSaleAttrValueEntity.setSkuId(skuId);
            return skuSaleAttrValueEntity;
        }).collect(Collectors.toList());

        skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
    }

    /**
     * 保存 spu: sku的优惠、满减等信息：gulimall_sms--->sms_sku_ladder、sms_sku_full_reduction、sms_member_price
     *
     * @param item
     * @param skuId
     */
    private void privateCouponRemoteSaveSkuReduction(Skus item, Long skuId) {
        SkuReductionTo skuReductionTo = new SkuReductionTo();
        BeanUtils.copyProperties(item, skuReductionTo);
        skuReductionTo.setSkuId(skuId);
        if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
            if (r1.getCode() != 0) {
                log.error("远程保存sku积分信息失败");
            }
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }


    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {

        //先查询sku表里的数据
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);

        //获得spuId
        Long spuId = skuInfoEntity.getSpuId();

        //再通过spuId查询spuInfo信息表里的数据
        SpuInfoEntity spuInfoEntity = this.baseMapper.selectById(spuId);

        //查询品牌表的数据获取品牌名
        BrandEntity brandEntity = brandService.getById(spuInfoEntity.getBrandId());
        spuInfoEntity.setBrandName(brandEntity.getName());

        return spuInfoEntity;
    }

}