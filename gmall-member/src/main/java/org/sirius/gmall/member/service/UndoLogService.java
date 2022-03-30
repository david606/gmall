package org.sirius.gmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.member.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 20:22:23
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

