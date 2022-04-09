package org.sirius.gmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 添加商品库存数量
     * <pre>
     * 如果存在库存记录，则更新;否则，新建一条记录．
     * </pre>
     *
     * @param skuId  商品id
     * @param wareId 仓库id
     * @param skuNum 商品数量
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);
}

