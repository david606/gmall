package org.sirius.gmall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.coupon.dao.MemberPriceDao;
import org.sirius.gmall.coupon.entity.MemberPriceEntity;
import org.sirius.gmall.coupon.service.MemberPriceService;
import org.springframework.util.StringUtils;


/**
 * 会员价格
 *
 * @author david
 */
@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceDao, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<MemberPriceEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key).or().eq("sku_id", key).or().eq("member_level_id", key);
        }

        IPage<MemberPriceEntity> page = this.page(
                new Query<MemberPriceEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}