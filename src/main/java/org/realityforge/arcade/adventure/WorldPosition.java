package org.realityforge.arcade.adventure;

final class WorldPosition
{
  private final int _column;
  private final int _row;

  WorldPosition( final int column, final int row )
  {
    _column = column;
    _row = row;
  }

  int getColumn()
  {
    return _column;
  }

  int getRow()
  {
    return _row;
  }
}
