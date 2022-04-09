package org.sirius.gmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 完成的采购单
 * @author david
 */
@Data
public class PurchaseDoneVo {
    /**
     * 采购单id
     */
//    @NotNull(message = "id不允许为空")
    private Long id;

    /**
     * 采购项(需求)
     */
    private List<PurchaseItemDoneVo> items;

}
