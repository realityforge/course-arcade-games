package org.realityforge.arcade.racing;

final class Body
{
  private double _x;
  private double _y;
  private double _angle;
  private double _speed;

  double getX()
  {
    return _x;
  }

  void setX( final double x )
  {
    _x = x;
  }

  double getY()
  {
    return _y;
  }

  void setY( final double y )
  {
    _y = y;
  }

  double getAngle()
  {
    return _angle;
  }

  void setAngle( final double angle )
  {
    _angle = angle;
  }

  double getSpeed()
  {
    return _speed;
  }

  void setSpeed( final double speed )
  {
    _speed = speed;
  }
}
