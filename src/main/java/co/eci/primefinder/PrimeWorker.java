package co.eci.primefinder;

/**
 * Hilo de trabajo que busca y calcula números primos dentro de un rango numérico específico.
 * En cada iteración verifica si se ha solicitado una pausa a través del monitor compartido (PrimeCounter).
 */
public final class PrimeWorker implements Runnable {
    private final int start;
    private final int end;
    private final PrimeCounter counter;

    /**
     * Constructor para PrimeWorker.
     * @param start Límite inferior del rango de búsqueda.
     * @param end Límite superior del rango de búsqueda.
     * @param counter Instancia del contador de primos compartido (monitor).
     */
    public PrimeWorker(int start, int end, PrimeCounter counter) {
        this.start = start;
        this.end = end;
        this.counter = counter;
    }

    /**
     * Ejecuta el ciclo de cálculo de números primos dentro del rango.
     * Verifica periódicamente si el hilo supervisor ha solicitado una pausa.
     */
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

    /**
     * Determina si un número dado es primo.
     * @param n Número a evaluar.
     * @return true si es primo, false en caso contrario.
     */
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
