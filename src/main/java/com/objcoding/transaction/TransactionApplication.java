package com.objcoding.transaction;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class TransactionApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApplication.class, args);
    }

    @Autowired
    private ForupdateMapper forupdateMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final ReentrantLock reentrantLock = new ReentrantLock();


    @Override
    public void run(String... args) throws Exception {
//        forupdateByTransaction();
        forupdateByConcurrent();
//        forupdateByConcurrentAndTransaction();
    }

    /**
     * for update加Spring事务，并且不提交事务
     * 这个情况肯定阻塞
     */
    private void forupdateByTransaction() throws Exception {

        // 主线程获取独占锁
        reentrantLock.lock();

        new Thread(() ->
                transactionTemplate.execute(transactionStatus -> {
                    this.forupdateMapper.findByName("testforupdate");
                    System.out.println("==========for update==========");
                    countDownLatch.countDown();
                    // 阻塞不让提交事务
                    reentrantLock.lock();
                    return null;
                })).start();

        countDownLatch.await();
        System.out.println("==========for update has countdown==========");
        this.forupdateMapper.updateByName("testforupdate");
        System.out.println("==========update success==========");

        reentrantLock.unlock();
    }

    /**
     * 并发执行for udpate不加Spring事务，每条sql的执行只有mybatis的jdbc事务
     * <p>
     * mysql
     * autocommit=true：不会阻塞
     * autocommit=false：不会阻塞
     * <p>
     * oracle
     * autocommit=false：如果有两个以上ID不同的connection对象并发执行for udpate，会发生阻塞
     */
    private void forupdateByConcurrent() {

        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                this.forupdateMapper.findByName("testforupdate");
                System.out.println("========ok:" + atomicInteger.getAndIncrement());
            }).start();
        }

    }


    /**
     * 并发执行for udpate加Srping事务
     * <p>
     * mysql
     * autocommit=true：不会阻塞
     * autocommit=false：不会阻塞
     * <p>
     * oracle
     * autocommit=false：不会阻塞
     */
    private void forupdateByConcurrentAndTransaction() {

        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < 100; i++) {
            new Thread(() ->
                    transactionTemplate.execute(transactionStatus -> {
                        this.forupdateMapper.findByName("testforupdate");
                        System.out.println("========ok:" + atomicInteger.getAndIncrement());
                        return null;
                    })).start();
        }
    }
}
