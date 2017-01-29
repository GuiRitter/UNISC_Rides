package main;

/**
 * Keeps the JavaScript server from dying.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings("SleepWhileInLoop")
public final class KeepAliver implements Runnable {

    private final long countTime = 150000;

    private final Handler handler;

    private long limitTime = 0;

    private long remainingTime = 0;

    public KeepAliver (Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run () {
        while (true) {
            limitTime = System.currentTimeMillis() + countTime;
            while (true) {
                remainingTime = limitTime - System.currentTimeMillis();
                if (remainingTime > 0) {
                    try {
                        Thread.sleep(remainingTime);
                    } catch (InterruptedException ex) {}
                } else {
                    handler.send("");
                    break;
                }
            }
        }
    }
}
