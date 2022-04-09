package org.sirius.gmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购需求服务
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    /**
     * 查询分页列表
     *
     * @param params 查询关键字
     * @return
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据采购单，查询采购需求（详情）
     *
     * @param id 采购单id
     * @return 采购需求列表
     */
    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

