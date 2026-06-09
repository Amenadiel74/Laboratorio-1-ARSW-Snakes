package co.eci.primefinder;

/**
 * Monitor e indicador de estado compartido para sincronizar el conteo de primos y pausar/reanudar
 * los hilos trabajadores de forma segura mediante synchronized y wait/notifyAll.
 */
public final class PrimeCounter {
    private int count = 0;
    private int workersDone = 0;
    private int totalWorkers;
    private boolean paused = false;

    /**
     * Constructor para PrimeCounter.
     * @param totalWorkers Número total de hilos trabajadores registrados.
     */
    public PrimeCounter(int totalWorkers) {
        this.totalWorkers = totalWorkers;
    }

    /**
     * Incrementa de manera segura el contador de primos encontrados.
     */
    public synchronized void increment() {
        count++;
    }

    /**
     * Obtiene la cantidad actual de números primos encontrados.
     * @return El contador acumulado de primos.
     */
    public synchronized int getCount() {
        return count;
    }

    /**
     * Comprueba si el sistema está pausado. Si es así, bloquea el hilo llamando a wait().
     * Se ejecuta en un bucle while para evitar despertares espurios (Lost Wakeups).
     * @throws InterruptedException Si ocurre una interrupción durante el wait.
     */
    public synchronized void checkPause() throws InterruptedException {
        while (paused) {
            wait();
        }
    }

    /**
     * Activa el estado de pausa.
     */
    public synchronized void pause() {
        paused = true;
    }

    /**
     * Desactiva el estado de pausa y notifica a todos los hilos suspendidos para reanudar.
     */
    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    /**
     * Registra que un hilo de trabajo ha terminado su labor.
     * Si todos los trabajadores han finalizado, notifica a los hilos en espera.
     */
    public synchronized void workerDone() {
        workersDone++;
        if (workersDone >= totalWorkers) {
            notifyAll();
        }
    }

    /**
     * Comprueba si todos los hilos trabajadores han terminado su rango asignado.
     * @return true si todos los hilos finalizaron, false de lo contrario.
     */
    public synchronized boolean allDone() {
        return workersDone >= totalWorkers;
    }
}
