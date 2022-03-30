package org.sirius.gmall.coupon.dao;

import org.sirius.gmall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:17:32
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
