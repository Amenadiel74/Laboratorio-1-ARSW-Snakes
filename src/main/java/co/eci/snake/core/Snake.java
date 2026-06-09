package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entidad de dominio que representa a una serpiente individual en el juego.
 * Mantiene la lista sincronizada de posiciones de su cuerpo, su estado de vida (viva o muerta),
 * su orden cronológico de muerte mediante un contador atómico y su dirección de movimiento.
 */
public final class Snake {
  private static final AtomicInteger idCounter = new AtomicInteger(0);
  private static final AtomicInteger deathCounter = new AtomicInteger(0);

  private final int id = idCounter.incrementAndGet();
  private final Deque<Position> body = new ArrayDeque<>();
  private volatile Direction direction;
  private int maxLength = 5;
  private volatile boolean alive = true;
  private volatile int deathOrder = -1;

  private Snake(Position start, Direction dir) {
    body.addFirst(start);
    this.direction = dir;
  }

  public static Snake of(int x, int y, Direction dir) {
    return new Snake(new Position(x, y), dir);
  }

  public Direction direction() { return direction; }
  public int id() { return id; }

  public void turn(Direction dir) {
    if ((direction == Direction.UP && dir == Direction.DOWN) ||
        (direction == Direction.DOWN && dir == Direction.UP) ||
        (direction == Direction.LEFT && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  public synchronized Position head() { return body.peekFirst(); }

  public synchronized Deque<Position> snapshot() { return new ArrayDeque<>(body); }

  public synchronized int length() { return body.size(); }

  public synchronized void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }

  public boolean isAlive() { return alive; }

  public int deathOrder() { return deathOrder; }

  public void kill() {
    alive = false;
    deathOrder = deathCounter.incrementAndGet();
  }
}
