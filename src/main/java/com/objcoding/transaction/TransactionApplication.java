package com.objcoding.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final ReentrantLock reentrantLock = new ReentrantLock();


    @Override
    public void run(String... args) throws Exception {
//        forupdate();
//        forupdateByTransaction();
        forupdateByConcurrent();
    }

    // for update不加事务
    private void forupdate() throws Exception {

        new Thread(() -> {
            this.forupdateMapper.findByName("testforupdate");
            System.out.println("==========for update==========");
            countDownLatch.countDown();
        }).start();

        countDownLatch.await();
        System.out.println("==========for update has countdown==========");
        this.forupdateMapper.updateByName("testforupdate");
        System.out.println("==========update success==========");

    }

    // for update加事务，并且不提交事务
    private void forupdateByTransaction() throws Exception {

        reentrantLock.lock();

        new Thread(() ->
            transactionTemplate.execute(transactionStatus -> {
                this.forupdateMapper.findByName("testforupdate");
                System.out.println("==========for update==========");
                countDownLatch.countDown();
                reentrantLock.lock(); // 阻塞不让提交事务
                return null;
            })).start();

        countDownLatch.await();
        System.out.println("==========for update has countdown==========");
        this.forupdateMapper.updateByName("testforupdate");
        System.out.println("==========update success==========");

        reentrantLock.unlock();
    }

    // 并发执行for udpate
    private void forupdateByConcurrent() {

        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < 100; i++) {
            this.forupdateMapper.findByName("testforupdate");
            System.out.println("========ok:" + atomicInteger.getAndIncrement());
        }
    }

}
