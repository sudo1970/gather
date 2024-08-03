package com.cq.seckill.service;

import cn.hutool.core.bean.BeanUtil;
import com.cq.commons.constant.ApiConstant;
import com.cq.commons.constant.RedisKeyConstant;
import com.cq.commons.exception.ParameterException;
import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.pojo.SeckillVouchers;
import com.cq.commons.pojo.VoucherOrders;
import com.cq.commons.utils.AssertUtil;
import com.cq.commons.utils.ResultInfoUtil;
import com.cq.commons.vo.SignInAcctInfo;
import com.cq.seckill.mapper.SeckillVouchersMapper;
import com.cq.seckill.mapper.VoucherOrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillService {
    @Resource
    private SeckillVouchersMapper seckillVouchersMapper;
    @Resource
    private VoucherOrdersMapper voucherOrdersMapper;
    @Value("${service.name.as-oauth-server}")
    private String oauthServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DefaultRedisScript defaultRedisScript;
//    @Resource
//    private RedisLock redisLock;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 抢购代金券
     * redis解决问题
     * @param voucherId   代金券 ID
     * @param accessToken 登录token
     * @Para path 访问路径
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultInfo doSeckill(Integer voucherId, String accessToken, String path) {
        // 基本参数校验
        AssertUtil.isTrue(voucherId == null || voucherId < 0, "请选择需要抢购的代金券");
        AssertUtil.isNotEmpty(accessToken, "请登录");

        // 注释原始的 关系型数据库 的流程
        // 判断此代金券是否加入抢购
        // SeckillVouchers seckillVouchers = seckillVouchersMapper.selectVoucher(voucherId);
        // AssertUtil.isTrue(seckillVouchers == null, "该代金券并未有抢购活动");
        // 判断是否有效
        // AssertUtil.isTrue(seckillVouchers.getIsValid() == 0, "该活动已结束");

        // 采用 Redis
        String key = RedisKeyConstant.seckill_vouchers.getKey() + voucherId;
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        SeckillVouchers seckillVouchers = BeanUtil.mapToBean(map, SeckillVouchers.class, true, null);

        // 判断是否开始、结束
        Date now = new Date();
        AssertUtil.isTrue(now.before(seckillVouchers.getStartTime()), "该抢购还未开始");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "该抢购已结束");
        // 判断是否卖完
        AssertUtil.isTrue(seckillVouchers.getAmount() < 1, "该券已经卖完了");
        // 获取登录用户信息
        String url = oauthServerName + "user/me?access_token={accessToken}";
        log.info("url:::{}", url);
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 这里的data是一个LinkedHashMap，SignInAcctInfo
        SignInAcctInfo SignInAcctInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInAcctInfo(), false);
        // 判断登录用户是否已抢到(一个用户针对这次活动只能买一次)
        VoucherOrders order = voucherOrdersMapper.findDinerOrder(SignInAcctInfo.getId(),
                seckillVouchers.getFkVoucherId());
        AssertUtil.isTrue(order != null, "该用户已抢到该代金券，无需再抢");

        // 注释原始的 关系型数据库 的流程
        // 扣库存
        // int count = seckillVouchersMapper.stockDecrease(seckillVouchers.getId());
        // AssertUtil.isTrue(count == 0, "该券已经卖完了");

        // 使用 Redis 锁一个账号只能购买一次
        String lockName = RedisKeyConstant.lock_key.getKey()
                + SignInAcctInfo.getId() + ":" + voucherId;
        long expireTime = seckillVouchers.getEndTime().getTime() - now.getTime();

        // Redisson 分布式锁
        RLock lock = redissonClient.getLock(lockName);

        try {
            // Redisson 分布式锁处理
            boolean isLocked = lock.tryLock(expireTime, TimeUnit.MILLISECONDS);
            if (isLocked) {
                // 下单
                VoucherOrders voucherOrders = new VoucherOrders();
                voucherOrders.setFkDinerId(SignInAcctInfo.getId());
                // Redis 中不需要维护外键信息
                // voucherOrders.setFkSeckillId(seckillVouchers.getId());
                voucherOrders.setFkVoucherId(seckillVouchers.getFkVoucherId());
//                String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
//                voucherOrders.setOrderNo(orderNo);
                voucherOrders.setOrderType(1);
                voucherOrders.setStatus(0);
                long count = voucherOrdersMapper.save(voucherOrders);
                AssertUtil.isTrue(count == 0, "用户抢购失败");

                // 采用 Redis
                // 扣库存
                // count = redisTemplate.opsForHash().increment(key, "amount", -1);
                // AssertUtil.isTrue(count < 0, "该券已经卖完了");

                // 采用 Redis + Lua 解决问题
                // 扣库存
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.add("amount");
                Long amount = (Long) redisTemplate.execute(defaultRedisScript, keys);
                AssertUtil.isTrue(amount == null || amount < 1, "该券已经卖完了");
            }
        } catch (Exception e) {
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 自定义 Redis 解锁
            // redisLock.unlock(lockName, lockKey);

            // Redisson 解锁
            lock.unlock();
            if (e instanceof ParameterException) {
                return ResultInfoUtil.buildError(0, "该券已经卖完了", path);
            }
        }
        return ResultInfoUtil.buildSuccess(path, "抢购成功");
    }

    public void addSeckillVouchers(SeckillVouchers seckillVouchers) {
        // 非空校验
        AssertUtil.isTrue(seckillVouchers.getFkVoucherId() == null, "请选择需要抢购的代金券");
        AssertUtil.isTrue(seckillVouchers.getAmount() == 0, "请输入抢购总数量");
        Date now = new Date();
        AssertUtil.isNotNull(seckillVouchers.getStartTime(), "请输入开始时间");
        AssertUtil.isNotNull(seckillVouchers.getEndTime(), "请输入结束时间");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "结束时间不能早于当前时间");
        AssertUtil.isTrue(seckillVouchers.getStartTime().after(seckillVouchers.getEndTime()), "开始时间不能晚于结束时间");

        // redis
        String key = RedisKeyConstant.seckill_vouchers.getKey() + seckillVouchers.getFkVoucherId();
        log.info("key:::{}", key);
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        AssertUtil.isTrue(!map.isEmpty() && (int)map.get("amount") > 0, "该券已经拥有了抢购活动");

        // 插入redis
        seckillVouchers.setIsValid(1);
        seckillVouchers.setCreateDate(now);
        seckillVouchers.setUpdateDate(now);
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(seckillVouchers));

        // 插入数据库
         seckillVouchersMapper.save(seckillVouchers);
    }
}
