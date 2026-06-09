package co.eci.primefinder;

/**
 * Hilo de trabajo que busca y calcula números primos dentro de un rango numérico específico.
 * En cada iteración verifica si se ha solicitado una pausa a través del monitor compartido (PrimeCounter).
 */
public final class PrimeWorker implements Runnable {
    private final int start;
    private final int end;
    private final PrimeCounter counter;

    public PrimeWorker(int start, int end, PrimeCounter counter) {
        this.start = start;
        this.end = end;
        this.counter = counter;
    }

    @Override
    public void run() {
        try {
            for (int i = start; i <= end; i++) {
                counter.checkPause();
                if (isPrime(i)) {
                    counter.increment();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            counter.workerDone();
        }
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
