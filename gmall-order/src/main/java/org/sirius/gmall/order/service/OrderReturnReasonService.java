package org.sirius.gmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * ้่ดงๅๅ 
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:00:06
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

