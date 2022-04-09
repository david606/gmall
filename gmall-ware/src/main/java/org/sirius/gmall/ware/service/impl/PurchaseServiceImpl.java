package org.sirius.gmall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.sirius.common.constant.ProductConstant;
import org.sirius.common.constant.WareConstant;
import org.sirius.common.constant.WareConstant.PurchaseDetailStatusEnum;
import org.sirius.common.constant.WareConstant.PurchaseStatusEnum;
import org.sirius.gmall.ware.entity.PurchaseDetailEntity;
import org.sirius.gmall.ware.service.PurchaseDetailService;
import org.sirius.gmall.ware.service.WareSkuService;
import org.sirius.gmall.ware.vo.MergeVo;
import org.sirius.gmall.ware.vo.PurchaseDoneVo;
import org.sirius.gmall.ware.vo.PurchaseItemDoneVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.ware.dao.PurchaseDao;
import org.sirius.gmall.ware.entity.PurchaseEntity;
import org.sirius.gmall.ware.service.PurchaseService;

import javax.annotation.Resource;

import static org.sirius.common.constant.WareConstant.*;


/**
 * 采购单
 *
 * @author david
 */
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;
    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().eq("assignee_name", key);
            });
        }

        // 状态可以为０：状态新建为０
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }

        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageForUnReceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<PurchaseEntity>()
                .eq("status", PurchaseStatusEnum.CREATED)
                .or()
                .eq("status", PurchaseStatusEnum.ASSIGNED);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void mergePurchase(MergeVo mergeVo) {

        Long purchaseId = mergeVo.getPurchaseId();

        // 1.没有选择任何[采购单]，将自动创建新单进行合并。
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            // 设置采购单默认状态
            purchaseEntity.setStatus(PurchaseStatusEnum.CREATED.getCode());

            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            // 提取新建采购单的id
            purchaseId = purchaseEntity.getId();
        }

        List<Long> purchaseDetailIds = mergeVo.getItems();

        // ２．判断是否满足合并条件
        Collection<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(purchaseDetailIds);
        purchaseDetailEntities.forEach((item) -> {
            // 状态是否为：[新建]
            boolean isStatusCreated = item.getStatus().equals(PurchaseDetailStatusEnum.CREATED.getCode());
            // 状态是否为：[已分配]
            boolean isStatusAssigned = item.getStatus().equals(PurchaseDetailStatusEnum.ASSIGNED.getCode());
            if (!isStatusCreated && !isStatusAssigned) {
                throw new IllegalArgumentException("正在采购，无法进行分配");
            }
        });

        // 3.合并采购需求：将采购需求绑定到采购单
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = purchaseDetailIds.stream().map(detailId -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(detailId);
            // 3.1 设置（绑定）采购单Id
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            // 3.2 更新采购需要状态：［已分配］
            purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        // 3.3 批量更新
        purchaseDetailService.updateBatchById(collect);

        // 4.更新采购单：update_time
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {

        // 1.确认当前采购单是[新建]或者是[已分配]状态 & 更新为状态［已领取］
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            // 1.1 获取采购单实例
            PurchaseEntity purchase = this.getById(id);
            return purchase;

        }).filter(item -> {
            boolean isCreated = item.getStatus() == PurchaseStatusEnum.CREATED.getCode();
            boolean isAssigned = item.getStatus() == PurchaseStatusEnum.ASSIGNED.getCode();
            // 1.2 过滤状态为［新建］［已分配］
            return isAssigned || isCreated;

        }).peek(item -> {
            // 1.3 更新采购单状态为：［已领取］
            item.setStatus(PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
        }).collect(Collectors.toList());

        // 1.4 批量更新
        this.updateBatchById(purchaseEntities);

        // 2.更新采购单下的采购需求的状态为［购买中］
        purchaseEntities.forEach((item) -> {

            // 2.1 获取采购单下所有采购需求
            List<PurchaseDetailEntity> list = purchaseDetailService.listDetailByPurchaseId(item.getId());

            List<PurchaseDetailEntity> detailEntities = list.stream().map(detail -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(detail.getId());
                // 2.2 更新状态为：［购买中］
                purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;

            }).collect(Collectors.toList());

            // 2.3 批量更新采购需求
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Override
    public void done(PurchaseDoneVo doneVo) {

        // 采购单Id
        Long id = doneVo.getId();
        // 是否有异常采购需求
        boolean hasError = false;

        // 收集待更新的采购需求，批量更新
        List<PurchaseDetailEntity> toUpdatePurchaseDetails = new ArrayList<>();

        /*
         * 1.遍历采购项（采购需求）
         * 1)．如果存在采购异常，则设置状态为异常
         * ２).如果采购项无异常，设置状态为完成．根据采购需求Id,获取采购需求（sku/ware/skuNum）更新库存
         * ３).将实例放到待更新列表，等待批量更新.
         */
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            hasError = item.getStatus().equals(PurchaseDetailStatusEnum.HASERROR.getCode());

            if (!hasError) {
                // 如果采购需求没有异常，则采购完成，更新采购需求状态为［已完成］
                purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.FINISH.getCode());
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                // 采购成功后，更新商品库存
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            } else {
                // 采购状态异常，设置状态［异常］
                purchaseDetailEntity.setStatus(item.getStatus());
            }
            purchaseDetailEntity.setId(item.getItemId());
            toUpdatePurchaseDetails.add(purchaseDetailEntity);
        }

        // 批量更新
        purchaseDetailService.updateBatchById(toUpdatePurchaseDetails);

        // 2.更新采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        // 设置采购单状态
        purchaseEntity.setStatus(hasError ? PurchaseStatusEnum.HASERROR.getCode() : PurchaseStatusEnum.FINISH.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}