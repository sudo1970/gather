 架构springCloud
 
 详细模块如下：
 
  1、ga-eureka (注册中心)
  
  2、ga-gateway （网关）
  
  整合sentinel实现限流功能；
  
  3、commons （常量 + 共用model + 统一异常处理）

  4、ga-account-manger （用户中心）
  
  添加使用easyexcel导出excel文件功能；
  
  5、ga-oauth2-server（权限模块）
  
  6、ga-seckill（秒杀模块）
     根据redis实现秒杀功能

=======
 架构模块
 1、commons （常量 + 共用model + 统一异常处理）
 2、as-gateway （网关）
 3、as-eureka (注册中心)

 业务模块
 1、用户中心模块（as-user）
    添加秒杀功能
   添加使用easyexcel导出excel文件功能
   
 2、任务营销中心模块（as-marketing-center）
 3、营销任务下单模块（as-task-order）
 4、oauth2权限模块（as-oauth2-server）