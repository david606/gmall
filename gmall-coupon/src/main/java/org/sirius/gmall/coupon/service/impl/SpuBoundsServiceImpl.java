package org.sirius.gmall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.coupon.dao.SpuBoundsDao;
import org.sirius.gmall.coupon.entity.SpuBoundsEntity;
import org.sirius.gmall.coupon.service.SpuBoundsService;
import org.springframework.util.StringUtils;


/**
 * 商品积分
 *
 * @author david
 */
@Service("spuBoundsService")
public class SpuBoundsServiceImpl extends ServiceImpl<SpuBoundsDao, SpuBoundsEntity> implements SpuBoundsService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuBoundsEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key).or().eq("spu_id", key);
        }

        IPage<SpuBoundsEntity> page = this.page(
                new Query<SpuBoundsEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}