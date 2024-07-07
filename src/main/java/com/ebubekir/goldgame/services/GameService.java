package com.ebubekir.goldgame.services;

import com.ebubekir.goldgame.models.Cell;
import com.ebubekir.goldgame.models.Player;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;



@Getter
@Service
public class GameService {
    private static final int BOARD_SIZE = 30;
    private static final double GOLD_PERCENTAGE = 0.20;
    private static final int INITIAL_GOLD = 100;
    private static final int MOVE_COST = 5;
    private static final int A_TARGET_COST = 5;
    private static final int B_TARGET_COST = 10;

    private Cell[][] board;
    private Player playerA;
    private Player playerB;

    @PostConstruct
    public void initializeGameOnStartup() {
        initializeGame();
    }

    public void initializeGame() {
        board = initializeBoard(BOARD_SIZE, GOLD_PERCENTAGE);
        playerA = new Player('A', 0, 0, INITIAL_GOLD, false, 0, 0, 0);
        playerB = new Player('B', BOARD_SIZE - 1, BOARD_SIZE - 1, INITIAL_GOLD, false, 0, 0, 0);
    }

    private Cell[][] initializeBoard(int size, double goldPercentage) {
        Cell[][] board = new Cell[size][size];
        int totalCells = size * size;
        int goldCells = (int) (totalCells * goldPercentage);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = new Cell(i, j, false, 0);
            }
        }

        while (goldCells > 0) {
            int x = (int) (Math.random() * size);
            int y = (int) (Math.random() * size);

            if (!board[x][y].isHasGold()) {
                board[x][y].setHasGold(true);
                board[x][y].setGold(10 + (int) (Math.random() * 41));
                goldCells--;
            }
        }

        return board;
    }

    public void makeMove(char playerType) {
        // Oyuncuların null olmadığını kontrol edin
        if (playerA == null || playerB == null) {
            throw new IllegalStateException("The game has not been initialized. Call initializeGame() first.");
        }

        Player player = (playerType == 'A') ? playerA : playerB;
        Cell target = findTarget(player, playerType);
        if (target != null) {
            moveToTarget(player, target);
        }
    }

    private Cell findTarget(Player player, char playerType) {
        int cost = (playerType == 'A') ? A_TARGET_COST : B_TARGET_COST;
        if (player.getGold() < cost) {
            player.setEliminated(true);
            return null;
        }
        player.setGold(player.getGold() - cost);
        player.setGoldSpent(player.getGoldSpent() + cost);
        return (playerType == 'A') ? findNearestGoldDijkstra(player) : findMaxGold();
    }

    private Cell findNearestGoldDijkstra(Player player) {
        int[][] distances = new int[BOARD_SIZE][BOARD_SIZE];
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        PriorityQueue<Cell> pq = new PriorityQueue<>(Comparator.comparingInt(cell -> distances[cell.getX()][cell.getY()]));

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                distances[i][j] = Integer.MAX_VALUE;  // mesafeyi sonsuza ayarlarız
                visited[i][j] = false;  // daha ziyaret edilmemişse
            }
        }

        // oyuncuların default mesafesini 0 a ayarlama
        distances[player.getX()][player.getY()] = 0;
        pq.add(board[player.getX()][player.getY()]);

        while (!pq.isEmpty()) {
            Cell current = pq.poll();  // en yakın mesafedeki kareyi getirme
            int cx = current.getX();
            int cy = current.getY();

            if (visited[cx][cy]) continue;  // eğer daha önce ziyaret edilmişse atla
            visited[cx][cy] = true;

            if (current.isHasGold()) {
                return current;  //altın varsa kareyi geri dondürür
            }

            // komşu kareleri tanımlama
            List<int[]> directions = List.of(
                    new int[]{0, 1}, new int[]{1, 0}, new int[]{0, -1}, new int[]{-1, 0}
            );

            for (int[] direction : directions) {
                int nx = cx + direction[0];
                int ny = cy + direction[1];

                if (isValidMove(nx, ny) && !visited[nx][ny]) {
                    int newDist = distances[cx][cy] + 1;
                    if (newDist < distances[nx][ny]) {
                        distances[nx][ny] = newDist;
                        pq.add(board[nx][ny]);
                    }
                }
            }
        }

        return null; //eğer altın yoksa null döndürür
    }

    private Cell findMaxGold() {
        Cell maxGoldCell = null;
        int maxGold = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isHasGold() && board[i][j].getGold() > maxGold) {
                    maxGold = board[i][j].getGold();
                    maxGoldCell = board[i][j];
                }
            }
        }
        return maxGoldCell;
    }

    private void moveToTarget(Player player, Cell target) {
        int dx = Integer.signum(target.getX() - player.getX());
        int dy = Integer.signum(target.getY() - player.getY());
        int steps = 0;
        while (steps < 4 && (player.getX() != target.getX() || player.getY() != target.getY())) {
            int newX = player.getX() + dx;
            int newY = player.getY() + dy;
            if (isValidMove(newX, newY)) {
                if (!player.move(newX, newY, MOVE_COST)) {
                    break;
                }
                steps++;
                if (player.getX() == target.getX() && player.getY() == target.getY()) {
                    player.collectGold(board[player.getX()][player.getY()]);
                    break;
                }
            }
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean gameOver() {
        return playerA.isEliminated() && playerB.isEliminated();
    }

    public String getGameSummary() {
        return String.format("Player A: Steps = %d, Gold Spent = %d, Gold Collected = %d\n" +
                        "Player B: Steps = %d, Gold Spent = %d, Gold Collected = %d",
                playerA.getSteps(), playerA.getGoldSpent(), playerA.getGoldCollected(),
                playerB.getSteps(), playerB.getGoldSpent(), playerB.getGoldCollected());
    }
}
