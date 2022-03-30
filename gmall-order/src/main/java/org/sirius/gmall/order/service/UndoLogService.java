package org.sirius.gmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.order.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:00:06
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

