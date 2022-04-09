package org.sirius.gmall.product.dao;

import org.apache.ibatis.annotations.Param;
import org.sirius.gmall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * spu信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    /**
     * 更新商品发布状态
     *
     * @param spuId
     * @param code 上架状态[0 - 下架，1 - 上架]
     */
    void updateSpuPublishStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
