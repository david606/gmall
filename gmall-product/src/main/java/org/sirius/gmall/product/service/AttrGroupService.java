package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.AttrGroupEntity;
import org.sirius.gmall.product.vo.AttrGroupWithAttrsVo;
import org.sirius.gmall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取分类下所有分组&关联属性
     *
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    /**
     * 根据分类返回属性分组分页列表
     *
     * @param params    查询关键字
     * @param catelogId 分类Id
     * @return 分页列表
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 查出当前spu对应的所有属性的分组信息;
     * 以及当前分组下的所有属性对应的值
     *
     * @param spuId spu
     * @param catalogId 分类
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

