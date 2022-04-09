package org.sirius.gmall.product.dao;

import org.apache.ibatis.annotations.Param;
import org.sirius.gmall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sirius.gmall.product.vo.SpuItemAttrGroupVo;

import java.util.List;

/**
 * 属性分组
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {
    /**
     * 查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
     *
     * @param spuId
     * @param catalogId
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
