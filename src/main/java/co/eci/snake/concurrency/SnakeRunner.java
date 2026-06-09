package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Snake;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tarea (Runnable) encargada de controlar el ciclo de vida autónomo y el movimiento de una serpiente específica.
 * Diseñada para ejecutarse en su propio hilo independiente (en este caso, un hilo virtual).
 * Ejecuta turnos aleatorios ocasionales y procesa el efecto del Turbo acelerando el hilo.
 */
public final class SnakeRunner implements Runnable {
  private final Snake snake;
  private final Board board;
  private final int baseSleepMs = 80;
  private final int turboSleepMs = 40;
  private int turboTicks = 0;

  /**
   * Crea un nuevo gestor de ejecución para una serpiente.
   * @param snake Instancia lógica de la serpiente.
   * @param board Tablero sobre el cual se desplaza.
   */
  public SnakeRunner(Snake snake, Board board) {
    this.snake = snake;
    this.board = board;
  }

  /**
   * Ejecuta el ciclo continuo de movimiento de la serpiente mientras el hilo no sea interrumpido.
   * Modula la velocidad de retardo según el estado del Turbo e interrumpe el ciclo si muere por colisión.
   */
  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        maybeTurn();
        var res = board.step(snake);
        if (res == Board.MoveResult.HIT_OBSTACLE) {
          snake.kill();
          return;
        } else if (res == Board.MoveResult.ATE_TURBO) {
          turboTicks = 100;
        }
        int sleep = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        if (turboTicks > 0) turboTicks--;
        Thread.sleep(sleep);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Ejecuta giros autónomos y aleatorios periódicos.
   * Ignora a las serpientes de IDs de jugadores humanos (1 y 2).
   */
  private void maybeTurn() {
    // Las serpientes controladas por los jugadores (id 1 e id 2) no realizan giros aleatorios automáticos.
    if (snake.id() == 1 || snake.id() == 2) {
      return;
    }
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) randomTurn();
  }

  /**
   * Cambia la dirección de la serpiente a un sentido aleatorio del enumerador Direction.
   */
  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }
}
