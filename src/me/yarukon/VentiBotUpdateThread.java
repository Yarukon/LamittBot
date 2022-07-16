package me.yarukon;

public class VentiBotUpdateThread extends Thread {
    int tickrate;
    long threadTime = 0;

    public VentiBotUpdateThread(int tickrate) {
        super("VentiBot update loopthread");
        this.tickrate = tickrate;
    }

    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();
        final long OPTIMAL_TIME = 1000000000 / tickrate;
        long lastUpdateTime = 0;

        BotMain.INSTANCE.getLogger().info("更新线程已注册!");
        while (Yarukon.INSTANCE.isRunning) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;

            if (updateLength < OPTIMAL_TIME) {
                continue;
            }

            lastLoopTime = now;

            lastUpdateTime += updateLength;
            if (lastUpdateTime >= 1000000000) {
                lastUpdateTime = 0;
            }

            try {
                Yarukon.INSTANCE.update();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                threadTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
                Thread.sleep(threadTime);
            } catch (Exception ignored) {}
        }
    }
}