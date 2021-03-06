package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.AttrAttrgroupRelationEntity;
import org.sirius.gmall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 批量保存属性分组关联关系
     *
     * @param relationVos
     */
    void saveBatch(List<AttrGroupRelationVo> relationVos);
}

