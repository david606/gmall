package org.sirius.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.product.entity.BrandEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
public interface BrandService extends IService<BrandEntity> {

    /**
     * 获取全部品牌分页列表
     *
     * @param params 查询过滤关键字(根据品牌名做为关键字)
     * @return 分页列表
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 更新品牌
     * <pre>
     * 因为在品牌与分类关联关系表中，冗余了品牌名
     * 所以，在品牌更新时，也同时更新关联关系表.
     * </pre>
     *
     * @param brand 品牌实体
     */
    void updateDetail(BrandEntity brand);

    /**
     * 批量查询品牌
     *
     * @param brandIds 品牌Id 列表
     * @return 品牌列表
     */
    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

