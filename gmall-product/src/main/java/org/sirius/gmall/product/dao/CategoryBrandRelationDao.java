package org.sirius.gmall.product.dao;

import org.apache.ibatis.annotations.Param;
import org.sirius.gmall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    /**
     * 更新分类
     *
     * @param catId 分类Id
     * @param name 分类名
     */
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);

}
