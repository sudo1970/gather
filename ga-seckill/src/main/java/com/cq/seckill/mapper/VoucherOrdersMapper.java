package com.cq.seckill.mapper;

import com.cq.commons.pojo.VoucherOrders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 代金券订单 Mapper
 */
public interface VoucherOrdersMapper {

    // 根据 ID 和秒杀 ID 查询代金券订单
    @Select("select id, order_no, fk_voucher_id, fk_diner_id, qrcode, payment," +
            " status, fk_seckill_id, order_type, create_date, update_date, " +
            " is_valid from t_voucher_orders where fk_diner_id = #{dinerId} " +
            " and fk_voucher_id = #{voucherId} and is_valid = 1 and status between 0 and 1 ")
    VoucherOrders findDinerOrder(@Param("dinerId") Integer dinerId,
                                 @Param("voucherId") String voucherId);

    // 新增代金券订单
    @Insert("insert into t_voucher_orders (fk_voucher_id, fk_diner_id, " +
            " status, fk_seckill_id, order_type, create_date, update_date,  is_valid, order_no)" +
            " values (#{fkVoucherId}, #{fkDinerId}, #{status}, #{fkSeckillId}," +
            " #{orderType}, now(), now(), 1, #{orderNo})")
    int save(VoucherOrders voucherOrders);

}