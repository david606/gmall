package org.sirius.gmall.ware.dao;

import org.sirius.gmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}