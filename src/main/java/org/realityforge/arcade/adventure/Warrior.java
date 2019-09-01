package org.realityforge.arcade.adventure;

import java.util.Objects;
import javax.annotation.Nonnull;

final class Warrior
{
  private static final double MAX_SPEED = 2.9;
  @Nonnull
  private final Body _body = new Body();
  @Nonnull
  private final String _name;
  private boolean _upHeld = false;
  private boolean _downHeld = false;
  private boolean _leftHeld = false;
  private boolean _rightHeld = false;

  Warrior( @Nonnull final String name )
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

  void setUpHeld( final boolean upHeld )
  {
    _upHeld = upHeld;
  }

  void setDownHeld( final boolean downHeld )
  {
    _downHeld = downHeld;
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
    _body.setX( _body.getX() + ( _leftHeld ? -MAX_SPEED : 0 ) + ( _rightHeld ? MAX_SPEED : 0 ) );
    _body.setY( _body.getY() + ( _upHeld ? -MAX_SPEED : 0 ) + ( _downHeld ? MAX_SPEED : 0 ) );
  }

  /**
   * Reverse position updates that occurred in simulation tick.
   */
  void reverseMove()
  {
    _body.setX( _body.getX() + ( _leftHeld ? MAX_SPEED : 0 ) + ( _rightHeld ? -MAX_SPEED : 0 ) );
    _body.setY( _body.getY() + ( _upHeld ? MAX_SPEED : 0 ) + ( _downHeld ? -MAX_SPEED : 0 ) );
  }
}
