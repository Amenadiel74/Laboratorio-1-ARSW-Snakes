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

  public SnakeRunner(Snake snake, Board board) {
    this.snake = snake;
    this.board = board;
  }

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

  private void maybeTurn() {
    // Las serpientes controladas por los jugadores (id 1 e id 2) no realizan giros aleatorios automáticos.
    if (snake.id() == 1 || snake.id() == 2) {
      return;
    }
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) randomTurn();
  }

  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }
}
