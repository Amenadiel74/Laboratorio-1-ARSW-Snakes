package co.eci.primefinder;

public final class PrimeCounter {
    private int count = 0;
    private int workersDone = 0;
    private int totalWorkers;
    private boolean paused = false;

    public PrimeCounter(int totalWorkers) {
        this.totalWorkers = totalWorkers;
    }

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }

    public synchronized void checkPause() throws InterruptedException {
        while (paused) {
            wait();
        }
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized void workerDone() {
        workersDone++;
        if (workersDone >= totalWorkers) {
            notifyAll();
        }
    }

    public synchronized boolean allDone() {
        return workersDone >= totalWorkers;
    }
}
