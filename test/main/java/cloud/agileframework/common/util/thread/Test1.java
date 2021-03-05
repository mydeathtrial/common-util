package cloud.agileframework.common.util.thread;

/**
 * @author 佟盟
 * 日期 2021-03-02 11:58
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class Test1 {
    private static boolean ready;

    private static class ReaderThread extends Thread {
        @Override
        public void run() {
            int number = 0;
            while (!ready) {
                number++;
                System.out.println(number);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new ReaderThread().start();
        Thread.sleep(1000);
        ready = true;
    }
}
