package org.realityforge.arcade.racing;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class Body
{
  @Nullable
  private HTMLImageElement _image;
  private boolean _imageLoaded;
  private double _x;
  private double _y;
  private double _angle;
  private double _speed;

  @Nonnull
  HTMLImageElement getImage()
  {
    assert isImageLoaded();
    assert null != _image;
    return _image;
  }

  boolean isImageLoaded()
  {
    return _imageLoaded;
  }

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

  @SuppressWarnings( "SameParameterValue" )
  void loadImage( @Nonnull final String src )
  {
    final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement( "img" );
    _image =  img;
    img.onload = e -> {
      _imageLoaded = true;
      //Fix this after Elemental2 is fixed
      return null;
    };
    img.src = src;
  }
}
