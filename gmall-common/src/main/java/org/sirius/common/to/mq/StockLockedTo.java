package org.sirius.common.to.mq;

import lombok.Data;

/**
 * 发送到mq消息队列的to
 * @author david
 */
@Data
public class StockLockedTo {

    /** 库存工作单的id **/
    private Long id;

    /** 工作单详情的所有信息 **/
    private StockDetailTo detailTo;
}
