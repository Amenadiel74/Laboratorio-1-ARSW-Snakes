package co.eci.primefinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada para el ejercicio de calentamiento de la Parte I.
 * Inicializa y arranca hilos trabajadores (PrimeWorker) junto con un hilo de control (pauser)
 * que suspende la ejecución del cálculo de números primos periódicamente para imprimir resultados.
 */
public final class PrimeFinder {

    private static final int MAX_NUMBER = 100_000;
    private static final int NUM_THREADS = 4;
    private static final int PAUSE_INTERVAL_MS = 3_000;

    private PrimeFinder() {}

    /**
     * Punto de entrada principal para ejecutar la búsqueda de primos.
     * @param args Argumentos de consola (no se utilizan).
     * @throws InterruptedException Si ocurre una interrupción esperando por los hilos trabajadores.
     */
    public static void main(String[] args) throws InterruptedException {
        var counter = new PrimeCounter(NUM_THREADS);
        var workers = new ArrayList<Thread>();

        int chunk = MAX_NUMBER / NUM_THREADS;
        for (int i = 0; i < NUM_THREADS; i++) {
            int lo = i * chunk + 1;
            int hi = (i == NUM_THREADS - 1) ? MAX_NUMBER : (i + 1) * chunk;
            workers.add(new Thread(new PrimeWorker(lo, hi, counter), "worker-" + i));
        }

        System.out.println("=== PrimeFinder (wait/notify) ===");
        System.out.println("Range: 1 to " + MAX_NUMBER + ", workers: " + NUM_THREADS);
        System.out.println("Pause interval: " + PAUSE_INTERVAL_MS + " ms");
        System.out.println("Press ENTER after each pause to resume.\n");

        workers.forEach(Thread::start);

        var pauser = new Thread(() -> {
            var reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (!counter.allDone()) {
                    Thread.sleep(PAUSE_INTERVAL_MS);
                    if (counter.allDone()) break;
                    counter.pause();
                    System.out.println("\n[PAUSED] Primes found so far: " + counter.getCount());
                    System.out.print("Press ENTER to resume... ");
                    reader.readLine();
                    counter.resume();
                    System.out.println("Resumed.\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                System.err.println("IO error: " + e.getMessage());
            }
        }, "pauser");
        pauser.setDaemon(true);
        pauser.start();

        for (var t : workers) {
            t.join();
        }
        pauser.interrupt();

        System.out.println("\n=== DONE ===");
        System.out.println("Total primes found between 1 and " + MAX_NUMBER + ": " + counter.getCount());
    }
}
