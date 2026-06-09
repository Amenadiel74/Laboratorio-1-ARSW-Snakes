package co.eci.snake.app;

import co.eci.snake.ui.legacy.SnakeApp;

/**
 * Clase principal de inicio (Bootstrap) para la aplicación del juego Snake Race.
 * Se encarga de inicializar y lanzar la interfaz gráfica (UI).
 */
public final class Main {
  private Main() {}
  public static void main(String[] args) {
    SnakeApp.launch();
  }
}
