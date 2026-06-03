# Solución Laboratorio 2 (SnakeRace & PrimeFinder) — ARSW

**Estudiante:** Stiven Esneider Pardo Gutierrez  
**Materia:** Arquitecturas de Software (ARSW)  

---

## Parte I — PrimeFinder (`wait/notify`)

### Diseño de Sincronización y Solución
El programa `PrimeFinder` calcula números primos concurrentemente dividiendo el rango de búsqueda entre múltiples hilos trabajadores (`PrimeWorker`). Se implementó un control de suspensión y reanudación global cada 3 segundos sin incurrir en espera activa (*busy-waiting*):

1. **Monitor y Lock**: Se utilizó la misma instancia de `PrimeCounter` como el monitor compartido por todos los hilos (`PrimeWorker` y el hilo de control `pauser`). Todos los métodos que acceden o modifican el estado de la pausa o el contador están marcados como `synchronized`.
2. **Evitar Esperas Activas (*Busy-Waiting*)**: 
   - En lugar de ciclos vacíos evaluando variables booleanas, los hilos de trabajo llaman a `counter.checkPause()` en cada iteración.
   - Si el estado `paused` es `true`, el hilo entra en suspensión bloqueada llamando a `wait()`.
3. **Prevenir *Lost Wakeups***:
   - Se utiliza un ciclo `while(paused)` alrededor de `wait()` en `checkPause()` para evitar que despertares espurios hagan que un hilo continúe trabajando cuando el estado sigue en pausa.
   - El hilo `pauser` despierta a todos los trabajadores llamando a `counter.resume()`, el cual cambia `paused = false` y ejecuta `notifyAll()` sobre el monitor.

---

## Parte II — SnakeRace Concurrente

### 1) Análisis de Concurrencia e Identificación de Condiciones de Carrera
En la estructura base del juego, cada serpiente corría en su propio hilo actualizando de forma asíncrona sus coordenadas y el tablero de juego. Se identificaron los siguientes riesgos y problemas:
- **Condiciones de Carrera en el Movimiento**: Varias serpientes podían intentar leer o modificar los conjuntos compartidos del tablero (`mice`, `obstacles`, `turbo`, `teleports`) simultáneamente al avanzar.
- **Colecciones no Seguras**: El uso de colecciones estándar como `HashSet` y `HashMap` en la clase `Board` causaba problemas de inconsistencia y excepciones de modificación concurrente al ser modificadas por múltiples hilos y leídas en paralelo por el hilo de pintado de la UI (Event Dispatch Thread).
- **Espera Activa en la Pausa**: Detener el movimiento de los hilos de las serpientes mediante encuestas repetitivas de banderas booleanas consumía ciclos de CPU innecesarios.

### 2) Solución e Implementación de Regiones Críticas
Para resolver los problemas identificados, se reestructuró la concurrencia del juego con el menor alcance de bloqueo posible:

- **Sincronización del Tablero (`Board.java`)**: 
  - El método principal `step(Snake snake)` está marcado como `synchronized`. Esto asegura que solo una serpiente pueda validar colisiones, comer un ratón/turbo o teletransportarse a la vez, eliminando las condiciones de carrera en los recursos compartidos.
- **Acceso Seguro desde la UI**: 
  - Para evitar `ConcurrentModificationException` en el hilo de renderizado (`paintComponent`), los métodos de acceso a las colecciones (`mice()`, `obstacles()`, `turbo()`, `teleports()`) fueron sincronizados y programados para retornar copias defensivas independientes (ej. `return new HashSet<>(obstacles);`).
  - La clase `Snake` implementa un método `snapshot()` sincronizado para duplicar de manera segura la cola de posiciones del cuerpo de la serpiente antes de ser dibujada en pantalla.
- **Pausa Eficiente sin Busy-Wait**:
  - Al pausar el juego mediante el botón `Action` o la tecla `SPACE`, se establece el estado de pausa en el tablero (`board.pauseAll()`).
  - Cualquier serpiente que intente dar su siguiente paso llamando a `board.step(...)` quedará suspendida en `wait()`, liberando el procesador.
  - Al reanudar con `board.resumeAll()`, se notifica a todos los hilos usando `notifyAll()` para continuar el juego de inmediato.

### 3) Consistencia Visual al Pausar (Evitando el *Tearing*)
El programa calcula y muestra de forma consistente:
1. La serpiente viva más larga.
2. La primera serpiente en morir (mediante una variable atómica global `deathCounter` que asigna un orden de muerte unívoco en `Snake.kill()`).

**Garantía de Consistencia**: Como `step(...)` y `pauseAll()` se sincronizan sobre el mismo monitor del tablero, ningún hilo de serpiente puede modificar sus datos a mitad de cálculo o mientras se calculan las estadísticas en `togglePause()`. Una vez que el hilo de la UI activa la pausa y lee las longitudes y estados de vida, se asegura de que el estado global esté completamente estático y no "a medias".

---

## Instrucciones de Ejecución

1. **Compilar y Verificar**:
   ```bash
   mvn clean verify
   ```
2. **Ejecutar el juego con N serpientes** (ej. 4 serpientes):
   ```bash
   mvn -q -DskipTests exec:java -Dsnakes=4
   ```
