package org.realityforge.arcade.racing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class World
{
  private static final int CELL_INVALID_TYPE = -1;
  private static final int CELL_ROAD_TYPE = 0;
  private static final int CELL_WALL_TYPE = 1;
  private static final int CELL_START_TYPE = 2;
  private static final int CELL_GOAL_TYPE = 3;
  private static final int CELL_TREE_TYPE = 4;
  private static final int CELL_FLAG_TYPE = 5;
  static final int MAX_CELL_TYPE_COUNT = 6;
  private static final int[] world = new int[]{
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 5, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 5, 1, 1, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 1, 5, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 4, 4, 4, 4, 4, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 1, 1, 4, 4, 4, 4, 4, 1, 0, 0, 1, 0, 0, 0, 1, 1,
    1, 0, 0, 0, 1, 4, 4, 4, 4, 4, 4, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 1, 4, 4, 4, 1, 1, 1, 1, 0, 0, 1, 0, 5, 0, 0, 1,
    1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 3, 3, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1,
    };
  static final int COLUMN_COUNT = 20;
  static final int ROW_COUNT = 15;
  static final double CELL_WIDTH = 800D / COLUMN_COUNT;
  static final double CELL_HEIGHT = 40D;

  @Nullable
  WorldPosition toPosition( final double x, final double y )
  {
    final int column = toCellColumn( x );
    final int row = toCellRow( y );
    return isValidCell( column, row ) ? new WorldPosition( column, row ) : null;
  }

  int getCell( final int column, final int row )
  {
    return world[ cellIndex( column, row ) ];
  }

  int getCell( @Nonnull final Body body )
  {
    final int column = toCellColumn( body.getX() );
    final int row = toCellRow( body.getY() );
    return isValidCell( column, row ) ? getCell( column, row ) : CELL_INVALID_TYPE;
  }

  boolean isSolid( final int flag )
  {
    return !( CELL_ROAD_TYPE == flag || CELL_START_TYPE == flag );
  }

  @Nullable
  WorldPosition getStartCell()
  {
    for ( int i = 0; i < COLUMN_COUNT; i++ )
    {
      for ( int j = 0; j < ROW_COUNT; j++ )
      {
        if ( CELL_START_TYPE == getCell( i, j ) )
        {
          return new WorldPosition( i, j );
        }
      }
    }
    return null;
  }

  boolean isValidCell( final double column, final double row )
  {
    return column >= 0 && column < COLUMN_COUNT && row >= 0 && row < ROW_COUNT;
  }

  int toCellRow( final double mouseY )
  {
    return (int) Math.floor( mouseY / CELL_HEIGHT );
  }

  int toCellColumn( final double x )
  {
    return (int) Math.floor( x / CELL_WIDTH );
  }

  private int cellIndex( final int column, final int row )
  {
    return row * COLUMN_COUNT + column;
  }
}
