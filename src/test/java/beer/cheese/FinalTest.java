package beer.cheese;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class FinalTest {

    @Test
    public void testFinal() {
        System.out.println(returnValue());
    }

    public int returnValue() {
        int a = 3;
        try {
            a = 5;
            return a;
        } catch (Exception e) {
            a = 2;
            return a;
        } finally {
            a = 1;
//            return a;
        }
    }


    @Test
    public void testDynamic() {
        String h = new String("hello");
        System.out.println("hello".equals(h));
    }

    @Test
    public void testInterrupted() {
        Thread t = new Thread(() -> {
            boolean innterrupted;
            try {
                TimeUnit.SECONDS.sleep(3);
                innterrupted = Thread.interrupted();
                System.out.println(innterrupted);
                System.out.println(Thread.interrupted());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();

        try {
            TimeUnit.SECONDS.sleep(4);
            t.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        Thread t = new Thread(() -> {
            boolean interrupted;
            System.out.println("before park");
            interrupted = parkAndCheckInterrupt();
            System.out.println(interrupted);
        });
        t.start();
        try {
            TimeUnit.SECONDS.sleep(4);
            LockSupport.unpark(t);
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        System.out.println("parked");
        return Thread.interrupted();
    }

    @Test
    public void test2() throws InterruptedException {
        TackOne tackOne = new TackOne();

        Thread t = new Thread(tackOne, "t");
        Thread t2 = new Thread(tackOne, "t2");
        t2.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.start();
        t.interrupt();
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void test3() {
        int t = 2;
        System.out.println(t != (t = 3));
        String s = "hello world";
        char[] chars = s.toCharArray();
//        chars.toString();
    }

    @Test
    public void test4(){
        int result = twoFinal();
        System.out.println(result);
    }

    @Test
    public void labelBreak(){
        int i = 0;
        retry:
        for(;;){
            i = 0;
            for(;;){
                i++;
                if(i > 5)
                    break retry;
            }

        }
        System.out.println(i);
    }

    int twoFinal(){
        int t = 2;
        int i = 0;
        try{
            t = 3;
            try {
                t = t / i;
            }catch (Exception e){
                System.out.println("catch");
                throw e;
            }finally {
                System.out.println("inner finally");
            }
            return t;
        }catch (Exception e ){
            t = 4;
            return t;
        }finally {
            t = 5;
            System.out.println("outer finally");
//            return t;
        }
    }
    static class TackOne implements Runnable {
        Lock lock = new ReentrantLock();

        @Override
        public void run() {
            String name = Thread.currentThread().getName();

            System.out.println(name + "tryAcquire");
            lock.lock();
            System.out.println(name + "acquire");
            try {
                System.out.println(name + "before sleep");
                TimeUnit.SECONDS.sleep(6);
                System.out.println(name + "after sleep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
    }
}