package com.skd.snake_ladder.core

data class CellPosition(val row: Int, val col: Int)

object BoardMapper {

    fun map(position: Int): CellPosition {

        if (position <= 0) return CellPosition(9, 0)

        val rowFromBottom = (position - 1) / 10
        val actualRow = 9 - rowFromBottom

        val colInRow = (position - 1) % 10

        val actualCol =
            if (rowFromBottom % 2 == 0) {
                colInRow
            } else {
                9 - colInRow
            }

        return CellPosition(actualRow, actualCol)
    }
}
