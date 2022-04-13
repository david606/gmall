package org.sirius.gmall.ware.dao;

import org.apache.ibatis.annotations.Param;
import org.sirius.gmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /**
     * 添加商品库存记录
     *
     * @param skuId  商品
     * @param wareId 仓库
     * @param skuNum 商品数量
     */
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    /**
     * 根据 skuId 查询其是否有库存
     *
     * @param skuId
     * @return
     */
    Long getSkuStock(@Param("skuId") Long skuId);
}
