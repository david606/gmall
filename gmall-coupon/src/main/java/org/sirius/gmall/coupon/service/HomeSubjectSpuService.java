package org.sirius.gmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * δΈι’εε
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:17:32
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

