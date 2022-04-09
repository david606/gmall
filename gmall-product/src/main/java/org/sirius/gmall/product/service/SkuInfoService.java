package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.SkuInfoEntity;
import org.sirius.gmall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据查询过滤关键字获取分页列表
     *
     * @param params 查询关键字 key：分类/品牌/状态/可否检索
     * @return 分页列表
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据 spu 获取 sku 列表
     *
     * @param spuId 指定 spu
     * @return skus
     */
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * 查询商品(sku)详情
     *
     * @param skuId sku
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;

    /**
     * sku的基本信息:pms_sku_info
     * @param skuInfoEntity
     */
    void saveSkuInfo(SkuInfoEntity skuInfoEntity);
}

