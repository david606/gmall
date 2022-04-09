package org.sirius.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.sirius.gmall.product.entity.CategoryBrandRelationEntity;
import org.sirius.gmall.product.service.CategoryBrandRelationService;
import org.sirius.gmall.product.vo.Catelog2Vo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.Query;

import org.sirius.gmall.product.dao.CategoryDao;
import org.sirius.gmall.product.entity.CategoryEntity;
import org.sirius.gmall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * @author david
 */
@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        /*
         2 组装成父子的树形结构
         2.1 找到所有的一级分类
        */
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0).peek((menu) -> menu.setChildren(getChildren(menu, entities))).sorted((menu1, menu2) -> {
            // 菜单排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }


    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).peek(categoryEntity -> {
            // 1、找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
        }).sorted((menu1, menu2) -> {
            // 2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();

        // 递归查询是否还有父节点：查找过程是根据子找父，所以最后返回结果应该是: [孙/子/父]
        List<Long> parentPath = findParentPath(catalogId, paths);

        // 进行一个逆序排列，结果变成: [父/子/孙]
        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 递归获取父路径
     *
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {

        // 1、收集当前节点id
        paths.add(catelogId);
        // 根据当前分类id查询信息
        CategoryEntity byId = this.getById(catelogId);

        // 如果当前不是父分类
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) throws RuntimeException {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        List<CategoryBrandRelationEntity> categoryBrandRelation = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().in("catelog_id", asList));

        if (categoryBrandRelation.size() == 0) {
            // 逻辑删除
            baseMapper.deleteBatchIds(asList);
        } else {
            List<String> brands = new ArrayList<>();
            categoryBrandRelation.forEach(relation -> {
                brands.add(relation.getBrandName());
            });
            log.warn("该菜单下面还有属性，无法删除!");
            throw new RuntimeException("该菜单下面还有属性:" + brands + "，无法删除!");
        }
    }



    /*
     * 级联更新所有关联的数据
     * @CacheEvict:失效模式
     * 1、同时进行多种缓存操作  @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀
     * @param category
     *         @Caching(evict = {
     *         @CacheEvict(value = "category",key = "'getLevel1Categories'"),
     *         @CacheEvict(value = "category",key = "'getCatalogJson'")
     */

    /**
     * 更新后也会清除缓存
     *
     * @param category
     */
    @CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.baseMapper.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /*
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 2、 @Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *      1）、如果缓存中有，方法不用调用。
     *      2）、key默认自动生成；缓存的名字::SimpleKey [](自主生成的key值)
     *      3）、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4）、默认ttl时间 -1；
     *
     *    自定义：
     *      1）、指定生成的缓存使用的key：  key属性指定，接受一个SpEL
     *             SpEL的详细https://docs.spring.io/spring/docs/5.1.12.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *      2）、指定缓存的数据的存活时间： 配置文件中修改ttl
     *      3）、将数据保存为json格式:
     *              自定义RedisCacheConfiguration即可
     * 4、Spring-Cache的不足；
     *      1）、读模式：
     *          缓存穿透：查询一个null数据。解决：缓存空数据；ache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；？默认是无加锁的;sync = true（加锁，解决击穿）
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间。：spring.cache.redis.time-to-live=3600000
     *      2）、写模式：（缓存与数据库一致）
     *          1）、读写加锁。
     *          2）、引入Canal，感知到MySQL的更新去更新数据库
     *          3）、读多写多，直接去数据库查询就行
     *    总结：
     *      常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用Spring-Cache；写模式（只要缓存的数据有过期时间就足够了）
     *      特殊数据：特殊设计
     *
     *   原理：
     *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     */

    /**
     * 获取三级分类（一级根目录）
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Category() {
        log.info("{} method query from db ... ", "getLevel1Category");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        log.info("{} method query from db ...", "getCatalogJson");

        // 1.将数据库的多次查询变为一次
        List<CategoryEntity> allCategories = this.baseMapper.selectList(null);

        // ２.查出所有[一级]分类
        List<CategoryEntity> firstLevelCategories = getCategoriesByParent(allCategories, 0L);

        // 3.遍历所有一级分类，过滤出二级分类/三级分类并封装
        Map<String, List<Catelog2Vo>> secondaryLevelCatelogVos = firstLevelCategories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), firstLevelCategoryVal -> {

            List<Catelog2Vo> catelog2Vos = getCatelog2Vos(allCategories, firstLevelCategoryVal.getCatId());

            return catelog2Vos;
        }));

        return secondaryLevelCatelogVos;
    }

    /**
     * getCatelogJson：获取［第二级］类目　Json
     *
     * @param allCategories         全部分类列表
     * @param firstLevelCategoryCid [第一级]类目Id
     * @return
     */
    private List<Catelog2Vo> getCatelog2Vos(List<CategoryEntity> allCategories, Long firstLevelCategoryCid) {

        // 1.过滤出一级分类下全部二级分类
        List<CategoryEntity> secondaryLevelCategories = getCategoriesByParent(allCategories, firstLevelCategoryCid);

        // 2.封装二级分类到　Catelog2Vo
        if (secondaryLevelCategories == null) {
            return null;
        }

        List<Catelog2Vo> catelog2Vos = secondaryLevelCategories.stream().map(secondaryCategory -> {

            Catelog2Vo catelog2Vo = Catelog2Vo.builder().catalog1Id(firstLevelCategoryCid.toString()).catalog3List(null).id(secondaryCategory.getCatId().toString()).name(secondaryCategory.getName()).build();

            List<Catelog2Vo.Category3Vo> category3Vos = getCategory3Vos(allCategories, secondaryCategory.getCatId());
            catelog2Vo.setCatalog3List(category3Vos);

            return catelog2Vo;
        }).collect(Collectors.toList());

        return catelog2Vos;
    }

    /**
     * getCatelogJson：获取［第三级］类目　Json
     *
     * @param allCategories        整个分类列表
     * @param secondaryCategoryCid ［第二级］分类Id
     * @return
     */
    private List<Catelog2Vo.Category3Vo> getCategory3Vos(List<CategoryEntity> allCategories, Long secondaryCategoryCid) {

        // 1.找当前二级分类的三级分类封装成vo
        List<CategoryEntity> thirdLevelCategories = getCategoriesByParent(allCategories, secondaryCategoryCid);

        // 2.封闭三级分类到　Catelog2Vo.Category3Vo
        if (thirdLevelCategories == null) {
            return null;
        }

        List<Catelog2Vo.Category3Vo> category3Vos = thirdLevelCategories.stream().map(thirdLevelCategory -> {

            Catelog2Vo.Category3Vo category3Vo = Catelog2Vo.Category3Vo.builder().catalog2Id(secondaryCategoryCid.toString()).id(thirdLevelCategory.getCatId().toString()).name(thirdLevelCategory.getName()).build();

            return category3Vo;
        }).collect(Collectors.toList());
        return category3Vos;
    }


    /**
     * 在全部分类列表，根据给定的parentId 找到全部子分类项
     *
     * @param allCategories 全部分类列表
     * @param parentId      父分类Id
     * @return 子分类列表
     */
    private List<CategoryEntity> getCategoriesByParent(List<CategoryEntity> allCategories, Long parentId) {
        return allCategories.stream().filter(item -> item.getParentCid().equals(parentId)).collect(Collectors.toList());
    }


    /*
    产生堆外内存溢出：OutOfDirectMemoryError
    1.springboot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信。
    2.lettuce的bug导致netty堆外内存溢出 -Xmx300m；netty如果没有指定堆外内存，默认使用-Xmx300m
       可以通过-Dio.netty.maxDirectMemory进行设置

    Jedis 是一个优秀的基于 Java 语言的 Redis 客户端，但是，其不足也很明显：Jedis 在实现上是直接连接 Redis-Server，
    在多个线程间共享一个 Jedis 实例时是线程不安全的，如果想要在多线程场景下使用 Jedis，
    需要使用连接池，每个线程都使用自己的 Jedis 实例，当连接数量增多时，会消耗较多的物理资源。

    与 Jedis 相比，Lettuce 则完全克服了其线程不安全的缺点：Lettuce 是一个可伸缩的线程安全的 Redis 客户端，支持同步、异步和响应式模式。
    多个线程可以共享一个连接实例，而不必担心多线程并发问题。它基于优秀 Netty NIO 框架构建，支持 Redis 的高级功能，
    如 Sentinel，集群，流水线，自动重新连接和 Redis 数据模型。

    解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
    1.升级lettuce客户端。   2.切换使用jedis
    redisTemplate：
    lettuce、jedis操作redis的底层客户端。Spring再次封装redisTemplate；
    */


    /**
     * 为测试准备常量
     */
    private final String LOCK_KEY = "category-lock";
    private final String CACHE_KEY = "category::catelogJson";

    /**
     * 测试：通过　RedisTemplate 实现缓存
     * <pre>
     * 1.缓存对象：将对象序列化成JSON串，放到缓存
     * ２.获取缓存：从缓存获取的JSON串，反序列化成对象.
     * 3.JSON 语言跨语言，跨平台兼容．
     * </pre>
     * 测试整体逻辑：
     * <pre>
     * 1.获取数据，先从缓存中获取
     * ２．缓存中没有数据，再查询数据库（得到结果后，会放入缓存）
     * ３．查询数据库加分布式锁（分布式并发访问）
     * ----1）自己使用　Redis 实现分布式锁;
     * ----2) 使用　Redisson 封装的分布式锁.
     * </pre>
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonTest() {
        /*
         * 1、空结果缓存：解决缓存穿透问题
         * 2、设置过期时间(加随机值)：解决缓存雪崩
         * 3、加锁：解决缓存击穿问题
         */
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String catelogJson = opsForValue.get(CACHE_KEY);

        if (StringUtils.isEmpty(catelogJson)) {
            log.info("method {}: There is no data in the cache, go to query the database.", "getCatalogJsonTest()");
            // Map<String, List<Catelog2Vo>> cateLogJson = getCateLogJsonFromDbWithRedisLock();
            Map<String, List<Catelog2Vo>> cateLogJson = getCateLogJsonFromDbWithRedissonLock();
            return cateLogJson;
        }

        log.info("method {} : Cache the data you want, get it from here.", "getCatalogJsonTest()");
        Map<String, List<Catelog2Vo>> cateLog2Vos = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return cateLog2Vos;
    }

    /**
     * 测试：查询数据库
     * <pre>
     * 自己封装 Redis 分布式锁
     * </pre>
     *
     * @return
     */
    @SneakyThrows
    public Map<String, List<Catelog2Vo>> getCateLogJsonFromDbWithRedisLock() {

        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        // lock value
        final String uuid = UUID.randomUUID().toString();
        /*
         * 1.抢占锁: 抢占锁和设置过期时间必须同步，要保存原子性.
         * 抢锁成功，但是没能设置过期时间，如果服务宕机，锁会一直存在，出现死锁;
         */
        Boolean isLocked = opsForValue.setIfAbsent(LOCK_KEY, uuid, 300, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isLocked)) {

            try {
                // 2.查询 DB
                log.info("method {}: Successfully grab the {} lock. Now I can query the database.", "getCateLogJsonFromDbWithRedisLock()", LOCK_KEY);
                return getCatelogJsonFromDb();

            } finally {
                log.info("method {}：Release the {} lock.", LOCK_KEY, "getCateLogJsonFromDbWithRedisLock()");
                // 3.释放锁(删除锁)
                // hardCodeReleaseRedisLock(uuid);
                scriptCodeReleaseRedisLock(uuid);
            }

        } else {
            // 自旋锁：如果抢占锁没有成功，休眠后再次去获取锁
            TimeUnit.MILLISECONDS.sleep(100);
            getCateLogJsonFromDbWithRedisLock();
        }
        return null;
    }

    /**
     * 通过 Lua 脚本释放锁，保证操作原子性．
     *
     * @param uuid 抢占锁时设置，在本地生成的 uuid 做为锁的 value
     */
    private void scriptCodeReleaseRedisLock(String uuid) {
        /*
         * 脚本执行内容：
         * 如果根据传入KEY获取的VALUE 与传入的值相等，则执行删除 KEY 对应的记录
         */
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        /*
         * 参数１：要执行脚本
         * 参数２：传入KEY
         * 参数３：传入值
         */
        stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), List.of(LOCK_KEY), uuid);
    }

    /**
     * 通过硬编码方式去释放锁
     * <pre>
     * 逻辑：
     * １．根据抢占锁时设置　key,获取相应的　value　（uuid）
     * 2. 和本地的uuid对比，是否是同一个．如果是则删除（释放锁）;否则不执行删除．
     *
     * 问题：
     * 从Redis获取到value后：比对value与本地uuid，再删除锁 ,并不能保证是原子操作．
     * 1.有可能比对成功后，同时锁也到了过期时间，锁自动释放．
     * 2.锁自动释放后，其它线程抢占到锁．
     * 3.释放锁的代码，执行到了删除这一步．这时再删除的，就是别人的锁．
     * </pre>
     *
     * @param uuid 抢占锁时设置，在本地生成的 uuid 做为锁的 value
     */
    private void hardCodeReleaseRedisLock(String uuid) {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String lockVal = opsForValue.get(LOCK_KEY);
        if (uuid.equals(lockVal)) {
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }


    /**
     * 测试：查询数据库
     * <pre>
     * 使用 Redisson 封装的分布式锁
     * </pre>
     *
     * @return
     */
    @SneakyThrows
    public Map<String, List<Catelog2Vo>> getCateLogJsonFromDbWithRedissonLock() {

        /*
         * ReadWriteLock维护一对关联的locks ，一个用于只读操作，一个用于写入。
         * -- 1.只要没有写者， read lock可能被多个读线程同时持有。
         * -- 2.write lock是独占的。在非公平模式下工作。因此，未指定读写锁定的顺序。
         *
         * redissonClient.getReadWriteLock(LOCK_KEY)
         * -- 1.按名称返回 ReadWriteLock 实例。
         * -- 2.为了提高故障转移期间的可靠性，所有操作都等待传播到所有 Redis 从站。
         *
         * RLock
         * -- 基于 Redis 的Lock实现实现可重入锁。
         */

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_KEY);

        Map<String, List<Catelog2Vo>> data;
        // 获取读锁
        RLock readLock = readWriteLock.readLock();

        try {
            // 如果锁不可用，则当前线程将被禁用以用于线程调度目的并处于休眠状态，直到获得锁为止。
            readLock.lock();
            log.info("method {}: Successfully grab the {} lock. Now I can query the database.", "getCateLogJsonFromDbWithRedissonLock()", LOCK_KEY);

            data = getCatelogJsonFromDb();

        } finally {
            // Lock实现通常会对哪个线程可以释放锁施加限制（通常只有锁的持有者可以释放它）
            log.info("method {}：Release the {} lock.", LOCK_KEY, "getCateLogJsonFromDbWithRedissonLock()");
            // 加上这个睡眼是有时间看看它在　redis　里放了什么锁
            // TimeUnit.SECONDS.sleep(30);
            readLock.unlock();
        }
        return data;
    }

    /**
     * 测试：从数据库查询　CatelogJSON
     * <pre>
     * 1.先判断缓存中是否相应数据，如果有直接返回缓存数据．
     * 2.如果没有，查询数据库．
     * 3.查询结果，设置到缓存．
     * </pre>
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {

        // 获取锁后，在查询DB之前，再确认一下缓存是否有数据，可避免一次查询．
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        log.info("method {}：Double check whether there is matching data in the cache.", "getCatelogJsonFromDb()");
        String catelogJson = opsForValue.get(CACHE_KEY);

        if (!StringUtils.isEmpty(catelogJson)) {
            log.info("method {}：The cache has data again, get it from here!", "getCatelogJsonFromDb()");
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }

        log.info("method {}：There is still no data  and now query db ...", "getCatelogJsonFromDb()");
        // 1.将数据库的多次查询变为一次
        List<CategoryEntity> allCategories = this.baseMapper.selectList(null);

        // ２.查出所有[一级]分类
        List<CategoryEntity> firstLevelCategories = getCategoriesByParent(allCategories, 0L);

        // 3.遍历所有一级分类，过滤出二级分类/三级分类并封装
        Map<String, List<Catelog2Vo>> secondaryLevelCatelogVos = firstLevelCategories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), firstLevelCategoryVal -> {

            List<Catelog2Vo> catelog2Vos = getCatelog2Vos(allCategories, firstLevelCategoryVal.getCatId());

            return catelog2Vos;
        }));

        // 从数据库查询出结果后，直接放到缓存中
        log.info("method {}：Put the query result in the cache...", "getCatelogJsonFromDb()");
        String catelogJsonString = JSON.toJSONString(secondaryLevelCatelogVos);
        opsForValue.set(CACHE_KEY, catelogJsonString);
        log.info("method {}：Successfully cached data.", "getCatelogJsonFromDb()");

        return secondaryLevelCatelogVos;
    }
}