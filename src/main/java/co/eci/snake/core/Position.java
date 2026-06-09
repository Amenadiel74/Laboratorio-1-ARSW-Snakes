package co.eci.snake.core;

/**
 * Registro (Record) inmutable que representa una coordenada bidimensional (x, y) en el tablero.
 * Incluye lógica de envoltura (wrap-around) para que el tablero sea toroidal en los límites de la pantalla.
 */
public record Position(int x, int y) {
  public Position wrap(int width, int height) {
    int nx = ((x % width) + width) % width;
    int ny = ((y % height) + height) % height;
    return new Position(nx, ny);
  }
}
