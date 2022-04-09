package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.SpuInfoEntity;
import org.sirius.gmall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存 Spu
     *
     * @param vo
     */
    void saveSpuInfo(SpuSaveVo vo);

    /**
     * 分页查询 spu 列表
     *
     * @param params 查询过滤条件
     * @return
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架
     *
     * @param spuId
     */
    void spuUp(Long spuId);

    /**
     * 保存 spu 基本信息
     *
     * @param spuInfoEntity
     */
    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    /**
     * 根据skuId查询spu的信息
     * @param skuId
     * @return
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

