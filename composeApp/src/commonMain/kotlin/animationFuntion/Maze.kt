package animationFuntion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class CellType {
    Space, Wall
}

data class MazeTile(val x: Int, val y: Int, var type: CellType = CellType.Wall)


class Maze(private val width: Int, private val height: Int) {
    val maze: Array<Array<MazeTile>> = Array(height) { y ->
        Array(width) { x -> MazeTile(x, y) }
    }
    val visited: Array<Array<Boolean>> = Array(height) { Array(width) { false } }
    val stack: MutableList<MazeTile> = mutableListOf()

    init {
        generateMaze(1, 1)
        addEntranceAndExit()
    }


    private fun generateMaze(x: Int, y: Int) {
        val startTile = maze[y][x]
        stack.add(startTile)
        visited[y][x] = true
        startTile.type = CellType.Space

        while (stack.isNotEmpty()) {
            val currentTile = stack.last()
            val neighbors = mutableListOf<MazeTile>()

            listOf(
                currentTile.x to (currentTile.y + 2),
                currentTile.x to (currentTile.y - 2),
                (currentTile.x + 2) to currentTile.y,
                (currentTile.x - 2) to currentTile.y
            ).forEach { (nx, ny) ->
                if (isValidCoordinate(nx, ny) && !visited[ny][nx]) {
                    neighbors.add(maze[ny][nx])
                }
            }

            if (neighbors.isNotEmpty()) {
                val nextTile = neighbors.random()
                stack.add(nextTile)
                nextTile.type = CellType.Space
                visited[nextTile.y][nextTile.x] = true

                val wallX = (currentTile.x + nextTile.x) / 2
                val wallY = (currentTile.y + nextTile.y) / 2
                maze[wallY][wallX].type = CellType.Space
            } else {
                stack.removeLast()
            }
        }
    }

    private fun addEntranceAndExit() {
        maze[0][1].type = CellType.Space
        maze[height - 1][width - 2].type = CellType.Space
    }

    private fun isValidCoordinate(x: Int, y: Int) =
        x in 1 until width - 1 && y in 1 until height - 1
}

@Composable
fun MazeCanvas(maze: Maze, cellSize: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        maze.maze.forEach { row ->
            row.forEach { tile ->
                if (tile.type == CellType.Wall) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset((tile.x * cellSize), (tile.y * cellSize)),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

@Composable
fun MazeScreen(modifier: Modifier = Modifier) {
    var width by remember { mutableIntStateOf(21) }
    var height by remember { mutableIntStateOf(21) }
    var cellSize by remember { mutableFloatStateOf(40f) }
    val maze = remember(width, height) { Maze(width, height) }

    Column(modifier = modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Maze Size: Width ($width), Height ($height)")
        Slider(
            value = width.toFloat(),
            onValueChange = {
                width = it.toInt().coerceIn(5, 51)
                height = it.toInt().coerceIn(5, 51)
            },
            valueRange = 5f..51f,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(text = "Cell Size: ${cellSize.toInt()} px")
        Slider(
            value = cellSize,
            onValueChange = {
                cellSize = it.coerceIn(20f, 80f)
            },
            valueRange = 20f..80f,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Box(modifier = Modifier.padding(16.dp)) {
            MazeCanvas(
                maze,
                cellSize,
                modifier = Modifier.size((width * cellSize).dp, (height * cellSize).dp)
            )
        }
    }
}