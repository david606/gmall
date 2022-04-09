package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.AttrEntity;
import org.sirius.gmall.product.vo.AttrGroupRelationVo;
import org.sirius.gmall.product.vo.AttrRespVo;
import org.sirius.gmall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询销售属性和规格参数（分页查询）
     *
     * @param params    其它过滤查询参数
     * @param catelogId 分类Id 所属分类
     * @param attrType  属性类型 [0-(sale)销售属性，1-(base)基本属性]。
     *                  （实际传来的参数是 base 或 sale）
     * @return 分页
     */
    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    /**
     * 查询属性信息(更新时回显数据)
     *
     * @param attrId
     * @return
     */
    AttrRespVo getAttrInfo(Long attrId);

    /**
     * 保存属性
     *
     * @param attrVo
     */
    void saveAttr(AttrVo attrVo);

    /**
     * 更新属性及关联分组
     *
     * @param attrVo
     */
    void updateAttrById(AttrVo attrVo);

    /**
     * 根据分组id找到关联的所有属性
     *
     * @param attrgroupId
     * @return
     */
    List<AttrEntity> getRelationAttr(Long attrgroupId);

    /**
     * 批量删除关联关系
     *
     * @param relationVos
     */
    void deleteRelation(AttrGroupRelationVo[] relationVos);

    /**
     * 获取当前分组没有被关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定的所有属性集合里面，挑出检索属性
     *
     * @param attrIds
     * @return
     */
    List<Long> selectSearchableAttrs(List<Long> attrIds);
}

