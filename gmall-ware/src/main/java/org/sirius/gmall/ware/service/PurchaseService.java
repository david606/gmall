package org.sirius.gmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sirius.common.utils.PageUtils;
import org.sirius.gmall.ware.entity.PurchaseEntity;
import org.sirius.gmall.ware.vo.MergeVo;
import org.sirius.gmall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询未领取的采购单列表
     * <pre>
     * 采购单状态：创建　0; 已分配　１
     * </pre>
     *
     * @param params
     * @return
     */
    PageUtils queryPageForUnReceive(Map<String, Object> params);

    /**
     * 合并采购需求
     * <pre>
     * １．如果没有采购单，则新创建一个，且设置状态为：新建
     * ２．合并采购需求
     * -- 1). 判断需要合并的采购需求状态为：［新建/已分配］，才能够合并
     * -- 2). 将每一个采购需求与采购单绑定，并设置采购需求状态：［已分配］
     * -- 3). 更新当前采购单的　update_time
     * </pre>
     *
     * @param mergeVo
     */
    void mergePurchase(MergeVo mergeVo);

    /**
     * 领取采购单
     *
     * @param ids 采购单列表
     */
    void received(List<Long> ids);

    /**
     * 完成采购单
     *
     * @param doneVo
     */
    void done(PurchaseDoneVo doneVo);
}

