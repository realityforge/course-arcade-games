package org.realityforge.arcade.adventure;

import java.util.Objects;
import javax.annotation.Nonnull;

final class Car
{
  private static final double TURN_RATE = 0.05D;
  private static final double DRIVE_POWER = 0.5D;
  private static final double REVERSE_POWER = 0.2D;
  private static final double SPEED_DECAY_RATE = 0.04;
  private static final double MIN_SPEED_TO_TURN = 0.5;
  @Nonnull
  private final Body _body = new Body();
  @Nonnull
  private final String _name;
  private boolean _accelerateHeld = false;
  private boolean _brakeHeld = false;
  private boolean _leftHeld = false;
  private boolean _rightHeld = false;

  Car( @Nonnull final String name )
  {
    _name = Objects.requireNonNull( name );
  }

  @Nonnull
  String getName()
  {
    return _name;
  }

  @Nonnull
  Body getBody()
  {
    return _body;
  }

  void setAccelerateHeld( final boolean accelerateHeld )
  {
    _accelerateHeld = accelerateHeld;
  }

  void setBrakeHeld( final boolean brakeHeld )
  {
    _brakeHeld = brakeHeld;
  }

  void setLeftHeld( final boolean leftHeld )
  {
    _leftHeld = leftHeld;
  }

  void setRightHeld( final boolean rightHeld )
  {
    _rightHeld = rightHeld;
  }

  void update()
  {
    _body.setSpeed( _body.getSpeed() * ( 1.0 - SPEED_DECAY_RATE ) );
    if ( Math.abs( _body.getSpeed() ) > MIN_SPEED_TO_TURN )
    {
      if ( _leftHeld )
      {
        _body.setAngle( _body.getAngle() - TURN_RATE );
      }
      if ( _rightHeld )
      {
        _body.setAngle( _body.getAngle() + TURN_RATE );
      }
    }
    if ( _accelerateHeld )
    {
      _body.setSpeed( _body.getSpeed() + DRIVE_POWER );
    }
    if ( _brakeHeld )
    {
      _body.setSpeed( _body.getSpeed() - REVERSE_POWER );
    }

    move();
  }

  private void move()
  {
    _body.setX( _body.getX() + Math.cos( _body.getAngle() ) * _body.getSpeed() );
    _body.setY( _body.getY() + Math.sin( _body.getAngle() ) * _body.getSpeed() );
  }

  /**
   * Reverse position updates that occurred in simulation tick.
   */
  void reverseMove()
  {
    _body.setX( _body.getX() - Math.cos( _body.getAngle() ) * _body.getSpeed() );
    _body.setY( _body.getY() - Math.sin( _body.getAngle() ) * _body.getSpeed() );
  }
}
