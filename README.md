# Lab 1 Solution (SnakeRace & PrimeFinder) — ARSW

**Student:** Stiven Esneider Pardo Gutierrez  
**Course:** Software Architectures (ARSW)  

---

## Part I — PrimeFinder (`wait/notify`)

### Synchronization Design and Solution
The `PrimeFinder` program calculates prime numbers concurrently by dividing the search range among multiple worker threads (`PrimeWorker`). A global suspend and resume control was implemented every 3 seconds without incurring busy-waiting:

1. **Monitor and Lock**: The same instance of `PrimeCounter` was used as the shared monitor for all threads (`PrimeWorker` and the control thread `pauser`). All methods accessing or modifying the pause state or the counter are marked as `synchronized`.
2. **Avoiding Busy-Waiting**: 
   - Instead of empty loops evaluating boolean flags, worker threads call `counter.checkPause()` in each iteration.
   - If the `paused` state is `true`, the worker thread enters a blocked suspend state by calling `wait()`.
3. **Preventing Lost Wakeups**:
   - A `while(paused)` loop is used around the `wait()` call in `checkPause()` to prevent spurious wakeups from letting a thread continue working while the state is still paused.
   - The `pauser` thread wakes up all workers by calling `counter.resume()`, which changes `paused = false` and executes `notifyAll()` on the monitor.

---

## Part II — Concurrent SnakeRace

### 1) Concurrency Analysis and Identification of Race Conditions
In the baseline game structure, each snake ran on its own thread, updating its coordinates and the game board asynchronously. The following risks and issues were identified:
- **Race Conditions in Movement**: Multiple snakes could attempt to read or modify the shared board sets (`mice`, `obstacles`, `turbo`, `teleports`) simultaneously as they advanced.
- **Unsafe Collections**: The use of standard collections like `HashSet` and `HashMap` in the `Board` class caused inconsistency issues and concurrent modification exceptions when modified by multiple threads and read in parallel by the UI rendering thread (Event Dispatch Thread).
- **Busy-Waiting on Pause**: Stopping snake thread movements through repetitive polling of boolean flags consumed unnecessary CPU cycles.

### 2) Solution and Implementation of Critical Regions
To resolve the identified issues, the game's concurrency was restructured with the narrowest possible lock scope:

- **Board Synchronization (`Board.java`)**: 
  - The main `step(Snake snake)` method is marked as `synchronized`. This ensures that only one snake can validate collisions, eat a mouse/turbo, or teleport at a time, eliminating race conditions on shared resources.
- **Safe Access from the UI**: 
  - To prevent `ConcurrentModificationException` in the rendering thread (`paintComponent`), getter methods for the collections (`mice()`, `obstacles()`, `turbo()`, `teleports()`) were synchronized and programmed to return independent defensive copies (e.g., `return new HashSet<>(obstacles);`).
  - The `Snake` class implements a synchronized `snapshot()` method to safely duplicate the body queue before it is drawn on the screen.
- **Efficient Pause Without Busy-Wait**:
  - When pausing the game via the `Action` button or the `SPACE` key, the pause state is set on the board (`board.pauseAll()`).
  - Any snake attempting to take its next step by calling `board.step(...)` will be suspended on `wait()`, releasing the CPU.
  - Upon resuming with `board.resumeAll()`, all threads are notified using `notifyAll()` to resume the game immediately.

### 3) Visual Consistency on Pause (Avoiding Tearing)
The program calculates and consistently displays:
1. The longest alive snake.
2. The first snake to die (using a global atomic variable `deathCounter` that assigns a unique death order in `Snake.kill()`).

**Consistency Guarantee**: Since `step(...)` and `pauseAll()` synchronize on the same board monitor, no snake thread can modify its data midway through calculation or while statistics are calculated in `togglePause()`. Once the UI thread triggers the pause and reads the lengths and life states, it is guaranteed that the global state is completely static and not "halfway done".

---

## Execution Instructions

1. **Compile and Verify**:
   ```bash
   mvn clean verify
   ```
2. **Run the game with N snakes** (e.g., 4 snakes):
   ```bash
   mvn -q -DskipTests exec:java -Dsnakes=4
   ```
