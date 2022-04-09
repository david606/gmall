package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.CategoryEntity;
import org.sirius.gmall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     *
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 找到catelogId的完整路径；
     * [父/子/孙]
     *
     * @param catalogId
     * @return
     */
    Long[] findCatalogPath(Long catalogId);

    /**
     * 批量删除分类(如果没有其它关联)
     *
     * @param asList
     */
    void removeMenuByIds(List<Long> asList);

    /**
     * 级联更新所有关联数据; category,及中间表
     *
     * @param category
     */
    void updateCascade(CategoryEntity category);


    /**
     * 商城获取一级分类
     *
     * @return
     */
    List<CategoryEntity> getLevel1Category();


    /**
     * 商城获取分类　Json
     * @return
     */
    Map<String, List<Catelog2Vo>> getCatalogJson();

}

