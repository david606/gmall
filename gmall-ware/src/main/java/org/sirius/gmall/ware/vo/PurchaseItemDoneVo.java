package org.sirius.gmall.ware.vo;

import lombok.Data;

/**
 * 采购项（需求）
 * <pre>
 * 未必所有商品（采购项）都能采购成功, 所以，每一商品有　status/reason;
 * 状态是成功或异常，如果异常要说明原因：如无货/货品不足/其它原因．
 * </pre>
 *
 * @author david
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;

    /**
     * 采购项（需求）状态
     * 采购完成; 采购异常
     */
    private Integer status;

    /**
     * 采购项，采购失败原因
     */
    private String reason;

}
