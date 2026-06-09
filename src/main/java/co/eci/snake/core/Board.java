package co.eci.snake.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Representa el tablero compartido de juego en el que se ubican e interactúan los ratones,
 * los obstáculos, los portales (teletransportadores), los turbos y las serpientes.
 * Proporciona métodos sincronizados para mover de forma segura a una serpiente (región crítica)
 * y para realizar operaciones de pausa/reanudación coordinadas mediante wait/notify sin busy-waiting.
 */
public final class Board {
  private final int width;
  private final int height;

  private final Set<Position> mice = new HashSet<>();
  private final Set<Position> obstacles = new HashSet<>();
  private final Set<Position> turbo = new HashSet<>();
  private final Map<Position, Position> teleports = new HashMap<>();

  public enum MoveResult { MOVED, ATE_MOUSE, HIT_OBSTACLE, ATE_TURBO, TELEPORTED }

  public Board(int width, int height) {
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Board dimensions must be positive");
    this.width = width;
    this.height = height;
    for (int i=0;i<6;i++) mice.add(randomEmpty());
    for (int i=0;i<4;i++) obstacles.add(randomEmpty());
    for (int i=0;i<3;i++) turbo.add(randomEmpty());
    createTeleportPairs(2);
  }

  public int width() { return width; }
  public int height() { return height; }

  public synchronized Set<Position> mice() { return new HashSet<>(mice); }
  public synchronized Set<Position> obstacles() { return new HashSet<>(obstacles); }
  public synchronized Set<Position> turbo() { return new HashSet<>(turbo); }
  public synchronized Map<Position, Position> teleports() { return new HashMap<>(teleports); }

  private volatile boolean paused = false;

  public synchronized MoveResult step(Snake snake) {
    while (paused) {
      try {
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return MoveResult.HIT_OBSTACLE;
      }
    }
    Objects.requireNonNull(snake, "snake");
    var head = snake.head();
    var dir = snake.direction();
    Position next = new Position(head.x() + dir.dx, head.y() + dir.dy).wrap(width, height);

    if (obstacles.contains(next)) return MoveResult.HIT_OBSTACLE;

    boolean teleported = false;
    if (teleports.containsKey(next)) {
      next = teleports.get(next);
      teleported = true;
    }

    boolean ateMouse = mice.remove(next);
    boolean ateTurbo = turbo.remove(next);

    snake.advance(next, ateMouse);

    if (ateMouse) {
      mice.add(randomEmpty());
      obstacles.add(randomEmpty());
      if (ThreadLocalRandom.current().nextDouble() < 0.2) turbo.add(randomEmpty());
    }

    if (ateTurbo) return MoveResult.ATE_TURBO;
    if (ateMouse) return MoveResult.ATE_MOUSE;
    if (teleported) return MoveResult.TELEPORTED;
    return MoveResult.MOVED;
  }

  public synchronized void pauseAll() {
    paused = true;
  }

  public synchronized void resumeAll() {
    paused = false;
    notifyAll();
  }

  private void createTeleportPairs(int pairs) {
    for (int i=0;i<pairs;i++) {
      Position a = randomEmpty();
      Position b = randomEmpty();
      teleports.put(a, b);
      teleports.put(b, a);
    }
  }

  private Position randomEmpty() {
    var rnd = ThreadLocalRandom.current();
    Position p;
    int guard = 0;
    do {
      p = new Position(rnd.nextInt(width), rnd.nextInt(height));
      guard++;
      if (guard > width*height*2) break;
    } while (mice.contains(p) || obstacles.contains(p) || turbo.contains(p) || teleports.containsKey(p));
    return p;
  }
}
