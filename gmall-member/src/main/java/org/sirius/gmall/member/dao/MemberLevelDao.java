package org.sirius.gmall.member.dao;

import org.sirius.gmall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 20:22:23
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {
	
}
