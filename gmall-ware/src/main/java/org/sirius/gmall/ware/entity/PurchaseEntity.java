package org.sirius.gmall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 采购信息（采购的整体情况）
 * <pre>
 * １．分配给谁去采购（责任到人）
 * ２．采购人的电话（联系方式）
 * ３．优先级（紧急程度）
 * ４．采购状态
 * ５．采购到哪个仓库
 * ６．采购数量
 *
 * 采购单可以合并：如属于一仓库的货物，可以合并到一起采购．
 * 只有在［新建］和［已经分配］状态下才能合并．
 * </pre>
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 16:27:27
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    /**
     * 采购人Id
     */
    private Long assigneeId;
    /**
     * 　采购人姓名
     */
    private String assigneeName;
    /**
     * 采购人联系电话
     */
    private String phone;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 新建 0;
     * 已分配 1;
     * 已领取 2(正在采购);
     * 已完成 3;
     * 有异常 4(采购失败);
     */
    private Integer status;
    /**
     * 采购到哪个仓库
     */
    private Long wareId;
    /**
     * 采购数量
     */
    private BigDecimal amount;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}
